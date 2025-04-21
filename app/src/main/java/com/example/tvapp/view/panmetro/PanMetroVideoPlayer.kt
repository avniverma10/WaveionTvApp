package com.example.tvapp.view.panmetro

import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.example.tvapp.R
import com.example.tvapp.extensions.playerErrorHandling
import com.example.tvapp.extensions.provideCryptoGuardMediaSource
import com.example.tvapp.view.navigationhelper.Destination
import com.example.tvapp.view.player.PlaybackErrorDialog
import com.example.tvapp.view.player.addWatermarkToPlayer
import com.example.tvapp.view.playeroverlay.NewPlayerOverlay
import com.example.tvapp.viewmodels.SharedViewModel
import com.example.tvapp.viewmodels.WTVPlayerViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.M)
@OptIn(UnstableApi::class)
@Composable
fun PanMetroVideoPlayer(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    wtvPlayerViewModel:WTVPlayerViewModel= hiltViewModel()
) {
    val context = LocalContext.current
    // 1) Get a ticking clock (updates every minute)

    val epgList by sharedViewModel.wtvEPGList.collectAsState()
    val selectedChannel by sharedViewModel.selectedChannel.collectAsState()
    // Remember the last interaction time (ms since epoch)
    var lastInteraction by remember { mutableStateOf(System.currentTimeMillis()) }
    // 2) How long have we been idle? (ms)
    var idleDurationMs by remember { mutableStateOf(0L) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val channelRequesters = remember(epgList) {
        List(epgList.size) { FocusRequester() }
    }

    val selectedChannelIndex = remember { mutableStateOf(0) }

    // We’ll need the FocusManager to move focus programmatically
    val focusManager = LocalFocusManager.current
    // Keep track of which index is focused
    val listState = rememberLazyListState()

    // Mutable state for UI updates
    // Set FLAG_SECURE if desired.
    (context as? Activity)?.window?.setFlags(
        WindowManager.LayoutParams.FLAG_SECURE,
        WindowManager.LayoutParams.FLAG_SECURE
    )

    // Get the keyboard controller
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        keyboardController?.hide()
    }

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorCodeState by remember { mutableStateOf(0) }
    var errorMessageState by remember { mutableStateOf("") }


    // State management
    var isOverlayVisible by remember { mutableStateOf(true) }
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var overlayHideJob by remember { mutableStateOf<Job?>(null) }

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

// Remember the player and recreate it when the DRM type changes
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
            .apply {
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
                // Add a listener to handle playback errors.
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        val httpCode = (error.cause as? HttpDataSource.InvalidResponseCodeException)
                            ?.responseCode

                        val rawCode = httpCode ?: error.errorCode

                        val (displayCode, message) = playerErrorHandling(rawCode)

                        errorCodeState = displayCode
                        errorMessageState = message
                        showErrorDialog = true
                    }
                })
            }
    }
    // Condition to display the error dialog if an error is encountered.


        PlaybackErrorDialog(
            showDialog   = showErrorDialog,
            errorCode    = errorCodeState,
            errorMessage = errorMessageState,
            onDismiss    = { showErrorDialog = false }
        )



    // Whenever the selected channel changes, load its media
    LaunchedEffect(selectedChannel) {
        selectedChannelIndex.value = epgList.indexOfFirst {
            it.content?.videoUrl == (selectedChannel.content?.videoUrl ?: "")
        }
        selectedChannel.content?.videoUrl?.takeIf { it.isNotEmpty() }?.let { url ->
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            val mediaItem = if (selectedChannel.content?.drmType.equals("cryptoguard", ignoreCase = true)) {
                context.provideCryptoGuardMediaSource(contentUrl = selectedChannel.content?.videoUrl, contentId = selectedChannel.content?.assetId)
            } else {
                MediaItem.fromUri(url)
            }
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true  //  Ensure playback starts automatically
        }
    }

    BackHandler {
        navController.navigate(Destination.epgScreen) {
            popUpTo(Destination.panMetroScreen) { inclusive = true }
        }
    }

    fun playNextChannel() {
        if (selectedChannelIndex.value < (epgList.lastIndex )) {
            selectedChannelIndex.value++
            sharedViewModel.updateSelectedChannel(epgList[selectedChannelIndex.value])
        }
    }

    fun playPreviousChannel() {
        if (selectedChannelIndex.value > 0) {
            selectedChannelIndex.value--
            sharedViewModel.updateSelectedChannel(epgList[selectedChannelIndex.value])
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusable()
            .onPreviewKeyEvent { keyEvent ->
                showOverlay()
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
                            if (selectedChannelIndex.value < (epgList.size)) {
                                sharedViewModel.updateSelectedChannel(epgList[selectedChannelIndex.value])
                            }
                            true
                        }

                        KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_CHANNEL_DOWN -> {
                            if (selectedChannelIndex.value > 0) {
                                focusManager.moveFocus(FocusDirection.Down)
                                selectedChannelIndex.value--
                                scope.launch {
                                    delay(200)
                                    // Scroll into view
                                    listState.animateScrollToItem(selectedChannelIndex.value)
                                }
                            }
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_CHANNEL_UP -> {
                            if (selectedChannelIndex.value < (epgList.size - 1)) {
                                focusManager.moveFocus(FocusDirection.Up)
                                selectedChannelIndex.value++
                                scope.launch {
                                    delay(200)
                                    // Scroll into view
                                    listState.animateScrollToItem(selectedChannelIndex.value)
                                }
                            }
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
//        // Overlay logo in the top-right corner with fixed width and height.
//        Image(
//            painter = painterResource(id = R.drawable.panmetro_logo_t),
//            contentDescription = "Panmetro Logo",
//            modifier = Modifier
//                .size(width = 200.dp, height = 150.dp)
//                .align(Alignment.TopEnd)
//                .padding(16.dp) // optional padding from the top/right edges
//        )

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

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_PAUSE) {
                    exoPlayer.pause()
                }else if (event == Lifecycle.Event.ON_RESUME) {
                    exoPlayer.play()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                exoPlayer.release()
                overlayHideJob?.cancel()
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        if (isOverlayVisible) {
            NewPlayerOverlay(
                selectedIndex =  selectedChannelIndex,
                lazyListState = listState,
                sharedViewModel = sharedViewModel,
                channelFocusRequesters = channelRequesters,
                onChannelFocused = { sharedViewModel.updateSelectedChannel(it) }
            )

        }
    }


}



