package com.example.tvapp.view.panmetro.player

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.provider.Settings
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
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
import com.android.panmetroiptv.R
import com.example.tvapp.extensions.playerErrorHandling
import com.example.tvapp.extensions.provideCryptoGuardMediaSource
import com.example.tvapp.extensions.toJSONObject
import com.example.tvapp.utils.uistate.PreferenceManager
import com.example.tvapp.view.navigationhelper.Destination
import com.example.tvapp.view.uicomponent.addWatermarkToPlayer
import com.example.tvapp.view.uicomponent.error.CommonDialog
import com.example.tvapp.view.uicomponent.generateWatermark
import com.example.tvapp.view.uicomponent.keyboard.HideKeyboardOnEnter
import com.example.tvapp.viewmodels.SharedViewModel
import com.example.tvapp.viewmodels.player.PlayerViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException

@OptIn(UnstableApi::class)
@Composable
fun PanMetroVideoPlayer(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    playerViewModel: PlayerViewModel= hiltViewModel()
) {

    HideKeyboardOnEnter()
    val context = LocalContext.current
    val epgList = playerViewModel.provideAvailableEPG()
    val selectedChannel by sharedViewModel.selectedChannel.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val channelRequesters = remember(epgList) {
        List((epgList.size)) { FocusRequester() }
    }


    var showErrorDialog by remember { mutableStateOf(false) }
    var errorCodeState by remember { mutableStateOf(0) }
    var errorMessageState by remember { mutableStateOf("") }
    var errorTitleState by remember { mutableStateOf("") }

    //Compute the initial value
    /*
    val initial = epgList.indexOfFirst { it.content?.videoUrl == selectedChannel.content?.videoUrl }
        if(initial>-1){
            mutableIntStateOf(initial)
        }else{
            mutableIntStateOf(0)
        }
     */
    //Finally return the MutableState
    val selectedChannelIndex = remember {mutableIntStateOf(0) }

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


    // State management
    var isOverlayVisible by remember { mutableStateOf(true) }
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
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
    LaunchedEffect(Unit) {
        showOverlay()
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
                        if (error.cause is HttpDataSource.HttpDataSourceException) {
                            // handle timeout: show a "Retry" UI
                            val (errorCode, errorTitle, errorMessage) = playerErrorHandling(2002)

                            Log.e("rawCode","$errorCode")
                            errorCodeState    = errorCode
                            errorTitleState   = errorTitle
                            errorMessageState = errorMessage
                            showErrorDialog   = true
                        } else {
                            val httpCode = (error.cause as? HttpDataSource.InvalidResponseCodeException)
                                ?.responseCode
                            var rawCode = httpCode ?: error.errorCode
                            // now returns (appCode, title, message)
                            val (errorCode, errorTitle, errorMessage) = playerErrorHandling(rawCode)

                            Log.e("rawCode","$errorCode")
                            errorCodeState    = errorCode
                            errorTitleState   = errorTitle
                            errorMessageState = errorMessage
                            showErrorDialog   = true

                        }
                    }
                })
            }
    }

    // Whenever the selected channel changes, load its media
    LaunchedEffect(selectedChannel) {
        selectedChannelIndex.intValue = epgList.indexOfFirst {
            it.content?.videoUrl == (selectedChannel?.content?.videoUrl ?: "")
        }
        selectedChannel.content?.videoUrl?.takeIf { it.isNotEmpty() }?.let { url ->
            selectedChannel.tv?.programme?.let { playerViewModel.providePlayableProgramData(it) }
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            showErrorDialog = false
            val drmData = HashMap<String,String>()
            drmData.put("DRMType",selectedChannel?.content?.drmType?:"")
            drmData.put("contentId",selectedChannel?.content?.assetId?:"")
            drmData.put("contentUrl",selectedChannel?.content?.videoUrl?:""?:"")
            val mediaItem = if (selectedChannel?.content?.drmType.equals("cryptoguard", ignoreCase = true)) {
                context.provideCryptoGuardMediaSource(loginInfo = sharedViewModel.loginInfo?.value, contentUrl = selectedChannel?.content?.videoUrl, contentId = selectedChannel?.content?.assetId, logData = drmData)
            } else {
                MediaItem.fromUri(url)
            }
            Log.e("Requested Data>",drmData.toJSONObject().toString())
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
        if (selectedChannelIndex.intValue < (epgList.lastIndex )) {
            selectedChannelIndex.intValue++
            sharedViewModel.updateSelectedChannel(epgList[selectedChannelIndex.intValue])
        }
    }

    fun playPreviousChannel() {
        if (selectedChannelIndex.intValue > 0) {
            selectedChannelIndex.intValue--
            sharedViewModel.updateSelectedChannel(epgList[selectedChannelIndex.intValue])
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
                                PreferenceManager.selectedGenreIndex = 0
                                PreferenceManager.selectedChannelIndex = 0
                                PreferenceManager.lastEpgDataItem = null
                                popUpTo(Destination.panMetroScreen) { inclusive = true }
                            }
                            true
                        }

                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            navController.navigate(Destination.genreScreen) {
                                PreferenceManager.selectedGenreIndex = 0
                                PreferenceManager.selectedChannelIndex = 0
                                PreferenceManager.lastEpgDataItem = null
                                popUpTo(Destination.panMetroScreen) { inclusive = true }
                            }
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            navController.navigate(Destination.epgScreen) {
                                PreferenceManager.lastEpgDataItem = null
                                popUpTo(Destination.panMetroScreen) { inclusive = true }
                            }
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_CENTER -> {
                            if (selectedChannelIndex.intValue>=0 && selectedChannelIndex.intValue < (epgList.size)) {
                                sharedViewModel.updateSelectedChannel(epgList[selectedChannelIndex.intValue])
                            }
                            true
                        }

                        KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_CHANNEL_DOWN -> {
                            if (selectedChannelIndex.intValue > 0) {
                                focusManager.moveFocus(FocusDirection.Down)
                                selectedChannelIndex.intValue--
                                scope.launch {
                                    delay(200)
                                    // Scroll into view
                                    listState.animateScrollToItem(selectedChannelIndex.intValue)
                                }
                            }
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_CHANNEL_UP -> {
                            if (selectedChannelIndex.intValue >= 0 && selectedChannelIndex.intValue < (epgList.size-1)) {
                                focusManager.moveFocus(FocusDirection.Up)
                                selectedChannelIndex.intValue++
                                scope.launch {
                                    delay(200)
                                    // Scroll into view
                                    listState.animateScrollToItem(selectedChannelIndex.intValue)
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
                    addWatermarkToPlayer(this, provideWatermarkHash(context))

                    // addLogoToPlayer(this)
                }

                view

            }
        )
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.size(width = 150.dp, height = 100.dp)
                .align(Alignment.TopEnd)
        ){
            Image(
                painter = painterResource(R.drawable.player_logo),
                contentDescription = null,
                modifier = Modifier.padding(20.dp)
            )
        }
        /*ZoomInOutSwitcher(
            epgDataItem = sharedViewModel.selectedChannel, modifier = Modifier
            .size(width = 200.dp, height = 150.dp)
            .align(Alignment.TopEnd)
            .padding(end = 16.dp))*/


        if (showErrorDialog) {
            val borderColor = remember(errorCodeState) {
                if (errorCodeState in 606..700) Color(0xFF6B2828) else Color(0xFF49FEDD)
            }

            CommonDialog(
                painter = painterResource(id = R.drawable.media_error),
                showDialog         = true,
                title              = errorTitleState,
                message            = null,
                errorCode          = errorCodeState,
                errorMessage       = errorMessageState,
                borderColor        = borderColor,
                confirmButtonText  = null,
                onConfirm          = null,
                dismissButtonText  = null,
                onDismiss          = null,
            )
        }


        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE-> {
                         exoPlayer.pause()
                    }
                    Lifecycle.Event.ON_STOP-> {
                        //context.showToastS("ON_STOP>${selectedChannel.displayName}")
                        sharedViewModel.persistToPlayerPrefs(prefs = PreferenceManager,selectedChannel = selectedChannel)
                    }
                    Lifecycle.Event.ON_START-> {
                        if(PreferenceManager.lastEpgDataItem != null) {
                            //context.showToastS("ON_START>${PreferenceManager.lastEpgDataItem?.displayName}")
                            sharedViewModel.updateSelectedChannel(PreferenceManager.lastEpgDataItem!!)
                            PreferenceManager.lastEpgDataItem = null
                        }

                        exoPlayer.play()
                    }
                    else -> Unit
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                exoPlayer.release()
                overlayHideJob?.cancel()
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
        if (isOverlayVisible && selectedChannelIndex.intValue >=0) {
            FullScreenPlayerOverlay(
                selectedIndex =  selectedChannelIndex,
                lazyListState = listState,
                sharedViewModel= sharedViewModel,
                playerViewModel = playerViewModel,
                channelFocusRequesters = channelRequesters,
                onChannelFocused = { sharedViewModel.updateSelectedChannel(it) }
            )
        }
    }


}

@SuppressLint("HardwareIds")
fun provideWatermarkHash(context: Context):String{
    // Device ID
    val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    return generateWatermark(null, deviceId)
}


