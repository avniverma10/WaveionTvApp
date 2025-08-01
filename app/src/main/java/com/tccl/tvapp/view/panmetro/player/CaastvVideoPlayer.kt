package com.tccl.tvapp.view.panmetro.player


import android.app.Activity
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
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
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.android.tccl.R
import com.tccl.tvapp.extensions.hideKeyboard
import com.tccl.tvapp.extensions.loge
import com.tccl.tvapp.extensions.playerErrorHandling
import com.tccl.tvapp.extensions.provideCryptoGuardMediaSource
import com.tccl.tvapp.extensions.showToastS
import com.tccl.tvapp.utils.uistate.PreferenceManager
import com.tccl.tvapp.view.uicomponent.InputOverlay
import com.tccl.tvapp.view.uicomponent.addWatermarkToPlayer
import com.tccl.tvapp.view.uicomponent.audio.AnimatedAudio
import com.tccl.tvapp.view.uicomponent.error.CommonDialog
import com.tccl.tvapp.view.uicomponent.fingerprint.ChannelFingerprintOverlay
import com.tccl.tvapp.view.uicomponent.fingerprint.ScrollingMessageOverlay
import com.tccl.tvapp.view.uicomponent.keyboard.HideKeyboardOnEnter
import com.tccl.tvapp.viewmodels.SharedViewModel
import com.tccl.tvapp.viewmodels.player.PlayerViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.text.contains

