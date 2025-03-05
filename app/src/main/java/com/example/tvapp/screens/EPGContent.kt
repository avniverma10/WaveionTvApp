package com.example.tvapp.screens


import android.util.Log
import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
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
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.tvapp.components.TimeHeader
import com.example.tvapp.components.parseFixedTime
import com.example.tvapp.models.EPGChannel
import com.example.tvapp.viewmodels.EPGViewModel
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@Composable
fun EPGContent(viewModel: EPGViewModel = hiltViewModel()) {

    val filteredPrograms by viewModel.filteredPrograms.collectAsState()
    val epgChannels by viewModel.epgChannels.collectAsState()

    val channelMap = epgChannels.associateBy { it.id }


    val selectedVideoUrl by viewModel.selectedVideoUrl.collectAsState()

    // Update current time every second.
    val currentTimeMillis = remember { mutableStateOf(parseFixedTime("20250208104600")) }

    val wishlistPopupProgram by viewModel.wishlistPopupProgram.collectAsState()
    val wishlistAlertProgram by viewModel.wishlistAlertProgram.collectAsState()



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

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)) {
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
                        // Get the channel data from the map.
                        val channelData = channelMap[channelId]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp)
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Panel: Channel info.
                            // Pass channelData and a callback for video click.
                            channelData?.let { channel ->
                                ChannelInfo(
                                    leftPanelWidth = 205.dp,
                                    channel = channel,
                                    onPlayClicked = { videoUrl ->
                                        viewModel.onChannelVideoSelected(videoUrl)
                                    }
                                )
                            }

                            // Timeline area for program listings.
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = maxOf(
                                            0,
                                            -((currentTimeMillis.value / 60000) % minutesPerPixel).toInt()
                                        ).dp
                                    )
                            ) {
                                itemsIndexed(programs) { programIndex, program ->
                                    val programWidth = calculateProgramWidth(
                                        program.startTime,
                                        program.endTime
                                    )

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
                                                if (keyEvent.type == KeyEventType.KeyDown) {
                                                    when (keyEvent.nativeKeyEvent.keyCode) {
                                                        KeyEvent.KEYCODE_DPAD_CENTER -> {
                                                            Log.i("RISHI", "EPGContent: dpad center")
                                                            val currentTime = currentTimeMillis.value
                                                            val programStartMillis = getTimeInMillis(program.startTime)
                                                            val programEndMillis = getTimeInMillis(program.endTime)

                                                            // Debug logging to verify values:
                                                            Log.i("DEBUG_TIME", "Current: $currentTime, Start: $programStartMillis, End: $programEndMillis")

                                                            // For testing, force one branch:
                                                            if (currentTime in programStartMillis..programEndMillis || true /*temporary override*/) {
                                                                Log.i("RISHIRAJ", "EPGContent: video should play")
                                                                viewModel.onChannelVideoSelected(channelData?.videoUrl)
                                                            } else if (currentTime < programStartMillis) {
                                                                Log.i("RISHIRAJ", "EPGContent: pop should show")
                                                                viewModel.onShowWishlistPopup(program)
                                                            }
                                                            true
                                                        }
                                                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                                            if (isFocused.value && isLastProgram) true else false
                                                        }
                                                        else -> false
                                                    }
                                                } else false
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
    if (selectedVideoUrl != null) {
        Dialog(
            onDismissRequest = { viewModel.onChannelVideoSelected(null)  },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                VideoPlayer(
                    videoUrl = selectedVideoUrl!!,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    if (wishlistPopupProgram != null) {
        Dialog(onDismissRequest = { viewModel.clearWishlistPopup() }) {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)) {
                // Retrieve channel name using channelMap if available
                val channelName = channelMap[wishlistPopupProgram!!.channelId]?.name ?: "Unknown Channel"
                Column(modifier = Modifier.align(Alignment.Center)) {
                    Text(text = "Channel: $channelName", color = Color.White)
                    Text(text = "Start: ${wishlistPopupProgram!!.startTime}", color = Color.White)
                    Text(text = "End: ${wishlistPopupProgram!!.endTime}", color = Color.White)
                    // Button to add to wishlist:
                    Button(onClick = { viewModel.addToWishlist(wishlistPopupProgram!!) }) {
                        Text("Add to Wishlist")
                    }
                    Button(onClick = { viewModel.clearWishlistPopup() }) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
    if (wishlistAlertProgram != null) {
        Dialog(onDismissRequest = { viewModel.clearWishlistAlert() }) {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)) {
                val channelName = channelMap[wishlistAlertProgram!!.channelId]?.name ?: "Unknown Channel"
                Column(modifier = Modifier.align(Alignment.Center)) {
                    Text(text = "Your wishlist event is starting", color = Color.White)
                    Text(text = "Channel: $channelName", color = Color.White)
                    Text(text = "Start: ${wishlistAlertProgram!!.startTime}", color = Color.White)
                    Text(text = "End: ${wishlistAlertProgram!!.endTime}", color = Color.White)
                    // Button to play:
                    Button(onClick = {
                        viewModel.onChannelVideoSelected(channelMap[wishlistAlertProgram!!.channelId]?.videoUrl)
                        viewModel.clearWishlistAlert()
                    }) {
                        Text("Play")
                    }
                    Button(onClick = { viewModel.clearWishlistAlert() }) {
                        Text("Cancel")
                    }
                }
            }
        }
    }


}

fun calculateProgramWidth(startTime: String, endTime: String): Dp {
    val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss Z")
    val startDateTime = ZonedDateTime.parse(startTime, formatter)
    val endDateTime = ZonedDateTime.parse(endTime, formatter)
    val duration = Duration.between(startDateTime, endDateTime).toMinutes()
    val blocks = duration / 30.0
    val widthPerBlock = 50.dp
    return (blocks.toFloat() * widthPerBlock.value).dp
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
fun ChannelInfo(leftPanelWidth: Dp, channel: EPGChannel,onPlayClicked: (String?) -> Unit) {
    Row(
        modifier = Modifier
            .width(leftPanelWidth)
            .clickable { onPlayClicked(channel.videoUrl) },
        verticalAlignment = Alignment.CenterVertically) {
        Spacer(modifier = Modifier.width(8.dp))
        // Optionally, display the channel index or remove it.
        // Text(text = "Channel", color = Color.White, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(Color.Gray)
        )
        // Display the channel logo.
        AsyncImage(
            model = channel.logoUrl,
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
        // Display the channel title.
        Column(
            modifier = Modifier.width(120.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = channel.name, color = Color.White, fontSize = 12.sp)
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(Color.Gray)
        )
    }
}

fun getTimeInMillis(timeStr: String): Long {
    return try {
        // Assuming your program time strings follow "yyyyMMddHHmmss Z"
        val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss Z")
        val zdt = ZonedDateTime.parse(timeStr, formatter)
        zdt.toInstant().toEpochMilli()
    } catch (e: Exception) {
        // Fallback to no timezone format if necessary
        val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        val zdt = ZonedDateTime.parse("$timeStr +0000", DateTimeFormatter.ofPattern("yyyyMMddHHmmss Z"))
        zdt.toInstant().toEpochMilli()
    }
}

