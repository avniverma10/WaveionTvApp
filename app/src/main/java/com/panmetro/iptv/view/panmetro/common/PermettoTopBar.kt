package com.panmetro.iptv.view.panmetro.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.panmetro.iptv.R
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun PermettoTopBar(topBGColor:Color?=Color.Black) {
    // Example of dynamic time formatting
    // Hold the current time and formatted string as state
    val currentTime = remember { mutableStateOf(System.currentTimeMillis()) }
    val formattedCombinedTime = remember { mutableStateOf("") }
    val formattedDate = remember { mutableStateOf("") }
    val formattedTime = remember { mutableStateOf("") }

    // Launch a coroutine that updates the time every minute
    LaunchedEffect(Unit) {
        while (true) {
            currentTime.value = System.currentTimeMillis()
            val calendar = Calendar.getInstance().apply {
                timeInMillis = currentTime.value
            }
            val sdfCombined = SimpleDateFormat("yyyy/MM/dd • HH:mm", Locale.US)
            formattedCombinedTime.value = sdfCombined.format(calendar.time)
            val dateTimeValues = formattedCombinedTime.value.split("•")
            formattedDate.value = dateTimeValues.getOrNull(0)?.trim()?:""
            formattedTime.value = dateTimeValues.getOrNull(1)?.trim()?:""
            // Wait for 60 seconds before updating again
            delay(60_000)
        }
    }

    TopAppBar(
        backgroundColor = Color(0xFF1F3A6B),
        elevation = 0.dp,
        modifier = Modifier.padding(bottom = 10.dp)
    ) {
        Box {

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thin green line below
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height((2).dp)
                        .background(Color(0xFF49FEDD))
                        .align(Alignment.CenterVertically)
                )
                // Logo or brand text
                Box(modifier = Modifier
                    .weight(.5f)
                    .fillMaxHeight()) {
                    Image(
                        painter = painterResource(id = R.drawable.top_corner),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 5.dp, bottom = 5.dp)
                            .align(Alignment.Center)
                    )
                }
                // Thin green line below
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height((2).dp)
                        .background(Color(0xFF49FEDD))
                        .align(Alignment.CenterVertically)
                        .padding(start = 5.dp, end = 10.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 2.dp, end = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 1.dp, end = 5.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.calendar), // Replace with your drawable
                            contentDescription = "Time Icon",
                            tint = Color(0xFF49FEDD),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = formattedDate.value,
                            color = Color(0xFF49FEDD),
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.clock), // Replace with your drawable
                            contentDescription = "Time Icon",
                            tint = Color(0xFF49FEDD),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = formattedTime.value,
                            color = Color(0xFF49FEDD),
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // Dynamic date/time
                /*Text(
                    text = formattedTime.value,
                    color = Color(0xFF49FEDD),
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold

                    ),
                    modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                )*/
            }
        }
    }

}
