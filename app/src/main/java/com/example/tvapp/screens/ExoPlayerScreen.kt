package com.example.tvapp.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.applicationscreens.models.Data
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.Log
import com.example.tvapp.viewmodels.ExoPlayerViewModel

@Composable
fun ExoPlayerScreen(viewModel: ExoPlayerViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val videoUrls by remember { viewModel.videoList }
    val isLoading by remember { viewModel.isLoading }

    Log.d("AVNI", "Video Titles --> ${viewModel.videoList.value.joinToString(", ") { it.title }}")


    var selectedVideo by remember { mutableStateOf<String?>(null) }

    if (isLoading) {
        // Show a loading indicator while the data is being fetched
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        if (selectedVideo == null) {
            // Show Video List
            VideoList(videoItems = videoUrls) { videoUrl ->
                selectedVideo = videoUrl
            }
        } else {
            // Show ExoPlayer in Full Screen
            ExoPlayerView(context, selectedVideo!!) {
                selectedVideo = null // Hide Player when clicked
            }
        }
    }
}

@Composable
fun VideoList(videoItems: List<Data>, onVideoSelected: (String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().background(Color.White)) {
        items(videoItems) { videoItem ->
            Text(
                text = videoItem.title,
                modifier = Modifier
                    .padding(16.dp)
                    .clickable { onVideoSelected(videoItem.videoUrl)},
                color = Color.Black
            )
        }
    }
}

@Composable
fun ExoPlayerView(context: Context, videoUrl: String, onExitPlayer: () -> Unit) {
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            playWhenReady = true
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .clickable { onExitPlayer() } // Exit full screen on click
    ) {
        AndroidView(
            factory = { PlayerView(context).apply { player = exoPlayer } },
            modifier = Modifier.fillMaxSize()
        )
    }
}
