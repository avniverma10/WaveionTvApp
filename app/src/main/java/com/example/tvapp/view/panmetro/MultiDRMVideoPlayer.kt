package com.example.tvapp.view.panmetro

import android.app.Activity
import android.media.MediaDrm
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.example.tvapp.R
import com.example.tvapp.extensions.logReport
import com.example.tvapp.extensions.provideCryptoGuardMediaSource
import com.example.tvapp.extensions.provideSigmaSourceFactory
import com.example.tvapp.view.navigationhelper.Destination
import com.example.tvapp.view.player.addWatermarkToPlayer
import com.example.tvapp.viewmodels.SharedViewModel
import com.example.tvapp.viewmodels.WTVPlayerViewModel
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun MultiDRMVideoPlayer(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    wtvPlayerViewModel:WTVPlayerViewModel= hiltViewModel()
) {
    val context = LocalContext.current
    val epgList by sharedViewModel.wtvEPGList.collectAsState()
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


    // Remember the player and recreate it when the DRM type changes
    // UI state
    var uiState by remember { mutableStateOf(PlayerUiState()) }

    // Build / rebuild ExoPlayer on DRM type change
    val exoPlayer = remember(selectedChannel.content?.drmType) {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            ExoPlayer.Builder(context).apply {
                logReport("selectedChannel.content?.drmType:::::${selectedChannel.content?.drmType}")
                if (selectedChannel.content?.drmType.equals("sigma", true)) {
                    setMediaSourceFactory(context.provideSigmaSourceFactory())
                }
            }
            .build()
            .apply {
                playWhenReady = true
                // Listen for state changes to drive loading indicator
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        uiState = when (state) {
                            Player.STATE_BUFFERING -> uiState.copy(isLoading = false)
                            Player.STATE_READY     -> uiState.copy(isLoading = false)
                            Player.STATE_IDLE,
                            Player.STATE_ENDED     -> uiState.copy(isLoading = false)
                            else                   -> uiState
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        // Distinguish HTTP 404 from DRM errors
                        val msg = when (val cause = error.cause) {
                            is HttpDataSource.InvalidResponseCodeException ->
                                "Video not found (HTTP ${cause.responseCode})"
                            is MediaDrm.MediaDrmStateException ->
                                "DRM error: unable to play content"
                            else ->
                                "Playback error: ${error.message}"
                        }
                        uiState = uiState.copy(isLoading = false, errorMessage = msg)
                    }
                })
            }
    }
    // Release player when leaving this Composable or when DRM type changes
    // Release on DRM type change / exit
    DisposableEffect(
        selectedChannel.content?.drmType,
        selectedChannel.content?.videoUrl
    ) {
        logReport("DisposableEffect:::${selectedChannel.content?.drmType}")

        onDispose { exoPlayer.release() }
    }

    // Whenever the selected channel changes, load its media
    // Load media when channel changes
    LaunchedEffect(selectedChannel.content?.drmType,
        selectedChannel.content?.videoUrl) {
        uiState = uiState.copy(isLoading = true, errorMessage = null)
        val url = selectedChannel.content?.videoUrl
        if (!url.isNullOrEmpty()) {

            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            val mediaItem = if (
                selectedChannel.content?.drmType.equals("cryptoguard", true)
            ) {
                context.provideCryptoGuardMediaSource()
            } else {
                MediaItem.fromUri(url)
            }
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            uiState = uiState.copy(
                isLoading = false,
                errorMessage = null
            )
        } else {
            uiState = uiState.copy(
                isLoading = false,
                errorMessage = "No video URL available."
            )
        }
    }

    // Auto-hide overlay after 5 sec
    LaunchedEffect(isOverlayVisible) {
        if (isOverlayVisible) {
            delay(8000)
            isOverlayVisible = false
        }
    }

    BackHandler {
        navController.navigate(Destination.epgScreen) {
            popUpTo(Destination.panMetroScreen) { inclusive = true }
        }
    }

    fun playNextChannel() {
        if (currentIndex < (epgList.lastIndex )) {
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusable()
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_BACK -> {
                            navController.navigate(Destination.epgScreen) {
                                popUpTo(Destination.panMetroScreen) { inclusive = true }
                            }
                            /*if (navController.previousBackStackEntry == null) {
                                navController.navigate(Destination.homeScreen) {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            } else {
                                navController.popBackStack()
                            }*/
                            true
                        }

                        KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_CHANNEL_UP -> {
                            navController.navigate(Destination.genreScreen) {
                                popUpTo(Destination.panMetroScreen)// { inclusive = true }
                            }
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            navController.navigate(Destination.epgScreen) {
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


        if (isOverlayVisible) {
            PlayerOverlay(
                navController= navController,
                dataItem = selectedChannel,
                onDismiss = { isProgramOverlayVisible = false }
            )
        }
        if (uiState.isLoading) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        uiState.errorMessage?.let { msg ->
            Box(
                Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = msg,
                    color = Color.White,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }

}



