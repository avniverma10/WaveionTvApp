
import android.view.LayoutInflater
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.tvapp.viewmodels.ExoPlayerViewModel
import com.example.applicationscreens.models.Data
import com.example.tvapp.R

@Composable
fun ExoPlayerScreen(viewModel: ExoPlayerViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val videoList by remember { viewModel.videoList }
    val isLoading by remember { viewModel.isLoading }

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
                viewModel.initializePlayer(context, videoList, index)
            }
        } else {
            // Show ExoPlayer in Full Screen
            ExoPlayerView(
                viewModel = viewModel,
               currentIndex = selectedIndex!!,
                onExitPlayer = { selectedIndex = null }
            )
        }
    }
}

@Composable
fun VideoList(videoItems: List<Data>, onVideoSelected: (Int) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        items(videoItems.withIndex().toList()) { (index, videoItem) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onVideoSelected(index) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail Image
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(videoItem.thumbnailUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Thumbnail",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Video Title
                Text(
                    text = videoItem.title,
                    color = Color.White
                )
            }
        }

    }
}


@OptIn(UnstableApi::class)
@Composable
fun ExoPlayerView(
    viewModel: ExoPlayerViewModel,
    currentIndex: Int,
    onExitPlayer: () -> Unit
) {

    val exoPlayer = viewModel.exoPlayer ?: return

    // Handle Back Press to Exit Player
    BackHandler {
        viewModel.stopPlayback()
        onExitPlayer()
    }


    // Update ExoPlayer when index changes
    LaunchedEffect(currentIndex) {
        exoPlayer.seekTo(currentIndex, 0L)
        exoPlayer.playWhenReady = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val view = LayoutInflater.from(ctx).inflate(R.layout.exoplayer_view, null)
                val playerView = view.findViewById<androidx.media3.ui.PlayerView>(R.id.player_view)
                playerView.player = exoPlayer
                playerView.useController = true
                view
//                PlayerView(ctx).apply {
//                    player = exoPlayer
//                    useController = true
//                    setShowNextButton(true)  // Enable Next button
//                    setShowPreviousButton(true) // Enable Previous button
//                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }

}
