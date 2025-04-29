   package com.example.tvapp.view.panmetro.genre

import android.util.Log
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.tvapp.R
import com.example.tvapp.extensions.showToastS
import com.example.tvapp.model.data.epgdata.EPGDataItem
import com.example.tvapp.utils.uistate.PreferenceManager
import com.example.tvapp.view.navigationhelper.Destination
import com.example.tvapp.view.panmetro.GenreMultiDRMPlayer
import com.example.tvapp.view.panmetro.PoweredBy
import com.example.tvapp.view.panmetro.common.PermettoTopBar
import com.example.tvapp.view.uicomponent.ZoomInOutSwitcher
import com.example.tvapp.view.uicomponent.keyboard.HideKeyboardOnEnter
import com.example.tvapp.viewmodels.SharedViewModel
import com.example.tvapp.viewmodels.genre.GenreViewModel

@Composable
fun PanmetroGenreScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    genreViewModel: GenreViewModel= hiltViewModel()
) {
    HideKeyboardOnEnter()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val epgList = genreViewModel.provideAvailableEPG()

    val availableGenre = genreViewModel.provideAvailableGenre()


    // Lift the selected genre index state.
    var channelToGenreFocus = remember { mutableStateOf(false) }


    // 2. Use rememberSaveable with that Saver instead of plain remember
    val selectedGenreIndex: MutableState<Int> = remember { mutableStateOf( 0) }
    val selectedChannelIndex: MutableState<Int> = remember { mutableStateOf( 0) }


    // FocusRequester for the channel list area.
    var genreListFocusRequester = remember { FocusRequester() }
    var channelListFocusRequester = remember { FocusRequester() }


    // Create a list of FocusRequesters for the genre items.
    val genreFocusRequesters = remember(availableGenre) { List(availableGenre.size) { FocusRequester() } }



    // When a channel is selected, update the video URL in the ViewModel.
    val onVideoChange: (EPGDataItem,Int) -> Unit = { channel,channelIndex ->
        selectedChannelIndex.value = channelIndex
        sharedViewModel.updateSelectedChannel(channel)  // This method should update selectedVideoUrl.
    }


    BackHandler {
        navController.navigate(Destination.epgScreen) {
            popUpTo(Destination.genreScreen) { inclusive = true }
        }
    }

    LaunchedEffect(Unit) {
        genreViewModel.filterPanMetroChannelsByGenre()

    }

    // The entire screen is a Box so we can layer items if needed
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1F3A6B)) // Example dark blue background
    ){
        // Column to hold the top bar and main content
        Column(
            modifier = Modifier.fillMaxSize().background(Color.Black)
        ) {
            // 1) Top bar with brand logo on left and date/time on right
            PermettoTopBar()
//            GradientBackground(content = {
            // 2) Main content row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 10.dp)
            ) {
                Box(modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)) {
                    Row(modifier = Modifier) {
                        // Left: Categories
                        GenreListMenu(
                            genres = availableGenre,
                            genreSelectedIndex = selectedGenreIndex,
                            channelToGenreFocus = channelToGenreFocus,
                            focusRequesters = genreFocusRequesters,
                            // On selection, update the index, filter channels, and move focus to the channel list.
                            onCategoryForward = { index, selectedGenre ->
                                channelToGenreFocus.value = false
                                selectedChannelIndex.value = 0
                                val genreName = selectedGenre.name ?: "All"
                                genreViewModel.filterPanMetroChannelsByGenre(genreName)
                                // Request focus back to the channel list so its first item is focused.
                                channelListFocusRequester.requestFocus()
                            }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        // Middle: Channel List
                        ChannelListMenuScreen(
                            sharedViewModel= sharedViewModel,
                            genreViewModel = genreViewModel,
                            selectedChannelIndex= selectedChannelIndex,
                            channelListFocusRequester = channelListFocusRequester,
                            channelToGenreFocus = channelToGenreFocus,
                            onNavigateToGenre = {
                                // Request focus on the genre item that was last selected.
                                genreFocusRequesters.getOrNull(selectedGenreIndex.value)?.let { requester ->
                                    try {
                                        channelToGenreFocus.value = true
                                        requester.requestFocus()
                                    } catch (e: IllegalStateException) {
                                        Log.e("FocusError", "FocusRequester not initialized", e)
                                    }
                                }
                            },
                            onVideoChange =  onVideoChange,
                            onPlayerScreenIntent = { channelInfo ->
                                epgList?.find { it.content?.videoUrl == channelInfo.content?.videoUrl }
                                    ?.let { channelItem ->
                                        navController.navigate(Destination.panMetroScreen) {
                                            PreferenceManager.selectedGenreIndex = 0
                                            PreferenceManager.selectedChannelIndex = 0
                                            PreferenceManager.lastEpgDataItem = null
                                            popUpTo(Destination.genreScreen) {
                                                inclusive = true
                                            }
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
                            .fillMaxHeight(.6f)) {
                            GenreMultiDRMPlayer (
                                selectedChannelIndex= selectedChannelIndex,
                                sharedViewModel = sharedViewModel,
                                genreViewModel = genreViewModel
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxHeight(1f)
                                .padding(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize() // Force the inner Box to fill the outer Box.
                                    .background(Color.Transparent, shape = RoundedCornerShape(10.dp))
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.banner3),
                                    contentDescription = "Panmetro Logo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize() // Stretch the image to fill the inner Box.
                                        .clip(RoundedCornerShape(16.dp)) // Adjust the corner radius as needed.
                                )
                            }
                        }

                    }
                }
            }
            //powered by footer
            PoweredBy()
        }
    }


    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                //context.showToastS("ON_STOP>${selectedGenreIndex.value} and ${selectedChannelIndex.value}")
                sharedViewModel.persistToGenrePrefs(prefs = PreferenceManager,selectedGenreIndex=selectedGenreIndex.value, selectedChannelIndex = selectedChannelIndex.value)
            }else if (event == Lifecycle.Event.ON_START) {
                if(PreferenceManager.selectedChannelIndex >0) {
                   // context.showToastS("ON_RESUME>${PreferenceManager.selectedGenreIndex} and ${PreferenceManager.selectedChannelIndex}")
                    selectedGenreIndex.value = PreferenceManager.selectedGenreIndex
                    selectedChannelIndex.value = PreferenceManager.selectedChannelIndex
                    availableGenre.getOrNull(selectedGenreIndex.value)?.let {
                        genreViewModel.filterPanMetroChannelsByGenre(it.name)
                        /*genreViewModel.filteredPanMetroChannels.value.getOrNull(sharedViewModel.selectedChannelIndex.value)
                            ?.let { it1 -> sharedViewModel.updateSelectedChannel(it1) }*/
                    }
                    PreferenceManager.selectedGenreIndex = 0
                    PreferenceManager.selectedChannelIndex = 0
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}