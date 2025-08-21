package com.android.panmetroiptv.view.uicomponent.fingerprint

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import com.android.panmetroiptv.model.data.sseresponse.PlayerFingerprint
import com.android.panmetroiptv.utils.uistate.PreferenceManager
import kotlinx.coroutines.delay

@Composable
fun PrePlayerFingerprintOverlay(
    fingerprintRule: PlayerFingerprint
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    // Convert screen dimensions to pixels once
    val screenWidth = with(density) { (LocalConfiguration.current.screenWidthDp).dp.toPx()/2 }
    val screenHeight = with(density) { (LocalConfiguration.current.screenHeightDp).dp.toPx()/2 }
    val displayMessage = context.generateTextFingerprint(method = fingerprintRule.method?:"SHA2", obfuscationKey = fingerprintRule.obfuscationKey?:"12")

    val fontColor = runCatching {
        Color(android.graphics.Color.parseColor(fingerprintRule.fontColorHex ?: "#000000"))
            .copy(alpha = fingerprintRule.fontTransparency
                ?.getFloatValue()
                ?.let { 1f - it.coerceIn(0f, 1f) } ?: 0.25f)
    }.getOrDefault(Color.Black.copy(alpha = 0.25f))

    val bgColor = runCatching {
        Color(android.graphics.Color.parseColor(fingerprintRule.backgroundColorHex ?: "#ffffff"))
            .copy(alpha = fingerprintRule.backgroundTransparency
                ?.getFloatValue()
                ?.let { 1f - it.coerceIn(0f, 1f) } ?: 0.25f)
    }.getOrDefault(Color.White.copy(alpha = 0.25f))

    var visible by remember { mutableStateOf(false) }
    var currentOffset by remember { mutableStateOf(Offset.Zero) }
    var isRandom by remember { mutableStateOf(false) }



    val posX = fingerprintRule.posXPercent?.coerceIn(0f, .9f) ?: 0.5f
    val posY = fingerprintRule.posYPercent?.coerceIn(0f, .9f) ?: 0.5f

    var textWidth by remember { mutableStateOf(0) }
    var textHeight by remember { mutableStateOf(0) }
    val paddingPx = with(density) { 10.dp.toPx() }

    val measurer = rememberTextMeasurer()
    var fontSize = remember(fingerprintRule.fontSizeDp) {
        fingerprintRule.fontSizeDp?.toString()?.getFloatValue()?.sp ?: 16.sp
    }

    // Function to update position ensuring text stays within bounds
    fun updatePosition() {
        // Calculate safe bounds to prevent negative values
        val minX = paddingPx
        val maxX = (screenWidth - textWidth - paddingPx).coerceAtLeast(paddingPx)

        val minY = paddingPx
        val maxY = (screenHeight - textHeight - paddingPx).coerceAtLeast(paddingPx)

        // Log warnings if bounds are invalid
        if (maxX < minX) {
            Log.w("PositionUpdate", "X bounds invalid: maxX($maxX) < minX($minX). Using minX as fallback.")
        }

        if (maxY < minY) {
            Log.w("PositionUpdate", "Y bounds invalid: maxY($maxY) < minY($minY). Using minY as fallback.")
        }

        if (isRandom) {
            val randomXValue = ((0..9).random() / 10f).coerceIn(0f, 0.9f)
            val randomYValue = ((0..9).random() / 10f).coerceIn(0f, 0.9f)

            val adjustedX = (screenWidth * randomXValue).coerceIn(minX, maxX)
            val adjustedY = (screenHeight * randomYValue).coerceIn(minY, maxY)

            currentOffset = Offset(adjustedX, adjustedY)
        } else {
            // For fixed position, adjust to ensure text fits
            val adjustedX = (screenWidth * posX).coerceIn(minX, maxX)
            val adjustedY = (screenHeight * posY).coerceIn(minY, maxY)

            currentOffset = Offset(adjustedX, adjustedY)
        }
    }

    LaunchedEffect(fingerprintRule) {
        // Validate duration and interval
        val durationMs = (fingerprintRule.durationMs?.toString()?.getFloatValue() ?: 0f) * 1000
        val intervalMs = (fingerprintRule.intervalSec?.toString()?.getFloatValue() ?: 0f) * 1000
        val repeatCount = fingerprintRule.repeatCount?.toString()?.toInt() ?: 1
        val repeatCountMessage = if(repeatCount>0) repeatCount else Int.MAX_VALUE
        // Calculate font size safely
        val textResult = measurer.measure(
            text = buildAnnotatedString { append(displayMessage) },
            style = TextStyle(fontSize = fontSize),
            softWrap = false,
            maxLines = 1
        )
        isRandom = fingerprintRule.positionMode?.uppercase().equals("RANDOM",true)
        textResult?.let {
            textWidth = textResult.size.width
            textHeight = textResult.size.height
        }
        updatePosition()
        repeat(repeatCountMessage) {
            delay(intervalMs.toLong())
            updatePosition()
            visible = true
            delay(durationMs.toLong())
            visible = false
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
                fontSize = fontSize,
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
