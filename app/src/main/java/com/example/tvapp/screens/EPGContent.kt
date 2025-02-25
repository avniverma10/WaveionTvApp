package com.example.tvapp.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tvapp.R
import com.example.tvapp.viewmodels.EPGViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import androidx.compose.ui.input.key.Key


import java.util.Locale
@Composable
fun EPGContent(viewModel: EPGViewModel = hiltViewModel()) {
    val filteredPrograms by viewModel.filteredPrograms.collectAsState()
    val currentTimeMillis = remember { mutableStateOf(parseFixedTime("20250208104600")) }

    // Define a fixed width for the left panel that contains channel info.
    // Adjust this value to the total width of all elements in your left panel.
    val leftPanelWidth = 205.dp

    // Update current time every second.
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTimeMillis.value = System.currentTimeMillis()
        }
    }

    // minutesPerPixel for program scrolling.
    val minutesPerPixel = 2

    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Top row with "All" text and TimeHeader.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color.Black),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LeftPanelHeader(leftPanelWidth)
            TimeHeader(0.dp)
        }

        // The BoxWithConstraints now uses the same leftPanelWidth.
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val containerWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
            val leftPanelWidthPx = with(LocalDensity.current) { leftPanelWidth.toPx() }
            val timelineWidthPx = containerWidthPx - leftPanelWidthPx

            // Compute the start of the current 30-minute block.
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
            // Fraction of the half‑hour that has elapsed.
            val fraction = ((currentTimeMillis.value - blockStartMillis)
                .coerceAtLeast(0)
                .toFloat()) / (30 * 60 * 1000).toFloat()

            // Assuming 5 time slots, calculate width per slot.
            val oneSlotWidthPx = timelineWidthPx / 5f
            val indicatorOffsetPx = leftPanelWidthPx + fraction * oneSlotWidthPx
            val indicatorOffsetDp = with(LocalDensity.current) { indicatorOffsetPx.toDp() }

            Column(modifier = Modifier.fillMaxSize()) {

                // Horizontal divider under the header row.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.Gray)
                )

                // Channel list.
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(filteredPrograms.groupBy { it.channelId }.entries.toList()) { channelIndex, channelGroup ->
                        val (channelId, programs) = channelGroup
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp)
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Panel: Channel info.
                            ChannelInfo(leftPanelWidth, channelIndex, channelId)

                            // Timeline area for program listings.
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = maxOf(0, -((currentTimeMillis.value / 60000) % minutesPerPixel).toInt()).dp)
                            ) {
                                itemsIndexed(programs) { programIndex, program ->
                                    val timeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss Z", Locale.getDefault())
                                    val startTime = OffsetDateTime.parse(program.startTime, timeFormatter).toLocalTime()
                                    val endTime = OffsetDateTime.parse(program.endTime, timeFormatter).toLocalTime()
                                    val programDuration = Duration.between(startTime, endTime).toMinutes()
                                    val programWidth = maxOf((programDuration.toInt() / minutesPerPixel).dp, 50.dp)

                                    val focusRequester = remember { FocusRequester() }
                                    val isFocused = remember { mutableStateOf(false) }

                                    if (channelIndex == 0 && programIndex == 0) {
                                        LaunchedEffect(Unit) {
                                            focusRequester.requestFocus()
                                        }
                                    }

                                    val isLastProgram = (programIndex == programs.lastIndex)
                                    Box(
                                        modifier = Modifier
                                            .width(programWidth)
                                            .height(60.dp)
                                            .background(Color.Black)
                                            .then(
                                                if (isFocused.value)
                                                    Modifier.border(2.dp, Color.White)
                                                else Modifier
                                            )
                                            .onFocusChanged { isFocused.value = it.isFocused }
                                            .focusRequester(focusRequester)
                                            .focusable()
                                        // Intercept DPAD Right if it’s the last item and currently focused
                                        .onPreviewKeyEvent { keyEvent ->
                                        if (
                                            keyEvent.type == KeyEventType.KeyDown &&
                                            keyEvent.key == Key.DirectionRight &&
                                            isFocused.value &&
                                            isLastProgram
                                        ) {
                                            // Consume the event; don't move focus
                                            true
                                        } else {
                                            // Otherwise, let the event pass
                                            false
                                        }
                                    },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = program.eventName,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    // Vertical divider between programs.
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(1.dp)
                                            .background(Color.Gray)
                                    )
                                }
                            }
                        }
                        // Horizontal divider between channels.
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .background(Color.Gray)
                        )
                    }
                }
            }
            // Red progress indicator overlay.
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
fun LeftPanelHeader(width: Dp) {
    // A Box or Row that is exactly `width` wide
    Row(modifier = Modifier.width(width), verticalAlignment = Alignment.CenterVertically) {
        Spacer(modifier = Modifier.width(14.dp))
        Text(text = "All", color = Color.White, fontSize = 18.sp)
    }
}

@Composable
fun ChannelInfo(leftPanelWidth: Dp, channelIndex: Int, channelId: String) {
    // This composable consolidates all channel info into a fixed-width container.
    Row(modifier = Modifier.width(leftPanelWidth), verticalAlignment = Alignment.CenterVertically) {
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${channelIndex + 1}",
            color = Color.White,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(Color.Gray)
        )
        Image(
            painter = painterResource(id = R.drawable.aajtak),
            contentDescription = "Channel Logo",
            modifier = Modifier.size(40.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(Color.Gray)
        )
        Spacer(modifier = Modifier.width(8.dp))
        // Channel name column (using a portion of the left panel).
        Column(
            modifier = Modifier.width(120.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = channelId, color = Color.White, fontSize = 12.sp)
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(Color.Gray)
        )
    }
}

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
