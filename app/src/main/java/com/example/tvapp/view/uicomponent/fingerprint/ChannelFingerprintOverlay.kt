package com.example.tvapp.view.uicomponent.fingerprint

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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.ui.PlayerView
import com.example.tvapp.extensions.generateTextFingerprint
import com.example.tvapp.extensions.getFloatValue
import com.example.tvapp.extensions.getIntValue
import com.example.tvapp.extensions.showToastS
import com.example.tvapp.model.data.sseresponse.PlayerFingerprint
import kotlinx.coroutines.delay

@Composable
fun ChannelFingerprintOverlay(
    player: PlayerView?=null,
    fingerprintRule: MutableState<PlayerFingerprint>
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

    var visible by remember { mutableStateOf(true) }
    var currentOffset by remember { mutableStateOf(Offset.Zero) }

    val screenWidth = player?.width?:LocalConfiguration.current.screenWidthDp
    val screenHeight = player?.height?:LocalConfiguration.current.screenHeightDp
    val density = LocalDensity.current
    val posX = fingerprintRule.value.posXPercent?.coerceIn(0f, .9f) ?: 0.5f
    val posY = fingerprintRule.value.posYPercent?.coerceIn(0f, .9f) ?: 0.5f
    //val qrBitmap = remember { displayMessage.generateQRCodeBitmap() }


    LaunchedEffect(fingerprintRule) {
        repeat(fingerprintRule.value.repeatCount?.toString().getIntValue()) {
            delay(fingerprintRule.value.intervalSec?.toString().getIntValue() * 1000L)
            if (fingerprintRule.value.positionMode?.uppercase() == "RANDOM") {
                val randX = (24..(screenWidth - 24)).random()
                val randY = (48..(screenHeight - 48)).random()
                currentOffset = Offset(randX.toFloat(), randY.toFloat())
            } else {
                 context.showToastS("posXPercent${screenWidth * posX} and posYPercent: ${screenHeight * posY}")
                currentOffset = Offset(
                    x = screenWidth * posX,
                    y = screenHeight * posY
                )
            }

            visible = true
            delay(fingerprintRule.value.durationMs?.toString().getIntValue().toLong())
            visible = false
        }
    }

    if (visible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = with(density) { currentOffset.x.dp }, top = with(density) { currentOffset.y.dp })
        ) {

            /*Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer(alpha = 0.5f)
            )*/

            Text(
                text = displayMessage,
                fontSize = fingerprintRule.value.fontSizeDp?.toString().getIntValue().sp,
                color = fontColor,
                modifier = Modifier
                    .background(bgColor, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
