import android.content.Context
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.tvapp.viewmodels.ExoPlayerViewModel
import com.example.applicationscreens.models.Data
//import com.example.tvapp.utils.DeviceInfoHelper

@Composable
fun ExoPlayerScreen(viewModel: ExoPlayerViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val videoList by remember { viewModel.videoList }
    val isLoading by remember { viewModel.isLoading }

//    val deviceInfo = remember { DeviceInfoHelper.getDeviceInfo(context) }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        if (selectedIndex == null) {
            // Show Video List
            VideoList(videoItems = videoList) { index ->
                selectedIndex = index
            }
        } else {
            // Show ExoPlayer in Full Screen
            ExoPlayerView(
                context = context,
                videoList = videoList,
                currentIndex = selectedIndex!!,
                onIndexChange = { newIndex -> selectedIndex = newIndex },
                onExitPlayer = { selectedIndex = null }
            )
        }
    }
}

@Composable
fun VideoList(videoItems: List<Data>, onVideoSelected: (Int) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().background(Color.White)) {
        items(videoItems.withIndex().toList()) { (index, videoItem) ->
            Text(
                text = videoItem.title,
                modifier = Modifier
                    .padding(16.dp)
                    .clickable { onVideoSelected(index) },
                color = Color.Black
            )
        }
    }
}


@OptIn(UnstableApi::class)
@Composable
fun ExoPlayerView(
    context: Context,
    videoList: List<Data>,
    currentIndex: Int,
    onIndexChange: (Int) -> Unit,
    onExitPlayer: () -> Unit
) {
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItems = videoList.map { MediaItem.fromUri(it.videoUrl) }
            setMediaItems(mediaItems, currentIndex, 0L)
            prepare()
            playWhenReady = true
        }
    }

    // Update ExoPlayer when index changes
    LaunchedEffect(currentIndex) {
        exoPlayer.seekTo(currentIndex, 0L)
        exoPlayer.playWhenReady = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                    setShowNextButton(true)  // Enable Next button
                    setShowPreviousButton(true) // Enable Previous button
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    // Handle Next & Previous Button Clicks
    LaunchedEffect(Unit) {
        exoPlayer.addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                val newIndex = player.currentMediaItemIndex
                if (newIndex != currentIndex) {
                    onIndexChange(newIndex)
                }
            }
        })
    }

}
