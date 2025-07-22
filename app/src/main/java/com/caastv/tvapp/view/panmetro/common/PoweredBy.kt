package com.caastv.tvapp.view.panmetro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.caastv.tvapp.utils.theme.base_color

@Composable
fun PoweredBy() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(top = 10.dp)
            .background(Color.Black),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Thin green line below
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .background(color = base_color)
                .align(Alignment.CenterVertically)
                .padding(start = 5.dp, end = 5.dp)
        )


        // Dynamic date/time
        Text(
            text = "Powered by GTPL",
            color = base_color,
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold

            ),
            modifier = Modifier.padding(start = 10.dp, end = 10.dp),

            )
    }

}
