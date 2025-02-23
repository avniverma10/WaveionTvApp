package com.example.tvapp.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tvapp.viewmodels.EPGViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun EPGContent(viewModel: EPGViewModel = hiltViewModel()) {
    val filteredPrograms by viewModel.filteredPrograms.collectAsState()
    // Use a fixed timestamp for initialization; will update with system time.
    val currentTimeMillis = remember { mutableStateOf(parseFixedTime("20250208104600")) }

    // Update current time every second for smooth movement.
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTimeMillis.value = System.currentTimeMillis()
        }
    }

    // minutesPerPixel for program scrolling (adjust as needed).
    val minutesPerPixel = 2

    // Define a fixed left margin equal to the channel names column width.
    val channelColumnWidth = 120.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Top row with "All" text.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color.Black),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "All",
                color = Color.White,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.width(50.dp))
        }

        // Wrap the time header and program listings in one Box so we can overlay the red indicator.
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            // Get full container width.
            val containerWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
            // Compute the left margin (channel column width) in pixels.
            val channelColumnWidthPx = with(LocalDensity.current) { channelColumnWidth.toPx() }
            // The timeline area is the remainder of the width.
            val timelineWidthPx = containerWidthPx - channelColumnWidthPx

            // Compute the start of the current 30-minute block by flooring minutes.
            val blockStartMillis = run {
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = currentTimeMillis.value
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    val minute = get(Calendar.MINUTE)
                    set(Calendar.MINUTE, if (minute < 30) 0 else 30)
                }
                calendar.timeInMillis
            }
            // Calculate the fraction (0.0 to 1.0) of the current half‑hour that has elapsed.
            val fraction = ((currentTimeMillis.value - blockStartMillis)
                .coerceAtLeast(0)
                .toFloat()) / (30 * 60 * 1000).toFloat()
            // Assume the TimeHeader displays 5 slots (each 30 minutes) over the timeline area.
            val oneSlotWidthPx = timelineWidthPx / 5f
            // The red indicator should start at the beginning of the timeline (i.e. after the channel column)
            // and then move within the first slot.
            val indicatorOffsetPx = channelColumnWidthPx + fraction * oneSlotWidthPx
            val indicatorOffsetDp = with(LocalDensity.current) { indicatorOffsetPx.toDp() }

            // Column containing header and program listings.
            Column(modifier = Modifier.fillMaxSize()) {
                // Time header: add a left spacer so time slots start from the timeline area.
                TimeHeader(channelColumnWidth)
                // Program listings.
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredPrograms.groupBy { it.channelId }.entries.toList()) { (channelId, programs) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp)
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Channel names column.
                            Column(
                                modifier = Modifier
                                    .width(channelColumnWidth)
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(text = "ID: $channelId", color = Color.White, fontSize = 14.sp)
                                Text(text = "Channel $channelId", color = Color.Gray, fontSize = 12.sp)
                            }
                            // Program timeline.
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    // Adjust scrolling of the program timeline as needed.
                                    .padding(start = maxOf(0, -((currentTimeMillis.value / 60000) % minutesPerPixel).toInt()).dp)
                            ) {
                                items(programs) { program ->
                                    // Use a formatter that includes the timezone offset.
                                    val timeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss Z", Locale.getDefault())
                                    // Parse the program’s start and end times.
                                    val startTime = OffsetDateTime.parse(program.startTime, timeFormatter).toLocalTime()
                                    val endTime = OffsetDateTime.parse(program.endTime, timeFormatter).toLocalTime()
                                    val programDuration = Duration.between(startTime, endTime).toMinutes()
                                    val programWidth = maxOf((programDuration.toInt() / minutesPerPixel).dp, 50.dp)

                                    // Each program is focusable and shows a white border when focused.
                                    val isFocused = remember { mutableStateOf(false) }
                                    Box(
                                        modifier = Modifier
                                            .width(programWidth)
                                            .height(60.dp)
                                            .background(Color.DarkGray)
                                            .then(
                                                if (isFocused.value)
                                                    Modifier.border(2.dp, Color.White)
                                                else Modifier
                                            )
                                            .onFocusChanged { isFocused.value = it.isFocused }
                                            .focusable(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = program.eventName,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    // Divider between program items.
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(50.dp)
                                            .background(Color.White)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            // Overlay the vertical red indicator spanning the full height.
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffsetDp)
                    .fillMaxHeight()
                    .width(2.dp)
                    .background(Color.Red)
            )
        }
    }
}

@Composable
fun TimeHeader(channelOffset: androidx.compose.ui.unit.Dp) {
    // Use a fixed current time for initialization; update every minute.
    val fixedCurrentTime = remember { mutableStateOf(parseFixedTime("20250208104600")) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000) // Update every minute.
            fixedCurrentTime.value = System.currentTimeMillis()
        }
    }
    // Generate 30-minute time slots (by flooring the current time) for the timeline area.
    val timeSlots = remember(fixedCurrentTime.value) { generateTimeSlots(fixedCurrentTime.value) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Color.Black)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Add a left spacer equal to the channel column width so the time slots align with the programs.
        Spacer(modifier = Modifier.width(channelOffset))
        // The remaining width is used for time slots.
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

// Parse the custom timestamp format (yyyyMMddHHmmss) into milliseconds.
fun parseFixedTime(timestamp: String): Long {
    val format = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
    return format.parse(timestamp)?.time ?: System.currentTimeMillis()
}

// Generate 30-minute time slots by flooring the current time to the nearest half‑hour.
fun generateTimeSlots(currentMillis: Long): List<String> {
    val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val calendar = Calendar.getInstance().apply {
        timeInMillis = currentMillis
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        val minute = get(Calendar.MINUTE)
        // Floor minutes to 0 or 30.
        set(Calendar.MINUTE, if (minute < 30) 0 else 30)
    }
    // Generate 5 slots (covering a 150‑minute window).
    return List(5) {
        val time = dateFormat.format(calendar.time)
        calendar.add(Calendar.MINUTE, 30)
        time
    }
}
