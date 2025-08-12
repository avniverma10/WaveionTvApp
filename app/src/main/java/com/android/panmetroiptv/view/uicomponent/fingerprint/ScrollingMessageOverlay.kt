package com.android.panmetroiptv.view.uicomponent.fingerprint

import android.animation.ValueAnimator
import android.content.Context
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import com.android.panmetroiptv.extensions.capitalizeFirstLetter
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
    val fontColor = runCatching {
        Color(android.graphics.Color.parseColor(scrollMessageInfo.value.fontColorHex ?: "#000000"))
            .copy(alpha = scrollMessageInfo.value.fontTransparency
                ?.getFloatValue()
                ?.let { 1f - it.coerceIn(0f, 1f) } ?: 0.25f)
    }.getOrDefault(Color.Black.copy(alpha = 0.25f))

    val bgColor = runCatching {
        Color(android.graphics.Color.parseColor(scrollMessageInfo.value.backgroundColorHex ?: "#ffffff"))
            .copy(alpha = scrollMessageInfo.value.backgroundTransparency
                ?.getFloatValue()
                ?.let { 1f - it.coerceIn(0f, 1f) } ?: 0.25f)
    }.getOrDefault(Color.White.copy(alpha = 0.25f))
    var visible by remember { mutableStateOf(true) }


    LaunchedEffect(scrollMessageInfo.value.message, scrollMessageInfo.value.repeatCount) {
        visible = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (visible) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(bgColor)
            ) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 12.dp, start = 8.dp, end = 8.dp)
                        .align(Alignment.CenterStart),
                    factory = { ctx ->
                        TextView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            setSingleLine(true)
                            // We do manual scroll, so no built-in marquee/ellipsize
                            ellipsize = null
                        }
                    },
                    update = { tv ->
                        val raw = context.checkPatternMatchInfo(scrollMessageInfo.value.message.orEmpty())
                        tv.text = raw
                        tv.setTextColor(fontColor.toArgb())
                        tv.textSize = scrollMessageInfo.value.fontSizeDp.toString().getFloatValue()

                        // Speed mapping: seconds per screen = 20 / speed
                        val baseSecondsPerScreen = 20f
                        val speedValue = (scrollMessageInfo.value.scrollSpeed ?: 1f).coerceAtLeast(0.1f)
                        val secondsPerScreen = (baseSecondsPerScreen / speedValue).coerceIn(0.2f, 60f)

                        // -1 = infinite, else finite repeats
                        val repeatLimit = scrollMessageInfo.value.repeatCount?.takeIf { it > 0 } ?: -1

                        startTickerAnimation(
                            tv = tv,
                            speedScreenPerSec = 1f / secondsPerScreen,
                            pauseMs = 0L,
                            repeatLimit = repeatLimit,
                            directionRtl = true,
                            onFinished = {
                                // Hide the entire bar when repeats are done
                                visible = false
                            }
                        )
                    }
                )
            }
        }
    }
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

fun Context.checkPatternMatchInfo(message: String): String {
    return message
        .replace("$$@User", " ${PreferenceManager.getUsername()} ")
        .replace("$$@Mac", " ${this.provideMacAddress()} ")
        .replace(
            "$$@Package",
            " ${PreferenceManager.getLoginResponse()?.pkgdata?.activepack
                ?.map { it.servicename }
                ?.joinToString(",")
                ?.capitalizeFirstLetter() ?: ""} "
        )
}
