package com.example.tvapp.view.panmetro

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.ui.PlayerView
import com.example.tvapp.R
import com.example.tvapp.extensions.provideCryptoGuardMediaSource
import com.example.tvapp.viewmodels.SharedViewModel
import com.example.tvapp.viewmodels.WTVPlayerViewModel

@OptIn(UnstableApi::class)
@Composable
fun GenreMultiDRMPlayer(
    sharedViewModel: SharedViewModel,
    wtvPlayerViewModel:WTVPlayerViewModel= hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val selectedVideoUrl by sharedViewModel.selectedChannel.collectAsState()

    // Mutable state for UI updates
    val isBuffering = remember { mutableStateOf(false) }

    // Set FLAG_SECURE if desired.
    (context as? Activity)?.window?.setFlags(
        WindowManager.LayoutParams.FLAG_SECURE,
        WindowManager.LayoutParams.FLAG_SECURE
    )


    // Remember the player and recreate it when the DRM type changes
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .build()
            .apply {
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

    // Whenever the selected channel changes, load its media
    LaunchedEffect(selectedVideoUrl) {
        selectedVideoUrl.content?.videoUrl?.takeIf { it.isNotEmpty() }?.let { url ->
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            val mediaItem = if (selectedVideoUrl?.content?.drmType.equals("cryptoguard", ignoreCase = true)) {
                context.provideCryptoGuardMediaSource(contentUrl = selectedVideoUrl.content?.videoUrl, contentId = selectedVideoUrl.content?.assetId)
            } else {
                MediaItem.fromUri(url)
            }
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true  //  Ensure playback starts automatically
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
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

    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.run {
                stop()
                release()
            }
        }
    }
}


