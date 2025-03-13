package com.example.tvapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun GradientBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF121417).copy(alpha = 0.8f),
                        Color(0xFF121417).copy(),
                        Color(0xFF121417)

                    ),
                    center = Offset(x = 1000f, y = 1600f),  // Center it lower for a smooth bottom arc
                    radius = 2700f // Large radius to ensure full coverage//
                )
            )
    )
}