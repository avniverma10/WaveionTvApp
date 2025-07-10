package com.example.tvapp.view.uicomponent.fingerprint

import android.content.Context
import android.text.TextUtils
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import com.example.tvapp.extensions.capitalizeFirstLetter
import com.example.tvapp.extensions.getFloatValue
import com.example.tvapp.extensions.getIntValue
import com.example.tvapp.extensions.provideMacAddress
import com.example.tvapp.model.data.message.ScrollMessageInfo
import com.example.tvapp.model.data.sseresponse.ScrollMessage
import com.example.tvapp.utils.uistate.PreferenceManager
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun ScrollingMessageOverlay(
    player: PlayerView?=null,
    scrollMessageInfo: MutableState<ScrollMessage>
) {
    val context = LocalContext.current

    val density = LocalDensity.current
    val config = LocalConfiguration.current

    val screenWidthPx = player?.width?:with(density) { config.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) {player?.height?.dp?.toPx()}?:with(density) { config.screenHeightDp.dp.toPx() }

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

    // Measure text width in pixels
    var yOffsetDp by remember { mutableStateOf(0.dp) }


    LaunchedEffect(scrollMessageInfo.value.positionMode, scrollMessageInfo.value.posYPercent, scrollMessageInfo.value.repeatCount) {
        if (scrollMessageInfo.value.positionMode.equals("RANDOM", ignoreCase = true)) {
            // Random mode: change Y `repeatCount` times, waiting `intervalMs` each time
            repeat(scrollMessageInfo.value.repeatCount ?: Int.MAX_VALUE) {
                val pct = Random.nextFloat().coerceIn(0f, 0.8f)
                val yPx  = screenHeightPx * pct
                yOffsetDp = with(density) { yPx.toDp() }
                delay(scrollMessageInfo.value.randomIntervalSec?.toString().getIntValue() * 1000L)
            }
        } else {
            // Fixed mode: only once
            val pct = scrollMessageInfo.value.posYPercent?.coerceIn(0f, 0.8f) ?: 0.5f
            val yPx = screenHeightPx * pct
            yOffsetDp = with(density) { yPx.toDp() }
        }
    }

    Box(
        modifier= Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(start = with(density) { 0.dp }, top = with(density) { yOffsetDp })
            .background(bgColor)
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp, bottom = 15.dp)
                .align(Alignment.Center),
            factory = { ctx ->
                TextView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setSingleLine(true)
                    ellipsize = TextUtils.TruncateAt.MARQUEE
                    isFocusable = true
                    isFocusableInTouchMode = true
                    isSelected = true       // this actually starts the marquee
                }
            },
            update = { tv ->
                // Always update text *and* color (and bg if you like)
                tv.text = context.checkPatternMatchInfo(scrollMessageInfo.value.message.orEmpty())
                tv.setTextColor(fontColor.toArgb())
                tv.textSize = scrollMessageInfo.value.fontSizeDp.toString().getFloatValue()
                tv.marqueeRepeatLimit = scrollMessageInfo.value.repeatCount?.toString().getIntValue()  // infinite
            }
        )
    }
}




fun Context.checkPatternMatchInfo(message:String):String{
    /* var modifiedMessage = message
    if(message.contains("$$@User")){
        modifiedMessage = message.replace("$$@User"," ${PreferenceManager.getUsername()} ")
    }else if(message.contains("$$@Mac")){
        modifiedMessage = message.replace("$$@Mac"," ${this.provideMacAddress()} ")
    }else if(message.contains("$$@Package")){
        modifiedMessage = message.replace("$$@Package"," ${PreferenceManager.getLoginResponse()?.pkgdata?.activepack?.map { it.servicename }?.joinToString(",")?.capitalizeFirstLetter()} ")
    }*/
    return  message.replace("$$@User"," ${PreferenceManager.getUsername()} ").replace("$$@Mac"," ${this.provideMacAddress()} ").replace("$$@Package"," ${PreferenceManager.getLoginResponse()?.let {
        it.loginData?.packages?.joinToString( ",") { it.packageName }}?.capitalizeFirstLetter()} ")
}

