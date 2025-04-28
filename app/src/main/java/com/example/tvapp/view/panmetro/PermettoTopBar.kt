package com.example.tvapp.view.panmetro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.example.tvapp.ui.theme.base_color
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun PermettoTopBar(topBGColor:Color?=Color.Black) {
    // Example of dynamic time formatting
    // Hold the current time and formatted string as state
    val currentTime = remember { mutableStateOf(System.currentTimeMillis()) }
    val formattedTime = remember { mutableStateOf("") }

    // Launch a coroutine that updates the time every minute
    LaunchedEffect(Unit) {
        while (true) {
            currentTime.value = System.currentTimeMillis()
            val calendar = Calendar.getInstance().apply {
                timeInMillis = currentTime.value
            }
            val sdf = SimpleDateFormat("yyyy/MM/dd • HH:mm", Locale.US)
            formattedTime.value = sdf.format(calendar.time)
            // Wait for 60 seconds before updating again
            delay(60_000)
        }
    }

    TopAppBar(
        backgroundColor = Color.Black,
        elevation = 0.dp,
        modifier = Modifier.padding(bottom = 10.dp)
    ) {
        Box {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thin green line below
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height((1.5).dp)
                        .background(color = base_color)
                        .align(Alignment.CenterVertically)
                )
                // Logo or brand text
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        // .data("https://nextwave.waveiontechnologies.com:5000/uploads/banner/news5.jpg")
                        .diskCachePolicy(CachePolicy.ENABLED)    // cache image on disk
                        .memoryCachePolicy(CachePolicy.ENABLED)  // cache image in memory
                        .build(),
                    contentDescription = "Default Background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(200.dp)               // take full available width
                        .height(200.dp),
                    error = painterResource(R.drawable.gtpl_banner_transparent),      // Error state
                    placeholder = painterResource(R.drawable.gtpl_banner_transparent) // Loading state
                )

                // Thin green line below
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height((1.5).dp)
                        .background(color = base_color)
                        .align(Alignment.CenterVertically)
                        .padding(start = 5.dp, end = 10.dp)
                )

                // Dynamic date/time
                Text(
                    text = formattedTime.value,
                    color = base_color,
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold

                    ),
                    modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                )
            }
        }
    }

}
