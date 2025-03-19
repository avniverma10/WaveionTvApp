package com.example.tvapp.screens

import android.app.Activity
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager
import androidx.media3.exoplayer.drm.FrameworkMediaDrm
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.example.tvapp.R
import com.example.tvapp.drm.WidevineMediaDrmCallback

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Set FLAG_SECURE if desired.
    (context as? Activity)?.window?.setFlags(
        WindowManager.LayoutParams.FLAG_SECURE,
        WindowManager.LayoutParams.FLAG_SECURE
    )

    // Create a default DataSource.Factory (Media3 version).
    val defaultDataSourceFactory = DefaultDataSource.Factory(context)

    // Create a DefaultHttpDataSource.Factory from Media3.
    val httpDataSourceFactory = DefaultHttpDataSource.Factory()

    // Create your WidevineMediaDrmCallback.
    // (Ensure you have implemented WidevineMediaDrmCallback as per your requirements.)
    val drmCallback = WidevineMediaDrmCallback(
        defaultLicenseUrl = "https://license-staging.sigmadrm.com/license/verify/widevine",
        forceDefaultLicenseUrl = false,
        dataSourceFactory = httpDataSourceFactory
    )

    // Create a DRM session manager using Media3's DefaultDrmSessionManager.Builder.
    val drmSessionManager = DefaultDrmSessionManager.Builder()
        .setUuidAndExoMediaDrmProvider(C.WIDEVINE_UUID) { uuid ->
            FrameworkMediaDrm.newInstance(uuid)
        }
        .build(drmCallback)

    // Create a media source factory with DRM integration.
    val mediaSourceFactory = DefaultMediaSourceFactory(defaultDataSourceFactory)
        .setDrmSessionManagerProvider { drmSessionManager }

    // Create a MediaItem from your video URL.
    val mediaItem = MediaItem.fromUri(videoUrl)

    // Build ExoPlayer with the DRM-enabled media source factory.
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().apply {
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
            }
    }

    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    // Inflate the PlayerView layout.
    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val view = LayoutInflater.from(ctx).inflate(R.layout.exoplayer_view, null)
                val playerView = view.findViewById<PlayerView>(R.id.player_view)
                playerView.player = exoPlayer
                playerView.useController = true
                playerView.keepScreenOn = true
                view
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
