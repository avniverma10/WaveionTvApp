package com.example.tvapp.screens

import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import com.example.tvapp.R
import com.example.tvapp.models.EPGChannel
import com.example.tvapp.viewmodels.EPGViewModel

@Composable
fun VideoPlayer(
    initialVideoUrl: String,
    allChannels: List<EPGChannel>,
    epgViewModel: EPGViewModel,
    onVideoChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentIndex by remember { mutableStateOf(allChannels.indexOfFirst { it.videoUrl == initialVideoUrl }) }
    val currentChannel = allChannels.getOrNull(currentIndex)

    var isOverlayVisible by remember { mutableStateOf(false) } // Track overlay visibility
    val nextProgram by epgViewModel.nextProgram.collectAsState()

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    // Auto-hide overlay after 5 seconds when it appears
    LaunchedEffect(isOverlayVisible) {
        if (isOverlayVisible) {
            kotlinx.coroutines.delay(5000) // 5 seconds delay
            isOverlayVisible = false
        }
    }

    // Whenever `currentIndex` changes, update the video URL and start playback
    LaunchedEffect(currentIndex) {
        if (currentIndex in allChannels.indices) {
            val newVideoUrl = allChannels[currentIndex].videoUrl ?: ""
            Log.d("AVNI99", "Playing video: $newVideoUrl at index: $currentIndex")
            exoPlayer.setMediaItem(MediaItem.fromUri(newVideoUrl))
            exoPlayer.prepare()
        }
    }

    // Fetch Next Program when Channel Changes
    LaunchedEffect(currentChannel) {
        currentChannel?.id?.let { channelId ->
            val currentTime = System.currentTimeMillis().toString()
            epgViewModel.fetchNextProgram(channelId, currentTime)
        }
    }
    fun playNextChannel() {
        if (currentIndex < allChannels.lastIndex) {
            currentIndex++
            onVideoChange(allChannels[currentIndex].videoUrl ?: "")
        }
    }

    fun showOverlay() {
        isOverlayVisible = true
    }

    fun playPreviousChannel() {
        if (currentIndex > 0) {
            currentIndex--
            onVideoChange(allChannels[currentIndex].videoUrl ?: "")
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusable()
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            playNextChannel()
                            showOverlay()
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            playPreviousChannel()
                            showOverlay()
                            true
                        }
                        KeyEvent.KEYCODE_CHANNEL_UP -> {
                            playNextChannel()
                            showOverlay()
                            true
                        }
                        KeyEvent.KEYCODE_CHANNEL_DOWN -> {
                            playPreviousChannel()
                            showOverlay()
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_DOWN -> {
                            showOverlay()
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_UP -> {
                            isOverlayVisible = false
                            true
                        }
                        else -> false
                    }
                } else false
            }
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                    keepScreenOn = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isOverlayVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)) // Semi-transparent background
            ) {
                // Channel Info at Bottom Left
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(16.dp)
                        .align(Alignment.BottomStart) // Stick to bottom-left
                ) {
                    androidx.compose.foundation.layout.Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Channel Logo
                        AsyncImage(
                            model = currentChannel?.logoUrl,
                            contentDescription = "Channel Logo",
                            modifier = Modifier
                                .size(80.dp)
                                .background(Color.Gray)
                        )

                        // Channel Name
                        Text(
                            text = currentChannel?.name ?: "Unknown Channel",
                            color = Color.White,
                            fontSize = 20.sp,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .weight(1f) // Ensures text takes remaining space
                        )
                    }
                }

                // Next Program Info at Bottom Right
                nextProgram?.let {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd) // Correct alignment
                            .background(Color.Black.copy(alpha = 0.9f))
                            .padding(16.dp)
                    ) {
                        androidx.compose.foundation.layout.Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Next: ${it.eventName}",
                                color = Color.White,
                                fontSize = 18.sp
                            )
//                            Text(
//                                text = "${it.startTime} - ${it.endTime}",
//                                color = Color.Gray,
//                                fontSize = 14.sp,
//                                modifier = Modifier.padding(top = 4.dp)
//                            )
                        }
                    }
                }
            }
        }


    }
}
