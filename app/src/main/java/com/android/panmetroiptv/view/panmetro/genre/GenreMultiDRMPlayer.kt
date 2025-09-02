package com.android.panmetroiptv.view.panmetro.genre

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.ui.PlayerView
import com.android.panmetroiptv.R
import com.android.panmetroiptv.extensions.extractYouTubeId
import com.android.panmetroiptv.extensions.loge
import com.android.panmetroiptv.extensions.playerErrorHandling
import com.android.panmetroiptv.extensions.provideCryptoGuardMediaSource
import com.android.panmetroiptv.extensions.showToastS
import com.android.panmetroiptv.extensions.toJSONObject
import com.android.panmetroiptv.model.data.sseresponse.PlayerFingerprint
import com.android.panmetroiptv.utils.Constants
import com.android.panmetroiptv.utils.uistate.PreferenceManager
import com.android.panmetroiptv.view.uicomponent.audio.AnimatedAudio
import com.android.panmetroiptv.view.uicomponent.error.BlockUserScreen
import com.android.panmetroiptv.view.uicomponent.error.PlaybackErrorPreview
import com.android.panmetroiptv.view.uicomponent.fingerprint.PrePlayerFingerprintOverlay
import com.android.panmetroiptv.view.uicomponent.fingerprint.ScrollingMessageOverlay
import com.android.panmetroiptv.viewmodels.SharedViewModel
import com.android.panmetroiptv.viewmodels.genre.GenreViewModel
import com.techit.youtubelib.PlayerConstants
import com.techit.youtubelib.interfaces.YouTubePlayer
import com.techit.youtubelib.listeners.AbstractYouTubePlayerListener
import com.techit.youtubelib.options.IFramePlayerOptions
import com.techit.youtubelib.view.YouTubePlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(UnstableApi::class)
@Composable
fun GenreMultiDRMPlayer(
    selectedChannelIndex : MutableState<Int>,
                        sharedViewModel: SharedViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val selectedVideoUrl by sharedViewModel.selectedChannel.collectAsState()
    val appPkgChannels by sharedViewModel.appPkgChannels.collectAsStateWithLifecycle()
    val availablePkg by sharedViewModel.availablePkg.collectAsStateWithLifecycle()
    val prePlayerSSERules by sharedViewModel.prePlayerSSERules.collectAsState()
    val visibleFingerprint = remember { mutableStateListOf<PlayerFingerprint>() }
    val globalSSERules by sharedViewModel.globalSSERules.collectAsState()

    // Mutable state for UI updates
    var isAudio = remember { mutableStateOf(false) }
    var isYoutube = remember { mutableStateOf(false) }
    val youtubeId = remember { mutableStateOf<String?>(null) }
    var isDRMUrl = remember { mutableStateOf(false) }

    // Mutable state for UI updates
    val isBuffering = rememberSaveable { mutableStateOf(false) }

    val playerView = remember {
        mutableStateOf<PlayerView?>(null)
    }




    var showErrorDialog by remember { mutableStateOf(false) }
    var errorCodeState by remember { mutableStateOf(0) }
    var errorTitle by remember { mutableStateOf("") }
    var errorMessageState by remember { mutableStateOf("") }

    // Set FLAG_SECURE if desired.
    (context as? Activity)?.window?.setFlags(
        WindowManager.LayoutParams.FLAG_SECURE,
        WindowManager.LayoutParams.FLAG_SECURE
    )

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                // optional: any static setup
                playWhenReady = true
            }
    }

    // Remember the player and recreate it when the DRM type changes
    DisposableEffect(exoPlayer) {
        // Analytics listener (unchanged)
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
                    isBuffering.value = false
                    //Show your dialog
                    val httpCode = (error.cause as? HttpDataSource.InvalidResponseCodeException)
                        ?.responseCode
                    val rawCode = httpCode ?: error.errorCode
                    val (code, title, message) = playerErrorHandling(rawCode)
                    errorCodeState = code
                    errorMessageState = message
                    showErrorDialog = true

                    //Schedule a retry in 0-2 minutes
                    scope.launch {
                        val retryDelay = Random.nextLong(0L, 120_000L)
                        loge("Retry", "Retrying live stream in ${retryDelay / 1000}s…")
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
                        showErrorDialog = false
                    }

                    isBuffering.value = (playbackState == Player.STATE_BUFFERING)
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
            errorTitle = title
            errorMessageState = message
            showErrorDialog = true
            exoPlayer.clearMediaItems()
        }else{
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true  //  Ensure playback starts automatically
            //make fingerprint request
            sharedViewModel.providePrePlayerSSERequest(channel = "${selectedVideoUrl?.content?.channelNo}:${selectedVideoUrl?.content?.title}")
        }
    }




    LaunchedEffect(globalSSERules) {
        if(globalSSERules?.blockUser?.size == 0){
            selectedVideoUrl.content?.videoUrl?.let {
                handleMediaUrlAllowToPlay(videoUrl = it, assetId = selectedVideoUrl.content?.assetId )
            }
            return@LaunchedEffect
        }
        globalSSERules?.blockUser?.forEach {
            if(PreferenceManager.getUsername()?.equals(it.username) == true && it.isBlocked == 1){
                exoPlayer.clearMediaItems()
            }
        }
    }

    // Whenever the selected channel changes, load its media
    LaunchedEffect(selectedVideoUrl) {
        loge("selectedVideoUrl>","$selectedChannelIndex")
        selectedVideoUrl.content?.videoUrl?.takeIf { it.isNotEmpty() }?.let { url ->
            if(selectedVideoUrl.content?.drmType.equals("cryptoguard", ignoreCase = true)){
                isDRMUrl.value = true
            }else{
                isDRMUrl.value = false
            }


            if(selectedVideoUrl?.content?.contentType.equals("audio",true)){
                isAudio.value = true
            }else if(selectedVideoUrl?.content?.contentType.equals("youtube",true)){
                isYoutube.value = true
                youtubeId.value = selectedVideoUrl?.content?.videoUrl?.extractYouTubeId()
            }else{
                isAudio.value = false
                isYoutube.value = false
            }
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            showErrorDialog = false
            if(!isYoutube.value) {
                handleMediaUrlAllowToPlay(videoUrl = url, assetId = selectedVideoUrl.content?.assetId )
            }
        }
    }


    LaunchedEffect(appPkgChannels,availablePkg) {
        if(!isYoutube.value) {
            selectedVideoUrl.content?.videoUrl?.let {
                handleMediaUrlAllowToPlay(videoUrl = it, assetId = selectedVideoUrl.content?.assetId )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        val hasVideo = selectedVideoUrl.content?.videoUrl?.isNotEmpty() == true
        /*if (!hasVideo && isBuffering.value ) {
            Image(
                painter = painterResource(id = R.drawable.panlogin),
                contentDescription = "Panmetro Poster",
                modifier = Modifier.width(200.dp).height(200.dp),
                contentScale = ContentScale.Fit
            )
        }*/
        if(isYoutube.value){
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
                                /*val ui = DefaultPlayerUiController(ytView, player).apply {
                                    showSeekBar(false)       // hide progress bar
                                    showYouTubeButton(false) // hide “Y” logo
                                    showVideoTitle(false)    // hide title
                                }
                                player.setCustomPlayerUi(ui.rootView)*/

                                youtubeId.value?.let {
                                    Log.e("loadYoutubeVideo","$youtubeId")
                                    player.loadVideo(it, 0f)
                                    // youtubeId.value = it
                                }
                            }

                            override fun onStateChange(
                                youTubePlayer: YouTubePlayer,
                                state: PlayerConstants.PlayerState
                            ) {
                                if(state == PlayerConstants.PlayerState.PLAYING){
                                    isBuffering.value = false
                                }
                                super.onStateChange(youTubePlayer, state)
                            }

                            override fun onError(
                                youTubePlayer: YouTubePlayer,
                                error: PlayerConstants.PlayerError
                            ) {
                                isBuffering.value = false
                                super.onError(youTubePlayer, error)
                            }
                        }, opts)
                    }
                },
                update = { view ->
                    // If the composable is still alive but the videoId changed, load the new video
                    /* if (lastVideoId.value != videoId) {
                         *//*view.getYouTubePlayerWhenReady { youTubePlayer ->
                            youTubePlayer.loadVideo(videoId, 0f)
                            lastVideoId.value = videoId
                        }*//*
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
                    }

                    view

                }
            )
        }


        val stops = selectedVideoUrl.content?.bgGradient
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


        if(isAudio.value){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush, shape = RoundedCornerShape(0.dp)),
                contentAlignment = Alignment.Center
            ) {

                Box(
                    modifier = Modifier
                        .widthIn(max = LocalConfiguration.current.screenWidthDp.dp * 0.6f)
                        .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.5f)
                        .clip(MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedAudio(
                        isSongPlaying = true,
                        channel = selectedVideoUrl
                    )
                }
            }
        }

        /*if ((playerSSERules?.scrollMessages?.size ?: 0) > 0) {
            playerSSERules?.scrollMessages?.forEach {
                ScrollingMessageOverlay(
                    player = playerView.value,
                    scrollMessageInfo = mutableStateOf(it)
                )
            }
        }*/

        // Show Loading Indicator if Buffering
        /*Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isBuffering.value) {
                Image(
                    painter = painterResource(id = R.drawable.panlogin),
                    contentDescription = "Loading poster",
                    modifier = Modifier.width(200.dp).height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }*/

        if (showErrorDialog) {
            PlaybackErrorPreview(
                errorCode = errorCodeState,
                title = errorTitle,
                errorMessage = errorMessageState,
                modifier = Modifier.align(Alignment.Center)
            )
        }



        if ((prePlayerSSERules?.fingerprints?.size ?: 0) > 0) {
            prePlayerSSERules?.fingerprints?.forEach {
                PrePlayerFingerprintOverlay(
                    fingerprintRule = it
                )
            }
        }
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> {6
                        exoPlayer.pause()
                    }
                    Lifecycle.Event.ON_STOP -> {
                        if (sharedViewModel.goingToFullPlayer.value) {
                            // Case A: going full screen → free secure decoder
                            exoPlayer.stop()
                            exoPlayer.clearMediaItems()
                            exoPlayer.release()
                            sharedViewModel.goingToFullPlayer.value = false // reset flag
                        } else {
                            // Case B: app minimized → keep preview alive
                            exoPlayer.pause()
                        }
                    }
                    Lifecycle.Event.ON_START -> {
                        exoPlayer.play()
                    }
                    else -> Unit
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                exoPlayer.stop()
                sharedViewModel.stopPrePlayerSSE()
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }
}


