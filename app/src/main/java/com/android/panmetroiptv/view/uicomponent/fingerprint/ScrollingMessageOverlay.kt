package com.android.panmetroiptv.view.uicomponent.fingerprint

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Rect
import android.text.method.ScrollingMovementMethod
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import com.android.panmetroiptv.extensions.getFloatValue
import com.android.panmetroiptv.extensions.provideMacAddress
import com.android.panmetroiptv.model.data.sseresponse.ScrollMessage
import com.android.panmetroiptv.utils.uistate.PreferenceManager
import kotlin.math.abs

@Composable
fun ScrollingMessageOverlay(
    player: PlayerView? = null,
    scrollMessageInfo: MutableState<ScrollMessage>
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val config = LocalConfiguration.current

    // Calculate screen dimensions
    val screenWidthPx = with(density) {
        player?.width?.toDp()?.toPx() ?: config.screenWidthDp.dp.toPx()
    }

    // Message processing with proper context
    val processedMessage = remember(scrollMessageInfo.value.message) {
        context.checkPatternMatchInfo(scrollMessageInfo.value.message.orEmpty())
    }

    // Colors with fallbacks
    val (fontColor, bgColor) = remember(scrollMessageInfo.value) {
        val fc = runCatching {
            Color(android.graphics.Color.parseColor(scrollMessageInfo.value.fontColorHex ?: "#000000"))
                .copy(alpha = 1f - (scrollMessageInfo.value.fontTransparency?.getFloatValue()?.coerceIn(0f, 1f) ?: 0.25f))
        }.getOrDefault(Color.Black.copy(alpha = 0.25f))

        val bg = runCatching {
            Color(android.graphics.Color.parseColor(scrollMessageInfo.value.backgroundColorHex ?: "#ffffff"))
                .copy(alpha = 1f - (scrollMessageInfo.value.backgroundTransparency?.getFloatValue()?.coerceIn(0f, 1f) ?: 0.25f))
        }.getOrDefault(Color.White.copy(alpha = 0.25f))

        Pair(fc, bg)
    }

    var isVisible by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isVisible) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(bgColor)
            ) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    factory = { ctx ->
                        TextView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            setSingleLine(true)
                            ellipsize = null
                            movementMethod = ScrollingMovementMethod()
                            isHorizontalScrollBarEnabled = false
                        }
                    },
                    update = { tv ->
                        tv.text = processedMessage
                        tv.setTextColor(fontColor.toArgb())
                        tv.textSize = scrollMessageInfo.value.fontSizeDp?.toString()?.getFloatValue() ?: 14f

                        // Calculate animation duration based on speed
                        val speed = (scrollMessageInfo.value.scrollSpeed ?: 1f).coerceIn(0.1f, 10f)
                        val baseDuration = 20000L // 20 seconds for normal speed (1.0)
                        val durationMs = (baseDuration / speed).toLong().coerceIn(2000L, 120000L)

                        val repeatCount = scrollMessageInfo.value.repeatCount ?: -1

                        startSmoothTickerAnimation(
                            tv = tv,
                            durationMs = durationMs,
                            repeatCount = if (repeatCount > 0) repeatCount else ValueAnimator.INFINITE,
                            onFinished = { isVisible = false }
                        )
                    }
                )
            }
        }
    }
}

