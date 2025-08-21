package com.android.panmetroiptv.view.panmetro.player


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
import androidx.compose.runtime.getValue
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
import com.android.panmetroiptv.R
import com.android.panmetroiptv.extensions.extractYouTubeId
import com.android.panmetroiptv.extensions.hideKeyboard
import com.android.panmetroiptv.extensions.loge
import com.android.panmetroiptv.extensions.playerErrorHandling
import com.android.panmetroiptv.extensions.provideCryptoGuardMediaSource
import com.android.panmetroiptv.extensions.showToastS
import com.android.panmetroiptv.model.data.epgdata.EPGDataItem
import com.android.panmetroiptv.utils.Constants
import com.android.panmetroiptv.utils.uistate.PreferenceManager
import com.android.panmetroiptv.view.uicomponent.addWatermarkToPlayer
import com.android.panmetroiptv.view.uicomponent.audio.AnimatedAudio
import com.android.panmetroiptv.view.uicomponent.error.CommonDialog
import com.android.panmetroiptv.view.uicomponent.fingerprint.ChannelFingerprintOverlay
import com.android.panmetroiptv.view.uicomponent.fingerprint.GlobalFingerprintOverlay
import com.android.panmetroiptv.view.uicomponent.fingerprint.ScrollingMessageOverlay
import com.android.panmetroiptv.view.uicomponent.fingerprint.state.ForceMessageDialogState
import com.android.panmetroiptv.view.uicomponent.search.InputOverlay
import com.android.panmetroiptv.viewmodels.SharedViewModel
import com.android.panmetroiptv.viewmodels.player.PlayerViewModel
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
import kotlin.text.get

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
    var dialogStates = remember { mutableStateListOf<ForceMessageDialogState>()}
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

    var isAudio = remember { mutableStateOf(false) }
    var isYoutube = remember { mutableStateOf(false) }
    val youtubeId = remember { mutableStateOf<String?>(null) }
    // --- Channel‑number search state -------------------------------
    var typedDigits     by remember { mutableStateOf("") }   // the running “123”
    var inputJob        by remember { mutableStateOf<Job?>(null) }
    val DEBOUNCE_MS = 1_000L                                 // change if you want longer

    val currentProgrammeIndex = remember { mutableIntStateOf(0) }
    LaunchedEffect(selectedChannel) {
        val idx = epgList.indexOfFirst {
            it.content?.videoUrl == selectedChannel?.content?.videoUrl
        }.coerceAtLeast(0)
        currentChannelIndex.intValue = idx
        previewChannelIndex.intValue = idx
    }


    LaunchedEffect(playerSSERules) {
        if((playerSSERules?.forceMessages?.size ?: 0) > 0){
            dialogStates.clear()
            playerSSERules?.forceMessages?.forEach { message ->
                dialogStates.add(ForceMessageDialogState(message,true))
            }
        }else{
            dialogStates = mutableStateListOf<ForceMessageDialogState>()
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



    val stops = selectedChannel.content?.bgGradient
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

    // Whenever the selected channel changes, load its media
    LaunchedEffect(selectedChannel) {
        selectedChannelIndex.intValue = epgList.indexOfFirst {
            it.content?.videoUrl == (selectedChannel?.content?.videoUrl ?: "")
        }
        selectedChannel.content?.videoUrl?.takeIf { it.isNotEmpty() }?.let { url ->
            if(selectedChannel?.content?.contentType.equals("audio",true)){
                isAudio.value = true
            }else if(selectedChannel?.content?.contentType.equals("youtube",true)){
                isYoutube.value = true
                youtubeId.value = selectedChannel?.content?.videoUrl?.extractYouTubeId()
            }else{
                isAudio.value = false
                isYoutube.value = false
            }
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            showErrorDialog = false
            if(!isYoutube.value) {
                val mediaItem = if (selectedChannel?.content?.drmType.equals(
                        "cryptoguard",
                        ignoreCase = true
                    )
                ) {
                    context.provideCryptoGuardMediaSource(
                        contentUrl = selectedChannel?.content?.videoUrl,
                        contentId = selectedChannel?.content?.assetId
                    )
                } else {
                    MediaItem.fromUri(url)
                }

                /*if(Constants.userChannelResult?.any{it.name.equals(selectedChannel.content?.title,true)} == false){
                    val (code, title, message) = playerErrorHandling(6200)
                    errorCodeState = code
                    errorMessageState = message
                    showErrorDialog = true
                }*/
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true  //  Ensure playback starts automatically

                //make fingerprint request
                sharedViewModel.providePlayerSSERequest(channel = "${selectedChannel?.content?.channelNo}:${selectedChannel?.content?.title}")

            }
        }
    }

    fun commitChannelSearch(numberStr: String) {
        val number = numberStr.toIntOrNull() ?: return
        val idx = epgList.indexOfFirst { (it.content?.channelNo ?: 0) == number }
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
                val code = keyEvent.nativeKeyEvent.keyCode
                showOverlay()
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.nativeKeyEvent.keyCode) {
                        in KeyEvent.KEYCODE_0..KeyEvent.KEYCODE_9->  {
                            val digit = (code - KeyEvent.KEYCODE_0).toString()
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
        if(isYoutube.value){
            val videoId = selectedChannel.content?.videoUrl?:""

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
                        selectedChannel?.content?.ChannelID?.let { PreferenceManager.saveChannel(it) }
                        exoPlayer.pause()
                    }
                    Lifecycle.Event.ON_STOP-> {
                        selectedChannel?.content?.ChannelID?.let { PreferenceManager.saveChannel(it) }
                        exoPlayer.pause()
                    }
                    Lifecycle.Event.ON_START-> {
                        // Launch coroutine in the lifecycle scope
                        lifecycleOwner.lifecycleScope.launch {
                            val selectedChannel = epgList.firstOrNull<EPGDataItem>() { channel ->
                                (channel as? EPGDataItem)?.content?.ChannelID ==
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

        if((playerSSERules?.fingerprints?.size ?: 0) > 0){
            playerSSERules?.fingerprints?.forEach {
                if (it.updatedAt?.equals(PreferenceManager.getPlayerFingerTime(), true) != true){
                    ChannelFingerprintOverlay(fingerprintRule = mutableStateOf(it))
                }
            }
        }

        //Show dialogs
        if((playerSSERules?.forceMessages?.size ?: 0) > 0){
            dialogStates.forEachIndexed { index, dialogState ->
                if(dialogStates[index].show) {
                    if (dialogStates[index].message.forcePush == true){
                        ForceMessageDialog(
                            showDialog = true,
                            forceMessage = dialogState.message,
                            onConfirm = {
                            }
                        )
                    }else{
                        if (dialogStates[index].message.updatedAt?.equals(PreferenceManager.getPlayerForceTime(), true) != true){
                            ForceMessageDialog(
                                showDialog = true,
                                forceMessage = dialogState.message,
                                onConfirm = {
                                    // Mark this dialog as dismissed
                                    dialogStates[index] = dialogState.copy(show = false)
                                    dialogStates[index].message?.updatedAt?.let { PreferenceManager.savePlayerForceTime(it) }
                                }
                            )
                        }
                    }
                }
            }
        }


        if((playerSSERules?.scrollMessages?.size ?: 0) > 0){
            playerSSERules?.scrollMessages?.forEach {
                if (it.updatedAt?.equals(PreferenceManager.getPlayerScrollTime(), true) != true){
                    ScrollingMessageOverlay(scrollMessageInfo = mutableStateOf(it))
                }
            }
        }

    }
}


