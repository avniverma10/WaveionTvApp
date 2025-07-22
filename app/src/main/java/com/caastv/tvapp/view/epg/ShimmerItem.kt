package com.caastv.tvapp.view.epg

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerItem() {
    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF27363B).copy(alpha = 0.6f),
            Color(0xFF49FEDD).copy(alpha = 0.2f),
            Color(0xFF27363B).copy(alpha = 0.6f)
        ),
        start = Offset(translateAnim - 500, 0f),
        end = Offset(translateAnim, 0f)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(vertical = 4.dp)
            .background(brush)
    ) {
        Box(
            modifier = Modifier
                .width(180.dp)
                .fillMaxHeight()
                .background(Color(0xFF2A3139))
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(Color(0xFF2A3139))
        )
    }
}