package com.caastv.tvapp.view.uicomponent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun panGradientBackground(
    topColor: Color = Color(0xFF5B7AB0),  // Example top color (Deep Orange)
    bottomColor: Color = Color(0xFF94C2F3), // Example bottom color (Green)
    content: @Composable () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(topColor, bottomColor)
                )
            )
    ) {
        content()
    }
}