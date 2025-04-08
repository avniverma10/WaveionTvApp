package com.example.tvapp.view.panmetro

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.tvapp.model.data.epgdata.EPGDataItem
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PanMetroNewOverlay(
    currentChannel: EPGDataItem?,
    epgList: List<EPGDataItem>
) {
    val listState = rememberLazyListState()

    val currentChannelIndex = currentChannel?.let { channel ->
        epgList.indexOfFirst { it.channelId == channel.channelId }
    } ?: -1

    val focusRequesters = remember { epgList.associate { it.channelId to FocusRequester() } }

    LaunchedEffect(currentChannelIndex) {
        if (currentChannelIndex >= 0) {
            listState.animateScrollToItem(currentChannelIndex)
        }
    }

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
                .background(brush = topBarGradient)
                .padding(horizontal = 24.dp, vertical = 13.dp)
        ) {
            TopOverlayInfo(dataItem = currentChannel)
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Transparent)
                .padding(horizontal = 24.dp, vertical = 13.dp)
        ) {
            LazyRow(
                state = listState,
                modifier = Modifier.focusTarget()
            ) {
                itemsIndexed(
                    items = epgList,
                    key = { index, item -> item.channelId?: index  } // Now using channelId as the key
                ) { index, item ->
                    ChannelCard(
                        epgDataItem = item,
                        modifier = Modifier
                            .focusRequester(focusRequesters[item.channelId]!!)
                    )
                    LaunchedEffect(currentChannelIndex) {
                        if (index == currentChannelIndex) {
                            focusRequesters[item.channelId]?.requestFocus()
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun ChannelCard(
    epgDataItem: EPGDataItem,
    modifier: Modifier = Modifier
) {
    val now = System.currentTimeMillis()
    val programList = epgDataItem.currentPrograms ?: epgDataItem.tv?.programme

    val currentProgram = programList
        ?.sortedBy { it.startTime }
        ?.firstOrNull { (it.startTime ?: 0) <= now && (it.endTime ?: 0) > now }
    val nextProgram = programList
        ?.firstOrNull { (it.startTime ?: 0) > now }

    val minutesLeft = currentProgram?.endTime?.let { ((it - now) / 60000).toInt() } ?: 0

    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .width(260.dp)
            .height(160.dp)
            .padding(4.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable() // Make the card focusable
            .border(
                width = if (isFocused) 2.5.dp else 0.dp,
                color = if (isFocused) Color(0xFF49FEDD) else Color.Transparent,
                shape = MaterialTheme.shapes.medium
            )
            .background(Color(0xFF1C1C1E), shape = MaterialTheme.shapes.medium)
            .padding(12.dp)
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





