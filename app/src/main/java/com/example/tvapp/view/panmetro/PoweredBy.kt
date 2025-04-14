package com.example.tvapp.view.panmetro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.example.tvapp.R
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
                .background(Color(0xFF49FEDD))
                .align(Alignment.CenterVertically)
                .padding(start = 5.dp, end = 5.dp)
        )


        // Dynamic date/time
        Text(
            text = "Powered by GTPL",
            color = Color(0xFF49FEDD),
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold

            ),
            modifier = Modifier.padding(start = 10.dp, end = 10.dp),

            )
    }

}
