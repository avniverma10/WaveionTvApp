package com.example.tvapp.view.playeroverlay

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.android.caastv.R
import com.example.tvapp.extensions.formatTime
import com.example.tvapp.extensions.provideProgramTime
import com.example.tvapp.model.data.epgdata.EPGDataItem
import com.example.tvapp.view.panmetro.common.TopOverlayInfo
import com.example.tvapp.view.uicomponent.keyboard.HideKeyboardOnEnter
import com.example.tvapp.viewmodels.SharedViewModel
import com.example.tvapp.viewmodels.player.PlayerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun FullScreenPlayerOverlay(
    selectedIndex: MutableState<Int>,
    lazyListState: LazyListState,
    sharedViewModel: SharedViewModel,
    playerViewModel: PlayerViewModel,
    epgList: List<EPGDataItem>,
    categoryName: String,
    languageName: String,
    channelFocusRequesters: List<FocusRequester>,
    onChannelFocused: (EPGDataItem) -> Unit
) {
    HideKeyboardOnEnter()
    val selectedChannel by sharedViewModel.selectedChannel.collectAsState()
    val scope = rememberCoroutineScope()

    val timestamp by playerViewModel.timestampFlow()
        .collectAsState(initial = System.currentTimeMillis())

    val currentTimeStamp: MutableState<Long> = remember { mutableStateOf( timestamp) }


    // Constants for animations
    val scaleDownBy = 0.85f
    val maxScale = 1f
    val minScale = 0.7f

    LaunchedEffect(selectedChannel) {
        val idx = epgList.indexOfFirst {
            it.content?.videoUrl == selectedChannel.content?.videoUrl
        }.coerceAtLeast(0)
        selectedIndex.value = idx
        playerViewModel.updateSelectedPProgramInfo(selectedChannel)
        lazyListState.scrollToItem(idx)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Top gradient overlay
        val topBarGradient = Brush.verticalGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.6f),
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

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(topBarGradient)
                .padding(horizontal = 24.dp, vertical = 13.dp)
        ) {
            TopOverlayInfo(sharedViewModel= sharedViewModel,playerViewModel = playerViewModel)
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 16.dp)
                .focusTarget()
        ) {
            Text(
                text = if (languageName == "All Languages")
                    "You are watching – $categoryName"
                else
                    "You are watching – $categoryName • $languageName",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily(Font(R.font.figtree_medium)),
                    fontStyle  = FontStyle.Italic,
                    fontSize = 18.sp      // bump up the size
                ),
                color = Color.White,
                modifier = Modifier
                    .background(
                        color = Color.Black.copy(alpha = 0.3f),  // lighter overlay
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
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
                        playerViewModel = playerViewModel,
                        epgDataItem = item,
                        timestamp =  currentTimeStamp,
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
                            .focusRequester(channelFocusRequesters.getOrNull(index) ?: FocusRequester())
                            .focusable()
                    )
                }
            }
        }
    }
}



@Composable
private fun ChannelCard(
    playerViewModel: PlayerViewModel,
    epgDataItem: EPGDataItem,
    timestamp: MutableState<Long>,
    isFocused: Boolean,
    scale: Float,
    modifier: Modifier
) {
    val now = System.currentTimeMillis()
    val programList = epgDataItem.tv?.programme?.let {
        playerViewModel.provideAvailablePrograms(
            it
        )
    }

    var programIndex = remember { mutableIntStateOf(0) }

    var currentProgram = remember(programIndex) {
        var program = programList?.getOrNull(programIndex.intValue)
        program
    }

    var nextProgram = remember(programIndex) {
        val nextIndex = programIndex.intValue+1
        var program = programList?.getOrNull(nextIndex)
        program
    }

    // 2) Format it once per emission
    val timeLeft = remember(timestamp) {
        val diff = programList?.getOrNull(programIndex.intValue)?.endTime?.minus(timestamp.value) ?: 0
        if( diff > 0){
            val timeLeft = diff.div(60000).toInt()
            if(timeLeft == 0){
                1
            }else{
                timeLeft
            }
        }else{
            ++programIndex.intValue
            (programList?.getOrNull(programIndex.intValue)?.endTime?.minus(timestamp.value)?.div(60000))?.toInt()?:0
        }
    }

    /*LaunchedEffect(programList) {
        currentProgram = programList?.getOrNull(0)
        nextProgram = programList?.getOrNull(1)
        currentProgram?.let { playerViewModel.updateCurrentRunningProgramTimings(it) }
    }
*/

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
            .padding(10.dp)
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
                        .align(Alignment.CenterVertically)
                ) {
                    Text(
                        text = epgDataItem.content?.channelNo?.toString() ?: "--",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Black
                    )
                }
                // Channel Logo
                AsyncImage(
                    model = epgDataItem.content?.thumbnailUrl,
                    contentDescription = "Channel Logo",
                    modifier = Modifier
                        .background(Color.Transparent, RoundedCornerShape(4.dp))
                        .width(50.dp)
                        .height(50.dp)
                        .padding(start = 5.dp)
                )
                /*Text(
                    text = epgDataItem.content?.title
                        ?: epgDataItem.displayName
                        ?: "Unknown Channel",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF49FEDD)
                )*/
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = currentProgram?.title ?: "No Info",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                maxLines = 1
            )

            Text(
                text = buildString {
                    append(currentProgram?.startTime?.formatTime())
                    append(" - ")
                    append(currentProgram?.endTime?.formatTime())
                    append(" • ")
                    append(timeLeft)
                    append(" MIN LEFT")
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Next at ${nextProgram?.startTime?.formatTime()}",
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

private fun formatTime(timeMillis: Long?): String {
    return timeMillis?.let {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it)
    } ?: "--"
}