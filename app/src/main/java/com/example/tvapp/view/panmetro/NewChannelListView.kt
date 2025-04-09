package com.example.tvapp.view.panmetro

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.example.tvapp.R
import com.example.tvapp.model.data.epgdata.EPGDataItem
import com.example.tvapp.viewmodels.SharedViewModel

@Composable
fun NewChannelListScreen(
    sharedViewModel: SharedViewModel,
    genreListFocusRequester: FocusRequester,
    channelListFocusRequester: FocusRequester,
    onVideoChange: (EPGDataItem, Int) -> Unit,
    onDoubleClickIntent: (EPGDataItem) -> Unit,
) {
    // Track the currently focused channel index
    var focusedIndex by remember { mutableStateOf(0) }
    val filteredChannels by sharedViewModel.filteredPanMetroChannels.collectAsState()
    // LazyListState tracks the scroll state of the LazyColumn.
    val listState = rememberLazyListState()
    // Derived state to determine if there are items above the visible area.
    val topArrowHighlighted by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }
    // Derived state to determine if there are items below the visible area.
    val bottomArrowHighlighted by remember {
        derivedStateOf {
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            if (visibleItems.isNotEmpty()) {
                visibleItems.last().index < listState.layoutInfo.totalItemsCount - 1
            } else {
                false
            }
        }
    }

    // Removed the LaunchedEffect block that automatically scrolled

    // Store the time and key of the last press for double-click detection.
    var lastPressTime by remember { mutableStateOf(0L) }
    var lastKey by remember { mutableStateOf<Key?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth(1f) // fixed width for the side menu
            .fillMaxHeight()
            .background(Color(0xFF151414), shape = RoundedCornerShape(8.dp))
    ) {
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

        // A vertical list of channels
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
                                // Switch focus back to the left list.
                                genreListFocusRequester.requestFocus()
                                true
                            }
                            KeyEvent.KEYCODE_DPAD_DOWN -> {
                                // Move focus down only if not at the last channel.
                                if (focusedIndex < filteredChannels.size - 1) {
                                    focusedIndex++
                                }
                                true
                            }
                            KeyEvent.KEYCODE_DPAD_UP -> {
                                // Move focus up only if not at the first channel.
                                if (focusedIndex > 0) {
                                    focusedIndex--
                                }
                                true
                            }
                            KeyEvent.KEYCODE_DPAD_CENTER -> {
                                val currentTime = System.currentTimeMillis()
                                val currentKey = keyEvent.key
                                // Check if this is a double-click.
                                if (currentKey == lastKey && (currentTime - lastPressTime) < 300L) {
                                    if (filteredChannels.isNotEmpty() && filteredChannels.size > focusedIndex) {
                                        onDoubleClickIntent(filteredChannels[focusedIndex])
                                    }
                                    true
                                } else {
                                    lastPressTime = currentTime
                                    lastKey = currentKey
                                    if (filteredChannels.isNotEmpty() && filteredChannels.size > focusedIndex) {
                                        onVideoChange(filteredChannels[focusedIndex], focusedIndex)
                                    }
                                    false
                                }
                            }
                            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                if (filteredChannels.isNotEmpty() && filteredChannels.size > focusedIndex) {
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
                    onFocus = { focusedIndex = index },
                    onVideoChange = onVideoChange
                )
            }
        }

        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
                .background(Color(0xFF2F2A2A), shape = RoundedCornerShape(8.dp))
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyUp) true else false
                }
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
    onFocus: () -> Unit,
    onVideoChange: (EPGDataItem, Int) -> Unit
) {
    val isCurrentFocused = remember { mutableStateOf(false) }
    val backgroundColor = if (isFocused) Color(0xFF49FEDD) else Color(0x001F3A6B)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .focusable()
            .onFocusChanged {
                if (it.isFocused) {
                    onVideoChange(channel, 0)
                    onFocus()
                }
            }
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = if (isFocused) (1f).dp else 0.dp,
                    color = backgroundColor,
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
                    .size(35.dp)
                    .background(Color.Black, RoundedCornerShape(4.dp))
                    .padding(4.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .background(Color.White, shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 2.dp, vertical = 2.dp)
            ) {
                androidx.compose.material3.Text(
                    text = channel.content?.channelNo?.toString() ?: "--",
                    color = Color.Black,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = channel.content?.title ?: "",
                color = Color.White,
                fontSize = 17.sp,
                fontFamily = FontFamily(Font(R.font.figtree_medium)),
                fontWeight = FontWeight(400),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
