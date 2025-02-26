package com.example.tvapp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun TimeHeader(leftPanelWidth: Dp) {
    // Use a fixed current time for initialization; update every minute.
    val fixedCurrentTime = remember { mutableStateOf(parseFixedTime("20250205010000")) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            fixedCurrentTime.value = System.currentTimeMillis()
        }
    }
    val timeSlots = remember(fixedCurrentTime.value) { generateTimeSlots(fixedCurrentTime.value) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Color.Black),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Use the leftPanelWidth so the time slots align with the timeline below.
        Spacer(modifier = Modifier.width(leftPanelWidth))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            timeSlots.forEach { time ->
                Text(
                    text = time,
                    color = Color.White,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

fun parseFixedTime(timestamp: String): Long {
    val format = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
    return format.parse(timestamp)?.time ?: System.currentTimeMillis()
}

fun generateTimeSlots(currentMillis: Long): List<String> {
    val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val calendar = Calendar.getInstance().apply {
        timeInMillis = currentMillis
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        val minute = get(Calendar.MINUTE)
        set(Calendar.MINUTE, if (minute < 30) 0 else 30)
    }
    return List(5) {
        val time = dateFormat.format(calendar.time)
        calendar.add(Calendar.MINUTE, 30)
        time
    }
}