package com.example.tvapp.view.home


import android.net.Uri
import android.view.LayoutInflater
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.compose.ui.viewinterop.AndroidView
import com.example.tvapp.R

import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun HomePlayer(navController: NavController, videoUrl: String) {
    val context = LocalContext.current

    // Decode the URL before passing it to ExoPlayer
    val decodedUrl = URLDecoder.decode(videoUrl, StandardCharsets.UTF_8.toString())

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(decodedUrl)))
            prepare()
            playWhenReady = true
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AndroidView(
            factory = { ctx ->
                val view = LayoutInflater.from(ctx).inflate(R.layout.exoplayer_view, null)
                val playerView = view.findViewById<androidx.media3.ui.PlayerView>(R.id.player_view)

                playerView.player = exoPlayer
                playerView.useController = true
                playerView.keepScreenOn = true  // Prevent TV from sleeping

                view
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
}

