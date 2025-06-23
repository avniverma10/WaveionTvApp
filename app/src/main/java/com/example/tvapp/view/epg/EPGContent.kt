package com.example.tvapp.view.epg

import android.annotation.SuppressLint
import android.util.Log
import android.view.KeyEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.android.caastv.R
import com.example.tvapp.extensions.calculateProgramWidth
import com.example.tvapp.extensions.calculateProgramsWidth
import com.example.tvapp.extensions.formatTime
import com.example.tvapp.extensions.hideKeyboard
import com.example.tvapp.model.data.epgdata.EPGDataItem
import com.example.tvapp.utils.uistate.PreferenceManager
import com.example.tvapp.view.navigationhelper.Destination
import com.example.tvapp.view.navigationhelper.TimeHeader
import com.example.tvapp.viewmodels.SharedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun EPGContent(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    firstChannelFocusRequester: FocusRequester,
    languageFocusRequesters: List<FocusRequester>,
    languageSelectedIndex: MutableState<Int>,
    categoryFocusRequesters: List<FocusRequester>,
    categorySelectedIndex: MutableState<Int>
) {

    val context = LocalContext.current
    val epgList by sharedViewModel.filteredEPGList.collectAsState()
    val noChannels = epgList.isEmpty()
    val currentTimeMillis = remember { mutableStateOf(System.currentTimeMillis()) }
    val rowStates = epgList.map { rememberLazyListState() }
    val scope = rememberCoroutineScope()
    val genre = sharedViewModel.filterState.value.genre ?: "All Channels"
    val lang  = sharedViewModel.filterState.value.language ?: "All Languages"
    val channelFocusRequesters = remember(epgList) {
        epgList.mapIndexed { idx, _ ->
            if (idx == 0) firstChannelFocusRequester else FocusRequester()
        }
    }
    val lastIndex by sharedViewModel.lastSelectedChannelIndex.collectAsState()
    var isFirstComposition by rememberSaveable { mutableStateOf(true) }
    var hasDoneInitialFocus by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(epgList) {
        if (isFirstComposition && epgList.isNotEmpty()) {
            delay(100)
            channelFocusRequesters[0].requestFocus()
            isFirstComposition = false
        } else if (!isFirstComposition && lastIndex in epgList.indices) {
            delay(100)
            channelFocusRequesters[lastIndex].requestFocus()
        }
    }
    val programFocusRequesters = remember(epgList) {
        epgList.map { channel ->
            sharedViewModel
                .provideAvailableProgram(channel.tv?.programme.orEmpty())
                .map { FocusRequester() }
        }
    }
    LaunchedEffect(languageSelectedIndex.value) {
        // only when *language* was just changed
        if (epgList.isEmpty()) {
            delay(100)    // wait for compose to settle
            languageFocusRequesters
                .getOrNull(languageSelectedIndex.value)
                ?.requestFocus()
        }
    }


    if (epgList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF2A3139)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No channels available",
                color = Color.White,
                fontSize = 25.sp,
                textAlign = TextAlign.Center
            )
        }
        return
    }
    val wishlistPopupProgram by sharedViewModel.wishlistPopupProgram.collectAsState()
    val wishlistAlertProgram by sharedViewModel.wishlistAlertProgram.collectAsState()
    val channelMap = epgList.associateBy { it.channelId }
    val hasInitiallyFocused = remember { mutableStateOf(false) }
    val leftPanelWidth = 180.dp
    //hide keyboard forcefully
    //HideKeyboardOnEnter()
    LaunchedEffect(Unit) {
        context.hideKeyboard()
        while (true) {
            delay(1000)
            currentTimeMillis.value = System.currentTimeMillis()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Color(0xFF2A3139))) {
        val containerWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val leftPanelWidthPx = with(LocalDensity.current) { leftPanelWidth.toPx() }
        val timelineWidthPx = containerWidthPx - leftPanelWidthPx
        val calendar = Calendar.getInstance().apply { timeInMillis = currentTimeMillis.value }
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val minute = calendar.get(Calendar.MINUTE)
        calendar.set(Calendar.MINUTE, if (minute < 30) 0 else 30)
        val blockStartMillis = calendar.timeInMillis
        val fraction = ((currentTimeMillis.value - blockStartMillis).coerceAtLeast(0).toFloat()) / (30 * 60 * 1000).toFloat()
        val oneSlotWidthPx = timelineWidthPx / 5f
        val indicatorOffsetPx = leftPanelWidthPx + fraction * oneSlotWidthPx
        val indicatorOffsetDp = with(LocalDensity.current) { indicatorOffsetPx.toDp() }

        Column(modifier = Modifier.fillMaxSize()) {
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
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize().background(Color(0xFF2A3139))) {
                    itemsIndexed(epgList) { channelIndex, channelData ->
                        val isFirstChannel = (channelIndex == 0)
                        val isLastChannel = (channelIndex == epgList.size - 1)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1A2124))
                                .height(70.dp)
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ChannelInfo(
                                leftPanelWidth = 180.dp,
                                channel = channelData,
                                channelIndex = channelIndex,
                                isFirstChannel = isFirstChannel,
                                isLastChannel = isLastChannel,
                                onPlayClicked = { videoUrl ->
                                    sharedViewModel.updateLastFocusedChannel(channelIndex)
                                    sharedViewModel.updateLastSelectedChannelIndex(channelIndex)
                                    sharedViewModel.updateSelectedChannel(channelData)
                                    if (genre != "All Channels" && lang != "All Languages") {
                                        sharedViewModel.setCurrentPlaylist(epgList, genre)
                                        sharedViewModel.updateLanguage(lang)
                                    }
                                    navController.navigate(Destination.panMetroScreen)
                                },
//                                hasInitiallyFocused = hasInitiallyFocused,
                                focusRequester = channelFocusRequesters[channelIndex],
                                languageFocusRequesters = languageFocusRequesters,
                                languageSelectedIndex = languageSelectedIndex,
                                categoryFocusRequesters = categoryFocusRequesters,
                                categorySelectedIndex = categorySelectedIndex
                            )
                            val rowState = rowStates[channelIndex]
                            LazyRow(
                                state = rowState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = maxOf(0, -((currentTimeMillis.value / 60000) % 2).toInt()).dp
                                    )
                            ) {
                                val availableProgram = sharedViewModel.provideAvailableProgram(channelData.tv?.programme?: arrayListOf())
                                Log.d("aProgram::>>>", availableProgram.joinToString(" | ") { it.startTime?.formatTime()
                                    .toString() })
                                itemsIndexed(availableProgram) { programIndex, program ->
                                    val programWidth = calculateProgramsWidth(program.startTime?:0, program.endTime?:0)
                                    val focusRequester = programFocusRequesters[channelIndex][programIndex]
                                    val isFocused = remember { mutableStateOf(false) }
                                    val isLastProgram = (programIndex == availableProgram.lastIndex)
                                    Box(
                                        modifier = Modifier
                                            .width(programWidth)
                                            .height(105.dp)
                                            .background(Color(0xFF2A3139), shape = RoundedCornerShape(4.dp))
                                            .then(
                                                if (isFocused.value)
                                                    Modifier
                                                        .border(1.dp, Color(0xFF49FEDD), RoundedCornerShape(4.dp))
                                                        .background(Color(0x1A49FEDD), RoundedCornerShape(4.dp))
                                                else Modifier
                                            )
//                                            .focusProperties {
//                                                if (channelIndex == 0) {
//                                                    up = languageFocusRequesters.getOrNull(languageSelectedIndex.value)!!
//                                                }
//                                            }
                                            .onFocusChanged { isFocused.value = it.isFocused }
                                            .focusRequester(focusRequester)
                                            .focusable()
                                            .onPreviewKeyEvent { keyEvent ->
                                                if (keyEvent.type == KeyEventType.KeyDown) {
                                                    when (keyEvent.nativeKeyEvent.keyCode) {
                                                        KeyEvent.KEYCODE_DPAD_CENTER -> {
                                                            sharedViewModel.updateLastSelectedChannelIndex(channelIndex)
                                                            sharedViewModel.setCurrentPlaylist(epgList)
                                                            epgList
                                                                .firstOrNull { it.content?.videoUrl == channelData.content?.videoUrl }
                                                                ?.let { channelItem ->
                                                                    sharedViewModel.updateSelectedChannel(channelItem)
                                                                    navController.navigate(Destination.panMetroScreen) {
                                                                        PreferenceManager.lastEpgDataItem = null
                                                                    }
                                                                }
                                                            true
                                                        }
                                                        KeyEvent.KEYCODE_DPAD_DOWN -> {
                                                            val nextRowIdx = channelIndex + 1
                                                            val nextRowFirst = programFocusRequesters
                                                                .getOrNull(nextRowIdx)
                                                                ?.getOrNull(0)
                                                            val nextRowState = rowStates.getOrNull(nextRowIdx)
                                                            if (nextRowFirst != null && nextRowState != null) {
                                                                scope.launch {
                                                                    // first scroll that LazyRow so item 0 is visible
                                                                    nextRowState.animateScrollToItem(0)
                                                                    // then give it a moment to bind
                                                                    delay(50)
                                                                    nextRowFirst.requestFocus()
                                                                }
                                                            }
                                                            true
                                                        }

                                                        KeyEvent.KEYCODE_DPAD_UP -> {
                                                            if (channelIndex == 0) {
                                                                // **First row → send focus to the selected language**
                                                                val langRequester = languageFocusRequesters
                                                                    .getOrNull(languageSelectedIndex.value)
                                                                if (langRequester != null) {
                                                                    scope.launch {
                                                                        delay(50)
                                                                        try { langRequester.requestFocus() } catch (_: Exception) {}
                                                                    }
                                                                }
                                                            } else {
                                                                // normal “go to previous row” behavior
                                                                val prevRowIdx = channelIndex - 1
                                                                val prevRowFirst = programFocusRequesters
                                                                    .getOrNull(prevRowIdx)
                                                                    ?.getOrNull(0)
                                                                val prevRowState = rowStates.getOrNull(prevRowIdx)
                                                                if (prevRowFirst != null && prevRowState != null) {
                                                                    scope.launch {
                                                                        prevRowState.animateScrollToItem(
                                                                            0
                                                                        )
                                                                        delay(50)
                                                                        prevRowFirst.requestFocus()
                                                                    }
                                                                }
                                                            }
                                                            true
                                                        }
                                                        else -> false
                                                    }
                                                } else false
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = program.title.orEmpty(),
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .let { base ->
                                                    if (isFocused.value) {
                                                        // only when DPAD-focus lands here…
                                                        base.basicMarquee(
                                                            iterations  = Int.MAX_VALUE,
                                                            initialDelayMillis  = 0,
                                                            velocity    = 30.dp
                                                        )
                                                    } else {
                                                        base
                                                    }
                                                }
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(3.dp)
                                            .background(Color(0xFF161D25))
                                    )
                                }
                            }
                        }
                    }
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .offset(x = indicatorOffsetDp)
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(Color(0xFF49FEDD))
                    )
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .offset(x = indicatorOffsetDp - 9.dp, y = (-18).dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(color = Color(0xFF49FEDD), style = Stroke(width = 1.dp.toPx()))
                        }
                        Image(
                            painter = painterResource(id = R.drawable.vector_271),
                            contentDescription = "Progress Indicator",
                            modifier = Modifier.size(16.dp).align(Alignment.Center)
                        )
                    }
                }
            }
        }

    }
    if (wishlistPopupProgram != null) {
        Dialog(onDismissRequest = { sharedViewModel.clearWishlistPopup() }) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                val channelName = channelMap.get(wishlistPopupProgram?.channelId)?.displayName ?: "Unknown Channel"
                Column(modifier = Modifier.align(Alignment.Center)) {
                    Text(text = "Channel: $channelName", color = Color.White)
                    Text(text = "Start: ${wishlistPopupProgram!!.startTime}", color = Color.White)
                    Text(text = "End: ${wishlistPopupProgram!!.endTime}", color = Color.White)
                    Button(onClick = { sharedViewModel.addToWishlist(wishlistPopupProgram!!) }) { Text("Add to Wishlist") }
                    Button(onClick = { sharedViewModel.clearWishlistPopup() }) { Text("Cancel") }
                }
            }
        }
    }
    if (wishlistAlertProgram != null) {
        Dialog(onDismissRequest = { sharedViewModel.clearWishlistAlert() }) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                val channelName = channelMap[wishlistAlertProgram?.channelId]?.displayName ?: "Unknown Channel"
                Column(modifier = Modifier.align(Alignment.Center)) {
                    Text(text = "Your wishlist event is starting", color = Color.White)
                    Text(text = "Channel: $channelName", color = Color.White)
                    Text(text = "Start: ${wishlistAlertProgram?.startTime}", color = Color.White)
                    Text(text = "End: ${wishlistAlertProgram?.endTime}", color = Color.White)
                    Button(onClick = {
                        epgList.find { it.channelId == wishlistAlertProgram?.channelId }?.let {channelItem->
                            sharedViewModel.updateSelectedChannel(channelItem)
                            navController.navigate(Destination.panMetroScreen) {
                                PreferenceManager.selectedGenreIndex = 0
                                PreferenceManager.selectedChannelIndex = 0
                                PreferenceManager.lastEpgDataItem = null
                                // popUpTo(Destination.epgScreen) { inclusive = true }
                            }
                        }
                        sharedViewModel.clearWishlistAlert()
                    }) { Text("Play") }
                    Button(onClick = { sharedViewModel.clearWishlistAlert() }) { Text("Cancel") }
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
            delay(1000)
            currentTime.value = System.currentTimeMillis()
        }
    }
    val formattedTime = remember(currentTime.value) {
        val calendar = Calendar.getInstance().apply { timeInMillis = currentTime.value }
        val sdf = SimpleDateFormat("hh:mma", Locale.US)
        sdf.format(calendar.time)
    }
    Row(
        modifier = Modifier
            .width(width)
            .background(Color(0xFF161D25))
            .padding(start = 35.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Row(
            modifier = Modifier.width(width).padding(start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = R.drawable.vector_271),
                contentDescription = "",
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = formattedTime,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(150.dp),
                style = TextStyle(fontSize = 16.sp, lineHeight = 28.01.sp, fontFamily = FontFamily(Font(R.font.figtree_light)), fontWeight = FontWeight(600), color = Color(0xFFB5B5B5))
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
//    hasInitiallyFocused: MutableState<Boolean>,
    focusRequester: FocusRequester,
    languageFocusRequesters: List<FocusRequester>,
    languageSelectedIndex: MutableState<Int>,
    categoryFocusRequesters: List<FocusRequester>,
    categorySelectedIndex: MutableState<Int>
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
                        KeyEvent.KEYCODE_DPAD_UP -> {
                            if (isFirstChannel) {
                                languageFocusRequesters.getOrNull(languageSelectedIndex.value)?.let { requester ->
                                    try {
                                        requester.requestFocus()
                                    } catch (e: IllegalStateException) {
                                        e.message
                                    }
                                }
                                true
                            } else false
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
                text = channel.content?.channelNo?.toString() ?: "",
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
                .focusRequester(focusRequester)
                .focusable()
                .clip(RoundedCornerShape(4.dp))
                .clickable { onPlayClicked(channel.content?.videoUrl) }
        ) {
//            if (channelIndex == 0 && !hasInitiallyFocused.value) {
//                LaunchedEffect(Unit) {
//                    actualFocusRequester.requestFocus()
//                    hasInitiallyFocused.value = true
//                }
//            }
            AsyncImage(
                model = channel.content?.thumbnailUrl,
                contentDescription = "Channel Logo",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp))
                    .padding(8.dp)
                    .background(Color(0xFF161D25), RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Fit
            )
        }
    }
}


