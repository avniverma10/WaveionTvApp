package com.example.tvapp.view.panmetro

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tvapp.R
import com.example.tvapp.extensions.appGenreLiveData
import com.example.tvapp.model.data.epgdata.EPGDataItem
import com.example.tvapp.model.data.genre.WTVGenre
import com.example.tvapp.utils.Constants
import com.example.tvapp.view.navigationhelper.Destination
import com.example.tvapp.view.uicomponent.GradientBackground
import com.example.tvapp.viewmodels.SharedViewModel


@Composable
fun PanmetroGenreScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel
) {
    val context = LocalContext.current
    val filteredChannels by sharedViewModel.filteredPanMetroChannels.collectAsState()
    val epgList by sharedViewModel.epgDataList.collectAsState()

    val availableGenre by sharedViewModel.availableGenre.collectAsState()


    // Track currently selected indices
    var selectedChannelIndex by remember { mutableIntStateOf(0) }
    // FocusRequester for the channel list area.
    var genreListFocusRequester = remember { FocusRequester() }
    var channelListFocusRequester = remember { FocusRequester() }

    // When a channel is selected, update the video URL in the ViewModel.
    val onVideoChange: (EPGDataItem,Int) -> Unit = { channel,channelIndex ->
        selectedChannelIndex = channelIndex
        channel.tv?.programme?.let {
            sharedViewModel.providePlayableProgramData(
                it
            )
        }
        sharedViewModel.onChannelVideoSelected(videoUrl = channel.content?.videoUrl, program = null)  // This method should update selectedVideoUrl.
    }

    BackHandler {
        navController.navigate(Destination.homeScreen) {
            popUpTo(0) { inclusive = true }
        }
    }

    // The entire screen is a Box so we can layer items if needed
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1F3A6B)) // Example dark blue background
    ) {
        // Column to hold the top bar and main content
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1) Top bar with brand logo on left and date/time on right
            PermettoTopBar()
            GradientBackground(content = {
                // 2) Main content row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Box(modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)) {
                        Row(modifier = Modifier) {
                            // Left: Categories
                            CategoryMenu(availableGenre,
                                channelListFocusRequester = channelListFocusRequester,
                                genreListFocusRequester = genreListFocusRequester,
                                onCategoryForward = { selectedGenre->
                                    channelListFocusRequester.requestFocus()
                                    val genreName = selectedGenre.name ?: "All"
                                    sharedViewModel.filterPanMetroChannelsByGenre(genreName)
                                })
                            Spacer(modifier = Modifier.width(5.dp))
                            // Middle: Channel List
                            ChannelListScreen(
                                channels = filteredChannels,
                                genreListFocusRequester = genreListFocusRequester,
                                channelListFocusRequester = channelListFocusRequester,
                                onVideoChange = onVideoChange,
                                onDoubleClickIntent = {channelInfo->
                                    epgList.find { it.content?.videoUrl == channelInfo.content?.videoUrl }?.let {channelItem->
                                        sharedViewModel.updateSelectedChannel(channelItem)
                                        navController.navigate(Destination.panMetroScreen) {
                                            popUpTo(Destination.genreScreen) { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier
                                .background(Color.Transparent)
                        ) {
                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(.5f)) {
                                PanMetroPlayer (
                                    sharedViewModel= sharedViewModel
                                )
                            }
                            Box(modifier = Modifier
                                .fillMaxHeight(1f)
                                .padding(20.dp)) {
                                Box(modifier = Modifier.background(Color.Transparent, shape = RoundedCornerShape(10.dp))
                                    ){
                                    Image(
                                        painter = painterResource(id = R.drawable.alliance_logo),
                                        contentDescription = "Panmetro Logo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .padding(10.dp)
                                            .clip(RoundedCornerShape(16.dp)) // Adjust the corner radius as needed
                                    )
                                }

                            }
                        }
                    }
                }
            })
        }
    }
}
