package com.android.panmetroiptv.view.uicomponent.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.text.isNotEmpty

@Composable
fun InputOverlay(typedDigits: String) {
    if (typedDigits.isNotEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, end = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)  // Explicitly align to top-end
                    .background(Color(0xFF49FEDD), RoundedCornerShape(4.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = typedDigits,
                    style = MaterialTheme.typography.h3,
                    color = Color.Black
                )
            }
        }
    }
}