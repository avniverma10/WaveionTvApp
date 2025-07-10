package com.example.tvapp.view.panmetro.player.amin

import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import coil3.gif.GifDecoder
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.android.caastv.R

/**
 * A Compose UI that displays an animated GIF alongside an audio player
 * with a visualizer. Use gifUrl for the GIF source, and audioUrl for the audio track.
 */
@Composable
fun GifAudioPlayer(
    audioUrl: String,
    gifUrl: String? = null,
    gifResId: Int? = null,
    gifAssetPath: String? = null,
    modifier: Modifier = Modifier,
    onVisualizerReady: (Visualizer) -> Unit = {},
    onGifLoadingFailed: (Throwable) -> Unit = {}
) {
    val context = LocalContext.current
    // Initialize MediaPlayer
    val mediaPlayer = remember {
        MediaPlayer().apply {
            setDataSource(context, Uri.parse(audioUrl))
            prepare()
        }
    }

    val gifPainter = when {
        gifUrl != null -> rememberAsyncImagePainter(
            model = ImageRequest.Builder(context)
                .data(gifUrl)
                .decoderFactory(GifDecoder.Factory())
                .listener(
                    onError = { _, result ->
                        onGifLoadingFailed(result.throwable)
                    }
                )
                .build(),
            placeholder = painterResource(com.android.caastv.R.drawable.caastv_icon_foreground),
            error = painterResource(R.drawable.caastv_icon_foreground)
        )

        gifResId != null -> painterResource(id = gifResId)

        gifAssetPath != null -> rememberAsyncImagePainter(
            model = ImageRequest.Builder(context)
                .data("file:///android_asset/$gifAssetPath")
                .decoderFactory(GifDecoder.Factory())
                .listener(
                    onError = { _, result ->
                        onGifLoadingFailed(result.throwable)
                    }
                )
                .build()
        )

        else -> null
    }

    var isPlaying by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated GIF
        // GIF Background
        gifPainter?.let { painter ->
            Image(
                painter = painter,
                contentDescription = "Audio visualization background",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
        /*AsyncImage(
            model = ImageRequest.Builder(context)
                .data(gifUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Animated GIF",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )*/

        Spacer(modifier = Modifier.height(16.dp))

        // Audio visualizer using your existing BarVisualizer
        AndroidView(
            factory = { ctx ->
                BarVisualizer(ctx).apply {
                    // Link the visualizer to the MediaPlayer's audio session
                    setAudioSessionId(mediaPlayer.audioSessionId)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Playback controls
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (isPlaying) mediaPlayer.pause() else mediaPlayer.start()
                isPlaying = !isPlaying
            }) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Person else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause audio" else "Play audio"
                )
            }
            Text(text = if (isPlaying) "Playing" else "Paused")
        }
    }

    // Release MediaPlayer when no longer in composition
    DisposableEffect(mediaPlayer) {
        onDispose {
            mediaPlayer.release()
        }
    }
}
