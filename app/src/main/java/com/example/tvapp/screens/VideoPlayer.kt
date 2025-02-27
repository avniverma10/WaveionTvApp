package com.example.tvapp.screens

import android.view.LayoutInflater
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.tvapp.R

@Composable
fun VideoPlayer(videoUrl: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    // Remember the ExoPlayer instance
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            playWhenReady = true
        }
    }
    // Dispose the player when the composable leaves the composition
    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val view = LayoutInflater.from(ctx).inflate(R.layout.exoplayer_view, null)
                val playerView = view.findViewById<androidx.media3.ui.PlayerView>(R.id.player_view)
                playerView.player = exoPlayer
                playerView.useController = true
                // Prevent TV from sleeping
                playerView.keepScreenOn = true
                view
//                PlayerView(ctx).apply {
//                    player = exoPlayer
//                    useController = true
//                    setShowNextButton(true)  // Enable Next button
//                    setShowPreviousButton(true) // Enable Previous button
//                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
