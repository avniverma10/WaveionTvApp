package com.example.tvapp.view.playeroverlay

import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
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
import com.example.tvapp.R
import com.example.tvapp.extensions.provideCryptoGuardMediaSource
import com.example.tvapp.view.navigationhelper.Destination
import com.example.tvapp.view.player.addWatermarkToPlayer
import com.example.tvapp.viewmodels.SharedViewModel
import com.example.tvapp.viewmodels.WTVPlayerViewModel
import kotlinx.coroutines.*

@RequiresApi(Build.VERSION_CODES.M)
@OptIn(UnstableApi::class)
@Composable
fun NewPanMetroVideoPlayer(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    wtvPlayerViewModel: WTVPlayerViewModel= hiltViewModel()
) {
    val context = LocalContext.current
    val epgList by sharedViewModel.wtvEPGList.collectAsState()
    val selectedChannel by sharedViewModel.selectedChannel.collectAsState()
    val scope = rememberCoroutineScope()

    // State management
    var isOverlayVisible by remember { mutableStateOf(true) }
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var overlayHideJob by remember { mutableStateOf<Job?>(null) }

    // Focus and list state management
    val channelRequesters = remember(epgList) { List(epgList.size) { FocusRequester() } }
    val selectedChannelIndex = remember { mutableStateOf(0) }
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    // Set up window flags
    (context as? Activity)?.window?.setFlags(
        WindowManager.LayoutParams.FLAG_SECURE,
        WindowManager.LayoutParams.FLAG_SECURE
    )

    // ExoPlayer setup
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            prepare()
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

    // Function to handle overlay visibility
    fun showOverlay() {
        isOverlayVisible = true
        lastInteractionTime = System.currentTimeMillis()
        
        // Cancel existing hide job if any
        overlayHideJob?.cancel()
        
        // Start new hide job
        overlayHideJob = scope.launch {
            delay(10000) // 10 seconds
            if (System.currentTimeMillis() - lastInteractionTime >= 10000) {
                isOverlayVisible = false
            }
        }
    }

    // Channel navigation functions
    fun playNextChannel() {
        if (selectedChannelIndex.value < epgList.lastIndex) {
            selectedChannelIndex.value++
            sharedViewModel.updateSelectedChannel(epgList[selectedChannelIndex.value])
            scope.launch {
                delay(200)
                listState.animateScrollToItem(selectedChannelIndex.value)
            }
        }
    }

    fun playPreviousChannel() {
        if (selectedChannelIndex.value > 0) {
            selectedChannelIndex.value--
            sharedViewModel.updateSelectedChannel(epgList[selectedChannelIndex.value])
            scope.launch {
                delay(200)
                listState.animateScrollToItem(selectedChannelIndex.value)
            }
        }
    }

    // Media setup when selected channel changes
    LaunchedEffect(selectedChannel) {
        selectedChannelIndex.value = epgList.indexOfFirst {
            it.content?.videoUrl == (selectedChannel.content?.videoUrl ?: "")
        }
        
        selectedChannel.content?.videoUrl?.takeIf { it.isNotEmpty() }?.let { url ->
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            
            val mediaItem = if (selectedChannel.content?.drmType.equals("cryptoguard", ignoreCase = true)) {
                context.provideCryptoGuardMediaSource(
                    contentUrl = selectedChannel.content?.videoUrl,
                    contentId = selectedChannel.content?.assetId
                )
            } else {
                MediaItem.fromUri(url)
            }
            
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    // Back handler
    BackHandler {
        navController.navigate(Destination.epgScreen) {
            popUpTo(Destination.panMetroScreen) { inclusive = true }
        }
    }

    // Main UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusable()
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    // Show overlay and reset timer on any key press
                    showOverlay()
                    
                    when (keyEvent.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_BACK -> {
                            navController.navigate(Destination.epgScreen) {
                                popUpTo(Destination.panMetroScreen) { inclusive = true }
                            }
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            navController.navigate(Destination.genreScreen) {
                                popUpTo(Destination.panMetroScreen) { inclusive = true }
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
                            if (selectedChannelIndex.value < epgList.size) {
                                sharedViewModel.updateSelectedChannel(epgList[selectedChannelIndex.value])
                            }
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_CHANNEL_DOWN -> {
                            focusManager.moveFocus(FocusDirection.Down)
                            playPreviousChannel()
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_CHANNEL_UP -> {
                            focusManager.moveFocus(FocusDirection.Up)
                            playNextChannel()
                            true
                        }
                        else -> false
                    }
                } else false
            }
    ) {
        // Video Player
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

        // Logo overlay
        Image(
            painter = painterResource(id = R.drawable.panmetro_logo_t),
            contentDescription = "Panmetro Logo",
            modifier = Modifier
                .size(width = 200.dp, height = 150.dp)
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )

        // Channel overlay
        if (isOverlayVisible) {
            NewPlayerOverlay(
                selectedIndex = selectedChannelIndex,
                lazyListState = listState,
                sharedViewModel = sharedViewModel,
                channelFocusRequesters = channelRequesters,
                onChannelFocused = { sharedViewModel.updateSelectedChannel(it) }
            )
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
            overlayHideJob?.cancel()
        }
    }
}