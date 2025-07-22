package com.caastv.tvapp.view.panmetro.genre

import android.view.KeyEvent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.android.caastv.R
import com.caastv.tvapp.extensions.loge
import com.caastv.tvapp.model.data.genre.WTVGenre
import com.caastv.tvapp.utils.theme.base_color
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun GenreListMenu(
    genres: List<WTVGenre>,
    genreSelectedIndex: MutableState<Int>,
    channelToGenreFocus: MutableState<Boolean>,
    focusRequesters: List<FocusRequester>,
    onCategoryForward: (Int, WTVGenre) -> Unit
) {
    // Track which item is focused or selected.
    var focusedIndex by remember { mutableStateOf(0) }
    // LazyListState to manage scrolling.
    val listState = rememberLazyListState()
    // Coroutine scope for launching suspend functions.
    val coroutineScope = rememberCoroutineScope()

    val bottomArrowHighlighted by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(genreSelectedIndex) {
        focusedIndex = if(genreSelectedIndex.value >0) genreSelectedIndex.value else 0
        listState.animateScrollToItem(focusedIndex)
        focusRequesters.getOrNull(genreSelectedIndex.value)?.let { requester ->
            try {
                requester.requestFocus()
            } catch (e: IllegalStateException) {
                loge("FocusError", "FocusRequester not initialized ${e.message}")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(0.4f)
            .fillMaxHeight()
            .background(Color(0xFF151414), shape = RoundedCornerShape(6.dp))
    ) {
        // Top arrow row.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
                .background(Color(0xFF2F2A2A), shape = RoundedCornerShape(8.dp))
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Filled.KeyboardArrowUp,
                contentDescription = "Up Icon",
                tint = Color.White,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .fillMaxWidth()
            )
        }
        // Genre list in a LazyColumn.
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(6.dp)
        ) {
            itemsIndexed(genres) { index, genre ->
                NewCategoryMenuItem(
                    categoryName = genre.name ?: "",
                    isFocused = (index == focusedIndex),
                    channelToGenreFocus= channelToGenreFocus,
                    onSelectedIndex = (index == genreSelectedIndex.value),
                    focusRequester = focusRequesters[index],
                    onFocus = { onCategoryForward(index, genre) },
                    onKeyEvent = { keyEvent ->
                        channelToGenreFocus.value = false
                        if (keyEvent.type == KeyEventType.KeyDown) {
                            when (keyEvent.nativeKeyEvent.keyCode) {
                                KeyEvent.KEYCODE_DPAD_UP -> {
                                    if (focusedIndex > 0) {
                                        focusedIndex--
                                        // Scroll if the new focused item is not visible.
                                        val visibleIndices = listState.layoutInfo.visibleItemsInfo.map { it.index }
                                        if (focusedIndex !in visibleIndices) {
                                            coroutineScope.launch {
                                                listState.animateScrollToItem(focusedIndex)
                                            }
                                        }
                                    }
                                    true
                                }
                                KeyEvent.KEYCODE_DPAD_DOWN -> {
                                    // Only move down if not at the last item.
                                    if (focusedIndex < genres.size - 1) {
                                        focusedIndex++
                                        val visibleIndices = listState.layoutInfo.visibleItemsInfo.map { it.index }
                                        if (focusedIndex !in visibleIndices) {
                                            coroutineScope.launch {
                                                listState.animateScrollToItem(focusedIndex)
                                            }
                                        }
                                    }
                                    true
                                }
                                KeyEvent.KEYCODE_DPAD_LEFT -> {
                                    true
                                }
                                KeyEvent.KEYCODE_DPAD_CENTER -> {
                                    genreSelectedIndex.value = focusedIndex
                                    genres.getOrNull(genreSelectedIndex.value)?.let { onCategoryForward(genreSelectedIndex.value, it) }
                                    true
                                }

                                else -> false
                            }
                        } else false
                    }
                )
            }
        }
        // Bottom arrow row.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
                .background(
                    if (bottomArrowHighlighted) Color(0xFF2F2A2A) else Color(0xFF2C2727),
                    shape = RoundedCornerShape(8.dp)
                )
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
fun NewCategoryMenuItem(
    categoryName: String,
    isFocused: Boolean,
    channelToGenreFocus: MutableState<Boolean>,
    onSelectedIndex: Boolean,
    focusRequester: FocusRequester,
    onFocus: () -> Unit,
    onKeyEvent: (androidx.compose.ui.input.key.KeyEvent) -> Boolean
) {
    val borderColor = if (isFocused) base_color else Color.Transparent
    val scale by animateFloatAsState(targetValue = if (isFocused && channelToGenreFocus.value) 1.3f else if (isFocused) 1.1f else 1f)
    val contentColor = if (categoryName == "All") {
        if (isFocused) base_color else Color.White
    } else {
        if (isFocused) base_color else Color.White
    }
    val borderWidth = 1.5.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .scale(scale)
            .clip(RoundedCornerShape(6.dp))
            .padding(borderWidth)
            .focusRequester(focusRequester)  // assign the FocusRequester here
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .focusable()
                .onFocusChanged { if (it.isFocused) onFocus() }
                .onKeyEvent(onKeyEvent)
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (categoryName == "All") {
                Text(
                    text = categoryName.toUpperCase(Locale.ROOT),
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.figtree_medium)),
                    fontWeight = FontWeight(400),
                    color = contentColor,
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFF2F2A2A), shape = RoundedCornerShape( bottomStart = 6.dp, topStart = 6.dp))
                        .padding(start = 16.dp)
                        .fillMaxHeight()
                        .wrapContentHeight(Alignment.CenterVertically)
                )
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF2F2A2A), shape = RoundedCornerShape( bottomEnd = 6.dp, topEnd  = 6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Arrow Icon",
                        tint = contentColor
                    )
                }
            } else {
                Text(
                    text = categoryName.toUpperCase(Locale.ROOT),
                    color = contentColor,
                    fontSize = 15.sp,
                    maxLines = 1,
                    fontFamily = FontFamily(Font(R.font.figtree_medium)),
                    fontWeight = FontWeight(400),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF232020), shape = RoundedCornerShape(6.dp))
                        .padding(start = 16.dp, end = 4.dp)
                        .fillMaxHeight()
                        .wrapContentHeight(Alignment.CenterVertically)
                )
            }
        }
    }
}
