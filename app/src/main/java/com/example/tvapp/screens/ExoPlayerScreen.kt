//
//import android.view.LayoutInflater
//import androidx.activity.compose.BackHandler
//import androidx.annotation.OptIn
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.focusable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.lazy.itemsIndexed
//import androidx.compose.foundation.lazy.rememberLazyListState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.focus.FocusRequester
//import androidx.compose.ui.focus.focusRequester
//import androidx.compose.ui.focus.onFocusChanged
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.viewinterop.AndroidView
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.media3.common.util.UnstableApi
//import androidx.media3.ui.PlayerView
//import coil3.compose.AsyncImage
//import coil3.request.ImageRequest
//import coil3.request.crossfade
//import com.example.tvapp.viewmodels.ExoPlayerViewModel
//import com.example.applicationscreens.models.Data
//import com.example.tvapp.R
//import androidx.compose.ui.input.key.Key
//import androidx.compose.ui.input.key.KeyEventType
//import androidx.compose.ui.input.key.onKeyEvent
//import androidx.compose.ui.input.key.key
//import androidx.compose.ui.input.key.type
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//
//
//@Composable
//fun ExoPlayerScreen(viewModel: ExoPlayerViewModel = hiltViewModel()) {
//    val context = LocalContext.current
//    val videoList by remember { viewModel.videoList }
//    val isLoading by remember { viewModel.isLoading }
//
//    var selectedIndex by remember { mutableStateOf<Int?>(null) }
//    var lastPlayedIndex by remember { mutableStateOf<Int?>(null) } // Store last played index
//
//    if (isLoading) {
//        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//            CircularProgressIndicator()
//        }
//    } else {
//        if (selectedIndex == null) {
//            // Show Video List
//            VideoList(videoItems = videoList, initialScrollIndex = lastPlayedIndex,  ) { index ->
//                selectedIndex = index
//                lastPlayedIndex = index // Store last played index
//                viewModel.initializePlayer(context, videoList, index)
//            }
//        } else {
//            // Show ExoPlayer in Full Screen
//            ExoPlayerView(
//                viewModel = viewModel,
//               currentIndex = selectedIndex!!,
//                onExitPlayer = { selectedIndex = null }
//            )
//        }
//    }
//}
//
//@Composable
//fun VideoList(
//    videoItems: List<Data>,
//    initialScrollIndex: Int?,
//    onVideoSelected: (Int) -> Unit
//) {
//    val listState = rememberLazyListState()
//    val focusRequesters = remember { List(videoItems.size) { FocusRequester() } }
//    var focusedIndex by remember { mutableStateOf(0) }
//    val coroutineScope = rememberCoroutineScope()
//
//    LaunchedEffect(initialScrollIndex, videoItems) {
//        initialScrollIndex?.let {
//            listState.scrollToItem(it)
//            focusedIndex = it
//        }
//        if (videoItems.isNotEmpty()) {
//            focusRequesters[focusedIndex].requestFocus()
//        }
//    }
//
//    LazyColumn(
//        state = listState,
//        modifier = Modifier.fillMaxSize().background(Color.Black)
//    ) {
//        itemsIndexed(videoItems) { index, videoItem ->
//            val isFocused = focusedIndex == index
//
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .focusRequester(focusRequesters[index])
//                    .focusable()
//                    .onFocusChanged { focusState ->
//                        if (focusState.isFocused) {
//                            focusedIndex = index
//                            coroutineScope.launch {
//                                listState.animateScrollToItem(index)
//                            }
//                        }
//                    }
//                    .onKeyEvent { keyEvent ->
//                        if (keyEvent.type == KeyEventType.KeyDown) {
//                            when (keyEvent.nativeKeyEvent.keyCode) {
//                                android.view.KeyEvent.KEYCODE_DPAD_DOWN -> {
//                                    if (index < videoItems.lastIndex) {
//                                        coroutineScope.launch {
//                                            delay(100) // Add a small delay to prevent skipping
//                                            focusedIndex = index + 1
//                                            focusRequesters[index + 1].requestFocus()
//                                        }
//                                    }
//                                    true
//                                }
//                                android.view.KeyEvent.KEYCODE_DPAD_UP -> {
//                                    if (index > 0) {
//                                        coroutineScope.launch {
//                                            delay(100)
//                                            focusedIndex = index - 1
//                                            focusRequesters[index - 1].requestFocus()
//                                        }
//                                    }
//                                    true
//                                }
//                                else -> false
//                            }
//                        } else false
//                    }
//                    .clickable { onVideoSelected(index) }
//                    .background(if (isFocused) Color.Blue else Color.Black)
//                    .border(
//                        width = if (isFocused) 4.dp else 0.dp,
//                        color = if (isFocused) Color.White else Color.Transparent,
//                        shape = RoundedCornerShape(8.dp)
//                    )
//                    .padding(16.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                AsyncImage(
//                    model = ImageRequest.Builder(LocalContext.current)
//                        .data(videoItem.thumbnailUrl)
//                        .crossfade(true)
//                        .build(),
//                    contentDescription = "Thumbnail",
//                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp))
//                )
//
//                Spacer(modifier = Modifier.width(16.dp))
//
//                Text(
//                    text = videoItem.title,
//                    color = Color.White
//                )
//            }
//        }
//    }
//}
//
//@OptIn(UnstableApi::class)
//@Composable
//fun ExoPlayerView(
//    viewModel: ExoPlayerViewModel,
//    currentIndex: Int,
//    onExitPlayer: () -> Unit
//) {
//
//    val exoPlayer = viewModel.exoPlayer ?: return
//
//    // Handle Back Press to Exit Player
//    BackHandler {
//        viewModel.stopPlayback()
//        onExitPlayer()
//    }
//
//
//    // Update ExoPlayer when index changes
//    LaunchedEffect(currentIndex) {
//        exoPlayer.seekTo(currentIndex, 0L)
//        exoPlayer.playWhenReady = true
//    }
//
//    Box(modifier = Modifier.fillMaxSize()) {
//        AndroidView(
//            factory = { ctx ->
//                val view = LayoutInflater.from(ctx).inflate(R.layout.exoplayer_view, null)
//                val playerView = view.findViewById<androidx.media3.ui.PlayerView>(R.id.player_view)
//                playerView.player = exoPlayer
//                playerView.useController = true
//                // Prevent TV from sleeping
//                playerView.keepScreenOn = true
//                view
////                PlayerView(ctx).apply {
////                    player = exoPlayer
////                    useController = true
////                    setShowNextButton(true)  // Enable Next button
////                    setShowPreviousButton(true) // Enable Previous button
////                }
//            },
//            modifier = Modifier.fillMaxSize()
//        )
//    }
//
//}
