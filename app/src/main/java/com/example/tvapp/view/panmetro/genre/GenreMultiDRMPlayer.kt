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
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.ui.PlayerView
import com.android.panmetroiptv.R
import com.example.tvapp.extensions.playerErrorHandling
import com.example.tvapp.extensions.provideCryptoGuardMediaSource
import com.example.tvapp.extensions.toJSONObject
import com.example.tvapp.view.uicomponent.error.PlaybackErrorPreview
import com.example.tvapp.viewmodels.SharedViewModel
import com.example.tvapp.viewmodels.genre.GenreViewModel
import java.net.SocketTimeoutException

@OptIn(UnstableApi::class)
@Composable
fun GenreMultiDRMPlayer(
    selectedChannelIndex : MutableState<Int>,
                        sharedViewModel: SharedViewModel,
                        genreViewModel: GenreViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val filteredChannels by genreViewModel.filteredPanMetroChannels.collectAsState()
    val selectedVideoUrl by sharedViewModel.selectedChannel.collectAsState()

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




    // Remember the player and recreate it when the DRM type changes
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .build()
            .apply {
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
                        if (error.cause is HttpDataSource.HttpDataSourceException ) {
                            // handle timeout: show a "Retry" UI
                            val (displayCode, message) = playerErrorHandling(2002)
                            errorCodeState = displayCode
                            errorMessageState = message
                            showErrorDialog = true
                        } else {
                            // handle other errors

                            val httpCode = (error.cause as? HttpDataSource.InvalidResponseCodeException)
                                ?.responseCode
                            var rawCode = httpCode ?: error.errorCode
                            Log.e("rawCode","$rawCode")
                            val (displayCode, message) = playerErrorHandling(rawCode)
                            Log.e("rawCode","$displayCode")
                            errorCodeState = displayCode
                            errorMessageState = message
                            showErrorDialog = true
                        }



                    }
                })
            }
    }

    // Whenever the selected channel changes, load its media
    LaunchedEffect(selectedVideoUrl) {
        Log.e("selectedVideoUrl>","$selectedChannelIndex")
        selectedVideoUrl.content?.videoUrl?.takeIf { it.isNotEmpty() }?.let { url ->
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            showErrorDialog = false
            val drmData = HashMap<String,String>()
            drmData.put("DRMType",selectedVideoUrl.content?.drmType?:"")
            drmData.put("contentId",selectedVideoUrl.content?.assetId?:"")
            drmData.put("contentUrl",selectedVideoUrl.content?.videoUrl?:""?:"")
            val mediaItem = if (selectedVideoUrl.content?.drmType.equals("cryptoguard", ignoreCase = true)) {
                context.provideCryptoGuardMediaSource(loginInfo = sharedViewModel.loginInfo?.value, contentUrl = selectedVideoUrl.content?.videoUrl, contentId = selectedVideoUrl.content?.assetId, logData = drmData)
            } else {
                MediaItem.Builder()
                    .setUri(url)
                    .setMimeType(MimeTypes.APPLICATION_MPD) // DASH
                    .build()
            }
            Log.e("Requested Data>",drmData.toJSONObject().toString())
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true  //  Ensure playback starts automatically
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
                val playerView = view.findViewById<PlayerView>(R.id.player_view)

                playerView.apply {
                    player = exoPlayer
                    useController = false
                    keepScreenOn = true
                }

                view

            }
        )

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