private fun startSmoothTickerAnimation(
    tv: TextView,
    durationMs: Long,
    repeatCount: Int,
    onFinished: (() -> Unit)? = null
) {
    (tv.tag as? ValueAnimator)?.cancel()

    val parent = tv.parent as? ViewGroup ?: run {
        tv.post { startSmoothTickerAnimation(tv, durationMs, repeatCount, onFinished) }
        return
    }

    if (tv.width == 0 || parent.width == 0) {
        tv.post { startSmoothTickerAnimation(tv, durationMs, repeatCount, onFinished) }
        return
    }

    val textWidth = tv.paint.measureText(tv.text.toString()).toInt()
    val parentWidth = parent.width

    val animator = ValueAnimator.ofInt(parentWidth, -textWidth).apply {
        this.duration = durationMs
        interpolator = LinearInterpolator()
        this.repeatCount = if (repeatCount == ValueAnimator.INFINITE) ValueAnimator.INFINITE else repeatCount - 1
        repeatMode = ValueAnimator.RESTART

        addUpdateListener { animation ->
            tv.translationX = (animation.animatedValue as Int).toFloat()
            tv.requestLayout()
        }

        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (repeatCount == 0) {
                    onFinished?.invoke()
                }
            }
        })
    }

    tv.tag = animator
    animator.start()
}
/**
 * Animates a single TextView across its parent bounds.
 * - We translate from just outside one edge to just outside the other.
 * - speedScreenPerSec: screens per second (0.2f => 1 screen in 5s).
 * - pauseMs: delay before each new pass (0 for seamless restart).
 * - repeatLimit: -1 = infinite, else number of passes.
 * - directionRtl: true = right→left, false = left→right.
 * - onFinished: called once when all repeats are done.
 */
private fun startTickerAnimation(
    tv: TextView,
    speedScreenPerSec: Float,
    pauseMs: Long,
    repeatLimit: Int,
    directionRtl: Boolean,
    onFinished: (() -> Unit)? = null
) {
    // Cancel previous animator if any
    (tv.tag as? ValueAnimator)?.cancel()
    tv.tag = null

    val parent = tv.parent as? ViewGroup
    if (parent == null) {
        tv.post { startTickerAnimation(tv, speedScreenPerSec, pauseMs, repeatLimit, directionRtl, onFinished) }
        return
    }

    // Defer until we have real sizes
    if (tv.width == 0 || parent.width == 0) {
        tv.post { startTickerAnimation(tv, speedScreenPerSec, pauseMs, repeatLimit, directionRtl, onFinished) }
        return
    }

    val textStr = tv.text?.toString().orEmpty()
    if (textStr.isEmpty()) {
        // Nothing to show; just finish
        onFinished?.let { tv.post { it() } }
        return
    }

    val textW = tv.paint.measureText(textStr).coerceAtLeast(1f)
    val parentW = parent.width.toFloat()

    val startX = if (directionRtl) parentW else -textW
    val endX   = if (directionRtl) -textW else parentW
    val distancePx = abs(endX - startX)

    // Convert "screens per second" to pixels per second; clamp to sane range
    val pxPerSec = (parentW * speedScreenPerSec).coerceIn(20f, 5000f)
    val durationMs = ((distancePx / pxPerSec) * 1000f).toLong().coerceAtLeast(50L)

    tv.translationX = startX

    var passesDone = 0
    val infinite = (repeatLimit == -1)

    val animator = ValueAnimator.ofFloat(startX, endX).apply {
        duration = durationMs
        interpolator = LinearInterpolator()
        repeatCount = 0
        addUpdateListener { anim ->
            tv.translationX = (anim.animatedValue as Float)
        }
        addListener(object : android.animation.Animator.AnimatorListener {
            override fun onAnimationStart(animation: android.animation.Animator) {}
            override fun onAnimationCancel(animation: android.animation.Animator) {}
            override fun onAnimationRepeat(animation: android.animation.Animator) {}

            override fun onAnimationEnd(animation: android.animation.Animator) {
                passesDone++
                val more = infinite || (passesDone < repeatLimit)
                if (more) {
                    tv.postDelayed({
                        tv.translationX = startX
                        (tv.tag as? ValueAnimator)?.start()
                    }, pauseMs)
                } else {
                    // Finished: clear and notify Compose to hide the bar
                    tv.text = ""
                    tv.tag = null
                    onFinished?.let { tv.post { it() } }
                }
            }
        })
    }

    tv.tag = animator
    animator.start()
}


