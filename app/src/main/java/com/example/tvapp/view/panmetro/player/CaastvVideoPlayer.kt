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
import com.android.caastv.R
import com.example.tvapp.extensions.hideKeyboard
import com.example.tvapp.extensions.loge
import com.example.tvapp.extensions.playerErrorHandling
import com.example.tvapp.extensions.provideCryptoGuardMediaSource
import com.example.tvapp.extensions.toJSONObject
import com.example.tvapp.utils.uistate.PreferenceManager
import com.example.tvapp.view.navigationhelper.Destination
import com.example.tvapp.view.playeroverlay.FullScreenPlayerOverlay
import com.example.tvapp.view.uicomponent.addWatermarkToPlayer
import com.example.tvapp.view.uicomponent.error.CommonDialog
import com.example.tvapp.view.uicomponent.fingerprint.ChannelFingerprintOverlay
import com.example.tvapp.view.uicomponent.fingerprint.ScrollingMessageOverlay
import com.example.tvapp.view.uicomponent.generateWatermark
import com.example.tvapp.view.uicomponent.keyboard.HideKeyboardOnEnter
import com.example.tvapp.viewmodels.SharedViewModel
import com.example.tvapp.viewmodels.player.PlayerViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import kotlin.random.Random

@OptIn(UnstableApi::class)
@Composable
fun CaastvVideoPlayer(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    playerViewModel: PlayerViewModel= hiltViewModel()
) {

    val context = LocalContext.current
    val playlist by sharedViewModel.currentPlaylist.collectAsState()
    val selectedChannel by sharedViewModel.selectedChannel.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val playerSSERules by sharedViewModel.playerSSERules.collectAsState()
    val playerView = remember {
        mutableStateOf<PlayerView?>(null)
    }
    val epgList = if (playlist.isNotEmpty()) {
        playlist
    } else {
        playerViewModel.provideAvailableEPG()
    }
    val channelRequesters = remember(epgList.size) {
        List(epgList.size) { FocusRequester() }
    }
    val filter by sharedViewModel.filterState.collectAsState()
    val language = filter.language ?: "All Languages"
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorCodeState by remember { mutableStateOf(0) }
    var errorMessageState by remember { mutableStateOf("") }
    var errorTitleState by remember { mutableStateOf("") }
    val currentChannelIndex = remember { mutableIntStateOf(0) }
    val previewChannelIndex = remember { mutableIntStateOf(0) }
    var switchJob by remember { mutableStateOf<Job?>(null) }

    var watchJob by remember { mutableStateOf<Job?>(null) }

    val currentProgrammeIndex = remember { mutableIntStateOf(0) }
    LaunchedEffect(selectedChannel) {
        val idx = epgList.indexOfFirst {
            it.content?.videoUrl == selectedChannel?.content?.videoUrl
        }.coerceAtLeast(0)
        currentChannelIndex.intValue = idx
        previewChannelIndex.intValue = idx
    }
    // whenever the channel changes…
    LaunchedEffect(selectedChannel) {
        watchJob?.cancel()
        watchJob = scope.launch {
            delay(300_000L) // 5 minutes
            selectedChannel?.let { sharedViewModel.recordRecentlyWatched(it) }
        }
    }
    LaunchedEffect(Unit) {
        context.hideKeyboard()
    }
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

    val categoryName by sharedViewModel.currentPlaylistName.collectAsState()

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
                currentProgrammeIndex.intValue = 0
            }
        }
    }
    fun switchNow() {
        switchJob?.cancel()
        currentChannelIndex.intValue = previewChannelIndex.intValue
        sharedViewModel.updateSelectedChannel(epgList[currentChannelIndex.intValue])
    }
    fun startSwitchCountdown() {
        switchJob?.cancel()
        switchJob = scope.launch {
            delay(3000)
            switchNow()
        }
    }
    LaunchedEffect(Unit) {
        showOverlay()
    }
// Remember the player and recreate it when the DRM type changes
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                // optional: any static setup
                playWhenReady = true
            }
    }

    DisposableEffect(exoPlayer) {
        val analyticsListener = object : AnalyticsListener {
            override fun onEvents(player: Player, events: AnalyticsListener.Events) {
                if (events.contains(AnalyticsListener.EVENT_DRM_KEYS_LOADED)) {
                    loge("DRM", "Keys loaded successfully")
                }
                if (events.contains(AnalyticsListener.EVENT_DRM_SESSION_MANAGER_ERROR)) {
                    loge("DRM", "Session manager error")
                }
            }
        }
        // Error listener
        val errorListener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                val httpCode = (error.cause as? HttpDataSource.InvalidResponseCodeException)
                    ?.responseCode
                val rawCode = httpCode ?: error.errorCode
                val (code, title, message) = playerErrorHandling(rawCode)
                errorCodeState    = code
                errorTitleState   = title
                errorMessageState = message
                showErrorDialog   = true

                // 2) Schedule a retry between 0ms and 120 000ms (i.e. 0–2 minutes)
                scope.launch {
                    val retryDelay = Random.nextLong(0L, 120_000L)
                    Log.d("Retry", "Retrying live stream in ${retryDelay / 1000}s…")
                    delay(retryDelay)

                    // re‐prepare the same live source
                    exoPlayer.prepare()
                    exoPlayer.playWhenReady = true
                }
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    // Video started playing successfully
                    showErrorDialog = false
                }
            }
        }

        // Attach them
        exoPlayer.addAnalyticsListener(analyticsListener)
        exoPlayer.addListener(errorListener)

        // Kick off the first playback
        exoPlayer.prepare()

        onDispose {
            exoPlayer.removeAnalyticsListener(analyticsListener)
            exoPlayer.removeListener(errorListener)
            exoPlayer.release()
        }
    }

    // Whenever the selected channel changes, load its media
    LaunchedEffect(selectedChannel) {
        selectedChannelIndex.intValue = epgList.indexOfFirst {
            it.content?.videoUrl == (selectedChannel?.content?.videoUrl ?: "")
        }
        selectedChannel.content?.videoUrl?.takeIf { it.isNotEmpty() }?.let { url ->
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            showErrorDialog = false
            val drmData = HashMap<String,String>()
            drmData.put("DRMType",selectedChannel?.content?.drmType?:"")
            drmData.put("contentId",selectedChannel?.content?.assetId?:"")
            drmData.put("contentUrl",selectedChannel?.content?.videoUrl?:""?:"")
            val mediaItem = if (selectedChannel?.content?.drmType.equals("cryptoguard", ignoreCase = true)) {
                context.provideCryptoGuardMediaSource( contentUrl = selectedChannel?.content?.videoUrl, contentId = selectedChannel?.content?.assetId, logData = drmData)
            } else {
                MediaItem.fromUri(url)
            }
            loge("Requested Data>",drmData.toJSONObject().toString())
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true  //  Ensure playback starts automatically
            //make fingerprint request
            sharedViewModel.providePlayerSSERequest(channel = "${selectedChannel?.content?.channelNo}:${selectedChannel?.content?.title}")

        }
    }

