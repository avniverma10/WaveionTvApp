package com.example.tvapp.view.epg

import android.util.Log
import android.view.KeyEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import com.example.tvapp.R
import com.example.tvapp.extensions.calculateProgramWidth
import com.example.tvapp.extensions.provideTimeInMillis
import com.example.tvapp.model.data.epgdata.EPGDataItem
import com.example.tvapp.view.navigationhelper.TimeHeader
import com.example.tvapp.view.navigationhelper.parseFixedTime
import com.example.tvapp.view.wtvplayer.WTVVideoPlayer
import com.example.tvapp.viewmodels.SharedViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun EPGContent(sharedViewModel: SharedViewModel,firstChannelFocusRequester: FocusRequester) {

    val epgList by sharedViewModel.filteredEPGList.collectAsState()
    val selectedVideoUrl by sharedViewModel.selectedVideoUrl.collectAsState()

    // Update current time every second.
    val currentTimeMillis = remember { mutableStateOf(parseFixedTime("20250208104600")) }

    val wishlistPopupProgram by sharedViewModel.wishlistPopupProgram.collectAsState()
    val wishlistAlertProgram by sharedViewModel.wishlistAlertProgram.collectAsState()
    val channelMap = epgList.associateBy { it.channelId }

    val hasInitiallyFocused = remember { mutableStateOf(false) }

    // Define a fixed width for the left panel that contains channel info.
    // Adjust this value to the total width of all elements in your left panel.
    val leftPanelWidth = 160.dp
//    val leftPanelWidth = 205.dp


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
                .background(Color(0xFF161D25)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LeftPanelHeader(leftPanelWidth)
            TimeHeader(0.dp)
        }

        // The BoxWithConstraints now uses the same leftPanelWidth.
        BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Color(0xFF2A3139))) {
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
                        .background(Color(0xFF353C44))
                )

                // Channel list.
                LazyColumn(modifier = Modifier.fillMaxSize().background(Color(0xFF2A3139))) {
                    itemsIndexed(epgList) { channelIndex,channelData->
                        val isFirstChannel = (channelIndex == 0)
                        val isLastChannel = (channelIndex == epgList.size-1)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth().background(color = Color(0xFF1A2124))
                                .height(70.dp)
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Panel: Channel info.
                            // Pass channelData and a callback for video click.
                            channelData.let { channel ->
                                ChannelInfo(
                                    leftPanelWidth = 180.dp,
                                    channel = channel,
                                    channelIndex = channelIndex,
                                    isFirstChannel = isFirstChannel,
                                    isLastChannel = isLastChannel,
                                    onPlayClicked = { videoUrl ->
                                       // val firstProgram = programs.firstOrNull()
                                        //viewModel.onShowWishlistPopup()
                                        sharedViewModel.onChannelVideoSelected(videoUrl,epgList[0].tv?.programme?.get(0))
                                    },
                                    hasInitiallyFocused = hasInitiallyFocused,
                                    focusRequester = if (channelIndex == 0) firstChannelFocusRequester else null // 👈 only for first
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
                                itemsIndexed(channelData.tv?.programme!!) { programIndex, program ->
                                    val programWidth = calculateProgramWidth(program.startTime?:"0",program.endTime?:"0")


                                    val focusRequester = remember { FocusRequester() }
                                    val isFocused = remember { mutableStateOf(false) }

                                    val isLastProgram = (programIndex == channelData.tv.programme.lastIndex)
                                    Box(
                                        modifier = Modifier
                                            .width(programWidth)
                                            .height(105.dp)
                                            .background(Color(0xFF2A3139), shape = RoundedCornerShape(4.dp)) // **Rounded corners applied**
                                            .then(
                                                if (isFocused.value)
                                                    Modifier.border(1.dp, Color(0xFF49FEDD),shape = RoundedCornerShape(4.dp)).background(Color(0x1A49FEDD),shape = RoundedCornerShape(size = 4.dp))
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
                                                            // When DPAD center is pressed, use the video URL fetched from your API.
                                                            sharedViewModel.onChannelVideoSelected(channelData.content?.videoUrl, program)
                                                            Log.i("RISHI", "EPGContent: dpad center")
                                                            val currentTime = currentTimeMillis.value
                                                            val programStartMillis = program.startTime?.provideTimeInMillis()?:0L
                                                            val programEndMillis = program.endTime?.provideTimeInMillis()?:0L

                                                            // For testing, force one branch:
                                                            sharedViewModel.onChannelVideoSelected(channelData.content?.videoUrl, program)
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
                                            text = program.title?:"",
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontFamily = FontFamily(Font(R.font.figtree_light)),
                                            fontWeight = FontWeight(400),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    // Vertical divider between programs.
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(3.dp)
                                            .background(Color(0xFF161D25))
                                    )
                                }
                            }
                        }
                        // Horizontal divider between channels.
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFF353C44))
                        )
                    }
                }
            }
            // Green progress indicator overlay.
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Vertical Progress Indicator
                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffsetDp) // Move dynamically based on time
                        .shadow(elevation = 4.800000190734863.dp, spotColor = Color(0xFF49FEDD), ambientColor = Color(0xFF49FEDD))
                        .fillMaxHeight()
                        .width(1.dp) // Slightly thicker progress bar
                        .background(Color(0xFF49FEDD)) // Green progress bar
                )

                // Outlined Circle with Image Inside
                Box(
                    modifier = Modifier
                        .size(16.dp) // Size of the outlined circle
                        .offset(x = indicatorOffsetDp - 9.dp, y = (-18).dp) // Positioning at top
                ) {
                    // Draw the outlined circle
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color(0xFF49FEDD), // Same color as progress bar
                            style = Stroke(width = 1.dp.toPx()) // Stroke for outline effect
                        )
                    }

                    // Place the Image Inside the Outlined Circle
                    Image(
                        painter = painterResource(id = R.drawable.vector_271), // Load from drawable
                        contentDescription = "Progress Indicator",
                        modifier = Modifier
                            .size(16.dp) // Ensure it fits inside the circle
                            .align(Alignment.Center) // Keep it centered
                    )
                }
            }
        }

    }
    if (selectedVideoUrl != null) {
        Dialog(
            onDismissRequest = { sharedViewModel.onChannelVideoSelected(null,null) },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                WTVVideoPlayer (
                    initialVideoUrl = selectedVideoUrl?:"",
                    allChannels = epgList,
                    onVideoChange = {

                        // Find the currently playing program based on the video URL
                       /* val currentProgram = epgList
                            .flatMap { channel -> filteredPrograms.filter { it.channelId == channel.id && channel.videoUrl == newVideoUrl } }
                            .firstOrNull()
                        sharedViewModel.onChannelVideoSelected(selectedVideoUrl,currentProgram)*/
                    }
                )
            }
        }
    }

    if (wishlistPopupProgram != null) {
        Dialog(onDismissRequest = { sharedViewModel.clearWishlistPopup() }) {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)) {
                // Retrieve channel name using channelMap if available
                val channelName = channelMap[wishlistPopupProgram?.channelId]?.displayName ?: "Unknown Channel"
                Column(modifier = Modifier.align(Alignment.Center)) {
                    Text(text = "Channel: $channelName", color = Color.White)
                    Text(text = "Start: ${wishlistPopupProgram!!.startTime}", color = Color.White)
                    Text(text = "End: ${wishlistPopupProgram!!.endTime}", color = Color.White)
                    // Button to add to wishlist:
                    Button(onClick = { sharedViewModel.addToWishlist(wishlistPopupProgram!!) }) {
                        Text("Add to Wishlist")
                    }
                    Button(onClick = { sharedViewModel.clearWishlistPopup() }) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
    if (wishlistAlertProgram != null) {
        Dialog(onDismissRequest = { sharedViewModel.clearWishlistAlert() }) {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)) {
                val channelName = channelMap[wishlistAlertProgram?.channelId]?.displayName ?: "Unknown Channel"
                Column(modifier = Modifier.align(Alignment.Center)) {
                    Text(text = "Your wishlist event is starting", color = Color.White)
                    Text(text = "Channel: $channelName", color = Color.White)
                    Text(text = "Start: ${wishlistAlertProgram?.startTime}", color = Color.White)
                    Text(text = "End: ${wishlistAlertProgram?.endTime}", color = Color.White)
                    // Button to play:
                    Button(onClick = {
                        sharedViewModel.onChannelVideoSelected(channelMap[wishlistAlertProgram?.channelId]?.content?.videoUrl, program = null)
                        sharedViewModel.clearWishlistAlert()
                    }) {
                        Text("Play")
                    }
                    Button(onClick = { sharedViewModel.clearWishlistAlert() }) {
                        Text("Cancel")
                    }
                }
            }
        }
    }


}

