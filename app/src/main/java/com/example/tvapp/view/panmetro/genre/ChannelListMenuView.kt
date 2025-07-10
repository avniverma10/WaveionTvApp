package com.example.tvapp.view.panmetro.genre

import android.util.Log
import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.android.caastv.R
import com.example.tvapp.model.data.epgdata.EPGDataItem
import com.example.tvapp.utils.theme.base_color
import com.example.tvapp.viewmodels.SharedViewModel
import com.example.tvapp.viewmodels.genre.GenreViewModel
import kotlinx.coroutines.launch

@Composable
fun ChannelListMenuScreen(
    sharedViewModel: SharedViewModel,
    genreViewModel: GenreViewModel,
    selectedChannelIndex : MutableState<Int>,
    channelListFocusRequester: FocusRequester,
    channelToGenreFocus: MutableState<Boolean>,
    onNavigateToGenre: () -> Unit,
    onVideoChange: (EPGDataItem, Int) -> Unit,
    onPlayerScreenIntent: (EPGDataItem) -> Unit,
) {

    var focusedIndex by remember { mutableStateOf(0) }
    var previewChannelIndex by remember { mutableStateOf(0) }
    val filteredChannels by genreViewModel.filteredPanMetroChannels.collectAsState()
    val selectedChannelIndex by remember { mutableStateOf( selectedChannelIndex) }
    LaunchedEffect(filteredChannels) {
        focusedIndex = if(selectedChannelIndex.value >0) selectedChannelIndex.value else 0
        previewChannelIndex = if(selectedChannelIndex.value >0) selectedChannelIndex.value else 0
        filteredChannels.getOrNull(selectedChannelIndex.value)
            ?.let { sharedViewModel.updateSelectedChannel(it) }
    }

//    LaunchedEffect(Unit) {
//        channelListFocusRequester.requestFocus()
//    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val topArrowHighlighted by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }
    val bottomArrowHighlighted by remember {
        derivedStateOf {
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            visibleItems.isNotEmpty() && visibleItems.last().index < listState.layoutInfo.totalItemsCount - 1
        }
    }

    // Store last press info for double-click detection.
    var lastPressTime by remember { mutableStateOf(0L) }
    var lastKey by remember { mutableStateOf<Key?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth(1f)
            .fillMaxHeight()
            .background(Color(0xFF151414), shape = RoundedCornerShape(8.dp))
    ) {
        // Top arrow row.
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
                .background(Color(0xFF2F2A2A), shape = RoundedCornerShape(8.dp))
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Filled.KeyboardArrowUp,
                contentDescription = "Up Icon",
                tint = Color.Gray,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .fillMaxWidth()
            )
        }
        if (filteredChannels.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sorry, no channels available\nGet back soon!",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.figtree_medium)),
                    fontWeight = FontWeight.Medium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(10.dp)
                .focusRequester(channelListFocusRequester)
                .focusable()
                .onPreviewKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        when (keyEvent.nativeKeyEvent.keyCode) {
                            KeyEvent.KEYCODE_DPAD_LEFT -> {
                                // Switch focus to the genre list.
                                onNavigateToGenre()
                                false
                            }
                            KeyEvent.KEYCODE_DPAD_DOWN -> {
                                if (focusedIndex < filteredChannels.size - 1) {
                                    focusedIndex++
                                    if (filteredChannels.isNotEmpty() && focusedIndex < filteredChannels.size) {
                                        previewChannelIndex = focusedIndex
                                        onVideoChange(filteredChannels[focusedIndex], focusedIndex)
                                    }
                                    // Scroll if needed.
                                    val visibleIndices = listState.layoutInfo.visibleItemsInfo.map { it.index }
                                    if (focusedIndex !in visibleIndices) {
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(focusedIndex)
                                        }
                                    }
                                }
                                true
                            }
                            KeyEvent.KEYCODE_DPAD_UP -> {
                                if (focusedIndex > 0) {
                                    focusedIndex--
                                    if (filteredChannels.isNotEmpty() && focusedIndex < filteredChannels.size) {
                                        previewChannelIndex = focusedIndex
                                        onVideoChange(filteredChannels[focusedIndex], focusedIndex)
                                    }
                                    val visibleIndices = listState.layoutInfo.visibleItemsInfo.map { it.index }
                                    if (focusedIndex !in visibleIndices) {
                                        coroutineScope.launch {
                                            listState.animateScrollToItem(focusedIndex)
                                        }
                                    }

                                }
                                true
                            }
                            KeyEvent.KEYCODE_DPAD_CENTER -> {
                                // val currentTime = System.currentTimeMillis()
                                // val currentKey = keyEvent.key
                                selectedChannelIndex.value = focusedIndex
                                if(filteredChannels.size > focusedIndex) {
                                    sharedViewModel.updateGenreScreenLastChannelIndex(focusedIndex)
                                    onPlayerScreenIntent(filteredChannels[focusedIndex])
                                }
                                true
                                /*if (currentKey == lastKey && (currentTime - lastPressTime) < 300L) {
                                    if (filteredChannels.isNotEmpty() && focusedIndex < filteredChannels.size) {
                                        onDoubleClickIntent(filteredChannels[focusedIndex])
                                    }
                                    true
                                } else {
                                    lastPressTime = currentTime
                                    lastKey = currentKey
                                    if (filteredChannels.isNotEmpty() && focusedIndex < filteredChannels.size) {
                                        previewChannelIndex = focusedIndex
                                        onVideoChange(filteredChannels[focusedIndex], focusedIndex)
                                    }
                                    false
                                }*/
                            }
                            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                if (filteredChannels.isNotEmpty() && focusedIndex < filteredChannels.size) {
                                    previewChannelIndex = focusedIndex
                                    onVideoChange(filteredChannels[focusedIndex], focusedIndex)
                                }
                                true
                            }
                            else -> false
                        }
                    } else false
                }
        ) {
            itemsIndexed(filteredChannels) { index, channel ->
                NewChannelRow(
                    channel = channel,
                    isFocused = (index == focusedIndex),
                    isPreview = (index == previewChannelIndex),
                    onFocus = { newIndex ->
                        focusedIndex = newIndex
                        previewChannelIndex = newIndex
                    },
                    onVideoChange = { channelData, index ->
                        previewChannelIndex = index
                        onVideoChange(channelData, index)
                    }
                )
            }
        } }

        // Bottom arrow row.
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
                .background(Color(0xFF2F2A2A), shape = RoundedCornerShape(8.dp))
                .onPreviewKeyEvent { event -> if (event.type == KeyEventType.KeyUp) true else false }
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = "Down Icon",
                tint = Color.Gray,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .fillMaxWidth()
            )
        }
    }
}



