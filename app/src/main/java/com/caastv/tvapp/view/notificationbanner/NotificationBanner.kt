package com.caastv.tvapp.view.notificationbanner

import android.text.TextUtils
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun NotificationBanner(
    message: String,
    visible: Boolean,
    modifier: Modifier
) {
    if (!visible) return

    // Semi-transparent black background, full width, fixed height
    Box(
        modifier
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
                    textSize = 18f
                    setTextColor(Color.White.toArgb())
                }
            },
            update = {
                it.text = message
            }
        )
    }
}
