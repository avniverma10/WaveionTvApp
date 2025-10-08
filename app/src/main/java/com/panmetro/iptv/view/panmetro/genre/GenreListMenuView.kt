package com.panmetro.iptv.view.panmetro.genre

import android.view.KeyEvent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.panmetro.iptv.R
import com.panmetro.iptv.extensions.requestFocusSafely
import com.panmetro.iptv.extensions.visiblePage
import com.panmetro.iptv.model.data.genre.WTVGenre
import com.panmetro.iptv.utils.theme.base_color
import com.panmetro.iptv.viewmodels.SharedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.compareTo

private const val GENRE_NAV_TAP_GAP_MS = 80L
private const val GENRE_NAV_HOLD_STEP_MS = 120L
@Composable
fun GenreMenuDesign(
    sharedViewModel : SharedViewModel,
    genres: List<WTVGenre>,
    genreSelectedIndex: MutableState<Int>,
    channelToGenreFocus: MutableState<Boolean>,
    focusRequesters: List<FocusRequester>,
    listState: LazyListState,
    onCategoryForward: (Int, WTVGenre) -> Unit
) {

    val currentFocusedGenreSelection by sharedViewModel.currentFocusedGenreSelection.collectAsState()
    val filteredChannels by sharedViewModel.filteredPanMetroChannels.collectAsState()
    // Track which item is focused or selected.
    var focusedIndex by remember { mutableStateOf(genreSelectedIndex.value) }
    // Adjust focusedIndex when dynamicGenres changes to prevent index out of bounds
    LaunchedEffect(genres) {
        if (focusedIndex >= genres.size) {
            focusedIndex = genres.lastIndex.coerceAtLeast(0)
            genreSelectedIndex.value = focusedIndex
        }
    }
    // LazyListState to manage scrolling.
    // Coroutine scope for launching suspend functions.
    val coroutineScope = rememberCoroutineScope()
    var lastNavTime by remember { mutableStateOf(0L) }

    // Sync focusedIndex with genreSelectedIndex when genreSelectedIndex changes
    LaunchedEffect(genreSelectedIndex.value) {
        if (focusedIndex != genreSelectedIndex.value) {
            focusedIndex = genreSelectedIndex.value
        }
    }

    val bottomArrowHighlighted by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(genreSelectedIndex.value, channelToGenreFocus.value) {
        if (channelToGenreFocus.value) {
            val targetIndex = genreSelectedIndex.value.coerceIn(0, genres.lastIndex)

            // Only update if changed to prevent unnecessary recompositions
            if (focusedIndex != targetIndex) {
                focusedIndex = targetIndex
            }

            // Check if target item is already visible
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            val isItemVisible = visibleItems.any { it.index == targetIndex }

            if (!isItemVisible) {
                // Item is not visible, scroll to it
                coroutineScope.launch {
                    listState.scrollToItem(targetIndex)

                    // Small delay to ensure layout is stable after scroll
                    delay(10)

                    // Request focus after scroll is complete
                    focusRequesters.requestFocusSafely(targetIndex=targetIndex, isFocusEnabled = true )
                }
            } else {
                // Item is already visible, just request focus
                focusRequesters.requestFocusSafely(targetIndex=targetIndex, isFocusEnabled = true )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(Color(0xFF151414), shape = RoundedCornerShape(6.dp))
    ) {
        // Top arrow row.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(5.dp)
                .background(Color(0xFF2F2A2A), shape = RoundedCornerShape(8.dp)),
            verticalAlignment = Alignment.CenterVertically
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
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Genre list in a LazyColumn.
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .focusable()
                    .onPreviewKeyEvent { keyEvent ->
                        if (keyEvent.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                        val repeat = keyEvent.nativeKeyEvent.repeatCount
                        val now = System.currentTimeMillis()
                        val gap = if (repeat == 0) GENRE_NAV_TAP_GAP_MS else GENRE_NAV_HOLD_STEP_MS
                        if (now - lastNavTime < gap) return@onPreviewKeyEvent true
                        lastNavTime = now

                        val total = genres.size
                        if (total == 0) return@onPreviewKeyEvent true

                        val (firstVis, lastVis, count) = listState.visiblePage()
                        fun scrollToKeepVisible(target: Int) {
                            if (count == 0) return
                            if (target < firstVis) {
                                val newFirst = (firstVis - count).coerceAtLeast(0)
                                coroutineScope.launch { listState.scrollToItem(newFirst) }
                            } else if (target > lastVis) {
                                val maxFirst = (total - count).coerceAtLeast(0)
                                val newFirst = (firstVis + count).coerceIn(0, maxFirst)
                                coroutineScope.launch { listState.scrollToItem(newFirst) }
                            }
                        }

                        when (keyEvent.nativeKeyEvent.keyCode) {
                            KeyEvent.KEYCODE_DPAD_UP -> {
                                if (focusedIndex > 0) {
                                    focusedIndex--
                                    scrollToKeepVisible(focusedIndex)
                                }else{
                                    focusedIndex = 0
                                }
                                true
                            }

                            KeyEvent.KEYCODE_DPAD_DOWN -> {
                                val lastIdx = total - 1
                                if (focusedIndex < lastIdx) {
                                    focusedIndex++
                                    scrollToKeepVisible(focusedIndex)
                                } else {
                                    focusedIndex = lastIdx
                                }
                                true
                            }

                            KeyEvent.KEYCODE_DPAD_CENTER -> {
                                // Confirm selection -> filter & jump to channels
                                genreSelectedIndex.value = focusedIndex
                                sharedViewModel.updateGenreScreenLastGenreIndex(focusedIndex)
                                sharedViewModel.updateGenreScreenLastChannelIndex(0)
                                genres.getOrNull(genreSelectedIndex.value)?.let {
                                    onCategoryForward(
                                        genreSelectedIndex.value,
                                        it
                                    )
                                }
                                true
                            }

                            KeyEvent.KEYCODE_DPAD_RIGHT ->{
                                if(filteredChannels.isNotEmpty()){
                                    channelToGenreFocus.value = false
                                    sharedViewModel._currentFocusedGenreSelection.value = false
                                    true
                                }else false
                            }
                            else -> false
                        }
                    }
            ) {
                itemsIndexed(genres) { index, genre ->
                    val isFocused = remember(focusedIndex) { mutableStateOf(index == focusedIndex) }
                    GenreMenuItemDesign(
                        categoryName = genre.name ?: "",
                        isFocused = isFocused,
                        currentFocusedGenreSelection = currentFocusedGenreSelection,
                        onSelectedIndex = (index == genreSelectedIndex.value),
                        focusRequester = focusRequesters[index],
                        onFocus = {
                            onCategoryForward(index, genre)
                        },
                        onKeyEvent = { false }// handled at LazyColumn level
                    )
                }

            }
        }
        // Bottom arrow row.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(5.dp)
                .background(Color(0xFF2F2A2A), shape = RoundedCornerShape(8.dp)),
            verticalAlignment = Alignment.CenterVertically
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
fun GenreMenuItemDesign(
    categoryName: String,
    isFocused: MutableState<Boolean>,
    currentFocusedGenreSelection: Boolean,
    onSelectedIndex: Boolean,
    focusRequester: FocusRequester,
    onFocus: () -> Unit,
    onKeyEvent: (androidx.compose.ui.input.key.KeyEvent) -> Boolean
) {

    val borderColor = if (isFocused.value && currentFocusedGenreSelection) base_color else Color.Transparent
    val scale by animateFloatAsState(targetValue = if (isFocused.value) 1f else .90f, label = "genreScale")
    val contentColor = when {
        onSelectedIndex -> base_color // selected is green always
        isFocused.value -> base_color
        else -> Color.White
    }
    val borderWidth = if (isFocused.value && currentFocusedGenreSelection)  1.dp else 0.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .scale(scale)
            .focusRequester(focusRequester)

    ) {
        Row(
            modifier = Modifier
                .focusable()
                .onKeyEvent(onKeyEvent)
                //.onFocusChanged { if (it.isFocused) onFocus() }
                .fillMaxWidth()
                .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(5.dp))
                .background(Color(0xFF232020), shape = RoundedCornerShape(6.dp))
                .padding(6.dp),
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
