package com.example.tvapp.view.panmetro

import android.app.Activity
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.tvapp.R
import com.example.tvapp.extensions.destinationExists
import com.example.tvapp.extensions.isNotNullOrEmpty
import com.example.tvapp.view.navigationhelper.Destination
import com.example.tvapp.view.player.addWatermarkToPlayer
import com.example.tvapp.viewmodels.SharedViewModel
import com.example.tvapp.viewmodels.WTVPlayerViewModel
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun PanMetroVideoPlayer(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    wtvPlayerViewModel:WTVPlayerViewModel= hiltViewModel()
) {
    val context = LocalContext.current
    val epgList by sharedViewModel.filteredEPGList.collectAsState()
    val selectedChannel by sharedViewModel.selectedChannel.collectAsState()

    var currentIndex by remember {
        mutableIntStateOf(epgList.indexOfFirst {
            it.content?.videoUrl == (selectedChannel.content?.videoUrl ?: "")
        })
    }

    // Mutable state for UI updates
    // Set FLAG_SECURE if desired.
    (context as? Activity)?.window?.setFlags(
        WindowManager.LayoutParams.FLAG_SECURE,
        WindowManager.LayoutParams.FLAG_SECURE
    )

    var isOverlayVisible by remember { mutableStateOf(true) }
    var isProgramOverlayVisible by remember { mutableStateOf(true) }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()

    // ExoPlayer Setup
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(wtvPlayerViewModel.provideMediaSourceFactory(context=context)).build().apply {
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

    // Update video when channel changes
    LaunchedEffect(selectedChannel) {
        if (selectedChannel.content?.videoUrl.isNotNullOrEmpty()) {
            Log.d("ExoPlayer", "Switching to video: $selectedChannel.content?.videoUrl")
            // Stop and clear previous media to avoid issues
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
           // wtvPlayerViewModel.provideMediaSourceFactory(contentUrl =selectedChannel.content?.videoUrl?:"",context=context )
            // Create a MediaItem from your video URL.
            val mediaItem = MediaItem.fromUri(selectedChannel.content?.videoUrl?:"")

            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true  //  Ensure playback starts automatically
            // Add a listener to update the buffering state
        }
    }
    // Auto-hide overlay after 5 sec
    LaunchedEffect(isOverlayVisible) {
        if (isOverlayVisible) {
            delay(8000)
            isOverlayVisible = false
        }
    }

    fun playNextChannel() {
        if (currentIndex < epgList.lastIndex) {
            currentIndex++
            sharedViewModel.updateSelectedChannel(epgList[currentIndex])
        }
    }

    fun playPreviousChannel() {
        if (currentIndex > 0) {
            currentIndex--
            sharedViewModel.updateSelectedChannel(epgList[currentIndex])
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
                        KeyEvent.KEYCODE_BACK -> {
                            if (navController.previousBackStackEntry == null) {
                                navController.navigate(Destination.homeScreen) {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            } else {
                                navController.popBackStack()
                            }
                            true
                        }

                        KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_CHANNEL_UP -> {
                            navController.navigate(Destination.epgScreen) {
                                popUpTo(Destination.panMetroScreen)// { inclusive = true }
                            }
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            navController.navigate(Destination.genreScreen) {
                                popUpTo(Destination.panMetroScreen) { inclusive = true }
                            }
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_CENTER -> {
                            if(isOverlayVisible){
                                isOverlayVisible= false
                            }else {
                                isOverlayVisible = true
                            }
                            true
                        }

                        KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_CHANNEL_DOWN -> {
                            playPreviousChannel()
                            isOverlayVisible = true
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_CHANNEL_UP -> {
                            playNextChannel()
                            isOverlayVisible = true
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

                    // addLogoToPlayer(this)
                }

                view

            }
        )
        // Overlay logo in the top-right corner with fixed width and height.
        Image(
            painter = painterResource(id = R.drawable.panmetro_logo_t),
            contentDescription = "Panmetro Logo",
            modifier = Modifier
                .size(width = 200.dp, height = 150.dp)
                .align(Alignment.TopEnd)
                .padding(16.dp) // optional padding from the top/right edges
        )

        // Show Loading Indicator if Buffering
        /*Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isPlaying) {
                CircularProgressIndicator()
            }
        }*/

        DisposableEffect(Unit) {
            onDispose {
                exoPlayer.release()
            }
        }

        if (isOverlayVisible) {
            PlayerOverlay(
                navController= navController,
                dataItem = selectedChannel,
                onDismiss = { isProgramOverlayVisible = false }
            )
        }
    }
}



