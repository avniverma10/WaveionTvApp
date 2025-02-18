package com.example.tvapp.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tvapp.R
import com.example.tvapp.models.ChannelWithPrograms
import com.example.tvapp.models.EPGChannel
import com.example.tvapp.models.EPGProgram
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

@Composable
fun EPGScreen2() {
    val currentTime = remember { mutableStateOf(System.currentTimeMillis()) }

    // Auto-update current time every minute
    LaunchedEffect(Unit) {
        while (true) {
            currentTime.value = System.currentTimeMillis()
            delay(60 * 1000)
        }
    }
    val channels = generateFakeEPGData()

    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // TIME HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color.Black),
            horizontalArrangement = Arrangement.Start
        ) {

            // Left spacer for channel logos (fixed width)
            Spacer(modifier = Modifier.width(100.dp))

            TimeHeader()
        }

        // EPG CONTENT WITH RED TIME INDICATOR
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            // The timeline area is the full width minus the 100.dp reserved for logos.
            val timelineWidth = maxWidth - 100.dp

            Box(modifier = Modifier.fillMaxSize()) {
                // Program grid for each channel
                LazyColumn {
                    items(channels) { channelWithPrograms ->
                        Row(modifier = Modifier.fillMaxWidth().height(60.dp)) {
                            // Channel logo
                            Image(
                                painter = painterResource(id = R.drawable.news8),
                                contentDescription = channelWithPrograms.channel.name,
                                modifier = Modifier.size(50.dp).padding(4.dp)
                            )
                            // Programs grid – constrained to the timeline width
                            LazyRow(modifier = Modifier.width(timelineWidth)) {
                                items(channelWithPrograms.programs) { program ->
                                    val durationMinutes =
                                        ((program.endTime - program.startTime) / 60000).toInt()
                                    // Assume the EPG covers 480 minutes (8:00 AM to 4:00 PM)
                                    val totalTimelineMinutes = 480f
                                    // Calculate program width proportionally
                                    val programWidthDp =
                                        (timelineWidth * (durationMinutes / totalTimelineMinutes))
                                            .coerceAtLeast(50.dp)
                                    Box(
                                        modifier = Modifier
                                            .width(programWidthDp)
                                            .height(50.dp)
                                            .background(Color.Gray)
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = program.eventName,
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Red time indicator drawn on top of the content.
                // It uses the timeline width (excluding the 100.dp logo space) for calculation.
                RedTimeIndicator(
                    currentTimeMillis = currentTime.value,
                    timelineWidthDp = timelineWidth
                )
            }
        }
    }
}

@Composable
fun RedTimeIndicator(currentTimeMillis: Long, timelineWidthDp: Dp) {
    // Define the fixed timeline boundaries for the EPG (8:00 AM to 4:00 PM)
    val startTimeMillis = getTimeInMillis(8, 0)  // 8:00 AM
    val endTimeMillis = getTimeInMillis(16, 0)   // 4:00 PM

    val totalDurationMillis = endTimeMillis - startTimeMillis
    val timeElapsedMillis = currentTimeMillis - startTimeMillis

    // Calculate the fraction of the timeline elapsed (clamped to [0, 1])
    val positionPercent =
        (timeElapsedMillis.toFloat() / totalDurationMillis.toFloat()).coerceIn(0f, 1f)
    // Determine the offset in dp based on the timeline width
    val offsetDp = timelineWidthDp * positionPercent

    // Draw a vertical red line across the entire content height.
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .offset(x = offsetDp)
            .width(2.dp)
            .background(Color.Red)
    )
}

// Generate Time Slots (Every 30 minutes) starting from currentTime
fun generateTimeSlots(currentTimeMillis: Long): List<String> {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    // You could adjust this to start at a fixed time (like 8:00 AM) if desired.
    val calendar = Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
    calendar.set(Calendar.MINUTE, 0)
    // Create 6 time slots (3 hours) for demonstration
    return List(4) {
        val timeSlot = sdf.format(calendar.time)
        calendar.add(Calendar.MINUTE, 30)
        timeSlot
    }
}

fun generateFakeEPGData(): List<ChannelWithPrograms> {
    return listOf(
        ChannelWithPrograms(
            channel = EPGChannel(id = "1", name = "Aaj Tak"),
            programs = listOf(
                EPGProgram("1", "1", "2025-02-18", startTime = getTimeInMillis(8, 0), endTime = getTimeInMillis(10, 0), eventName = "Morning News", eventDescription = "Latest headlines and political updates."),
                EPGProgram("2", "1", "2025-02-18", startTime = getTimeInMillis(10, 0), endTime = getTimeInMillis(12, 0), eventName = "Crime Report", eventDescription = "Breaking crime stories and analysis."),
                EPGProgram("3", "1", "2025-02-18", startTime = getTimeInMillis(12, 0), endTime = getTimeInMillis(14, 0), eventName = "Business Talk", eventDescription = "Stock market and financial news."),
                EPGProgram("4", "1", "2025-02-18", startTime = getTimeInMillis(14, 0), endTime = getTimeInMillis(16, 0), eventName = "Evening Update", eventDescription = "Mid-day news analysis.")
            )
        ),
        // ... add more channels as needed
        ChannelWithPrograms(
            channel = EPGChannel(id = "2", name = "R Bharat"),
            programs = listOf(
                EPGProgram("5", "2", "2025-02-18", startTime = getTimeInMillis(8, 0), endTime = getTimeInMillis(11, 0), eventName = "Morning Bulletin", eventDescription = "Top stories of the day with expert insights."),
                EPGProgram("6", "2", "2025-02-18", startTime = getTimeInMillis(11, 0), endTime = getTimeInMillis(13, 0), eventName = "Special Report", eventDescription = "In-depth investigative stories."),
                EPGProgram("7", "2", "2025-02-18", startTime = getTimeInMillis(13, 0), endTime = getTimeInMillis(15, 0), eventName = "Political Analysis", eventDescription = "Debates and expert opinions."),
                EPGProgram("8", "2", "2025-02-18", startTime = getTimeInMillis(15, 0), endTime = getTimeInMillis(17, 0), eventName = "Crime Files", eventDescription = "Investigative reports on major cases.")
            )
        ),
        ChannelWithPrograms(
            channel = EPGChannel(id = "1", name = "Aaj Tak"),
            programs = listOf(
                EPGProgram("1", "1", "2025-02-18", startTime = getTimeInMillis(8, 0), endTime = getTimeInMillis(10, 0), eventName = "Morning News", eventDescription = "Latest headlines and political updates."),
                EPGProgram("2", "1", "2025-02-18", startTime = getTimeInMillis(10, 0), endTime = getTimeInMillis(12, 0), eventName = "Crime Report", eventDescription = "Breaking crime stories and analysis."),
                EPGProgram("3", "1", "2025-02-18", startTime = getTimeInMillis(12, 0), endTime = getTimeInMillis(14, 0), eventName = "Business Talk", eventDescription = "Stock market and financial news."),
                EPGProgram("4", "1", "2025-02-18", startTime = getTimeInMillis(14, 0), endTime = getTimeInMillis(16, 0), eventName = "Evening Update", eventDescription = "Mid-day news analysis.")
            )
        ),
        // ... add more channels as needed
        ChannelWithPrograms(
            channel = EPGChannel(id = "2", name = "R Bharat"),
            programs = listOf(
                EPGProgram("5", "2", "2025-02-18", startTime = getTimeInMillis(8, 0), endTime = getTimeInMillis(11, 0), eventName = "Morning Bulletin", eventDescription = "Top stories of the day with expert insights."),
                EPGProgram("6", "2", "2025-02-18", startTime = getTimeInMillis(11, 0), endTime = getTimeInMillis(13, 0), eventName = "Special Report", eventDescription = "In-depth investigative stories."),
                EPGProgram("7", "2", "2025-02-18", startTime = getTimeInMillis(13, 0), endTime = getTimeInMillis(15, 0), eventName = "Political Analysis", eventDescription = "Debates and expert opinions."),
                EPGProgram("8", "2", "2025-02-18", startTime = getTimeInMillis(15, 0), endTime = getTimeInMillis(17, 0), eventName = "Crime Files", eventDescription = "Investigative reports on major cases.")
            )
        ),
        ChannelWithPrograms(
            channel = EPGChannel(id = "1", name = "Aaj Tak"),
            programs = listOf(
                EPGProgram("1", "1", "2025-02-18", startTime = getTimeInMillis(8, 0), endTime = getTimeInMillis(10, 0), eventName = "Morning News", eventDescription = "Latest headlines and political updates."),
                EPGProgram("2", "1", "2025-02-18", startTime = getTimeInMillis(10, 0), endTime = getTimeInMillis(12, 0), eventName = "Crime Report", eventDescription = "Breaking crime stories and analysis."),
                EPGProgram("3", "1", "2025-02-18", startTime = getTimeInMillis(12, 0), endTime = getTimeInMillis(14, 0), eventName = "Business Talk", eventDescription = "Stock market and financial news."),
                EPGProgram("4", "1", "2025-02-18", startTime = getTimeInMillis(14, 0), endTime = getTimeInMillis(16, 0), eventName = "Evening Update", eventDescription = "Mid-day news analysis.")
            )
        ),
        // ... add more channels as needed
        ChannelWithPrograms(
            channel = EPGChannel(id = "2", name = "R Bharat"),
            programs = listOf(
                EPGProgram("5", "2", "2025-02-18", startTime = getTimeInMillis(8, 0), endTime = getTimeInMillis(11, 0), eventName = "Morning Bulletin", eventDescription = "Top stories of the day with expert insights."),
                EPGProgram("6", "2", "2025-02-18", startTime = getTimeInMillis(11, 0), endTime = getTimeInMillis(13, 0), eventName = "Special Report", eventDescription = "In-depth investigative stories."),
                EPGProgram("7", "2", "2025-02-18", startTime = getTimeInMillis(13, 0), endTime = getTimeInMillis(15, 0), eventName = "Political Analysis", eventDescription = "Debates and expert opinions."),
                EPGProgram("8", "2", "2025-02-18", startTime = getTimeInMillis(15, 0), endTime = getTimeInMillis(17, 0), eventName = "Crime Files", eventDescription = "Investigative reports on major cases.")
            )
        ),
        ChannelWithPrograms(
            channel = EPGChannel(id = "1", name = "Aaj Tak"),
            programs = listOf(
                EPGProgram("1", "1", "2025-02-18", startTime = getTimeInMillis(8, 0), endTime = getTimeInMillis(10, 0), eventName = "Morning News", eventDescription = "Latest headlines and political updates."),
                EPGProgram("2", "1", "2025-02-18", startTime = getTimeInMillis(10, 0), endTime = getTimeInMillis(12, 0), eventName = "Crime Report", eventDescription = "Breaking crime stories and analysis."),
                EPGProgram("3", "1", "2025-02-18", startTime = getTimeInMillis(12, 0), endTime = getTimeInMillis(14, 0), eventName = "Business Talk", eventDescription = "Stock market and financial news."),
                EPGProgram("4", "1", "2025-02-18", startTime = getTimeInMillis(14, 0), endTime = getTimeInMillis(16, 0), eventName = "Evening Update", eventDescription = "Mid-day news analysis.")
            )
        ),
        // ... add more channels as needed
        ChannelWithPrograms(
            channel = EPGChannel(id = "2", name = "R Bharat"),
            programs = listOf(
                EPGProgram("5", "2", "2025-02-18", startTime = getTimeInMillis(8, 0), endTime = getTimeInMillis(11, 0), eventName = "Morning Bulletin", eventDescription = "Top stories of the day with expert insights."),
                EPGProgram("6", "2", "2025-02-18", startTime = getTimeInMillis(11, 0), endTime = getTimeInMillis(13, 0), eventName = "Special Report", eventDescription = "In-depth investigative stories."),
                EPGProgram("7", "2", "2025-02-18", startTime = getTimeInMillis(13, 0), endTime = getTimeInMillis(15, 0), eventName = "Political Analysis", eventDescription = "Debates and expert opinions."),
                EPGProgram("8", "2", "2025-02-18", startTime = getTimeInMillis(15, 0), endTime = getTimeInMillis(17, 0), eventName = "Crime Files", eventDescription = "Investigative reports on major cases.")
            )
        ),
        ChannelWithPrograms(
            channel = EPGChannel(id = "1", name = "Aaj Tak"),
            programs = listOf(
                EPGProgram("1", "1", "2025-02-18", startTime = getTimeInMillis(8, 0), endTime = getTimeInMillis(10, 0), eventName = "Morning News", eventDescription = "Latest headlines and political updates."),
                EPGProgram("2", "1", "2025-02-18", startTime = getTimeInMillis(10, 0), endTime = getTimeInMillis(12, 0), eventName = "Crime Report", eventDescription = "Breaking crime stories and analysis."),
                EPGProgram("3", "1", "2025-02-18", startTime = getTimeInMillis(12, 0), endTime = getTimeInMillis(14, 0), eventName = "Business Talk", eventDescription = "Stock market and financial news."),
                EPGProgram("4", "1", "2025-02-18", startTime = getTimeInMillis(14, 0), endTime = getTimeInMillis(16, 0), eventName = "Evening Update", eventDescription = "Mid-day news analysis.")
            )
        ),
        // ... add more channels as needed
        ChannelWithPrograms(
            channel = EPGChannel(id = "2", name = "R Bharat"),
            programs = listOf(
                EPGProgram("5", "2", "2025-02-18", startTime = getTimeInMillis(8, 0), endTime = getTimeInMillis(11, 0), eventName = "Morning Bulletin", eventDescription = "Top stories of the day with expert insights."),
                EPGProgram("6", "2", "2025-02-18", startTime = getTimeInMillis(11, 0), endTime = getTimeInMillis(13, 0), eventName = "Special Report", eventDescription = "In-depth investigative stories."),
                EPGProgram("7", "2", "2025-02-18", startTime = getTimeInMillis(13, 0), endTime = getTimeInMillis(15, 0), eventName = "Political Analysis", eventDescription = "Debates and expert opinions."),
                EPGProgram("8", "2", "2025-02-18", startTime = getTimeInMillis(15, 0), endTime = getTimeInMillis(17, 0), eventName = "Crime Files", eventDescription = "Investigative reports on major cases.")
            )
        ),
        ChannelWithPrograms(
            channel = EPGChannel(id = "1", name = "Aaj Tak"),
            programs = listOf(
                EPGProgram("1", "1", "2025-02-18", startTime = getTimeInMillis(8, 0), endTime = getTimeInMillis(10, 0), eventName = "Morning News", eventDescription = "Latest headlines and political updates."),
                EPGProgram("2", "1", "2025-02-18", startTime = getTimeInMillis(10, 0), endTime = getTimeInMillis(12, 0), eventName = "Crime Report", eventDescription = "Breaking crime stories and analysis."),
                EPGProgram("3", "1", "2025-02-18", startTime = getTimeInMillis(12, 0), endTime = getTimeInMillis(14, 0), eventName = "Business Talk", eventDescription = "Stock market and financial news."),
                EPGProgram("4", "1", "2025-02-18", startTime = getTimeInMillis(14, 0), endTime = getTimeInMillis(16, 0), eventName = "Evening Update", eventDescription = "Mid-day news analysis.")
            )
        ),
        // ... add more channels as needed
        ChannelWithPrograms(
            channel = EPGChannel(id = "2", name = "R Bharat"),
            programs = listOf(
                EPGProgram("5", "2", "2025-02-18", startTime = getTimeInMillis(8, 0), endTime = getTimeInMillis(11, 0), eventName = "Morning Bulletin", eventDescription = "Top stories of the day with expert insights."),
                EPGProgram("6", "2", "2025-02-18", startTime = getTimeInMillis(11, 0), endTime = getTimeInMillis(13, 0), eventName = "Special Report", eventDescription = "In-depth investigative stories."),
                EPGProgram("7", "2", "2025-02-18", startTime = getTimeInMillis(13, 0), endTime = getTimeInMillis(15, 0), eventName = "Political Analysis", eventDescription = "Debates and expert opinions."),
                EPGProgram("8", "2", "2025-02-18", startTime = getTimeInMillis(15, 0), endTime = getTimeInMillis(17, 0), eventName = "Crime Files", eventDescription = "Investigative reports on major cases.")
            )
        )
        // ... more channels can be appended similarly
    )
}

// Utility function to convert hours and minutes to milliseconds
fun getTimeInMillis(hour: Int, minute: Int): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, hour)
    calendar.set(Calendar.MINUTE, minute)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}
@Composable
fun TimeHeader() {
    // Get current system time
    val currentTime = remember { mutableStateOf(System.currentTimeMillis()) }

    // Update time every minute
    LaunchedEffect(Unit) {
        while (true) {
            currentTime.value = System.currentTimeMillis()
            delay(60 * 1000) // Update every minute
        }
    }

    // Generate 30-minute slots from current time
    val timeSlots = remember(currentTime.value) {
        generateTimeSlots(currentTime.value)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Color.Black)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        timeSlots.forEach { time ->
            androidx.compose.material3.Text(
                text = time,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

