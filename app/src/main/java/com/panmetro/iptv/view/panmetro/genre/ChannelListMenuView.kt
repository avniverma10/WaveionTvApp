package com.panmetro.iptv.view.panmetro.genre

import android.util.Log
import android.view.KeyEvent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.panmetro.iptv.R
import com.panmetro.iptv.extensions.loge
import com.panmetro.iptv.extensions.visiblePage
import com.panmetro.iptv.model.data.epgdata.EPGDataItem
import com.panmetro.iptv.utils.theme.base_color
import com.panmetro.iptv.viewmodels.SharedViewModel
import kotlinx.coroutines.launch
import kotlin.math.min

private const val NAV_TAP_GAP_MS = 80L
private const val NAV_HOLD_STEP_MS = 120L

@Composable
fun ChannelMenuDesign(
    sharedViewModel: SharedViewModel,
    selectedChannelIndex: MutableState<Int>,
    channelListFocusRequester: FocusRequester,
    channelToGenreFocus: MutableState<Boolean>,
    onNavigateToGenre: () -> Unit,
    onVideoChange: (EPGDataItem, Int) -> Unit,
    onPlayerScreenIntent: (EPGDataItem) -> Unit,
) {
    val currentFocusedGenreSelection by sharedViewModel.currentFocusedGenreSelection.collectAsState()
    val filteredChannels by sharedViewModel.filteredPanMetroChannels.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var focusedIndex by remember { mutableStateOf(0) }
    var previewIndex by remember { mutableStateOf(0) }
    var lastNavTime by remember { mutableStateOf(0L) }

    LaunchedEffect(selectedChannelIndex.value, filteredChannels) {
        if (filteredChannels.isEmpty()) {
            channelToGenreFocus.value = true
            sharedViewModel._currentFocusedGenreSelection.value = true
            return@LaunchedEffect
        }

        val last = filteredChannels.lastIndex
        val target = selectedChannelIndex.value.coerceIn(0, last)
        focusedIndex = target
        previewIndex = target

        // Handle scrolling and focus
        try {
            // Request focus first
            channelListFocusRequester.requestFocus()
            channelToGenreFocus.value = false
            sharedViewModel._currentFocusedGenreSelection.value = false

            // Then handle scrolling
            val (firstVis, lastVis, count) = listState.visiblePage()

            if (count > 0 && (target < firstVis || target > lastVis)) {
                // Item not visible, scroll to it
                val maxFirst = (filteredChannels.size - count).coerceAtLeast(0)
                val targetFirst = (target - count / 2).coerceIn(0, maxFirst)
                listState.scrollToItem(targetFirst)
            } else {
                // Item is visible, ensure smooth animation
                coroutineScope.launch {
                    listState.scrollToItem(target)
                }
            }
        } catch (e: Exception) {
            Log.e("ChannelList", "Focus/scroll error: ${e.message}")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color(0xFF151414), shape = RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(5.dp)
                .background(Color(0xFF2F2A2A), shape = RoundedCornerShape(8.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowUp,
                contentDescription = "Up Icon",
                tint = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )
        }

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
                        textAlign = TextAlign.Center
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
                            if (keyEvent.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

                            val repeat = keyEvent.nativeKeyEvent.repeatCount
                            val now = System.currentTimeMillis()
                            val gap = if (repeat == 0) NAV_TAP_GAP_MS else NAV_HOLD_STEP_MS
                            if (now - lastNavTime < gap) return@onPreviewKeyEvent true
                            lastNavTime = now

                            if (filteredChannels.isEmpty()) return@onPreviewKeyEvent true

                            val total = filteredChannels.size
                            val lastIndex = total - 1

                            // Unified function to update focus and scroll smoothly
                            fun navigateToIndex(newIdx: Int, shouldAnimate: Boolean = true) {
                                val idx = newIdx.coerceIn(0, lastIndex)
                                focusedIndex = idx
                                previewIndex = idx
                                selectedChannelIndex.value = idx
                                onVideoChange(filteredChannels[idx], idx)

                                coroutineScope.launch {
                                    try {
                                        /*if (shouldAnimate) {
                                            listState.animateScrollToItem(idx)
                                        } else {
                                        }*/
                                        listState.scrollToItem(idx)
                                    } catch (e: Exception) {
                                        // Fallback to non-animated scroll
                                        listState.scrollToItem(idx)
                                    }
                                }
                            }

                            val keyCode = keyEvent.nativeKeyEvent.keyCode
                            val scanCode = keyEvent.nativeKeyEvent.scanCode

                            when  {
                                scanCode == 402 || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_CHANNEL_UP -> {
                                    // Navigate up
                                    if (focusedIndex > 0) {
                                        navigateToIndex(focusedIndex - 1)
                                    }
                                    true
                                }

                                scanCode == 403 || keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_CHANNEL_DOWN -> {
                                    // Navigate down
                                    if (focusedIndex < lastIndex) {
                                        navigateToIndex(focusedIndex + 1)
                                    }
                                    true
                                }
                                keyCode == KeyEvent.KEYCODE_DPAD_LEFT -> {
                                    channelToGenreFocus.value = true
                                    sharedViewModel._currentFocusedGenreSelection.value = true
                                    onNavigateToGenre()
                                    true
                                }
                                keyCode == KeyEvent.KEYCODE_DPAD_CENTER -> {
                                    onPlayerScreenIntent(filteredChannels[focusedIndex])
                                    true
                                }
                                keyCode == KeyEvent.KEYCODE_DPAD_RIGHT -> true
                                keyCode == KeyEvent.KEYCODE_PAGE_DOWN -> {
                                    // Jump down by visible page count
                                    val (_, _, count) = listState.visiblePage()
                                    val pageSize = count.coerceAtLeast(5)
                                    val newIdx = (focusedIndex + pageSize).coerceAtMost(lastIndex)
                                    navigateToIndex(newIdx, shouldAnimate = false)
                                    true
                                }
                                keyCode == KeyEvent.KEYCODE_PAGE_UP -> {
                                    // Jump up by visible page count
                                    val (_, _, count) = listState.visiblePage()
                                    val pageSize = count.coerceAtLeast(5)
                                    val newIdx = (focusedIndex - pageSize).coerceAtLeast(0)
                                    navigateToIndex(newIdx, shouldAnimate = false)
                                    true
                                }
                                else -> false
                            }
                        }
                ) {
                    itemsIndexed(
                        items = filteredChannels,
                        key = { _, ch -> ch.channelId ?: ch.videoUrl ?: ch.title ?: "" }
                    ) { index, channel ->
                        ChannelMenuItemDesign(
                            channel = channel,
                            isFocused = index == focusedIndex,
                            isPreview = index == previewIndex,
                            currentFocusedGenreSelection = currentFocusedGenreSelection
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(5.dp)
                .background(Color(0xFF2F2A2A), shape = RoundedCornerShape(8.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = "Down Icon",
                tint = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ChannelMenuItemDesign(
    channel: EPGDataItem,
    isFocused: Boolean,
    isPreview: Boolean,
    currentFocusedGenreSelection: Boolean,
) {
    val titleColor = if (isFocused || isPreview) base_color else Color.White
    val scale by animateFloatAsState(targetValue = if (isFocused) 1f else 0.90f, label = "rowScale")

    val stops = channel.bgGradient?.colors?.sortedBy { it.percentage }?.mapNotNull {
        runCatching { Color(android.graphics.Color.parseColor(it.color)) }.getOrNull()
    }.orEmpty()

    val brush = if (stops.size >= 2) {
        Brush.horizontalGradient(stops)
    } else {
        Brush.verticalGradient(listOf(Color(0xFF232020), Color(0xFF232020)))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .scale(scale)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = if ( isFocused && !currentFocusedGenreSelection) 1.dp else 0.dp, color = if (isFocused && !currentFocusedGenreSelection) base_color else Color.Transparent, shape = RoundedCornerShape(5.dp))
                .background(Color(0xFF232020), shape = RoundedCornerShape(6.dp))
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = channel.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(35.dp)
                    .background(brush, shape = RoundedCornerShape(4.dp))
                    .padding(4.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = channel.channelNo?.toString() ?: "--",
                    color = Color.Black,
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.width(8.dp))

            Text(
                text = channel.title ?: "",
                color = titleColor,
                fontSize = 15.sp,
                fontFamily = FontFamily(Font(R.font.figtree_medium)),
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = if (isFocused) TextOverflow.Clip else TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