private fun startTickerAnimationLatest(
    tv: TextView,
    speedScreenPerSec: Float,
    pauseMs: Long,
    repeatLimit: Int,
    directionRtl: Boolean,
    onFinished: (() -> Unit)? = null
) {
    // Cancel previous animator if any
    (tv.tag as? ValueAnimator)?.cancel()
    tv.tag = null

    val parent = tv.parent as? ViewGroup
    if (parent == null) {
        tv.post { startTickerAnimation(tv, speedScreenPerSec, pauseMs, repeatLimit, directionRtl, onFinished) }
        return
    }

    // Defer until we have real sizes
    if (tv.width == 0 || parent.width == 0) {
        tv.post { startTickerAnimation(tv, speedScreenPerSec, pauseMs, repeatLimit, directionRtl, onFinished) }
        return
    }

    val textStr = tv.text?.toString().orEmpty()
    if (textStr.isEmpty()) {
        // Nothing to show; just finish
        onFinished?.let { tv.post { it() } }
        return
    }

    // Ensure text is set to single line and ellipsize is none
    tv.setSingleLine(true)
    tv.ellipsize = null
    tv.movementMethod = ScrollingMovementMethod()
    // tv.horizontalScrollBarEnabled = false
    tv.setHorizontallyScrolling(false)

    // Calculate text width with proper text measurement
    val textPaint = tv.paint
    val textBounds = Rect()
    textPaint.getTextBounds(textStr, 0, textStr.length, textBounds)
    val textW = textBounds.width().toFloat().coerceAtLeast(1f)
    val parentW = parent.width.toFloat()

    // Adjust positions to ensure full text visibility
    val startX = if (directionRtl) parentW else -textW
    val endX = if (directionRtl) -textW else parentW
    val distancePx = abs(endX - startX)

    // Convert "screens per second" to pixels per second with reasonable limits
    val pxPerSec = (parentW * speedScreenPerSec).coerceIn(20f, 5000f)
    val durationMs = ((distancePx / pxPerSec) * 1000f).toLong().coerceAtLeast(50L)

    // Reset translation before starting
    tv.translationX = startX
    tv.invalidate()

    var passesDone = 0
    val infinite = (repeatLimit == -1)

    val animator = ValueAnimator.ofFloat(startX, endX).apply {
        duration = durationMs
        interpolator = LinearInterpolator()
        repeatCount = 0

        addUpdateListener { anim ->
            tv.translationX = (anim.animatedValue as Float)
            // Force redraw to prevent clipping
            tv.invalidate()
        }

        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                passesDone++
                val more = infinite || (passesDone < repeatLimit)

                if (more) {
                    tv.postDelayed({
                        // Reset position before next run
                        tv.translationX = startX
                        tv.invalidate()
                        (tv.tag as? ValueAnimator)?.start()
                    }, pauseMs)
                } else {
                    // Clean up
                    tv.text = ""
                    tv.tag = null
                    onFinished?.let { tv.post { it() } }
                }
            }
        })
    }

    tv.tag = animator
    animator.start()
}
fun Context.checkPatternMatchInfo(message: String): String {
    var result = message
        .replace("$$@User", " ${PreferenceManager.getUsername()} ")
        .replace("$$@Mac", " ${this.provideMacAddress()} ")

    // Handle package info without Kotlin extensions
    val packageInfo = PreferenceManager.getUserPackageInfo()
    if (packageInfo != null && packageInfo.results != null) {
        val services = StringBuilder()
        for (result in packageInfo.results) {
            if (services.isNotEmpty()) {
                services.append(",")
            }
            services.append(result.serviceName)
        }
        val packageString = if (services.isNotEmpty()) {
            // Capitalize first letter manually
            val str = services.toString()
            if (str.isNotEmpty()) {
                str.substring(0, 1).toUpperCase() + str.substring(1)
            } else {
                str
            }
        } else {
            ""
        }
        result = result.replace("$$@Package", " $packageString ")
    } else {
        result = result.replace("$$@Package", " ")
    }

    return result
}
