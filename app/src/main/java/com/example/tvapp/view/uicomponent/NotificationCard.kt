package com.example.tvapp.view.uicomponent

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

import android.text.TextUtils
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.graphics.toArgb


@Composable
fun NotificationCard(message: String, onDismiss: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDismiss() }   // tap to dismiss early
        ) {
            // AndroidView to get a classic TextView marquee
            AndroidView(
                factory = { context ->
                    TextView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        setSingleLine(true)
                        ellipsize = TextUtils.TruncateAt.MARQUEE
                        isSelected = true   // marquee only works if selected==true
                        text = message
                        textSize = 16f
                        setPadding(16, 12, 16, 12)
                        setBackgroundColor(0xFF333333.toInt())
                        setTextColor(Color.White.toArgb())

                    }
                },
                update = { tv -> tv.text = message },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
