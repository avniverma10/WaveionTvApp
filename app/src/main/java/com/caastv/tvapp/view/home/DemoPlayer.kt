package com.caastv.tvapp.view.home

import android.content.Context
import android.net.Uri
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView


@Composable
private fun rememberExoPlayer(context: Context, mediaUri: Uri): ExoPlayer {
    return remember {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(mediaUri))
                prepare()
                playWhenReady = true
            }
    }
}

@Composable
fun DemoPlayerScreen(
    url: String,
    onBack: () -> Unit
) {
    // grab the Context from Compose
    val context = LocalContext.current

    // build & remember the ExoPlayer
    val exoPlayer = rememberExoPlayer(context, Uri.parse(url))

    // ensure we release it when this composable leaves the tree
    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // host the native PlayerView
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
