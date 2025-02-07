package com.example.tvapp.screens

import android.content.Context
import androidx.annotation.OptIn
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.tvapp.viewmodels.ExoPlayerViewModel
import com.example.applicationscreens.models.Data

import androidx.activity.compose.BackHandler
import androidx.navigation.NavController

@OptIn(UnstableApi::class)
@Composable
fun ExoPlayerScreen(navController: NavController, startIndex: Int, viewModel: ExoPlayerViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val videoList by remember { viewModel.videoList }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItems = videoList.map { MediaItem.fromUri(it.videoUrl) }
            setMediaItems(mediaItems, startIndex, 0L)
            prepare()
            playWhenReady = true
        }
    }

    // Handle back press to go back to list
    BackHandler {
        navController.popBackStack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                    setShowNextButton(true)
                    setShowPreviousButton(true)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
