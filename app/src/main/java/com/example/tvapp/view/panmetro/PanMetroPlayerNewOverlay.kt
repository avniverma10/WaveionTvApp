package com.example.tvapp.view.panmetro

import android.util.Log
import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tvapp.model.data.epgdata.EPGDataItem
import com.example.tvapp.view.navigationhelper.Destination
import com.example.tvapp.viewmodels.SharedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PanMetroNewOverlay(
    selectedIndex: MutableState<Int>,
    lazyListState: LazyListState,
    sharedViewModel: SharedViewModel,
    channelFocusRequesters: List<FocusRequester>,
    onChannelFocused: (EPGDataItem) -> Unit
) {

    val epgList by sharedViewModel.wtvEPGList.collectAsState()
    val selectedChannel by sharedViewModel.selectedChannel.collectAsState()
    val scope = rememberCoroutineScope()
    // 1) Create one FocusRequester for the container, so it can grab focus first
    val containerRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // 1) Request initial focus into the first card
    LaunchedEffect(selectedIndex) {
        selectedIndex.value = epgList.indexOfFirst { it.content?.videoUrl == selectedChannel.content?.videoUrl }
            .takeIf { it >= 0 } ?: 0
        // Delay a frame to ensure row is in composition
        channelFocusRequesters.getOrNull(selectedIndex.value)?.let { requester ->
            try {
                requester.requestFocus()
            } catch (e: IllegalStateException) {
                Log.e("FocusError", "FocusRequester not initialized", e)
            }
        }
        /* withFrameNanos {
             channelFocusRequesters[selectedIndex.value].requestFocus()
         }*/
    }

    // Whenever focusedIndex changes, scroll & focus
    /*LaunchedEffect(focusedIndex) {
        listState.animateScrollToItem(focusedIndex)
        channelRequesters[focusedIndex].requestFocus()
        onChannelFocused(epgList[focusedIndex])
    }*/

    Box(modifier = Modifier.fillMaxSize()) {
        // Top overlay for program info.
        val topBarGradient = Brush.horizontalGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.6f),
                Color.Transparent
            )
        )
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(topBarGradient)
                .padding(horizontal = 24.dp, vertical = 13.dp)
        ) {
            TopOverlayInfo(dataItem = selectedChannel)
        }

        // Bottom channel strip
        Box (
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp, 13.dp)
                .focusTarget()   // enable focus movement inside

        ) {
            LazyRow(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                itemsIndexed(
                    items = epgList,
                    key = { index, item -> item.channelId ?: index }
                ) { index, item ->
                    val isFocused = remember { mutableStateOf(false) }
                    val isSelected = selectedIndex.value == index
                    val modifier = Modifier
                        .then(
                            when {
                                isSelected ->
                                    Modifier.border(2.dp, Color(0xFF49FEDD), shape = RoundedCornerShape(10.dp))
                                else -> Modifier
                            }
                        )
                        .onFocusChanged {
                            isFocused.value = it.isFocused
                            if (it.isFocused) {
                                selectedIndex.value = index
                            }
                        }
                        .focusRequester(channelFocusRequesters[index])
                        .focusable()
                        .onPreviewKeyEvent { keyEvent ->
                            if (keyEvent.type == KeyEventType.KeyDown &&
                                keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                            ) {
                                channelFocusRequesters[selectedIndex.value].let { requester ->
                                    scope.launch {
                                        delay(50)
                                        requester.requestFocus()
                                    }
                                }
                                true
                            } else false
                        }

                    ChannelCard(
                        epgDataItem = item,
                        isFocused = isSelected,
                        modifier= modifier

                    )
                }
            }
        }
    }
}


@Composable
fun ChannelCard(
    epgDataItem: EPGDataItem,
    isFocused: Boolean,
    modifier: Modifier
) {
    val now = System.currentTimeMillis()
    val programList = epgDataItem.currentPrograms ?: epgDataItem.tv?.programme

    val currentProgram = programList
        ?.sortedBy { it.startTime }
        ?.firstOrNull { (it.startTime ?: 0) <= now && (it.endTime ?: 0) > now }
    val nextProgram = programList
        ?.firstOrNull { (it.startTime ?: 0) > now }

    val minutesLeft = currentProgram?.endTime?.let { ((it - now) / 60000).toInt() } ?: 0


    Box(
        modifier = modifier
            .width(260.dp)
            .height(160.dp)
            .padding(4.dp)
            .background(Color(0xFF1C1C1E), shape = MaterialTheme.shapes.medium)
            .padding(10.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF49FEDD), shape = MaterialTheme.shapes.small)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = epgDataItem.content?.channelNo?.toString() ?: "--",
                        style = MaterialTheme.typography.labelLarge.copy(color = Color.Black)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = epgDataItem.content?.title
                        ?: epgDataItem.displayName
                        ?: "Unknown Channel",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF49FEDD)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = currentProgram?.title ?: "No Info",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                maxLines = 1
            )
            Text(
                text = "${formatTime(currentProgram?.startTime)} - ${formatTime(currentProgram?.endTime)} • $minutesLeft MIN LEFT",
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Next at ${formatTime(nextProgram?.startTime)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                text = nextProgram?.title ?: "N/A",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

fun formatTime(timeMillis: Long?): String {
    return timeMillis?.let {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it)
    } ?: "--"
}





