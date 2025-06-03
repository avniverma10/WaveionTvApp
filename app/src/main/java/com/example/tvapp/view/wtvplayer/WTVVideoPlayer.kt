package com.example.tvapp.view.wtvplayer

import android.app.Activity
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import com.android.caastv.R
import com.example.tvapp.extensions.loge
import com.example.tvapp.model.data.epgdata.EPGDataItem
import com.example.tvapp.view.uicomponent.addWatermarkToPlayer
import com.example.tvapp.viewmodels.WTVPlayerViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun WTVVideoPlayer(
    initialVideoUrl: String,
    allChannels: List<EPGDataItem>,
    onVideoChange: (String) -> Unit,
    wtvPlayerViewModel:WTVPlayerViewModel= hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Mutable state for UI updates
    val isBuffering = remember { mutableStateOf(false) }
    val isPlaying = remember { mutableStateOf(true) }
    val currentPlaybackState = remember { mutableStateOf(Player.STATE_IDLE) }

    // Set FLAG_SECURE if desired.
    (context as? Activity)?.window?.setFlags(
        WindowManager.LayoutParams.FLAG_SECURE,
        WindowManager.LayoutParams.FLAG_SECURE
    )

    var currentIndex by remember { mutableStateOf(allChannels.indexOfFirst { it.content?.videoUrl == initialVideoUrl }) }
    val currentChannel = allChannels.getOrNull(currentIndex)
    var isOverlayVisible by remember { mutableStateOf(true) }

    val overlayJob = remember { mutableStateOf<Job?>(null) }


    // ExoPlayer Setup
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(wtvPlayerViewModel.provideMediaSourceFactory(context=context)).build().apply {
            playWhenReady = true
            addAnalyticsListener(object : AnalyticsListener {
                override fun onEvents(player: Player, events: AnalyticsListener.Events) {
                    if (events.contains(AnalyticsListener.EVENT_DRM_KEYS_LOADED)) {
                        loge("DRM", "Keys loaded successfully")
                    }
                    if (events.contains(AnalyticsListener.EVENT_DRM_SESSION_MANAGER_ERROR)) {
                        loge("DRM", "Session manager error")
                    }
                }
            })
        }
    }

    // Update video when channel changes
    LaunchedEffect(currentIndex) {
        if (currentIndex in allChannels.indices) {
            val newVideoUrl = allChannels[currentIndex].content?.videoUrl ?: ""
            Log.d("ExoPlayer", "Switching to video: $newVideoUrl")
            // Stop and clear previous media to avoid issues
            exoPlayer.stop()
            exoPlayer.clearMediaItems()

            // Create a MediaItem from your video URL.
            val mediaItem = MediaItem.fromUri(newVideoUrl)

            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true  //  Ensure playback starts automatically
        }
    }

    // Fetch next program info
    LaunchedEffect(currentChannel) {
        currentChannel?.channelId?.let { channelId ->
            val currentTime = System.currentTimeMillis().toString()
           // epgViewModel.fetchNextProgram(channelId, currentTime)
        }
    }

    fun playNextChannel() {
        if (currentIndex < allChannels.lastIndex) {
            currentIndex++
            onVideoChange(allChannels[currentIndex].content?.videoUrl ?: "")
        }
    }

    fun playPreviousChannel() {
        if (currentIndex > 0) {
            currentIndex--
            onVideoChange(allChannels[currentIndex].content?.videoUrl ?: "")
        }
    }

    fun showOverlay() {
        isOverlayVisible = true

        // Cancel any existing job
        overlayJob.value?.cancel()

        // Start a new 5s delay
        overlayJob.value = coroutineScope.launch {
            delay(5000)
            isOverlayVisible = false
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusable()
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_CHANNEL_UP -> {
                            playNextChannel()
                            showOverlay()
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_CHANNEL_DOWN -> {
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
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val view = LayoutInflater.from(ctx).inflate(R.layout.exoplayer_view, null)
                val playerView = view.findViewById<PlayerView>(R.id.player_view)

                playerView.apply {
                    player = exoPlayer
                    useController = false
                    keepScreenOn = true
                    addWatermarkToPlayer(this, wtvPlayerViewModel.provideWatermarkHash(context))
                }

                view

            }
        )

        // Show Loading Indicator if Buffering
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isBuffering.value) {
                CircularProgressIndicator()
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                exoPlayer.release()
            }
        }

        if (isOverlayVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(16.dp)
                        .align(Alignment.BottomStart)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AsyncImage(
                            model = currentChannel?.content?.thumbnailUrl,
                            contentDescription = "Channel Logo",
                            modifier = Modifier.size(80.dp)
                        )
                        Text(
                            text = currentChannel?.content?.title ?: "Unknown Channel",
                            color = Color.White,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                        // Spacer between title and LIVE
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))

                        // Live badge
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color.Red, shape = androidx.compose.foundation.shape.CircleShape)
                            )
                            Text(
                                text = " LIVE",
                                color = Color.Red,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                /*nextProgram?.let {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .background(Color.Black.copy(alpha = 0.9f))
                            .padding(16.dp)
                    ) {
                        Text(text = "Next: ${it.title}", color = Color.White, fontSize = 18.sp)
                    }
                }*/
        }
    }
}


