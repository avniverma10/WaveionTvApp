package com.example.tvapp.screens

import android.content.Context
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.widget.TextView
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import com.example.tvapp.models.DataStoreManager
import com.example.tvapp.models.EPGChannel
import com.example.tvapp.utils.JwtUtils
import com.example.tvapp.utils.JwtUtils.decodeJwtToken
import com.example.tvapp.utils.saveHashToDatabase
import com.example.tvapp.viewmodels.EPGViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest
import android.graphics.Color as AndroidColor
import android.os.Handler
import android.os.Looper
import kotlin.random.Random

@Composable
fun VideoPlayer(
    initialVideoUrl: String,
    allChannels: List<EPGChannel>,
    epgViewModel: EPGViewModel,
    onVideoChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }

    // Collect logged-in phone number from DataStore
    val phoneNumberRaw by dataStoreManager.authToken.collectAsState(initial = null)

    // ✅ Only extract phone number if token is not null
    val phoneNumber = phoneNumberRaw?.let { JwtUtils.decodeJwtToken(it) }

    Log.d("HASH","User phone number is --> $phoneNumber")
    // Get Android device ID
    fun getDeviceId(context: Context): String {
        Log.d("HASH","Getting DEVICE ID --> ${Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)}")
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
    val deviceId = getDeviceId(context)

    // Generate Watermark Hash
    fun generateWatermark(userPhone: String?, deviceId: String): String {
        val input = "$userPhone-$deviceId"
        Log.d("HASH","Input is --> $input")
        val digest = MessageDigest.getInstance("SHA-1")
        Log.d("HASH","digest is --> $digest")
        val hashBytes = digest.digest(input.toByteArray())
        Log.d("HASH","hashBytes is --> $hashBytes")
        val hashString = hashBytes.joinToString("") { "%02x".format(it) }
        Log.d("HASH", "Generated Hash: $hashString")

        return hashString.take(8) // Use the first 12 characters for brevity
    }
    val watermarkHash = generateWatermark(phoneNumber, deviceId)


    var currentIndex by remember { mutableStateOf(allChannels.indexOfFirst { it.videoUrl == initialVideoUrl }) }
    val currentChannel = allChannels.getOrNull(currentIndex)

    var isOverlayVisible by remember { mutableStateOf(false) } // Track overlay visibility
    val nextProgram by epgViewModel.nextProgram.collectAsState()

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    // ✅ Auto-hide overlay after 5 seconds
    LaunchedEffect(isOverlayVisible) {
        if (isOverlayVisible) {
            kotlinx.coroutines.delay(5000)
            isOverlayVisible = false
        }
    }

    // ✅ Update video when channel changes
    LaunchedEffect(currentIndex) {
        if (currentIndex in allChannels.indices) {
            val newVideoUrl = allChannels[currentIndex].videoUrl ?: ""
            Log.d("AVNI99", "Playing video: $newVideoUrl at index: $currentIndex")
            exoPlayer.setMediaItem(MediaItem.fromUri(newVideoUrl))
            exoPlayer.prepare()
        }
    }

    // ✅ Fetch next program info
    LaunchedEffect(currentChannel) {
        currentChannel?.id?.let { channelId ->
            val currentTime = System.currentTimeMillis().toString()
            epgViewModel.fetchNextProgram(channelId, currentTime)
        }
    }

    fun playNextChannel() {
        if (currentIndex < allChannels.lastIndex) {
            currentIndex++
            onVideoChange(allChannels[currentIndex].videoUrl ?: "")
        }
    }

    fun playPreviousChannel() {
        if (currentIndex > 0) {
            currentIndex--
            onVideoChange(allChannels[currentIndex].videoUrl ?: "")
        }
    }

    fun showOverlay() {
        isOverlayVisible = true
    }



    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusable()
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_CHANNEL_UP -> {
                            playNextChannel()
                            showOverlay()
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_CHANNEL_DOWN -> {
                            playPreviousChannel()
                            showOverlay()
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_DOWN -> {
                            showOverlay()
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_UP -> {
                            isOverlayVisible = false
                            true
                        }
                        else -> false
                    }
                } else false
            }
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                    keepScreenOn = true

                    // Add watermark to player
                    addWatermarkToPlayer(this, watermarkHash)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isOverlayVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(16.dp)
                        .align(Alignment.BottomStart)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AsyncImage(
                            model = currentChannel?.logoUrl,
                            contentDescription = "Channel Logo",
                            modifier = Modifier
                                .size(80.dp)
                                .background(Color.Gray)
                        )

                        Text(
                            text = currentChannel?.name ?: "Unknown Channel",
                            color = Color.White,
                            fontSize = 20.sp,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .weight(1f)
                        )
                    }
                }

                nextProgram?.let {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .background(Color.Black.copy(alpha = 0.9f))
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Next: ${it.eventName}",
                                color = Color.White,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}





