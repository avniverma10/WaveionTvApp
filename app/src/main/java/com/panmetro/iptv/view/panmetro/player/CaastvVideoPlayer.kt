package com.panmetro.iptv.view.panmetro.player


import ForceMessageDialog
import android.app.Activity
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.panmetro.iptv.R
import com.panmetro.iptv.extensions.extractYouTubeId
import com.panmetro.iptv.extensions.hideKeyboard
import com.panmetro.iptv.extensions.loge
import com.panmetro.iptv.extensions.playerErrorHandling
import com.panmetro.iptv.extensions.provideCryptoGuardMediaSource
import com.panmetro.iptv.extensions.showToastS
import com.panmetro.iptv.model.data.epgdata.EPGDataItem
import com.panmetro.iptv.model.data.sseresponse.PlayerFingerprint
import com.panmetro.iptv.model.data.sseresponse.ScrollMessage
import com.panmetro.iptv.utils.Constants
import com.panmetro.iptv.utils.uistate.PreferenceManager
import com.panmetro.iptv.view.panmetro.player.fav.VideoPlayerWithTopOverlay
import com.panmetro.iptv.view.uicomponent.addWatermarkToPlayer
import com.panmetro.iptv.view.uicomponent.audio.AnimatedAudio
import com.panmetro.iptv.view.uicomponent.error.CommonDialog
import com.panmetro.iptv.view.uicomponent.fingerprint.ChannelFingerprintOverlay
import com.panmetro.iptv.view.uicomponent.fingerprint.ScrollingMessageOverlay
import com.panmetro.iptv.view.uicomponent.fingerprint.state.ForceMessageDialogState
import com.panmetro.iptv.view.uicomponent.search.InputOverlay
import com.panmetro.iptv.viewmodels.SharedViewModel
import com.panmetro.iptv.viewmodels.player.PlayerViewModel
import com.techit.youtubelib.PlayerConstants
import com.techit.youtubelib.interfaces.YouTubePlayer
import com.techit.youtubelib.listeners.AbstractYouTubePlayerListener
import com.techit.youtubelib.options.IFramePlayerOptions
import com.techit.youtubelib.view.YouTubePlayerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

