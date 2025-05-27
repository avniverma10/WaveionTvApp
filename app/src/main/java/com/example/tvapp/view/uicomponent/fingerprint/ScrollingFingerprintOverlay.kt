package com.example.tvapp.view.uicomponent.fingerprint

import android.text.TextUtils
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.tvapp.extensions.getFloatValue
import com.example.tvapp.extensions.getIntValue
import com.example.tvapp.model.data.message.ScrollMessageInfo

@Composable
fun ScrollingFingerprintOverlay(
    scrollMessageInfo: MutableState<ScrollMessageInfo>,
    verticalPercent: Float? = 0.50f,      // 0.0 = top, 1.0 = bottom
    scrollDurationMs: Int? = 15_000,     // time to scroll from right → left
) {
    val context = LocalContext.current

    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val screenWidthPx = with(density) { config.screenWidthDp.dp.toPx() }
    val verticalOffsetDp = (config.screenHeightDp * verticalPercent.toString().getFloatValue()).dp

    val fontColor = runCatching {
        Color(android.graphics.Color.parseColor(scrollMessageInfo.value.fontColorHex ?: "#000000"))
            .copy(alpha = scrollMessageInfo.value.fontTransparency?.toString()
                ?.getFloatValue()
                ?.let { 1f - it.coerceIn(0f, 1f) } ?: 0.25f)
    }.getOrDefault(Color.Black.copy(alpha = 0.25f))

    val bgColor = runCatching {
        Color(android.graphics.Color.parseColor(scrollMessageInfo.value.backgroundColorHex ?: "#ffffff"))
            .copy(alpha = scrollMessageInfo.value.backgroundTransparency?.toString()
                ?.getFloatValue()
                ?.let { 1f - it.coerceIn(0f, 1f) } ?: 0.25f)
    }.getOrDefault(Color.White.copy(alpha = 0.25f))

    // Measure text width in pixels
    var textWidthPx by remember { mutableStateOf(0f) }
    val infiniteTransition = rememberInfiniteTransition()
    val offsetX by infiniteTransition.animateFloat(
        initialValue = screenWidthPx,
        targetValue = -textWidthPx,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = scrollDurationMs.toString().getIntValue(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )


    Box(
        modifier= Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color.Black.copy(alpha = 0.7f))
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            factory = { ctx ->
                TextView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setSingleLine(true)
                    ellipsize = TextUtils.TruncateAt.MARQUEE
                    marqueeRepeatLimit = -1  // infinite
                    isFocusable = true
                    isFocusableInTouchMode = true
                    isSelected = true       // this actually starts the marquee
                    textSize = scrollMessageInfo.value.fontSizeDp.toString().getFloatValue()
                    setTextColor(fontColor.toArgb())
                }
            },
            update = {
                it.text = scrollMessageInfo.value.message
            }
        )
    }
}
