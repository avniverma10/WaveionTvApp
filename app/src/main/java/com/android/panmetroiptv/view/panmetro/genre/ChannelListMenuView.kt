package com.android.panmetroiptv.view.panmetro.genre

import android.util.Log
import android.view.KeyEvent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.android.panmetroiptv.R
import com.android.panmetroiptv.model.data.epgdata.EPGDataItem
import com.android.panmetroiptv.utils.theme.base_color
import com.android.panmetroiptv.viewmodels.SharedViewModel
import com.android.panmetroiptv.viewmodels.genre.GenreViewModel
import kotlinx.coroutines.launch


@Composable
fun ChannelListMenuScreen(
    sharedViewModel: SharedViewModel,
    genreViewModel: GenreViewModel,
    selectedChannelIndex: MutableState<Int>,
    channelListFocusRequester: FocusRequester,
    channelToGenreFocus: MutableState<Boolean>,
    onNavigateToGenre: () -> Unit,
    onVideoChange: (EPGDataItem, Int) -> Unit,
    onPlayerScreenIntent: (EPGDataItem) -> Unit,
) {
    var focusedIndex by remember { mutableStateOf(0) }
    var previewChannelIndex by remember { mutableStateOf(0) }
    val filteredChannels by genreViewModel.filteredPanMetroChannels.collectAsState()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Sync indices with selectedChannelIndex
    LaunchedEffect(selectedChannelIndex.value) {
        focusedIndex = selectedChannelIndex.value.coerceAtLeast(0)
        previewChannelIndex = selectedChannelIndex.value.coerceAtLeast(0)
    }

    // Handle initial focus and scroll
    LaunchedEffect(filteredChannels) {
        if (filteredChannels.isNotEmpty()) {
            try {
                channelListFocusRequester.requestFocus()
                coroutineScope.launch {
                    listState.animateScrollToItem(focusedIndex)
                }
            } catch (e: Exception) {
                Log.e("ChannelList", "Focus error: ${e.message}")
            }
        }
    }

    // Auto-scroll when focused item changes
    LaunchedEffect(focusedIndex) {
        if (focusedIndex != -1 && focusedIndex !in listState.layoutInfo.visibleItemsInfo.map { it.index }) {
            coroutineScope.launch {
                listState.animateScrollToItem(focusedIndex)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color(0xFF151414), shape = RoundedCornerShape(8.dp))
    ) {
        // Top arrow (fixed height)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp) // Fixed height
                .padding(5.dp)
                .background(Color(0xFF2F2A2A), shape = RoundedCornerShape(8.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Filled.KeyboardArrowUp,
                contentDescription = "Up Icon",
                tint = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Main content area with weight
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (filteredChannels.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No channels available\nGet back soon!",
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
                        .fillMaxSize()
                        .focusRequester(channelListFocusRequester)
                        .focusable()
                        .onPreviewKeyEvent { keyEvent ->
                            handleKeyEvents(
                                keyEvent = keyEvent,
                                filteredChannels = filteredChannels,
                                focusedIndex = focusedIndex,
                                onNavigateToGenre = {
                                    channelToGenreFocus.value = true
                                    onNavigateToGenre()
                                },
                                onIndexChange = { newIndex ->
                                    focusedIndex = newIndex
                                    previewChannelIndex = newIndex
                                    selectedChannelIndex.value = newIndex
                                    onVideoChange(filteredChannels[newIndex], newIndex)
                                },
                                onSelectChannel = {
                                    onPlayerScreenIntent(filteredChannels[focusedIndex])
                                }
                            )
                        }
                ) {
                    itemsIndexed(filteredChannels) { index, channel ->
                        ChannelListItem(
                            channel = channel,
                            isFocused = index == focusedIndex,
                            isPreview = index == previewChannelIndex,
                            onFocus = {
                                focusedIndex = index
                                previewChannelIndex = index
                                onVideoChange(channel, index)
                            }
                        )
                    }
                }
            }
        }

        // Bottom arrow (fixed height)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp) // Fixed height
                .padding(5.dp)
                .background(Color(0xFF2F2A2A), shape = RoundedCornerShape(8.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = "Down Icon",
                tint = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun handleKeyEvents(
    keyEvent: androidx.compose.ui.input.key.KeyEvent,
    filteredChannels: List<EPGDataItem>,
    focusedIndex: Int,
    onNavigateToGenre: () -> Unit,
    onIndexChange: (Int) -> Unit,
    onSelectChannel: () -> Unit
): Boolean {
    if (keyEvent.type != KeyEventType.KeyDown) return false

    return when (keyEvent.nativeKeyEvent.keyCode) {
        KeyEvent.KEYCODE_DPAD_LEFT -> {
            onNavigateToGenre()
            true
        }
        KeyEvent.KEYCODE_DPAD_DOWN -> {
            if (focusedIndex < filteredChannels.lastIndex) {
                onIndexChange(focusedIndex + 1)
            }
            true
        }
        KeyEvent.KEYCODE_DPAD_UP -> {
            if (focusedIndex > 0) {
                onIndexChange(focusedIndex - 1)
            }
            true
        }
        KeyEvent.KEYCODE_DPAD_CENTER -> {
            onSelectChannel()
            true
        }
        KeyEvent.KEYCODE_DPAD_RIGHT -> true
        else -> false
    }
}

@Composable
fun ChannelListItem(
    channel: EPGDataItem,
    isFocused: Boolean,
    isPreview: Boolean,
    onFocus: () -> Unit
) {
    val titleColor = if (isFocused || isPreview) base_color else Color.White
    val scale by animateFloatAsState(targetValue = if (isFocused) 1.05f else if (isFocused && isPreview) 1f else .9f)

    val stops = channel.content?.bgGradient
        ?.colors
        ?.sortedBy { it.percentage }
        ?.mapNotNull {
            try {
                Color(android.graphics.Color.parseColor(it.color))
            } catch (e: IllegalArgumentException) {
                null
            }
        }
        .orEmpty()

    val brush = if (stops.size >= 2) {
        Brush.horizontalGradient(stops)
    } else {
        Brush.verticalGradient(listOf(Color(0xFF232020), Color(0xFF232020))) // fallback
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(8.dp)
            .focusable()
            .scale(scale)
            .onFocusChanged { if (it.isFocused) onFocus() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (isFocused) 1.dp else 0.dp,
                    color = if (isFocused) base_color else Color.Transparent,
                    shape = RoundedCornerShape(5.dp)
                )
                .background(Color(0xFF232020), shape = RoundedCornerShape(6.dp))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Channel logo
            AsyncImage(
                model = channel.content?.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(35.dp)
                    .background(brush, shape = RoundedCornerShape(4.dp))
                    .padding(4.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.width(12.dp))

            // Channel number
            Box(
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = channel.content?.channelNo?.toString() ?: "--",
                    color = Color.Black,
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.width(8.dp))

            // Channel title
            Text(
                text = channel.content?.title ?: "",
                color = titleColor,
                fontSize = 15.sp,
                fontFamily = FontFamily(Font(R.font.figtree_medium)),
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}