@OptIn(UnstableApi::class)
@Composable
fun CaastvVideoPlayer(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    playerViewModel: PlayerViewModel= hiltViewModel()
) {

    val context = LocalContext.current
    val epgList  by sharedViewModel.provideEPGDataManager().epgDataState.collectAsState()
    val selectedChannel by sharedViewModel.selectedChannel.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val appPkgChannels by sharedViewModel.appPkgChannels.collectAsStateWithLifecycle()
    val availablePkg by sharedViewModel.availablePkg.collectAsStateWithLifecycle()
    val playerSSERules by sharedViewModel.playerSSERules.collectAsState()
    var visibleForce = remember { mutableStateListOf<ForceMessageDialogState>()}
    val visibleMessages = remember { mutableStateListOf<ScrollMessage>() }
    val visibleFingerprint = remember { mutableStateListOf<PlayerFingerprint>() }
    val globalSSERules by sharedViewModel.globalSSERules.collectAsState()
    val playerView = remember {
        mutableStateOf<PlayerView?>(null)
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

    var isAudio = remember { mutableStateOf(false) }
    var isDRMUrl = remember { mutableStateOf(false) }
    var isYoutube = remember { mutableStateOf(false) }
    val youtubeId = remember { mutableStateOf<String?>(null) }
    // --- Channel‑number search state -------------------------------
    var typedDigits     by remember { mutableStateOf("") }   // the running “123”
    var inputJob        by remember { mutableStateOf<Job?>(null) }
    val DEBOUNCE_MS = 2_000L // change if you want longer

    val currentProgrammeIndex = remember { mutableIntStateOf(0) }

    // fav view state management
    var isTopOverlayVisible by remember { mutableStateOf(false) }
    var topOverlayHideJob by remember { mutableStateOf<Job?>(null) }
    var favViewlastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val topOverlayFocusRequester = remember { FocusRequester() }
    val favIds by sharedViewModel.favoriteChannelIds.collectAsState()
    val channelId = selectedChannel?.channelId ?: ""

    val isFav by remember(selectedChannel, favIds) {
        derivedStateOf {
            val id = selectedChannel?.channelId ?: ""
            id in favIds
        }
    }

    LaunchedEffect(selectedChannel) {
        val idx = epgList.indexOfFirst {
            it.videoUrl == selectedChannel?.videoUrl
        }.coerceAtLeast(0)
        currentChannelIndex.intValue = idx
        previewChannelIndex.intValue = idx
    }


    LaunchedEffect(playerSSERules) {
        visibleFingerprint.clear()
        visibleMessages.clear()
        visibleForce.clear()
        playerSSERules?.forceMessages?.forEach { item ->
            val messageId = item._id ?: return@forEach
            val storedTimestamp = PreferenceManager.getForceUpdatedAt(messageId)
            val currentTimestamp = item.updatedAt
            val shouldShow = when {
                currentTimestamp == null -> true
                storedTimestamp == null -> true
                currentTimestamp != storedTimestamp -> true
                else -> false
            }

            if (shouldShow) {
                visibleForce.add(ForceMessageDialogState(item,true))
            }
        }

        //handle it for scroll message
        playerSSERules?.scrollMessages?.forEach { item ->
            val messageId = item._id ?: return@forEach
            val storedTimestamp = PreferenceManager.getScrollUpdatedAt(messageId)
            val currentTimestamp = item.updatedAt
            val shouldShow = when {
                currentTimestamp == null -> true
                storedTimestamp == null -> true
                currentTimestamp != storedTimestamp -> true
                else -> false
            }

            if (shouldShow) {
                visibleMessages.add(item)
            }
        }

        //handle it for fingerprint
        playerSSERules?.fingerprints?.forEach { item ->
            val messageId = item._id ?: return@forEach
            val storedTimestamp = PreferenceManager.getFingerUpdatedAt(messageId)
            val currentTimestamp = item.updatedAt
            val shouldShow = when {
                currentTimestamp == null -> true
                storedTimestamp == null -> true
                currentTimestamp != storedTimestamp -> true
                else -> false
            }

            if (shouldShow) {
                visibleFingerprint.add(item)
            }
        }
    }

    LaunchedEffect(Unit) {
        context.hideKeyboard()
        //request for user hash
        sharedViewModel.provideUserHash()
        //register scroll message request
        sharedViewModel.provideGlobalSSERequest()
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


    // State management
    var isOverlayVisible by remember { mutableStateOf(true) }
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var overlayHideJob by remember { mutableStateOf<Job?>(null) }



    val stops = selectedChannel.bgGradient
        ?.colors
        ?.sortedBy { it.percentage }
        ?.mapNotNull {
            try {
                Color(android.graphics.Color.parseColor(it.color))
            } catch (e: IllegalArgumentException) {
                null
            }
        }
        .orEmpty()

    val brush = if (stops.size >= 2) {
        Brush.horizontalGradient(stops)
    } else {
        Brush.verticalGradient(listOf(Color(0xFF232020), Color(0xFF232020))) // fallback
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
                currentProgrammeIndex.intValue = 0
            }
        }
    }

    // Function to handle overlay visibility
    fun showFavOverlay(isHide: Boolean?=null) {
        isHide?.let {
            isTopOverlayVisible = false
            // Cancel existing hide job if any
            topOverlayHideJob?.cancel()
        }?:run {
            isTopOverlayVisible = true
            favViewlastInteractionTime = System.currentTimeMillis()

            // Cancel existing hide job if any
            topOverlayHideJob?.cancel()

            // Start new hide job
            topOverlayHideJob = scope.launch {
                delay(5000) // 10 seconds
                if (System.currentTimeMillis() - lastInteractionTime >= 5000) {
                    isTopOverlayVisible = false
                }
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
                if(!isYoutube.value) {
                    if (events.contains(AnalyticsListener.EVENT_DRM_KEYS_LOADED)) {
                        loge("DRM", "Keys loaded successfully")
                    }
                    if (events.contains(AnalyticsListener.EVENT_DRM_SESSION_MANAGER_ERROR)) {
                        loge("DRM", "Session manager error")
                    }
                }
            }
        }
        // Error listener
        val errorListener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                if(!isYoutube.value) {
                    val httpCode = (error.cause as? HttpDataSource.InvalidResponseCodeException)
                        ?.responseCode
                    val rawCode = httpCode ?: error.errorCode
                    val (code, title, message) = playerErrorHandling(rawCode)
                    errorCodeState = code
                    errorTitleState = title
                    errorMessageState = message
                    showErrorDialog = true

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
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                if(!isYoutube.value) {
                    if (playbackState == Player.STATE_READY) {
                        // Video started playing successfully
                        showErrorDialog = false
                    }
                }
            }
        }
        if(!isYoutube.value) {
            // Attach them
            exoPlayer.addAnalyticsListener(analyticsListener)
            exoPlayer.addListener(errorListener)

            // Kick off the first playback
            exoPlayer.prepare()
        }

        onDispose {
            if(!isYoutube.value) {
                exoPlayer.removeAnalyticsListener(analyticsListener)
                exoPlayer.removeListener(errorListener)
            }
            exoPlayer.release()
        }
    }


    suspend fun handleMediaUrlAllowToPlay(videoUrl: String, assetId:String?=null){
        val mediaItem = if (isDRMUrl.value) {
            context.provideCryptoGuardMediaSource(
                contentUrl = videoUrl,
                contentId = assetId
            )
        } else {
            MediaItem.fromUri(videoUrl)
        }

        if(isDRMUrl.value && sharedViewModel.getUserEnableToPlayChannel(assetId.toString()) != true){
            val (code, title, message) = playerErrorHandling(6200)
            errorCodeState = code
            errorMessageState = message
            showErrorDialog = true
            exoPlayer.clearMediaItems()
        }else{
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true  //  Ensure playback starts automatically
            //make fingerprint request
            sharedViewModel.providePlayerSSERequest(channel = "${selectedChannel?.channelNo}:${selectedChannel?.title}")
        }
    }


    // Whenever the selected channel changes, load its media
    LaunchedEffect(selectedChannel) {
        selectedChannelIndex.intValue = epgList.indexOfFirst {
            it.videoUrl == (selectedChannel?.videoUrl ?: "")
        }
        selectedChannel.videoUrl?.takeIf { it.isNotEmpty() }?.let { url ->
            if(selectedChannel.drmType.equals("cryptoguard", ignoreCase = true)){
                isDRMUrl.value = true
            }else{
                isDRMUrl.value = false
            }

            if(selectedChannel?.contentType.equals("audio",true)){
                isAudio.value = true
            }else if(selectedChannel?.contentType.equals("youtube",true)){
                isYoutube.value = true
                youtubeId.value = selectedChannel?.videoUrl?.extractYouTubeId()
            }else{
                isAudio.value = false
                isYoutube.value = false
            }
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            showErrorDialog = false
            if(!isYoutube.value) {
                handleMediaUrlAllowToPlay(videoUrl = url, assetId = selectedChannel.assetId )
            }
        }
    }


    LaunchedEffect(globalSSERules) {
        if(globalSSERules?.blockUser?.size == 0){
            selectedChannel.videoUrl?.let {
                handleMediaUrlAllowToPlay(videoUrl = it, assetId = selectedChannel.assetId )
            }
            return@LaunchedEffect
        }

        globalSSERules?.blockUser?.forEach {
            if(PreferenceManager.getUsername()?.equals(it.username) == true && it.isBlocked == 1){
                exoPlayer.clearMediaItems()
            }
        }
    }

    LaunchedEffect(appPkgChannels,availablePkg) {
        if(!isYoutube.value) {
            selectedChannel.videoUrl?.let {
                handleMediaUrlAllowToPlay(videoUrl = it, assetId = selectedChannel.assetId )
            }
        }
    }


    fun commitChannelSearch(numberStr: String) {
        val number = numberStr.toIntOrNull() ?: return
        val idx = epgList.indexOfFirst { (it.channelNo ?: 0) == number }
        if (idx != -1) {
            sharedViewModel.updateSelectedChannel(epgList[idx])
        } else {
            context.showToastS("This Channel $number not available.")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusable()
            .onPreviewKeyEvent { keyEvent ->
                val keyCode = keyEvent.nativeKeyEvent.keyCode
                val scanCode = keyEvent.nativeKeyEvent.scanCode
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when {
                        // Handle by ScanCode for custom remote buttons
                        scanCode == 402 -> { // Channel UP
                            if (isTopOverlayVisible) {
                                return@onPreviewKeyEvent false
                            }
                            showOverlay()
                            if (previewChannelIndex.intValue < epgList.lastIndex) {
                                previewChannelIndex.intValue++
                                currentProgrammeIndex.intValue = 0
                                startSwitchCountdown()
                            }
                            true
                        }

                        scanCode == 403 -> { // Channel DOWN
                            if (isTopOverlayVisible) {
                                return@onPreviewKeyEvent false
                            }
                            showOverlay()
                            if (previewChannelIndex.intValue > 0) {
                                previewChannelIndex.intValue--
                                currentProgrammeIndex.intValue = 0 // reset
                                startSwitchCountdown()
                            }
                            true
                        }
                        keyCode in KeyEvent.KEYCODE_0..KeyEvent.KEYCODE_9->  {
                            val digit = (keyCode - KeyEvent.KEYCODE_0).toString()
                            if(typedDigits.length<=3) {
                                typedDigits += digit
                            }

                            showOverlay()                       // optional – keep your overlay visible

                            // restart debounce timer
                            inputJob?.cancel()
                            inputJob = scope.launch {
                                delay(DEBOUNCE_MS)
                                commitChannelSearch(typedDigits)
                                typedDigits = ""                 // clear the onscreen prompt
                            }
                            return@onPreviewKeyEvent true        // we consumed the event
                        }
                        keyCode == KeyEvent.KEYCODE_DPAD_LEFT -> {
                            if (isTopOverlayVisible) {
                                return@onPreviewKeyEvent false
                            }
                            showOverlay()
                            if (previewChannelIndex.intValue > 0) {
                                previewChannelIndex.intValue--
                                currentProgrammeIndex.intValue = 0 // reset
                                startSwitchCountdown()
                            }
                            true
                        }
                        keyCode ==  KeyEvent.KEYCODE_DPAD_UP-> {
                            if (!isTopOverlayVisible && !isOverlayVisible) {
                                showFavOverlay()
                                scope.launch {
                                    delay(50) // Give Compose time to recompose the overlay
                                    topOverlayFocusRequester.requestFocus()
                                }
                                true // Consume the event
                            } else if (isTopOverlayVisible) {
                                return@onPreviewKeyEvent false
                            } else {
                                val currentChannel = epgList.getOrNull(previewChannelIndex.intValue)
                                val programmes = currentChannel?.tv?.programme?.let {
                                    playerViewModel.provideAvailablePrograms(it)
                                }.orEmpty()

                                val baseIndex = programmes.indexOfLast { it.startTime!! <= System.currentTimeMillis() }.coerceAtLeast(0)
                                val maxOffset = (programmes.lastIndex - baseIndex).coerceAtLeast(0)

                                currentProgrammeIndex.intValue =
                                    (currentProgrammeIndex.intValue - 1).coerceAtLeast(0)
                                true
                            }
                            /*
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
                            }*/
                            true
                        }
                        keyCode == KeyEvent.KEYCODE_DPAD_DOWN -> {
                            if (isTopOverlayVisible) {
                                showFavOverlay(isHide = true)
                                showOverlay()
                                return@onPreviewKeyEvent true
                            }
                            showOverlay()

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
                        keyCode == KeyEvent.KEYCODE_BACK -> {
                            if (isTopOverlayVisible) {
                                showFavOverlay(isHide = true)
                                return@onPreviewKeyEvent true
                            }
                            false
                        }
                        keyCode == KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            if (isTopOverlayVisible) {
                                return@onPreviewKeyEvent false
                            }
                            showOverlay()
                            if (previewChannelIndex.intValue < epgList.lastIndex) {
                                previewChannelIndex.intValue++
                                currentProgrammeIndex.intValue = 0
                                startSwitchCountdown()
                            }
                            true
                        }
                        keyCode == KeyEvent.KEYCODE_DPAD_CENTER -> {
                            if (isTopOverlayVisible) {
                                return@onPreviewKeyEvent false
                            }
                            showOverlay()
                            switchNow()
                            true
                        }
                        else -> false
                    }
                } else false
            }
    ) {
        if(isYoutube.value){
            val videoId = selectedChannel.videoUrl?:""

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    YouTubePlayerView(ctx).apply {
                        enableAutomaticInitialization = false           // 🔑
                        keepScreenOn = true
                        val opts = IFramePlayerOptions.Builder()
                            .controls(0)
                            .fullscreen(0)
                            .autoplay(1)
                            .rel(0)
                            .build()

                        initialize(object : AbstractYouTubePlayerListener() {
                            override fun onReady(player: YouTubePlayer) {
                                youtubeId.value?.let {
                                    Log.e("loadYoutubeVideo","$youtubeId")
                                    player.loadVideo(it, 0f)
                                }
                                // lastVideoId.value = videoId
                            }

                            override fun onError(
                                youTubePlayer: YouTubePlayer,
                                error: PlayerConstants.PlayerError
                            ) {
                                super.onError(youTubePlayer, error)
                            }
                        }, opts)
                    }
                },
                update = { view ->
                    // If the composable is still alive but the videoId changed, load the new video
                    /* if (lastVideoId.value != videoId) {
                         view.getYouTubePlayerWhenReady { youTubePlayer ->
                             youTubePlayer.loadVideo(videoId, 0f)
                             lastVideoId.value = videoId
                         }
                     }*/
                },
                onRelease = { view -> view.release() }    // called when the composable leaves the tree
            )
        }else{
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val view = LayoutInflater.from(ctx).inflate(R.layout.exoplayer_view, null)
                    playerView.value = view.findViewById<PlayerView>(R.id.player_view)

                    playerView.value?.apply {
                        player = exoPlayer
                        useController = false
                        keepScreenOn = true
                        PreferenceManager.provideUserHash()?.let {
                            addWatermarkToPlayer(this,it)
                        }
                    }

                    view

                }
            )
        }


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
                        selectedChannel?.channelId?.let { PreferenceManager.saveChannel(it) }
                        exoPlayer.pause()
                    }
                    Lifecycle.Event.ON_STOP-> {
                        selectedChannel?.channelId?.let { PreferenceManager.saveChannel(it) }
                        exoPlayer.pause()
                    }
                    Lifecycle.Event.ON_START-> {
                        // Launch coroutine in the lifecycle scope
                        lifecycleOwner.lifecycleScope.launch {
                            val selectedChannel = epgList.firstOrNull<EPGDataItem>() { channel ->
                                (channel as? EPGDataItem)?.channelId ==
                                        PreferenceManager.getSavedChannel()
                            }
                            selectedChannel?.let {
                                sharedViewModel.updateSelectedChannel(it)
                            }
                            exoPlayer.play()
                            // Clear preferences
                            withContext(Dispatchers.Main) {
                                PreferenceManager.clearSaveChannel()
                            }
                        }
                    }
                    else -> Unit
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                exoPlayer.release()
                overlayHideJob?.cancel()
                lifecycleOwner.lifecycle.removeObserver(observer)
                sharedViewModel.stopPlayerSSE()
                if(isOverlayVisible){
                    isOverlayVisible = false
                }
            }
        }


        if (isAudio.value) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(brush, shape = RoundedCornerShape(0.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(max = LocalConfiguration.current.screenWidthDp.dp * 0.8f)
                        .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.7f)
                        .clip(MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedAudio(
                        isSongPlaying = true,
                        channel = selectedChannel
                    )
                }
            }
        }
        //Numeric channel search
        if(typedDigits.isNotEmpty()){
            InputOverlay(typedDigits = typedDigits)
        }
        if (isOverlayVisible && selectedChannelIndex.intValue >=0) {
            epgList.getOrNull(previewChannelIndex.intValue)?.let {
                CaastvPlayerOverlay(
                    channel = it,
                    playerViewModel = playerViewModel,
                    programmeIndex = currentProgrammeIndex.intValue,
                )
            }
        }



        //Show dialogs
        visibleForce.forEachIndexed { index, dialogState ->
            if(visibleForce[index].show) {
                if (visibleForce[index].message.forcePush == true){
                    ForceMessageDialog(
                        showDialog = true,
                        forceMessage = dialogState.message,
                        onConfirm = {
                        }
                    )
                }else{
                    if (visibleForce[index].message.updatedAt?.equals(PreferenceManager.getForceUpdatedAt(visibleForce[index].message._id?:""), true) != true){
                        ForceMessageDialog(
                            showDialog = true,
                            forceMessage = dialogState.message,
                            onConfirm = {
                                // Remove this message from the visible list
                                visibleMessages.removeIf { it.updatedAt == visibleForce[index].message.updatedAt }
                                // Mark this dialog as dismissed
                                visibleForce[index] = dialogState.copy(show = false)
                                visibleForce[index].message?.let {
                                    PreferenceManager.saveForceUpdatedAt((it._id?:""),(it.updatedAt?:""))
                                }
                            }
                        )
                    }
                }
            }
        }


        // Display only visible messages
        visibleMessages.forEach { message ->
            key(message._id) { // Important for proper recomposition
                ScrollingMessageOverlay(
                    scrollMessageInfo = message,
                    onFinish = { updatedAt ->
                        // Remove this message from the visible list
                        visibleMessages.removeIf { it.updatedAt == updatedAt }

                        // Also save to preferences
                        message._id?.let { id ->
                            PreferenceManager.saveScrollUpdatedAt(id, updatedAt)
                        }
                    }
                )
            }
        }

    }

    // Display only visible messages
    visibleFingerprint.forEach { fingerprint ->
        key(fingerprint._id) { // Important for proper recomposition
            ChannelFingerprintOverlay(fingerprintRule = fingerprint,
                onFinish = { updatedAt ->
                    // Remove this message from the visible list
                    visibleMessages.removeIf { it.updatedAt == updatedAt }

                    // Also save to preferences
                    fingerprint._id?.let { id ->
                        PreferenceManager.saveFingerUpdatedAt(id, updatedAt)
                    }
                })
        }
    }


    if (isTopOverlayVisible) {
        LaunchedEffect(Unit) {
            delay(50)
            topOverlayFocusRequester.requestFocus()
        }
    }

    if (isTopOverlayVisible) {
        VideoPlayerWithTopOverlay(
            focusRequester   = topOverlayFocusRequester,
            isFavorite       = isFav,               // ← pass it here
            onFavoriteClick  = {
                if(isFav){
                    sharedViewModel.removeFavorite(channelId)
                }else{
                    val isAllowToAdd = sharedViewModel.addFavorite(channelId)
                    if(!isAllowToAdd){
                        context.showToastS("Favorite limit reached (${Constants.maxLimit}). Please remove some to add new ones.")//"Favorite limit reached up to $maxLimit. Please remove some favorites before adding new ones.")
                    }
                }
                showFavOverlay(isHide = true)
            },
            onDismiss = {
                showFavOverlay(isHide = true)
            }

        )
    }
}


