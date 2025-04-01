package com.example.tvapp.view.panmetro

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.example.tvapp.model.data.genre.WTVGenre


@Composable
fun CategoryMenu(genres: List<WTVGenre>,
                 channelListFocusRequester: FocusRequester,
                 genreListFocusRequester: FocusRequester,
                 onCategoryForward: (WTVGenre) -> Unit) {
    // Track which item is currently focused or selected
    var focusedIndex by remember { mutableStateOf(0) }
    var selectedIndex by remember { mutableStateOf(0) }
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
            // If there are visible items, check if the last visible index is less than the last index.
            if (visibleItems.isNotEmpty()) {
                visibleItems.last().index < listState.layoutInfo.totalItemsCount - 1
            } else {
                false
            }
        }
    }
    LaunchedEffect(focusedIndex) {
        listState.animateScrollToItem(index = focusedIndex)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth(.4f) // fixed width for the side menu
            .fillMaxHeight()
            .border(
                width = (.5).dp,
                color = Color.Green,
                shape = RoundedCornerShape(5.dp)
            )
            .background(Color.Transparent, shape = RoundedCornerShape(5.dp))
    ) {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
                .background(if(topArrowHighlighted)Color.Cyan else Color.Green, shape = RoundedCornerShape(8.dp))
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
        LazyColumn(
            state = listState,
            modifier = Modifier
            .weight(1f)
            .padding(10.dp)
            .focusRequester(genreListFocusRequester)
        ) {
            itemsIndexed(genres){index, genre ->
                CategoryMenuItem(
                    categoryName = genre.name?:"",
                    isFocused = (index == focusedIndex),
                    onSelectedIndex = (index == selectedIndex ),
                    onFocus = { focusedIndex = index },
                    onKeyEvent = { keyEvent ->
                        // Only handle key down events.
                        if (keyEvent.type == KeyEventType.KeyDown) {
                            when (keyEvent.nativeKeyEvent.keyCode) {
                                KeyEvent.KEYCODE_DPAD_UP -> {
                                    // Move focus upward: cycle to last item if at the top.
                                    focusedIndex = if (focusedIndex > 0) focusedIndex - 1 else genres.size - 1
                                    true // consume event
                                }
                                KeyEvent.KEYCODE_DPAD_DOWN -> {
                                    // Move focus downward: cycle to first item if at the bottom.
                                    focusedIndex = if (focusedIndex < genres.size - 1) focusedIndex + 1 else 0
                                    true // consume event
                                }
                                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                    // DPAD center (OK) key: trigger selection action.
                                    //channelListFocusRequester.requestFocus()
                                    selectedIndex = focusedIndex
                                    genres.getOrNull(selectedIndex)?.let { onCategoryForward(it) }
                                    true // consume event
                                }
                                else -> false
                            }
                        } else false
                    }
                )
            }
        }
        Row (
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
                .background(if(bottomArrowHighlighted)Color.Cyan else Color.Green, shape = RoundedCornerShape(8.dp))
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = "Up Icon",
                tint = Color.Gray,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun CategoryMenuItem(
    categoryName: String,
    isFocused: Boolean,
    onSelectedIndex: Boolean,
    onFocus: () -> Unit,
    onKeyEvent: (androidx.compose.ui.input.key.KeyEvent) -> Boolean
) {
    // Example highlight colors when focused
    val backgroundColor = if (onSelectedIndex) Color.Blue else Color.White
    val contentColor = if (isFocused || onSelectedIndex) Color.Green else Color.White

    val borderModifier = if (onSelectedIndex) {
        Modifier.background( backgroundColor)
    } else {
        Modifier // No border
    }
    // Each row is focusable
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .focusable()
            .onFocusChanged { if (it.isFocused) onFocus() }
            .onKeyEvent(onKeyEvent)
            .then(borderModifier)
            .padding(5.dp)
    ) {
        Text(
            text = categoryName,
            style = MaterialTheme.typography.bodyLarge,
            color = contentColor,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(end = 5.dp)
        )
        if(isFocused){
            androidx.compose.material3.Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Up Icon",
                tint =  contentColor,
                modifier = Modifier.padding(end = 4.dp)
            )
        }
    }
}

