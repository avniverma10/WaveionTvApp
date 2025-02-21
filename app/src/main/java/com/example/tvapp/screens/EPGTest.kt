package com.example.tvapp.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tvapp.viewmodels.EPGViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

@SuppressLint("UnusedBoxWithConstraintsScope", "SimpleDateFormat")
@Composable
fun EPGContent(viewModel: EPGViewModel = hiltViewModel()) {
    val filteredPrograms by viewModel.filteredPrograms.collectAsState()
    val currentTimeMillis = remember { mutableStateOf(System.currentTimeMillis()) }
    val minutesPerPixel = 2f // Ensures division results in a float



//    LaunchedEffect(key1 = currentTimeMillis.value) {
//        delay(1000)
//        currentTimeMillis.value = System.currentTimeMillis()
//    }

    // Update every second to keep the UI responsive and accurate
    LaunchedEffect(key1 = "timeUpdater") {
        while (true) {
            delay(1000) // Lowering this to 1000 ms for smoother updates
            currentTimeMillis.value = System.currentTimeMillis()
        }
    }

    val timelineStart = calculateTimelineStart(currentTimeMillis.value)
    val minutesPastSinceTimelineStart = ((currentTimeMillis.value - timelineStart) / 60000).toFloat()
    val startOffsetDp = ((minutesPastSinceTimelineStart / minutesPerPixel).toFloat()).dp // Correctly convert to float before .dp


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Time Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Spacer(Modifier.width(16.dp))
            TimeHeader(timelineStart = timelineStart)
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {

            // Calculate the start offset for the current time to be in the center
            val centerOffset = (this.maxWidth / 2) - (2.dp / 2) // Adjust the 2.dp to the width of the red line
            val totalOffsetDp = centerOffset + startOffsetDp

            // EPG Content
            LazyColumn {
                items(filteredPrograms.groupBy { it.channelId }.entries.toList()) { (channelId, programs) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Channel Info
                        Column(
                            modifier = Modifier.width(120.dp).padding(8.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "Channel $channelId", color = Color.White, fontSize = 14.sp)
                        }

                        // Programs
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = startOffsetDp)
                        ) {
                            items(programs) { program ->
                                val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss Z")
                                val startTime = LocalTime.parse(program.startTime, formatter)
                                val endTime = LocalTime.parse(program.endTime, formatter)

                                val duration = Duration.between(startTime, endTime).toMinutes()
                                val widthDp = (duration / minutesPerPixel).dp

                                ProgramItem(
                                    title = program.eventName,
                                    width = widthDp
                                )
                            }
                        }
                    }
                }
            }
            // Red line time indicator
            Box(
                modifier = Modifier

                    .fillMaxHeight()
                    .width(2.dp)
                    .background(Color.Red)
                    .offset(x = startOffsetDp)
//                    .fillMaxHeight()
//                    .width(2.dp)
//                    .background(Color.Red)
//                    .align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun ProgramItem(title: String, width: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .height(60.dp)
            .background(Color.DarkGray)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TimeHeader(timelineStart: Long, timeSlotIncrement: Int = 30) {
    val timeSlots = generateTimeSlots(timelineStart,timeSlotIncrement)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        timeSlots.forEach { time ->
            Text(
                text = time,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun calculateTimelineStart(currentMillis: Long): Long {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = currentMillis
       val minute = get(Calendar.MINUTE)
        val remainder = minute % 30
        set(Calendar.MINUTE, minute -remainder) // Adjust this to control how much of the past is visible
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}

private fun generateTimeSlots(timelineStart: Long, increment:Int): List<String> {
    val dateFormat = SimpleDateFormat("HH:mm")
    val calendar = Calendar.getInstance().apply { timeInMillis = timelineStart }
    return List(5) { // You can adjust the number of slots
        val time = dateFormat.format(calendar.time)
        calendar.add(Calendar.MINUTE, 30) // Adjust this for different slot durations
        time
    }
}
