//package com.example.tvapp.screens
//
//import android.annotation.SuppressLint
//import android.util.Log
//import androidx.compose.foundation.background
//import androidx.compose.foundation.horizontalScroll
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.unit.times
//import com.example.tvapp.viewmodels.EPGViewModel
//import kotlinx.coroutines.delay
//import java.text.SimpleDateFormat
//import java.util.*
//
//@SuppressLint("SimpleDateFormat")
//@Composable
//fun EPGContent1(viewModel: EPGViewModel) {
//    val programs by viewModel.filteredPrograms.collectAsState()
//    val currentTimeMillis = remember { mutableStateOf(System.currentTimeMillis()) }
//
//    LaunchedEffect(Unit) {
//        while (true) {
//            delay(60_000)
//            currentTimeMillis.value = System.currentTimeMillis()
//        }
//    }
//
//    val scrollState = rememberScrollState()
//
//    LaunchedEffect(currentTimeMillis.value) {
//        val minutesSinceMidnight = (currentTimeMillis.value / (1000 * 60)) % (24 * 60)
//        val scrollOffset = (minutesSinceMidnight / 30f) * 150f
//        scrollState.scrollTo(scrollOffset.toInt())
//    }
//
//    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
//        // TIME HEADER (Fixed at the top)
//        LazyRow(
//            modifier = Modifier.fillMaxWidth().height(50.dp).background(Color.DarkGray)
//        ) {
//            items(getTimeSlots()) { timeSlot ->
//                Box(modifier = Modifier.width(150.dp).padding(4.dp)) {
//                    Text(text = timeSlot, color = Color.White, fontSize = 14.sp)
//                }
//            }
//        }
//
//        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
//            val timelineWidth = maxWidth - 120.dp
//
//            Box(modifier = Modifier.fillMaxSize()) {
//                LazyColumn {
//                    items(programs.groupBy { it.channelId }.entries.toList()) { (channelId, channelPrograms) ->
//                        Log.d("AVNI", "Total programs for Channel $channelId: ${channelPrograms.size}")
//
//                        Row(modifier = Modifier.fillMaxWidth().height(70.dp).padding(4.dp)) {
//                            // Channel Info
//                            Column(
//                                modifier = Modifier.width(120.dp).padding(8.dp),
//                                verticalArrangement = Arrangement.Center
//                            ) {
//                                Text(text = "Channel $channelId", color = Color.White, fontSize = 14.sp)
//                            }
//
//                            // Programs
//                            Row(modifier = Modifier.horizontalScroll(scrollState)) {
//                                channelPrograms.filter { parseDateTimeToMillis(it.endTime) > currentTimeMillis.value }
//                                    .forEach { program ->
//                                        Log.d("AVNI", "Program name --> ${program.eventName}")
//
//                                        val startMillis = parseDateTimeToMillis(program.startTime)
//                                        val endMillis = parseDateTimeToMillis(program.endTime)
//
//                                        val durationMinutes = (endMillis - startMillis) / (1000 * 60)
//                                        val programWidth = (durationMinutes / 30f) * 150.dp
//
//                                        Box(
//                                            modifier = Modifier
//                                                .width(programWidth)
//                                                .height(60.dp)
//                                                .background(Color.DarkGray)
//                                                .padding(4.dp),
//                                            contentAlignment = Alignment.Center
//                                        ) {
//                                            Text(
//                                                text = program.eventName,
//                                                color = Color.White,
//                                                fontSize = 12.sp,
//                                                textAlign = TextAlign.Center
//                                            )
//                                        }
//                                    }
//                            }
//                        }
//                    }
//                }
//
//
//                // Fixed Red Time Indicator
//                Box(
//                    modifier = Modifier
//                        .fillMaxHeight()
//                        .width(2.dp)
//                        .background(Color.Red)
//                        .align(Alignment.Center)
//                )
//            }
//        }
//    }
//}
//
//fun getTimeSlots(): List<String> {
//    val slots = mutableListOf<String>()
//    val calendar = Calendar.getInstance()
//    calendar.set(Calendar.HOUR_OF_DAY, 0)
//    calendar.set(Calendar.MINUTE, 0)
//    val format = SimpleDateFormat("HH:mm")
//
//    repeat(48) { // 48 slots for 24 hours
//        slots.add(format.format(calendar.time))
//        calendar.add(Calendar.MINUTE, 30)
//    }
//    return slots
//}
//
//fun parseDateTimeToMillis(dateTime: String): Long {
//    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
//    return format.parse(dateTime)?.time ?: 0L
//}
