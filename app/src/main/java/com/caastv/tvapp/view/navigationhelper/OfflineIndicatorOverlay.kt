package com.caastv.tvapp.view.navigationhelper

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun OfflineIndicatorOverlay(
    text: String = "Server Offline") {
        // Pulsing animation for the red dot
        var pulse = rememberInfiniteTransition(label = "pulse")
            .animateFloat(
                initialValue = 1f,
                targetValue   = 0.4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 800, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ), label = "alpha"
            )

        Row(
            modifier = Modifier
                .background(Color(0x4B1E1E1E), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Absolute.Right
        ) {
            // Red dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .graphicsLayer { alpha = pulse.value }
                    .background(Color.Red, CircleShape)
            )

            /*Spacer(Modifier.width(8.dp))

            // Text
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.h3
            )*/
        }

}