@Composable
fun LeftPanelHeader(width: Dp) {
    val currentTime = remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000) // Update time every second
            currentTime.value = System.currentTimeMillis()
        }
    }

    val formattedTime = remember(currentTime.value) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTime.value
        val sdf = SimpleDateFormat("hh:mma", Locale.US) // 12-hour format with AM/PM
        sdf.format(calendar.time)
    }

    Row(
        modifier = Modifier
            .width(width)
            .background(Color(0xFF161D25))
            .padding(start = 35.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End // Moves content to the right

    ) {

        Row(
            modifier = Modifier
                .width(width)
                .padding(start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween // Keep the layout balanced
        ) {
            // **Logo from Drawable**
            Image(
                painter = painterResource(id = R.drawable.vector_271), // Replace with actual drawable name
                contentDescription = "",
                modifier = Modifier
                    .size(16.dp) // Adjust size as needed
            )
            Text(
                text = formattedTime,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(150.dp) , // Fixed width for uniform spacing
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 28.01.sp,
                    fontFamily = FontFamily(Font(R.font.figtree_light)),
                    fontWeight = FontWeight(600),
                    color = Color(0xFFB5B5B5),)
            )
            Spacer(modifier = Modifier.width(14.dp))
        }
    }
}


@Composable
fun ChannelInfo(
    leftPanelWidth: Dp,
    channel: EPGDataItem,
    channelIndex: Int,
    isFirstChannel: Boolean,
    isLastChannel: Boolean,
    onPlayClicked: (String?) -> Unit,
    hasInitiallyFocused: MutableState<Boolean>,
    focusRequester: FocusRequester? = null
) {
    val actualFocusRequester = focusRequester ?: remember { FocusRequester() }
    val isFocused = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .width(leftPanelWidth)
            .background(Color(0xFF161D25))
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_CENTER -> {
                            onPlayClicked(channel.content?.videoUrl)
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_DOWN -> isLastChannel
                        else -> false
                    }
                } else false
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(70.dp)
                .background(Color(0xFF161D25)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = (channelIndex + 1).toString(),
                color = Color.White,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        Box(
            modifier = Modifier
                .width(120.dp)
                .height(125.dp)
                .then(
                    if (isFocused.value)
                        Modifier.border(2.dp, Color(0xFF49FEDD), RoundedCornerShape(4.dp))
                    else Modifier
                )
                .onFocusChanged { isFocused.value = it.isFocused }
                .focusRequester(actualFocusRequester)
                .focusable()
                .clip(RoundedCornerShape(4.dp))
                .clickable { onPlayClicked(channel.content?.videoUrl) }
        ) {

            if (channelIndex == 0 && !hasInitiallyFocused.value) {
                LaunchedEffect(Unit) {
                    actualFocusRequester.requestFocus()
                    hasInitiallyFocused.value = true
                }
            }

            AsyncImage(
                model = channel.content?.thumbnailUrl,
                contentDescription = "Channel Logo",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF161D25), RoundedCornerShape(4.dp)),
                contentScale = ContentScale.FillBounds
            )
        }
    }
}


