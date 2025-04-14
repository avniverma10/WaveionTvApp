package com.example.tvapp.view.playeroverlay

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.tvapp.model.data.epgdata.EPGDataItem
import com.example.tvapp.viewmodels.SharedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun NewPlayerOverlay(
    selectedIndex: MutableState<Int>,
    lazyListState: LazyListState,
    sharedViewModel: SharedViewModel,
    channelFocusRequesters: List<FocusRequester>,
    onChannelFocused: (EPGDataItem) -> Unit
) {
    val epgList by sharedViewModel.wtvEPGList.collectAsState()
    val selectedChannel by sharedViewModel.selectedChannel.collectAsState()
    val scope = rememberCoroutineScope()

    // Constants for animations
    val scaleDownBy = 0.85f
    val maxScale = 1f
    val minScale = 0.7f

    LaunchedEffect(epgList, selectedIndex) {
        selectedIndex.value = epgList.indexOfFirst { it.content?.videoUrl == selectedChannel.content?.videoUrl }
            .takeIf { it >= 0 } ?: 0
        scope.launch {
            delay(200)
            lazyListState.animateScrollToItem(selectedIndex.value)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Top gradient overlay
        val topBarGradient = Brush.verticalGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.8f),
                Color.Transparent
            )
        )
        
        // Bottom gradient overlay
        val bottomBarGradient = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color.Black.copy(alpha = 0.8f)
            )
        )

        // Channel carousel at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(bottomBarGradient)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            LazyRow(
                state = lazyListState,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(
                    items = epgList,
                    key = { index, item -> item.channelId ?: index }
                ) { index, item ->
                    val isFocused = remember { mutableStateOf(false) }
                    val isSelected = selectedIndex.value == index
                    
                    // Calculate distance from center for scaling
                    val itemOffset = index - selectedIndex.value
                    val scale = when {
                        isSelected -> maxScale
                        else -> {
                            val scaleFactor = 1f - (kotlin.math.abs(itemOffset) * 0.15f)
                            scaleFactor.coerceIn(minScale, maxScale)
                        }
                    }
                    
                    val animatedScale by animateFloatAsState(
                        targetValue = scale,
                        label = "scale"
                    )

                    ChannelCard(
                        sharedViewModel = sharedViewModel,
                        epgDataItem = item,
                        isFocused = isSelected,
                        scale = animatedScale,
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = animatedScale
                                scaleY = animatedScale
                                alpha = scale
                            }
                            .onFocusChanged {
                                isFocused.value = it.isFocused
                                if (it.isFocused) {
                                    selectedIndex.value = index
                                    onChannelFocused(item)
                                }
                            }
                            .focusRequester(channelFocusRequesters[index])
                            .focusable()
                    )
                }
            }
        }
    }
}

@Composable
private fun ChannelCard(
    sharedViewModel: SharedViewModel,
    epgDataItem: EPGDataItem,
    isFocused: Boolean,
    scale: Float,
    modifier: Modifier
) {
    val now = System.currentTimeMillis()
    val programList = epgDataItem.tv?.programme?.let { 
        sharedViewModel.provideVideoPlayerProgramInfo(it) 
    }
    val minutesLeft = programList?.getOrNull(0)?.endTime?.let { 
        ((it - now) / 60000).toInt() 
    } ?: 0

    Box(
        modifier = modifier
            .width(260.dp)
            .height(160.dp)
            .background(
                color = if (isFocused) Color(0xFF2C2C2E) else Color(0xFF1C1C1E),
                shape = RoundedCornerShape(12.dp)
            )
            .then(
                if (isFocused) {
                    Modifier.border(
                        width = 2.dp,
                        color = Color(0xFF49FEDD),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else Modifier
            )
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF49FEDD), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = epgDataItem.content?.channelNo?.toString() ?: "--",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = epgDataItem.content?.title 
                        ?: epgDataItem.displayName 
                        ?: "Unknown Channel",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF49FEDD)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = programList?.getOrNull(0)?.title ?: "No Info",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                maxLines = 1
            )
            
            Text(
                text = buildString {
                    append(formatTime(programList?.getOrNull(0)?.startTime))
                    append(" - ")
                    append(formatTime(programList?.getOrNull(0)?.endTime))
                    append(" • ")
                    append(minutesLeft)
                    append(" MIN LEFT")
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Next at ${formatTime(programList?.getOrNull(1)?.startTime)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            
            Text(
                text = programList?.getOrNull(1)?.title ?: "N/A",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

private fun formatTime(timeMillis: Long?): String {
    return timeMillis?.let {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it)
    } ?: "--"
}