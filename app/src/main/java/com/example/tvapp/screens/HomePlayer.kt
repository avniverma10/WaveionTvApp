package com.example.tvapp.screens


import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.analytics.AnalyticsListener
import com.example.tvapp.R

@Composable
fun HomePlayer(navController: NavController, videoUrl: String) {
    val context = LocalContext.current

    // 🔹 DRM Configuration (Widevine)
    val widevinePX = "AB2909"
    val licenseUrl = "https://widevine-dash.ezdrm.com/proxy?pX=$widevinePX"
    val drmConfiguration = MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
        .setLicenseUri(licenseUrl)
        .build()

    val drmSupported = try {
        android.media.MediaDrm.isCryptoSchemeSupported(C.WIDEVINE_UUID)
    } catch (e: Exception) {
        false
    }

    Log.i("DRM", "Widevine Supported: $drmSupported")

    // 🔹 ExoPlayer with DRM
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            addAnalyticsListener(object : AnalyticsListener {
                @OptIn(UnstableApi::class)
                override fun onEvents(player: Player, events: AnalyticsListener.Events) {
                    if (events.contains(AnalyticsListener.EVENT_DRM_KEYS_LOADED)) {
                        Log.d("DRM", "Keys loaded successfully")
                    }
                    if (events.contains(AnalyticsListener.EVENT_DRM_SESSION_MANAGER_ERROR)) {
                        Log.e("DRM", "Session manager error")
                    }
                }
            })
        }
    }

    // 🔹 Set Media Item with DRM Support
    LaunchedEffect(videoUrl) {
        Log.d("HomePlayer", "Loading video: $videoUrl")

        val mediaItem = if (drmSupported) {
            MediaItem.Builder()
                .setUri(Uri.parse(videoUrl))
                .setDrmConfiguration(drmConfiguration)  // Add DRM
                .build()
        } else {
            Log.e("DRM", "Device does not support DRM. Playing clear content.")
            MediaItem.fromUri(Uri.parse(videoUrl))
        }

        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    // 🔹 Player UI
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black)
    ) {
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

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
}

