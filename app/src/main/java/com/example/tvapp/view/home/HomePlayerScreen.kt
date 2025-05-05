package com.example.tvapp.view.home

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.example.tvapp.model.data.epgdata.EPGDataItem
import com.example.tvapp.view.uicomponent.addWatermarkToPlayer
import com.example.tvapp.viewmodels.WTVPlayerViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("UnrememberedMutableState")
@OptIn(UnstableApi::class)
@Composable
fun HomePlayerScreen(
    initialVideoUrl: String,
    allChannels: List<EPGDataItem>,
    onBack: () -> Unit,
    onVideoChange: (String) -> Unit,
    wtvPlayerViewModel: WTVPlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isBuffering = remember { mutableStateOf(false) }

    // Ensure secure flag if needed.
    (context as? Activity)?.window?.setFlags(
        WindowManager.LayoutParams.FLAG_SECURE,
        WindowManager.LayoutParams.FLAG_SECURE
    )

    // Debug: Log the size of allChannels.
    Log.d("HomePlayerScreen", "allChannels.size = ${allChannels.size}")

    // Initialize currentIndex.
    var currentIndex by remember {
        mutableStateOf(
            allChannels.indexOfFirst {
                it.content?.videoUrl?.trim()?.lowercase() == initialVideoUrl.trim().lowercase()
            }
        )
    }
    if (currentIndex < 0 && allChannels.isNotEmpty()) {
        currentIndex = 0
    }

    // Use a separate state for the overlayChannel.
    var overlayChannel by remember { mutableStateOf<EPGDataItem?>(if (allChannels.isNotEmpty()) allChannels[currentIndex] else null) }

    // When currentIndex changes, update overlayChannel.
    LaunchedEffect(currentIndex) {
        val newChannel = allChannels.getOrNull(currentIndex)
        Log.d("HomePlayerScreen", "Updating overlayChannel: newChannel = ${newChannel?.content?.title}")
        overlayChannel = newChannel ?: allChannels.firstOrNull() // fallback to first channel if available
    }

    // Update currentIndex when initialVideoUrl changes, but only once.
    LaunchedEffect(initialVideoUrl) {
        delay(300) // small debounce delay
        val newIndex = allChannels.indexOfFirst { channel ->
            channel.content?.videoUrl?.trim()?.lowercase() == initialVideoUrl.trim().lowercase()
        }
        Log.d("HomePlayerScreen", "Incoming URL: $initialVideoUrl, Found index: $newIndex")
        if (newIndex != -1 && newIndex != currentIndex) {
            currentIndex = newIndex
        }
    }

    // ExoPlayer setup.
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(wtvPlayerViewModel.provideMediaSourceFactory(context = context))
            .build().apply {
                playWhenReady = true
                addAnalyticsListener(object : AnalyticsListener {
                    override fun onEvents(player: Player, events: AnalyticsListener.Events) {
                        if (events.contains(AnalyticsListener.EVENT_DRM_KEYS_LOADED)) {
                            Log.d("DRM", "Keys loaded successfully")
                        }
                        if (events.contains(AnalyticsListener.EVENT_DRM_SESSION_MANAGER_ERROR)) {
                            Log.e("DRM", "Session manager error")
                        }
                    }
                })
            }
    }

    // Update video when currentIndex changes.
    LaunchedEffect(currentIndex) {
        if (currentIndex in allChannels.indices) {
            val newVideoUrl = allChannels[currentIndex].content?.videoUrl ?: ""
            Log.d("HomePlayerScreen", "Switching to video: $newVideoUrl")
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            val mediaItem = MediaItem.fromUri(newVideoUrl)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    // Channel navigation functions.
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

    // Overlay visibility state.
    var isOverlayVisible by remember { mutableStateOf(true) }
    val overlayJob = remember { mutableStateOf<Job?>(null) }
    fun showOverlay() {
        isOverlayVisible = true
        overlayJob.value?.cancel()
        overlayJob.value = coroutineScope.launch {
            delay(5000)
            isOverlayVisible = false
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    // Main UI layout.
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
                        KeyEvent.KEYCODE_BACK -> {
                            onBack()
                            true
                        }
                        else -> false
                    }
                } else false
            }
    ) {
        // Player view.
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

        // Optional loading indicator.
        if (isBuffering.value) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        // Custom overlay using the stable overlayChannel.
        if (isOverlayVisible && overlayChannel != null) {
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
                        model = overlayChannel!!.content?.thumbnailUrl,
                        contentDescription = "Channel Logo",
                        modifier = Modifier.size(80.dp)
                    )
                    Text(
                        text = overlayChannel!!.content?.title ?: "Unknown Channel",
                        color = Color.White,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
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
        }
    }
}