//    BackHandler {
//        navController.navigate(Destination.epgScreen) {
//            popUpTo(Destination.panMetroScreen) { inclusive = true }
//        }
//    }

    BackHandler {
        sharedViewModel.updateLanguage(null)                         // clear language filter
        sharedViewModel.updateGenre(null)                            // clear genre filter
        sharedViewModel.setCurrentPlaylist(emptyList(), "All Channels")
        navController.popBackStack()
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
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            if (previewChannelIndex.intValue > 0) {
                                previewChannelIndex.intValue--
                                currentProgrammeIndex.intValue = 0 // reset
                                startSwitchCountdown()
                            }
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_UP-> {
                            if (!isOverlayVisible) {
                                isOverlayVisible = true
                            } else {
                                val currentChannel = epgList.getOrNull(previewChannelIndex.intValue)
                                val programmes = currentChannel?.tv?.programme?.let {
                                    playerViewModel.provideAvailablePrograms(it)
                                }.orEmpty()

                                val baseIndex = programmes.indexOfLast { it.startTime!! <= System.currentTimeMillis() }.coerceAtLeast(0)
                                val maxOffset = (programmes.lastIndex - baseIndex).coerceAtLeast(0)

                                currentProgrammeIndex.intValue = (currentProgrammeIndex.intValue - 1).coerceAtLeast(0)
                            }
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_DOWN -> {
                            if (!isOverlayVisible) {
                                isOverlayVisible = true
                            } else {
                                val currentChannel = epgList.getOrNull(previewChannelIndex.intValue)
                                val programmes = currentChannel?.tv?.programme?.let {
                                    playerViewModel.provideAvailablePrograms(it)
                                }.orEmpty()

                                val baseIndex = programmes.indexOfLast { it.startTime!! <= System.currentTimeMillis() }.coerceAtLeast(0)
                                val maxOffset = (programmes.lastIndex - baseIndex).coerceAtLeast(0)

                                currentProgrammeIndex.intValue = (currentProgrammeIndex.intValue + 1).coerceAtMost(maxOffset)
                            }
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            if (previewChannelIndex.intValue < epgList.lastIndex) {
                                previewChannelIndex.intValue++
                                currentProgrammeIndex.intValue = 0 // reset
                                startSwitchCountdown()
                            }
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_CENTER -> {
                            switchNow()
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
                playerView.value = view.findViewById<PlayerView>(R.id.player_view)

                playerView.value?.apply {
                    player = exoPlayer
                    useController = false
                    keepScreenOn = true
                    // addWatermarkToPlayer(this, provideWatermarkHash(context))

                    // addLogoToPlayer(this)
                }

                view

            }
        )

        if((playerSSERules?.fingerprints?.size ?: 0) > 0){
            playerSSERules?.fingerprints?.forEach {
                ChannelFingerprintOverlay(player= playerView.value, fingerprintRule = mutableStateOf(it))
            }
        }

        if((playerSSERules?.scrollMessages?.size ?: 0) > 0){
            playerSSERules?.scrollMessages?.forEach {
                ScrollingMessageOverlay(scrollMessageInfo = mutableStateOf(it))
            }
        }
//        Row(
//            verticalAlignment = Alignment.Top,
//            modifier = Modifier.size(width = 150.dp, height = 100.dp)
//                .align(Alignment.TopEnd)
//        ){
//            Image(
//                painter = painterResource(R.drawable.player_logo),
//                contentDescription = null,
//                modifier = Modifier.padding(20.dp)
//            )
//        }
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
            epgList.getOrNull(previewChannelIndex.intValue)?.let {
                BottomFullScreenPlayerOverlay(
                    channel = it,
                    sharedViewModel = sharedViewModel,
                    playerViewModel = playerViewModel,
                    onOptionClick = {},
                    programmeIndex = currentProgrammeIndex.intValue,
                )
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose { watchJob?.cancel() }
    }
}


