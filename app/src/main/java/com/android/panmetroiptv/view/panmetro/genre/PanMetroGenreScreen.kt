package com.android.panmetroiptv.view.panmetro.genre

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.android.panmetroiptv.extensions.appManifestLiveData
import com.android.panmetroiptv.extensions.coreEPGLiveData
import com.android.panmetroiptv.extensions.hideKeyboard
import com.android.panmetroiptv.extensions.isNotNullOrEmpty
import com.android.panmetroiptv.extensions.loge
import com.android.panmetroiptv.model.data.epgdata.EPGDataItem
import com.android.panmetroiptv.model.data.manifest.TabInfo
import com.android.panmetroiptv.utils.uistate.PreferenceManager
import com.android.panmetroiptv.view.navigationhelper.Destination
import com.android.panmetroiptv.view.panmetro.common.PermettoTopBar
import com.android.panmetroiptv.view.panmetro.common.PoweredBy
import com.android.panmetroiptv.view.uicomponent.ZoomInOutSwitcher
import com.android.panmetroiptv.view.uicomponent.keyboard.HideKeyboardOnEnter
import com.android.panmetroiptv.viewmodels.SharedViewModel
import com.android.panmetroiptv.viewmodels.genre.GenreViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun PanmetroGenreScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel
) {
    HideKeyboardOnEnter()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val appManifestData = sharedViewModel.provideApplicationContext().appManifestLiveData()
    val epgList = remember {
        sharedViewModel.provideApplicationContext().coreEPGLiveData().value
    }

    val availableGenre = appManifestData?.value?.genre?: arrayListOf()

    val filteredChannels by sharedViewModel.filteredPanMetroChannels.collectAsState()

    var channelToGenreFocus = remember { mutableStateOf(false) }

    val selectedGenreIndex: MutableState<Int> = remember { mutableStateOf( 0) }
    val selectedChannelIndex: MutableState<Int> = remember { mutableStateOf( 0) }
    val genreListState: LazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var genreListFocusRequester = remember { FocusRequester() }
    var channelListFocusRequester = remember { FocusRequester() }

    val genreFocusRequesters = remember(availableGenre) { List(availableGenre.size) { FocusRequester() } }

    val onVideoChange: (EPGDataItem,Int) -> Unit = { channel,channelIndex ->
        selectedChannelIndex.value = channelIndex
        sharedViewModel.updateSelectedChannel(channel)  // This method should update selectedVideoUrl.
    }
    val lastGenreIndex by sharedViewModel.genreScreenLastGenreIndex.collectAsState()
    val lastChannelIndex by sharedViewModel.genreScreenLastChannelIndex.collectAsState()


    BackHandler {
        navController.navigate(Destination.epgScreen) {
            popUpTo(Destination.genreScreen) { inclusive = true }
        }
    }

    LaunchedEffect(Unit) {
        context.hideKeyboard()
        val genreName = availableGenre.getOrNull(lastGenreIndex)?.name ?: "All"
        sharedViewModel.filterPanMetroChannelsByGenre(genreName)
        //register scroll message request
        sharedViewModel.provideGlobalSSERequest()
        //request for user hash
        sharedViewModel?.provideUserHash()
    }

    // Add this LaunchedEffect to handle initial focus and restoration
    LaunchedEffect(filteredChannels, availableGenre) {
        if (filteredChannels.isNotEmpty()) {
            // Delay focus request to ensure components are ready
            delay(100)
            // Handle different cases for initial focus
            when {
                sharedViewModel.isFromSplash.value -> {
                    val defaultChannel = epgList?.find {
                        it.content?.ChannelID == appManifestData.value?.landingChannel?.channelId
                    }
                    defaultChannel?.let {
                        selectedGenreIndex.value = lastGenreIndex.coerceAtLeast(0)
                        selectedChannelIndex.value = epgList.indexOf(it).coerceAtLeast(0)
                        sharedViewModel.updateGenreScreenLastChannelIndex(epgList.indexOf(it))
                        sharedViewModel.updateSelectedChannel(it)
                        sharedViewModel.isFromSplash.value = false
                    }
                }
                PreferenceManager.getSavedChannel().isNotNullOrEmpty() -> {
                    val selectedIndex = filteredChannels.indexOfFirst {
                            channel -> channel.content?.ChannelID == PreferenceManager.getSavedChannel()
                    }.coerceAtLeast(0)

                    selectedChannelIndex.value = selectedIndex
                    sharedViewModel.updateGenreScreenLastChannelIndex(selectedIndex)
                    filteredChannels.getOrNull(selectedIndex)?.let {
                        sharedViewModel.updateSelectedChannel(it)
                    }
                    PreferenceManager.clearSaveChannel()
                }
                else -> {
                    selectedGenreIndex.value = lastGenreIndex.coerceAtLeast(0)
                    selectedChannelIndex.value = lastChannelIndex.coerceAtLeast(0)
                    filteredChannels.getOrNull(selectedChannelIndex.value)?.let {
                        sharedViewModel.updateSelectedChannel(it)
                    }
                }
            }
            // Always request focus on the channel list after handling cases
            try {
                channelListFocusRequester.requestFocus()
                channelToGenreFocus.value = false
            } catch (e: Exception) {
                loge("FocusError", "Channel focus failed: ${e.message}")
            }
        } else {
            // Fallback to genre list if no channels
            try {
                genreFocusRequesters.getOrNull(selectedGenreIndex.value)?.requestFocus()
                channelToGenreFocus.value = true
            } catch (e: Exception) {
                loge("FocusError", "Genre focus failed: ${e.message}")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1F3A6B)) // Example dark blue background
    ){
        Column(
            modifier = Modifier.fillMaxSize().background(Color.Black)
        ) {
            PermettoTopBar()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Box(modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp, end = 10.dp)) {
                    Row(modifier = Modifier) {
                        // Left: Categories
                        GenreListMenu(
                            genres = availableGenre,
                            sharedViewModel = sharedViewModel,
                            genreSelectedIndex = selectedGenreIndex,
                            channelToGenreFocus = channelToGenreFocus,
                            listState = genreListState,
                            focusRequesters = genreFocusRequesters,
                            // On selection, update the index, filter channels, and move focus to the channel list.
                            onCategoryForward = { index, selectedGenre ->
                                selectedGenreIndex.value = index
                                selectedChannelIndex.value = 0
                                val genreName = selectedGenre.name ?: "All"
                                sharedViewModel.filterPanMetroChannelsByGenre(genreName)
                                // Request focus back to the channel list so its first item is focused.
                                channelToGenreFocus.value = false
                                try {
                                    channelListFocusRequester.requestFocus()
                                }catch (ex: Exception){}
                            }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        // Middle: Channel List
                        ChannelListMenuScreen(
                            sharedViewModel= sharedViewModel,
                            selectedChannelIndex= selectedChannelIndex,
                            channelListFocusRequester = channelListFocusRequester,
                            channelToGenreFocus = channelToGenreFocus,
                            onNavigateToGenre = {
                                channelToGenreFocus.value = true
                                scope.launch {
                                    val idx = selectedGenreIndex.value.coerceIn(0, (availableGenre.size - 1).coerceAtLeast(0))
                                    genreListState.animateScrollToItem(idx)
                                    snapshotFlow { genreListState.layoutInfo.visibleItemsInfo.any { it.index == idx } }
                                        .first { it }
                                    genreFocusRequesters.getOrNull(idx)?.requestFocus()
                                }
                            },
                            onVideoChange =  onVideoChange,
                            onPlayerScreenIntent = { channelInfo ->
                                epgList?.find { it.content?.videoUrl == channelInfo.content?.videoUrl }
                                    ?.let { channelItem ->
                                        sharedViewModel.goingToFullPlayer.value = true
                                        navController.navigate(Destination.panMetroScreen) {
                                            PreferenceManager.clearSaveGenre()
                                            PreferenceManager.clearSaveChannel()
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
                            .fillMaxHeight(.75f)
                            .padding(bottom = 10.dp)) {
                            GenreMultiDRMPlayer (
                                selectedChannelIndex= selectedChannelIndex,
                                sharedViewModel = sharedViewModel
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxHeight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 10.dp, end = 10.dp)
                                    .fillMaxSize() // Force the inner Box to fill the outer Box.
                                    .background(Color.Transparent, shape = RoundedCornerShape(10.dp))
                            ) {
                                ZoomInOutSwitcher()
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
                availableGenre?.getOrNull(selectedGenreIndex.value)?.name?.let { PreferenceManager.saveGenre(it) }
                filteredChannels?.getOrNull<EPGDataItem>(selectedChannelIndex.value)?.content?.ChannelID?.let { PreferenceManager.saveChannel(it) }
            }else if (event == Lifecycle.Event.ON_START) {
                val savedGenre = PreferenceManager.getSavedGenre()
                if (savedGenre.isNotNullOrEmpty()) {
                    try {
                        //Find and set genre index
                        selectedGenreIndex.value = availableGenre
                            ?.indexOfFirst { it.name == savedGenre } ?: 0
                        sharedViewModel.updateGenreScreenLastGenreIndex(selectedGenreIndex.value)
                    } catch (e: Exception) {
                        loge("ChannelRestore", "Error restoring channel state $e")
                    } finally {
                        PreferenceManager.clearSaveGenre()
                    }
                }
                scope.launch {
                    val idx = selectedGenreIndex.value.coerceIn(0, (availableGenre.size - 1).coerceAtLeast(0))
                    genreListState.scrollToItem(idx)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


}

