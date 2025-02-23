//package com.example.tvapp.screens
//import java.time.Duration
////betterrr
//
//import android.annotation.SuppressLint
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.BoxWithConstraints
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxHeight
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.hilt.navigation.compose.hiltViewModel
//import com.example.tvapp.viewmodels.EPGViewModel
//import kotlinx.coroutines.delay
//import java.text.SimpleDateFormat
//import java.time.LocalDateTime
//import java.time.LocalTime
//import java.time.format.DateTimeFormatter
//import java.util.Calendar
//import java.util.Locale
//
//@SuppressLint("UnusedBoxWithConstraintsScope")
//@Composable
//
//fun EPGContent(viewModel: EPGViewModel = hiltViewModel()) {
//    val filteredPrograms by viewModel.filteredPrograms.collectAsState()
//    val currentTimeMillis = remember { mutableStateOf(parseFixedTime("20250208104600")) }
//
//    // Update current time every second to ensure smooth scrolling
//    LaunchedEffect(Unit) {
//        while (true) {
//            delay(1000) // Updated to 1 second for real-time movement
//            currentTimeMillis.value = 20250208104600L
//        }
//    }
//
//
//    val minutesPerPixel = 2 // Adjust this to control scrolling speed
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.Black)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(50.dp)
//                .background(Color.Black),
//            horizontalArrangement = Arrangement.Start
//        ) {
//            Spacer(modifier = Modifier.width(10.dp))
//
//            Text(
//                text = "All",
//                color = Color.White,
//                fontSize = 18.sp,
//                modifier = Modifier.align(Alignment.CenterVertically)
//            )
//
//            Spacer(modifier = Modifier.width(50.dp))
//
////            TimeHeader()
//        }
//
//        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
//            LazyColumn {
//                items(filteredPrograms.groupBy { it.channelId }.entries.toList()) { (channelId, programs) ->
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(70.dp)
//                            .padding(vertical = 4.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Column(
//                            modifier = Modifier
//                                .width(120.dp)
//                                .padding(8.dp),
//                            verticalArrangement = Arrangement.Center
//                        ) {
//                            Text(text = "ID: $channelId", color = Color.White, fontSize = 14.sp)
//                            Text(text = "Channel $channelId", color = Color.Gray, fontSize = 12.sp)
//                        }
//
//                        LazyRow(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(start = maxOf(0, -((currentTimeMillis.value / 60000) % minutesPerPixel).toInt()).dp)
//
//                        ) {
//                            items(programs) { program ->
//                                val timeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss Z", Locale.getDefault())
//                                val startTime = LocalTime.parse(program.startTime, timeFormatter)
//                                val endTime = LocalTime.parse(program.endTime, timeFormatter)
//
//                                val programDuration = Duration.between(startTime, endTime).toMinutes()
//                                val programWidth = maxOf((programDuration.toInt() / minutesPerPixel).dp, 50.dp)
//
//                                Box(
//                                    modifier = Modifier
//                                        .width(programWidth)
//                                        .height(60.dp)
//                                        .background(Color.DarkGray)
//                                        .padding(4.dp),
//                                    contentAlignment = Alignment.Center
//                                ) {
//                                    Text(
//                                        text = program.eventName,
//                                        color = Color.White,
//                                        fontSize = 12.sp,
//                                        textAlign = TextAlign.Center
//                                    )
//                                }
//
//                                Box(
//                                    modifier = Modifier
//                                        .width(2.dp)
//                                        .height(50.dp)
//                                        .background(Color.White)
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//
//            // Fixed Red Time Indicator
//            Box(
//                modifier = Modifier
//                    .fillMaxHeight()
//                    .width(2.dp)
//                    .background(Color.Red)
//                    .align(Alignment.Center)
//            )
//        }
//    }
//}
//
