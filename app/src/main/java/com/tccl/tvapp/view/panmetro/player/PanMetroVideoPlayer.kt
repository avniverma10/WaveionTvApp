package com.tccl.tvapp.view.panmetro.player

import ForceMessageDialog
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
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
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import coil3.compose.rememberAsyncImagePainter
import coil3.gif.GifDecoder
import coil3.request.ImageRequest
import com.android.tccl.R
import com.tccl.tvapp.extensions.hideKeyboard
import com.tccl.tvapp.extensions.loge
import com.tccl.tvapp.extensions.playerErrorHandling
import com.tccl.tvapp.extensions.provideCryptoGuardMediaSource
import com.tccl.tvapp.extensions.showToastS
import com.tccl.tvapp.extensions.toJSONObject
import com.tccl.tvapp.utils.uistate.PreferenceManager
import com.tccl.tvapp.view.uicomponent.InputOverlay
import com.tccl.tvapp.view.uicomponent.addWatermarkToPlayer
import com.tccl.tvapp.view.uicomponent.audio.AnimatedAudio
import com.tccl.tvapp.view.uicomponent.error.CommonDialog
import com.tccl.tvapp.view.uicomponent.fingerprint.ChannelFingerprintOverlay
import com.tccl.tvapp.view.uicomponent.fingerprint.ScrollingMessageOverlay
import com.tccl.tvapp.view.uicomponent.fingerprint.state.ForceMessageDialogState
import com.tccl.tvapp.view.uicomponent.keyboard.HideKeyboardOnEnter
import com.tccl.tvapp.viewmodels.SharedViewModel
import com.tccl.tvapp.viewmodels.player.PlayerViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.orEmpty
import kotlin.random.Random

@OptIn(UnstableApi::class)
@Composable
fun PanMetroVideoPlayer(
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
    var isAudio = remember { mutableStateOf(false) }
    // --- Channel‑number search state -------------------------------
    var typedDigits     by remember { mutableStateOf("") }   // the running “123”
    var inputJob        by remember { mutableStateOf<Job?>(null) }
    val DEBOUNCE_MS = 1_000L                                 // change if you want longer


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

    //hide keyboard forcefully
    HideKeyboardOnEnter()
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

    val categoryName by sharedViewModel.currentPlaylistName.collectAsState()

    // State management
    var isOverlayVisible by remember { mutableStateOf(true) }
    var lastInteractionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var overlayHideJob by remember { mutableStateOf<Job?>(null) }


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
            if(selectedChannel?.content?.contentType.equals("audio",true)){
                isAudio.value = true
            }else{
                isAudio.value = false
            }
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
        //sharedViewModel.updateLanguage(null)                         // clear language filter
        //sharedViewModel.updateGenre(null)                            // clear genre filter
        sharedViewModel.setCurrentPlaylist(emptyList(), "All Channels")
        navController.popBackStack()
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
                        KeyEvent.KEYCODE_BACK -> {
                            PreferenceManager.selectedGenreIndex  = 0
                            PreferenceManager.selectedChannelIndex = 0
                            PreferenceManager.lastEpgDataItem       = null
                            navController.popBackStack()
                            true
                        }

//                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
//                            navController.navigate(Destination.genreScreen) {
//                                PreferenceManager.selectedGenreIndex = 0
//                                PreferenceManager.selectedChannelIndex = 0
//                                PreferenceManager.lastEpgDataItem = null
//                                popUpTo(Destination.panMetroScreen) { inclusive = true }
//                            }
//                            true
//                        }
//                        KeyEvent.KEYCODE_DPAD_LEFT -> {
//                            navController.navigate(Destination.epgScreen) {
//                                PreferenceManager.lastEpgDataItem = null
//                                popUpTo(Destination.panMetroScreen) { inclusive = true }
//                            }
//                            true
//                        }
                        KeyEvent.KEYCODE_DPAD_UP-> {
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_DOWN-> {
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_CENTER -> {
                            if (selectedChannelIndex.intValue>=0 && selectedChannelIndex.intValue < (epgList.size)) {
                                sharedViewModel.updateSelectedChannel(epgList[selectedChannelIndex.intValue])
                            }
                            true
                        }

                        KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_LEFT-> {
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
                        KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_RIGHT-> {
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

        if(isAudio.value){
            val gifPainter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("file:///android_asset/tiled_bar_anim.gif")
                    .decoderFactory(GifDecoder.Factory())
                    .build(),
                contentScale = ContentScale.FillWidth,
            )


            //playerViewModel.initPlayer(exoPlayer)
           // AudioExoplayerPlayerScreen(navController = navController,sharedViewModel= sharedViewModel,playerViewModel= playerViewModel)
           // AudioPlayerScreen(exoPlayer = exoPlayer)
            /*Box(modifier = Modifier.fillMaxSize()
                .background(brush, shape = RoundedCornerShape(0.dp)),
                contentAlignment = Alignment.Center) {
                AudioVisualizerCompose(
                    exoPlayer = exoPlayer,
                    gifAssetPath= "tiled_bar_anim.gif",
                    isPlaying = true,
                    currentTime = 0L,
                    totalTime = 20L,
                    onPlayPause = {
                       // viewModel.togglePlayback()
                    },
                    onSliderChange = {
                       // viewModel.seekTo(it.toLong())
                    }
                )
            }*/
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

                    /*Image(
                        painter = gifPainter,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentScale = ContentScale.Crop
                    )*/
                }
            }
        }


        //Show dialogs
        if((playerSSERules?.forceMessages?.size ?: 0) > 0){
            dialogStates.forEachIndexed { index, dialogState ->
                ForceMessageDialog(
                    showDialog = true,
                    forceMessage = dialogState.message,
                    onConfirm = {
                        // Mark this dialog as dismissed
                        dialogStates[index] = dialogState.copy(show = false)
                    }
                )
            }
        }

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

        //Numeric channel search
        if(typedDigits.isNotEmpty()){
            InputOverlay(typedDigits = typedDigits)
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
        if (isOverlayVisible && selectedChannelIndex.intValue >=0) {
            FullScreenPlayerOverlay(
                selectedIndex         = selectedChannelIndex,
                lazyListState         = listState,
                sharedViewModel       = sharedViewModel,
                playerViewModel       = playerViewModel,
                epgList               = epgList,
                categoryName          = categoryName,
                languageName          = language,
                channelFocusRequesters = channelRequesters,
                onChannelFocused      = { sharedViewModel.updateSelectedChannel(it) }
            )
        }
    }


}
