package com.example.tvapp.view.panmetro.genre

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
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
import com.android.tccl.R
import com.example.tvapp.extensions.loge
import com.example.tvapp.extensions.playerErrorHandling
import com.example.tvapp.extensions.provideCryptoGuardMediaSource
import com.example.tvapp.extensions.toJSONObject
import com.example.tvapp.view.uicomponent.error.PlaybackErrorPreview
import com.example.tvapp.view.uicomponent.fingerprint.ChannelFingerprintOverlay
import com.example.tvapp.view.uicomponent.fingerprint.GlobalFingerprintOverlay
import com.example.tvapp.view.uicomponent.fingerprint.ScrollingMessageOverlay
import com.example.tvapp.viewmodels.SharedViewModel
import com.example.tvapp.viewmodels.genre.GenreViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(UnstableApi::class)
@Composable
fun GenreMultiDRMPlayer(
    selectedChannelIndex : MutableState<Int>,
                        sharedViewModel: SharedViewModel,
                        genreViewModel: GenreViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val filteredChannels by genreViewModel.filteredPanMetroChannels.collectAsState()
    val selectedVideoUrl by sharedViewModel.selectedChannel.collectAsState()
    val playerSSERules by sharedViewModel.playerSSERules.collectAsState()
    val playerView = remember {
        mutableStateOf<PlayerView?>(null)
    }


    // Mutable state for UI updates
    val isBuffering = rememberSaveable { mutableStateOf(false) }


    var showErrorDialog by remember { mutableStateOf(false) }
    var errorCodeState by remember { mutableStateOf(0) }
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
    // Remember the player and recreate it when the DRM type changes
    DisposableEffect(exoPlayer) {
        // Analytics listener (unchanged)
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
                // 1) Show your dialog
                val httpCode = (error.cause as? HttpDataSource.InvalidResponseCodeException)
                    ?.responseCode
                val rawCode = httpCode ?: error.errorCode
                val (code, title, message) = playerErrorHandling(rawCode)
                errorCodeState    = code
                errorMessageState = message
                showErrorDialog   = true

                // 2) Schedule a retry in 0-2 minutes
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
    LaunchedEffect(selectedVideoUrl) {
        loge("selectedVideoUrl>","$selectedChannelIndex")
        selectedVideoUrl.content?.videoUrl?.takeIf { it.isNotEmpty() }?.let { url ->
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            showErrorDialog = false
            val drmData = HashMap<String,String>()
            drmData.put("DRMType",selectedVideoUrl.content?.drmType?:"")
            drmData.put("contentId",selectedVideoUrl.content?.assetId?:"")
            drmData.put("contentUrl",selectedVideoUrl.content?.videoUrl?:""?:"")
            val mediaItem = if (selectedVideoUrl.content?.drmType.equals("cryptoguard", ignoreCase = true)) {
                context.provideCryptoGuardMediaSource(contentUrl = selectedVideoUrl.content?.videoUrl, contentId = selectedVideoUrl.content?.assetId, logData = drmData)
            } else {
                MediaItem.fromUri(url)
            }
            loge("Requested Data>",drmData.toJSONObject().toString())
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true  //  Ensure playback starts automatically
            //make fingerprint request
            sharedViewModel.providePlayerSSERequest(channel = "${selectedVideoUrl?.content?.channelNo}:${selectedVideoUrl?.content?.title}")

        }

    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
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
                ScrollingMessageOverlay(player= playerView.value, scrollMessageInfo = mutableStateOf(it))
            }
        }

        // Show Loading Indicator if Buffering
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isBuffering.value) {
                CircularProgressIndicator()
            }
        }

        if (showErrorDialog) {
            PlaybackErrorPreview(
                errorCode    = errorCodeState,
                errorMessage = errorMessageState,
                modifier     = Modifier.align(Alignment.Center)
            )
        }

    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                exoPlayer.pause()
            }else if (event == Lifecycle.Event.ON_START) {
                exoPlayer.play()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            exoPlayer.run {
                stop()
            }
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}