@OptIn(UnstableApi::class)
@Composable
fun CaastvVideoPlayer(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    playerViewModel: PlayerViewModel= hiltViewModel()
) {

    var isAudio = remember { mutableStateOf(false) }

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
    // --- Channel‑number search state -------------------------------
    var typedDigits     by remember { mutableStateOf("") }   // the running “123”
    var inputJob        by remember { mutableStateOf<Job?>(null) }
    val DEBOUNCE_MS = 1_000L                                 // change if you want longer

    //overlay options
    var topOverlayHideJob by remember { mutableStateOf<Job?>(null) }
    val topOverlayFocusRequester = remember { FocusRequester() }
    var isTopOverlayVisible by remember { mutableStateOf(false) }
    val favIds by sharedViewModel.favoriteChannelIds.collectAsState()
    val subtitleTracks = remember { mutableStateListOf<String>() }  // just the languages
    val selectedSubtitle = remember { mutableStateOf<String?>(null) }
    val showSubtitleOverlay = remember { mutableStateOf(false) }
    val audioTracks = remember { mutableStateListOf<String>() }
    val selectedAudio = remember { mutableStateOf<String?>(null) }
    val showAudioOverlay = remember { mutableStateOf(false) }
    val videoTracks = remember { mutableStateListOf<String>() }
    val selectedVideo = remember { mutableStateOf<String?>(null) }
    val showVideoOverlay = remember { mutableStateOf(false) }
    val overlayFocusRequester = remember { FocusRequester() }
    val subtitleButtonFocusRequester = remember { FocusRequester() }
    val audioButtonFocusRequester = remember { FocusRequester() }
    val videoButtonFocusRequester = remember { FocusRequester() }
    val lastTopOverlayButtonFocus = remember { mutableStateOf<FocusRequester?>(null) }

    val qualityLabel by remember(selectedVideo.value) {
        mutableStateOf(
            selectedVideo.value?.let { label ->
                val heightPart = label
                    .substringAfter('x')
                    .substringBefore('p')
                val h = heightPart.toIntOrNull() ?: return@let "HD"
                if (h >= 720) "HD" else "SD"
            } ?: "HD"
        )
    }
    val trackSelector = remember {
        DefaultTrackSelector(context).apply {
            setParameters(buildUponParameters().setPreferredTextLanguage(null))
        }
    }


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
    //hide keyboard forcefully
    HideKeyboardOnEnter()
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
    val channelId = selectedChannel?.channelId ?: ""



    val stops = selectedChannel.content?.bgGradient
        ?.colors
        ?.sortedBy { it.percentage }
        ?.map { Color(android.graphics.Color.parseColor(it.color)) }
        .orEmpty()

    val brush = if (stops.size >= 2) {
        Brush.horizontalGradient(stops)
    } else {
        Brush.verticalGradient(listOf(Color(0xFF232020), Color(0xFF232020))) // fallback
    }

    val isFav by remember(selectedChannel, favIds) {
        derivedStateOf {
            val id = selectedChannel?.channelId ?: ""
            id in favIds
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


    LaunchedEffect(
        showVideoOverlay.value,
        showAudioOverlay.value,
        showSubtitleOverlay.value)
    {
        if (showVideoOverlay.value || showAudioOverlay.value || showSubtitleOverlay.value) {
            overlayFocusRequester.requestFocus()
        }
        if (!showAudioOverlay.value && !showVideoOverlay.value && !showSubtitleOverlay.value) {
            snapshotFlow { lastTopOverlayButtonFocus.value }
                .first()
            lastTopOverlayButtonFocus.value?.requestFocus()
        }
    }

    BackHandler(enabled = !isTopOverlayVisible) {
        sharedViewModel.setCurrentPlaylist(emptyList(), "All Channels")
        navController.popBackStack()
    }



    fun applySubtitle(lang: String?) {
        val mappedTrackInfo = trackSelector.currentMappedTrackInfo ?: return
        val subtitleRendererIndex = (0 until mappedTrackInfo.rendererCount)
            .firstOrNull { mappedTrackInfo.getRendererType(it) == C.TRACK_TYPE_TEXT }
            ?: return

        val builder = trackSelector.buildUponParameters()

        if (lang == null) {
            // Disable subtitle renderer and clear override
            trackSelector.parameters = builder
                .setRendererDisabled(subtitleRendererIndex, true)
                .clearSelectionOverrides(subtitleRendererIndex)
                .build()
        } else {
            trackSelector.parameters = builder
                .setRendererDisabled(subtitleRendererIndex, false)
                .setPreferredTextLanguage(lang)
                .build()
        }
    }

    fun applyAudio(lang: String?) {
        if (lang == null) return  // optionally do nothing if null
        trackSelector.parameters = trackSelector.buildUponParameters()
            .setPreferredAudioLanguage(lang)
            .build()
    }

    fun applyVideo(label: String?) {
        val mappedTrackInfo = trackSelector.currentMappedTrackInfo ?: return
        val videoRendererIndex = (0 until mappedTrackInfo.rendererCount).firstOrNull { i ->
            mappedTrackInfo.getRendererType(i) == C.TRACK_TYPE_VIDEO
        } ?: return  // no video renderer found
        val trackGroups = mappedTrackInfo.getTrackGroups(videoRendererIndex)
        var foundGroupIndex: Int? = null
        var trackIndex: Int? = null

        for (groupIndex in 0 until trackGroups.length) {
            val group = trackGroups.get(groupIndex)
            for (i in 0 until group.length) {
                val f = group.getFormat(i)
                val l = f.label ?: "${f.width}x${f.height}"
                if (l == label) {
                    foundGroupIndex = groupIndex
                    trackIndex = i
                    break
                }
            }
            if (foundGroupIndex != null) break
        }
        if (foundGroupIndex != null && trackIndex != null) {
            val override = DefaultTrackSelector.SelectionOverride(foundGroupIndex, trackIndex)
            trackSelector.parameters = trackSelector.buildUponParameters()
                .setSelectionOverride(
                    /* rendererIndex = */ videoRendererIndex,
                    /* trackGroups = */ trackGroups,
                    /* override = */ override
                )
                .build()
        }
    }

    fun clearVideoOverride() {
        val mappedTrackInfo = trackSelector.currentMappedTrackInfo ?: return
        val videoRendererIndex = (0 until mappedTrackInfo.rendererCount)
            .firstOrNull { mappedTrackInfo.getRendererType(it) == C.TRACK_TYPE_VIDEO }
            ?: return
        trackSelector.parameters = trackSelector
            .buildUponParameters()
            .clearSelectionOverrides(videoRendererIndex)
            .build()
    }

    val applySettings = remember {
        {
            PreferenceManager.preferredAudio?.let { saved ->
                applyAudio(saved)
                selectedAudio.value = saved
            }
            PreferenceManager.preferredSubtitle?.let { saved ->
                applySubtitle(saved)
                selectedSubtitle.value = saved
            }
            PreferenceManager.preferredVideoQuality?.let { saved ->
                applyVideo(saved)
                selectedVideo.value = saved
            }
        }
    }
    LaunchedEffect(Unit) {
        context.hideKeyboard()
        showOverlay()
        applySettings()
        PreferenceManager.preferredAudio?.let { saved ->
            applyAudio(saved)
            selectedAudio.value = saved
        }
        PreferenceManager.preferredSubtitle?.let { saved ->
            applySubtitle(saved)
            selectedSubtitle.value = saved
        }
        PreferenceManager.preferredVideoQuality?.let { saved ->
            applyVideo(saved)
            selectedVideo.value = saved
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

            override fun onTracksChanged(tracks: Tracks) {
                subtitleTracks.clear()
                audioTracks.clear()
                videoTracks.clear()
                for (group in tracks.groups) {
                    if (group.type == C.TRACK_TYPE_TEXT) {
                        for (i in 0 until group.mediaTrackGroup.length) {
                            val format = group.mediaTrackGroup.getFormat(i)
                            val lang = format.language ?: format.label ?: "Unknown"
                            subtitleTracks.add(lang)
                        }
                    }
                    if (group.type == C.TRACK_TYPE_AUDIO) {
                        for (i in 0 until group.mediaTrackGroup.length) {
                            val format = group.mediaTrackGroup.getFormat(i)
                            val lang = format.language ?: format.label ?: "Unknown"
                            audioTracks.add(lang)
                        }
                    }
                    if (group.type == C.TRACK_TYPE_VIDEO) {
                        for (i in 0 until group.mediaTrackGroup.length) {
                            val format = group.mediaTrackGroup.getFormat(i)
                            val resolution = "${format.width}x${format.height}" // fallback if no label
                            val label = format.label ?: resolution
                            videoTracks.add(label)
                        }
                    }
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
            if(selectedChannel?.content?.contentType.equals("audio",true)){
                isAudio.value = true
            }else{
                isAudio.value = false
            }
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            showErrorDialog = false
            val mediaItem = if (selectedChannel?.content?.drmType.equals("cryptoguard", ignoreCase = true)) {
                context.provideCryptoGuardMediaSource( contentUrl = selectedChannel?.content?.videoUrl, contentId = selectedChannel?.content?.assetId)
            } else {
                MediaItem.fromUri(url)
            }
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true  //  Ensure playback starts automatically
            //make fingerprint request
            sharedViewModel.providePlayerSSERequest(channel = "${selectedChannel?.content?.channelNo}:${selectedChannel?.content?.title}")

        }
    }

    fun commitChannelSearch(numberStr: String) {
        val number = numberStr.toIntOrNull() ?: return
        val idx = epgList.indexOfFirst { (it.content?.channelNo ?: 0) == number }
        if (idx != -1) {
            sharedViewModel.updateSelectedChannel(epgList[idx])
            //selectedChannelIndex.intValue = idx
            // scroll so it’s centred (optional)
            //scope.launch { listState.animateScrollToItem(idx) }
        } else {
            context.showToastS("This Channel $number not available.")
            // optional: toast / blink to show “no such channel”
            // Log.d("ChannelSearch", "Channel $number not found")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusTarget()
            .background(Color.Black)
            .onPreviewKeyEvent { keyEvent ->
                val code = keyEvent.nativeKeyEvent.keyCode
                if (showVideoOverlay.value || showAudioOverlay.value || showSubtitleOverlay.value) {
                    return@onPreviewKeyEvent false
                }
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
                        KeyEvent.KEYCODE_DPAD_UP-> {
                            if (!isTopOverlayVisible && !isOverlayVisible) {
                                isTopOverlayVisible = true
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
                        }
                        KeyEvent.KEYCODE_DPAD_DOWN -> {
                            if (isTopOverlayVisible) {
                                isTopOverlayVisible = false
                                topOverlayHideJob?.cancel()
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
                        KeyEvent.KEYCODE_BACK -> {
                            if (isTopOverlayVisible) {
                                isTopOverlayVisible = false
                                return@onPreviewKeyEvent true
                            }
                            false
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            if (isTopOverlayVisible) {
                                return@onPreviewKeyEvent false
                            }
                            showOverlay()
                            if (previewChannelIndex.intValue < epgList.lastIndex) {
                                previewChannelIndex.intValue++
                                currentProgrammeIndex.intValue = 0 // reset
                                startSwitchCountdown()
                            }
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_CENTER -> {
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
                BottomFullScreenPlayerOverlay(
                    channel = it,
                    sharedViewModel = sharedViewModel,
                    playerViewModel = playerViewModel,
                    onOptionClick = {},
                    programmeIndex = currentProgrammeIndex.intValue,
                )
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
                    Log.d("PlayerScreen", "Toggling fav (was=$isFav) for $channelId")
                    if (isFav) sharedViewModel.removeFavorite(channelId)
                    else       sharedViewModel.addFavorite(channelId)
                },
                onAudioClick = {
                    lastTopOverlayButtonFocus.value = audioButtonFocusRequester
                    showAudioOverlay.value = true
                },
                onSubtitlesClick = {
                    lastTopOverlayButtonFocus.value = subtitleButtonFocusRequester
                    showSubtitleOverlay.value = true
                },
                onHdClick = {
                    lastTopOverlayButtonFocus.value = videoButtonFocusRequester
                    showVideoOverlay.value = true
                },
                qualityLabel     = qualityLabel,
                subtitleButtonFocusRequester = subtitleButtonFocusRequester,
                audioButtonFocusRequester = audioButtonFocusRequester,
                videoButtonFocusRequester = videoButtonFocusRequester,
                onDismiss = {
                    isTopOverlayVisible = false
                }

            )
        }
        // AUDIO
        if (showAudioOverlay.value) {
            SelectionOverlay(
                modifier       = Modifier.focusRequester(overlayFocusRequester).focusable(),
                focusRequester = overlayFocusRequester,
                title          = "Audio",
                options        = if (audioTracks.isNotEmpty())
                    audioTracks
                else listOf("Audio unavailable"),
                selected       = selectedAudio.value,
                onSelect       = { lang ->
                    // only apply if real
                    if (audioTracks.isNotEmpty()) {
                        PreferenceManager.preferredAudio = lang
                        selectedAudio.value = lang
                        applyAudio(lang)
                    }
                    showAudioOverlay.value = false
                },
                onDismiss      = { showAudioOverlay.value = false }
            )
        }

        // SUBTITLES
        if (showSubtitleOverlay.value) {
            SelectionOverlay(
                modifier       = Modifier.focusRequester(overlayFocusRequester).focusable(),
                focusRequester = overlayFocusRequester,
                title          = "Subtitles",
                options        = if (subtitleTracks.isNotEmpty())
                    subtitleTracks + "Off"
                else listOf("Subtitles unavailable"),
                selected       = selectedSubtitle.value ?: if (subtitleTracks.isNotEmpty()) subtitleTracks.first() else null,
                onSelect       = { choice ->
                    if (subtitleTracks.isNotEmpty() && choice != "Off") {
                        PreferenceManager.preferredSubtitle = choice
                        selectedSubtitle.value = choice
                        applySubtitle(choice)
                    } else {
                        PreferenceManager.preferredSubtitle = null
                        selectedSubtitle.value = null
                    }
                    showSubtitleOverlay.value = false
                },
                onDismiss      = { showSubtitleOverlay.value = false }
            )
        }

        // VIDEO QUALITY
        if (showVideoOverlay.value) {
            SelectionOverlay(
                focusRequester   = overlayFocusRequester,
                title            = "Video Quality",
                options          = if (videoTracks.isNotEmpty())
                    videoTracks
                else listOf("Quality unavailable"),
                selected         = selectedVideo.value,
                extraTopOption   = if (videoTracks.isNotEmpty()) "Auto" else null,
                onExtraTopSelect = {
                    if (videoTracks.isNotEmpty()) {
                        clearVideoOverride()
                        selectedVideo.value = null
                    }
                    showVideoOverlay.value = false
                },
                onSelect         = { quality ->
                    if (videoTracks.isNotEmpty()) {
                        PreferenceManager.preferredVideoQuality = quality
                        selectedVideo.value = quality
                        applyVideo(quality)
                    }
                    showVideoOverlay.value = false
                },
                onDismiss        = { showVideoOverlay.value = false }
            )
        }
    }
    DisposableEffect(Unit) {
        onDispose { watchJob?.cancel() }
    }
}