@Composable
fun NewChannelRow(
    channel: EPGDataItem,
    isFocused: Boolean,
    isPreview: Boolean,
    onFocus: (Int) -> Unit,
    onVideoChange: (EPGDataItem, Int) -> Unit
) {
    val borderColor = if (isFocused) base_color else Color.Transparent

    val titleTextColor = if (isFocused || isPreview) base_color else Color.White

    val stops = channel.content?.bgGradient
        ?.colors
        ?.sortedBy { it.percentage }
        ?.map { Color(android.graphics.Color.parseColor(it.color)) }
        .orEmpty()

    // 2) make a brush (horizontal here, but you can respect angle if you want)
    val brush = if (stops.size >= 2) {
        Brush.horizontalGradient(stops)
    } else {
        Brush.verticalGradient(listOf(Color(0xFF232020), Color(0xFF232020))) // fallback
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .focusable()
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    // When this row gains focus, trigger the video change,
                    // and update the parent's focus and preview index.
                    onVideoChange(channel, 0)
                    // Depending on your use-case, you might want to pass the index from the parent.
                    // Here, assume the parent lambda onFocus is invoked with the row index.
                    // (This requires that you call onFocus(newIndex) from your LazyColumn.)
                }
            }
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (isFocused) 1.dp else 0.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(5.dp)
                )
                .background(Color(0xFF232020), shape = RoundedCornerShape(6.dp))
                .padding(8.dp)
        ) {
            Spacer(modifier = Modifier.width(8.dp))
            AsyncImage(
                model = channel.content?.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(44.dp)
                    .background(brush, shape = RoundedCornerShape(4.dp))
                    .padding(4.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .background(Color.White, shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 2.dp, vertical = 2.dp)
            ) {
                androidx.compose.material3.Text(
                    text = channel.content?.channelNo?.toString() ?: "--",
                    color = Color.Black,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = channel.content?.title ?: "",
                color = titleTextColor, // Yellow if either focused or preview, otherwise white.
                fontSize = 15.sp,
                fontFamily = FontFamily(Font(R.font.figtree_medium)),
                fontWeight = FontWeight(400),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
