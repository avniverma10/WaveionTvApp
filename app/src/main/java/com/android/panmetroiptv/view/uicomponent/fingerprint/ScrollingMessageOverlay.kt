package com.android.panmetroiptv.view.uicomponent.fingerprint

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.android.panmetroiptv.extensions.getFloatValue
import com.android.panmetroiptv.extensions.getIntValue
import com.android.panmetroiptv.extensions.provideMacAddress
import com.android.panmetroiptv.model.data.sseresponse.ScrollMessage
import com.android.panmetroiptv.utils.uistate.PreferenceManager
import com.android.panmetroiptv.view.uicomponent.fingerprint.state.MarqueeTextViewWithCallback
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.max


@Composable
fun ScrollingMessageOverlay(
    scrollMessageInfo: ScrollMessage,
    onFinish:(String)-> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    // Colors with fallbacks

    val fontColor = remember(scrollMessageInfo.fontColorHex, scrollMessageInfo.fontTransparency) {
        try {
            val colorHex = scrollMessageInfo.fontColorHex ?: "#FFFFFF"
            val baseColor = Color(android.graphics.Color.parseColor(colorHex))

            val transparency = scrollMessageInfo.fontTransparency?.getFloatValue() ?: 0.25f
            val alpha = 1f - transparency.coerceIn(0f, 1f)

            baseColor.copy(alpha = alpha)
        } catch (e: Exception) {
            Color.Black.copy(alpha = 0.25f) // Fallback color
        }
    }

    val bgColor = remember(scrollMessageInfo.backgroundColorHex, scrollMessageInfo.backgroundTransparency) {
        try {
            val colorHex = scrollMessageInfo.backgroundColorHex ?: "#FFFFFF"
            val baseColor = Color(android.graphics.Color.parseColor(colorHex))

            val transparency = scrollMessageInfo.backgroundTransparency?.getFloatValue() ?: 0.25f
            val alpha = 1f - transparency.coerceIn(0f, 1f)

            baseColor.copy(alpha = alpha)
        } catch (e: Exception) {
            Color.White.copy(alpha = 0.25f) // Fallback color
        }
    }

    // Calculate screen width in pixels
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    // Measure text width in pixels
    val yOffsetDp = with(density) {   (screenHeightPx * .85f).toDp() }

    // Processed message with pattern replacements
    var processedMessage = remember(scrollMessageInfo.message) {
         context.checkPatternMatchInfo(scrollMessageInfo.message.orEmpty()).trimIndent()
    }

    var visible by remember { mutableStateOf(true) }
    val repeatCount = remember { scrollMessageInfo.repeatCount?.toString()?.getIntValue() ?: -1 }
    var currentRepeats by remember { mutableStateOf(0) }
    // Calculate font size safely
    var fontSize = remember(scrollMessageInfo.fontSizeDp) {
        scrollMessageInfo.fontSizeDp?.toString()?.getFloatValue()?.sp ?: 16.sp
    }
    var textWidth by remember { mutableStateOf(0) }
    var containerWidth by remember { mutableStateOf(0) }

    // Get duration from scrollMessageInfo (in seconds, convert to milliseconds)
    /*val durationSeconds = remember {
        scrollMessageInfo.durationSec ?: 1
    }
    val durationMs = durationSeconds * 1000L

    // Auto-hide after duration if specified
    LaunchedEffect(visible, durationMs) {
        if (visible && durationMs > 0) {
            delay(durationMs)
            visible = false
        }
    }*/

    /*LaunchedEffect(visible, scrollMessageInfo.durationSec) {
        val isInfinite = (scrollMessageInfo.durationSec ?: 0) == -1
        if (visible) {
            durationMs = (if(isInfinite){ Int.MAX_VALUE } else (scrollMessageInfo.durationSec ?: 1)) * 1000L

            if (durationMs > 0 && !isInfinite) {
                // Start a new coroutine that can be cancelled if duration changes
                val job = launch {
                    delay(durationMs)
                    visible = false
                }

                // Cancel the previous job if duration changes
                awaitCancellation()
                job.cancel()

                if(scrollMessageInfo?.value?.messageScope?.equals("GLOBAL",true) == true){
                    scrollMessageInfo?.value?.updatedAt?.let { PreferenceManager.saveGlobalScrollTime(it) }
                }else if(scrollMessageInfo?.value?.messageScope?.equals("PLAYER",true) == true){
                    scrollMessageInfo?.value?.updatedAt?.let { PreferenceManager.savePlayerScrollTime(it) }
                }
            }
        }
    }*/




    LaunchedEffect(visible, scrollMessageInfo.durationSec) {
        if (!visible) return@LaunchedEffect

        val durationSec = scrollMessageInfo.durationSec ?: 1
        val isInfinite = durationSec == -1

        if (isInfinite) {
            // Wait indefinitely for infinite duration
            try {
                Log.e("sTimestamp >isInfinite:","$isInfinite $durationSec")

                awaitCancellation()
            } catch (e: CancellationException) {
                // Clean exit
            }
            return@LaunchedEffect
        }

        val durationMs = durationSec * 1000L
        if (durationMs <= 0) {
            visible = false // Hide immediately for zero/negative duration
            return@LaunchedEffect
        }

        try {
            delay(durationMs)
            visible = false
            scrollMessageInfo.updatedAt?.let { onFinish(it) }

            // Save timestamp AFTER successful completion
           /* scrollMessageInfo?.let { info ->
                info._id?.let { id ->
                    info.updatedAt?.let { updatedAt ->
                        PreferenceManager.saveScrollUpdatedAt(id, updatedAt)
                    }
                }
            }*/
        } catch (e: CancellationException) {
            // Don't save timestamp if cancelled
        }
    }

    if(visible) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(start = with(density) { 0.dp }, top = with(density) { yOffsetDp })
                .background(bgColor).onSizeChanged { containerWidth = it.width }
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 10.dp)
                    .align(Alignment.Center),
                factory = { ctx ->

                    MarqueeTextViewWithCallback(ctx, onMarqueeComplete = {

                    }).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        setSingleLine(true)
                        ellipsize = TextUtils.TruncateAt.MARQUEE
                        isFocusable = true
                        isFocusableInTouchMode = true
                        isSelected = true
                    }
                },
                update = { tv ->
                    // Measure current text width
                    // Measure current text width
                    val textPaint = tv.paint
                    val currentTextWidth = textPaint.measureText(processedMessage)

                    // Calculate if we need whitespace
                    val needsWhiteSpace = currentTextWidth < containerWidth

                    // Add whitespace if needed
                    val displayText = if (needsWhiteSpace) {
                        val spaceCount = max(10, (containerWidth / textPaint.measureText(" ")).toInt() / 2)
                        val spaces = " ".repeat(spaceCount)
                        "$spaces$processedMessage$spaces"
                    } else {
                        processedMessage
                    }

                    // Update text and styling
                    tv.text = displayText
                    tv.setTextColor(fontColor.toArgb())
                    tv.textSize = fontSize.value

                    // Set duration or repeat count
                   // (tv as MarqueeTextViewWithCallback).setDuration(durationMs)

                    /*if (durationMs > 0) {
                        (tv as MarqueeTextViewWithCallback).setDuration(durationMs)
                    } else {
                        (tv as MarqueeTextViewWithCallback).setRepeatCount(repeatCount)
                    }*/
                }
            )
        }
    }

    DisposableEffect(Unit) {
        // onDispose runs when the composable leaves composition
        onDispose {
            // Only save if we're being disposed but the timer hasn't completed yet
            if (visible) {
                scrollMessageInfo?.let { info ->
                    info._id?.let { id ->
                        info.updatedAt?.let { updatedAt ->
                            Log.w("ScrollingMessage", "Message disposed before completion: $id")
                            // Optional: decide if you want to save partial progress
                        }
                    }
                }
            }
        }
    }

}


fun formatMarqueeText(rawText: String): String {
    return rawText
        .lines()                   // Split into lines
        .map { it.trim() }         // Trim each line
        .filter { it.isNotBlank() }// Remove blank lines
        .joinToString(" ")         // Join with single spaces
        .replace(Regex("\\s+"), " ") // Collapse any remaining multiple spaces
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