fun addWatermarkToPlayer(playerView: PlayerView, watermarkText: String) {
    val context = playerView.context
    val textView = TextView(context).apply {
        text = watermarkText
        setTextColor(AndroidColor.WHITE)
        textSize = 14f
        alpha = 0.3f // Visible but subtle
        setPadding(16, 16, 16, 16)
    }

    val overlayLayout = FrameLayout(context).apply {
        addView(textView)
    }

    playerView.addView(overlayLayout)

    val handler = Handler(Looper.getMainLooper())

    val updatePositionRunnable = object : Runnable {
        override fun run() {
            playerView.post {
                val parentWidth = playerView.width
                val parentHeight = playerView.height

                textView.measure(0, 0) // Ensure TextView is measured
                val textWidth = textView.measuredWidth
                val textHeight = textView.measuredHeight

                if (parentWidth > textWidth && parentHeight > textHeight) {
                    val maxX = parentWidth - textWidth
                    val maxY = parentHeight - textHeight

                    if (maxX > 0 && maxY > 0) {
                        val randomX = Random.nextInt(0, maxX)
                        val randomY = Random.nextInt(0, maxY)

                        // ✅ Correctly update layout parameters
                        val layoutParams = overlayLayout.layoutParams as FrameLayout.LayoutParams
                        layoutParams.leftMargin = randomX
                        layoutParams.topMargin = randomY
                        overlayLayout.layoutParams = layoutParams

                        // ✅ Force layout refresh
                        overlayLayout.requestLayout()
                        overlayLayout.invalidate()
                    }
                }
            }
            handler.postDelayed(this, 50_00) // ✅ Move every 10 seconds
        }
    }

    // ✅ Only start moving the watermark once the layout is measured
    playerView.post {
        textView.measure(0, 0) // Ensure TextView gets measured before first position update
        handler.post(updatePositionRunnable) // Only start once
    }

    // ✅ Ensure the handler stops when the player is destroyed
    playerView.addOnAttachStateChangeListener(object : android.view.View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: android.view.View) {}
        override fun onViewDetachedFromWindow(v: android.view.View) {
            handler.removeCallbacks(updatePositionRunnable) // Stop movement when view is removed
        }
    })
}







//// ✅ Function to add watermark overlay to ExoPlayer
//fun addWatermarkToPlayer(playerView: PlayerView, watermarkText: String) {
//    val context = playerView.context
//    val textView = TextView(context).apply {
//        text = watermarkText
//        setTextColor(AndroidColor.WHITE)
//        textSize = 14f
//        alpha = 0.2f // 20% opacity
//        setPadding(16, 16, 16, 16)
//    }
//
//    val overlayLayout = FrameLayout(context).apply {
//        addView(textView)
//        layoutParams = FrameLayout.LayoutParams(
//            FrameLayout.LayoutParams.WRAP_CONTENT,
//            FrameLayout.LayoutParams.WRAP_CONTENT
//        ).apply {
//            marginStart = 20 // Left position
//            bottomMargin = 20 // Bottom position
//        }
//    }
//
//    playerView.addView(overlayLayout)
//}
