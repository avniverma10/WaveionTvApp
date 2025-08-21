package com.android.panmetroiptv.view.uicomponent.fingerprint

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.panmetroiptv.extensions.generateTextFingerprint
import com.android.panmetroiptv.extensions.getFloatValue
import com.android.panmetroiptv.extensions.getIntValue
import com.android.panmetroiptv.model.data.sseresponse.Fingerprint
import kotlinx.coroutines.delay

@Composable
fun GlobalFingerprintOverlay(
    fingerprintRule: MutableState<Fingerprint>
) {
    val context = LocalContext.current

    val displayMessage = context.generateTextFingerprint(method = fingerprintRule.value.method?:"SHA2", obfuscationKey = fingerprintRule.value.obfuscationKey?:"12")
    val fontColor = runCatching {
        Color(android.graphics.Color.parseColor(fingerprintRule.value.fontColorHex ?: "#000000"))
            .copy(alpha = fingerprintRule.value.fontTransparency
                ?.getFloatValue()
                ?.let { 1f - it.coerceIn(0f, 1f) } ?: 0.25f)
    }.getOrDefault(Color.Black.copy(alpha = 0.25f))

    val bgColor = runCatching {
        Color(android.graphics.Color.parseColor(fingerprintRule.value.backgroundColorHex ?: "#ffffff"))
            .copy(alpha = fingerprintRule.value.backgroundTransparency
                ?.getFloatValue()
                ?.let { 1f - it.coerceIn(0f, 1f) } ?: 0.25f)
    }.getOrDefault(Color.White.copy(alpha = 0.25f))

    var visible by remember { mutableStateOf(false) }
    var currentOffset by remember { mutableStateOf(Offset.Zero) }
    var isRandom by remember { mutableStateOf(false) }


    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    // Convert screen dimensions to pixels once
    val screenWidth = with(density) { (LocalConfiguration.current.screenWidthDp).dp.toPx() }
    val screenHeight = with(density) { (LocalConfiguration.current.screenHeightDp).dp.toPx() }


    val posX = fingerprintRule.value.posXPercent?.coerceIn(0f, .9f) ?: 0.5f
    val posY = fingerprintRule.value.posYPercent?.coerceIn(0f, .9f) ?: 0.5f

    var textWidth by remember { mutableStateOf(0) }
    var textHeight by remember { mutableStateOf(0) }

    // Calculate maximum available width for text (80% of screen width)
    val maxTextWidthPx = screenWidth * 0.8f
    val paddingPx = with(density) { 16.dp.toPx() }

    val measurer = rememberTextMeasurer()
    var fontSize = remember(fingerprintRule.value.fontSizeDp) {
        fingerprintRule.value.fontSizeDp?.toString()?.getFloatValue()?.sp ?: 16.sp
    }

    // Function to update position ensuring text stays within bounds
    fun updatePosition() {
        if (isRandom) {
            val randomXValue = ((0..9).random() / 10f)?.coerceIn(0f, .9f) ?: 0.5f
            val randomYValue = ((0..9).random() / 10f)?.coerceIn(0f, .9f) ?: 0.5f
            val adjustedX = (screenWidth * randomXValue.toFloat()).coerceIn(
                paddingPx,
                screenWidth - textWidth - paddingPx
            )
            val adjustedY = (screenHeight * randomYValue).coerceIn(
                paddingPx,
                screenHeight - textHeight - paddingPx
            )
            currentOffset = Offset(adjustedX, adjustedY)
            // For random position, ensure text stays within screen bounds
            /*val minX = paddingPx
            val maxX = screenWidth - textWidth - paddingPx
            val minY = paddingPx
            val maxY = screenWidth - textHeight - paddingPx

            val randX = (minX.toInt()..(max(maxX, minX + 1f)).toInt()).random()
            val randY = (minY.toInt()..(max(maxY, minY + 1f)).toInt()).random()
            currentOffset = Offset(randX.toFloat(), randY.toFloat())*/
        } else {
            // For fixed position, adjust to ensure text fits
            val adjustedX = (screenWidth * posX).coerceIn(
                paddingPx,
                screenWidth - textWidth - paddingPx
            )
            val adjustedY = (screenHeight * posY).coerceIn(
                paddingPx,
                screenHeight - textHeight - paddingPx
            )
            currentOffset = Offset(adjustedX, adjustedY)
        }
    }

    LaunchedEffect(fingerprintRule) {
        // Calculate font size safely
       val textResult = measurer.measure(
            text = buildAnnotatedString { append(displayMessage) },
            style = TextStyle(fontSize = fontSize),
            softWrap = false,
            maxLines = 1
        )
        // Validate duration and interval
        val durationMs = (fingerprintRule.value.durationMs?.toString()?.getFloatValue() ?: 0f) * 1000L
        val intervalMs = (fingerprintRule.value.intervalSec?.toString()?.getFloatValue() ?: 0f) * 1000L
        val repeatCount = fingerprintRule.value.repeatCount?.toString()?.getIntValue() ?: 1
        isRandom = fingerprintRule.value.positionMode?.uppercase().equals("RANDOM",true)
        textResult?.let {
            textWidth = textResult.size.width
            textHeight = textResult.size.height
        }
        updatePosition()
        if (repeatCount > 0) {
            repeat(repeatCount) {
                delay(intervalMs.toLong())
                updatePosition()
                visible = true
                delay(durationMs.toLong())
                visible = false
            }
        }else {
            // Infinite loop
            while (true) {
                delay(intervalMs.toLong())
                updatePosition()
                visible = true
                delay(durationMs.toLong())
                visible = false
            }
        }
    }

    if (visible) {
        Box(
            modifier = Modifier
                .fillMaxSize().padding(
                    start = with(density) { currentOffset.x.toDp() },
                    top = with(density) { currentOffset.y.toDp() }
                )
        ) {
            Text(
                text = displayMessage,
                fontSize = fingerprintRule.value.fontSizeDp?.toString().getIntValue().sp,
                color = fontColor,
                maxLines = 1,
                overflow = TextOverflow.Visible,
                modifier = Modifier
                    .background(bgColor, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .onSizeChanged { size ->
                        textWidth = size.width
                        textHeight = size.height
                    }
            )
        }
    }
}
