package com.panmetro.iptv.view.panmetro.genre

import android.graphics.Bitmap
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.bitmapConfig
import coil3.size.Precision
import coil3.size.Scale
import com.panmetro.iptv.R
import com.panmetro.iptv.extensions.appManifestLiveData
import com.panmetro.iptv.extensions.hideKeyboard
import com.panmetro.iptv.extensions.isNotNullOrEmpty
import com.panmetro.iptv.extensions.loge
import com.panmetro.iptv.extensions.requestFocusSafely
import com.panmetro.iptv.model.data.epgdata.EPGDataItem
import com.panmetro.iptv.model.data.genre.WTVGenre
import com.panmetro.iptv.utils.uistate.PreferenceManager
import com.panmetro.iptv.view.navigationhelper.Destination
import com.panmetro.iptv.view.panmetro.common.PermettoTopBar
import com.panmetro.iptv.view.panmetro.common.PoweredBy
import com.panmetro.iptv.view.uicomponent.keyboard.HideKeyboardOnEnter
import com.panmetro.iptv.viewmodels.SharedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PanmetroGenreScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel
) {
    HideKeyboardOnEnter()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val appManifestData = sharedViewModel.providePanmetroAppInstance().appManifestLiveData()
    val genres = appManifestData.value?.genre?:arrayListOf()
    val epgList  by sharedViewModel.provideEPGDataManager().epgDataState.collectAsState()


    val filteredChannels by sharedViewModel.filteredPanMetroChannels.collectAsState()
    val favIds by sharedViewModel.favoriteChannelIds.collectAsState()
    // Create dynamic genre list that includes/excludes Favorites based on favoriteChannelIds
    val dynamicGenres = remember(genres, favIds) {
        val updatedGenres = genres.toMutableList()
        val favoriteGenre = WTVGenre(name = "Favorites")

        // Check if we need to add or remove the favorite genre
        val shouldHaveFavorite = favIds.isNotEmpty()
        val hasFavorite = updatedGenres.any { it.name == "Favorites" }

        when {
            shouldHaveFavorite && !hasFavorite -> {
                // Add Favorites at position 1 (after "All")
                if (updatedGenres.isNotEmpty() == true && updatedGenres[0].name == "All") {
                    updatedGenres.add(1, favoriteGenre)
                } else {
                    updatedGenres.add(0, favoriteGenre)
                }
            }
            !shouldHaveFavorite && hasFavorite -> {
                // Remove Favorites
                updatedGenres.removeAll { it.name == "Favorites" }
            }
        }
        updatedGenres
    }


    var channelToGenreFocus = remember { mutableStateOf(false) }
    val currentFocusedGenreSelection by sharedViewModel.currentFocusedGenreSelection.collectAsState()
    val lastGenreIndex by sharedViewModel.genreScreenLastGenreIndex.collectAsState()
    val lastChannelIndex by sharedViewModel.genreScreenLastChannelIndex.collectAsState()

    val selectedGenreIndex: MutableState<Int> = remember { mutableStateOf( lastGenreIndex) }
    val selectedChannelIndex: MutableState<Int> = remember { mutableStateOf( lastChannelIndex) }


    val genreListState: LazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var genreListFocusRequester = remember { FocusRequester() }
    var channelListFocusRequester = remember { FocusRequester() }

    val genreFocusRequesters = remember(dynamicGenres.size) { List(dynamicGenres.size) { FocusRequester()  } }

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
        context.hideKeyboard()
        sharedViewModel.filterPanMetroChannelsByGenre()
        //register scroll message request
        sharedViewModel.provideGlobalSSERequest()
        //request for user hash
        sharedViewModel?.provideUserHash()
    }

    // Add this LaunchedEffect to handle initial focus and restoration
    LaunchedEffect(filteredChannels.size, lastGenreIndex, dynamicGenres) {

        if (filteredChannels.isNotEmpty()) {
            // Delay focus request to ensure components are ready
            delay(30)
            // Handle different cases for initial focus
            when {
                sharedViewModel.isFromSplash.value -> {
                    val defaultChannel = epgList?.find {
                        it.channelId == appManifestData.value?.landingChannel?.channelId
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
                            channel -> channel.channelId == PreferenceManager.getSavedChannel()
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
                channelToGenreFocus.value = false
                // Request focus back to the channel list so its first item is focused.
                sharedViewModel._currentFocusedGenreSelection.value = false
                try {
                    channelListFocusRequester.requestFocus()
                } catch (ex: Exception) {
                }
            } catch (e: Exception) {
                loge("FocusError", "Channel focus failed: ${e.message}")
            }
        } else {
            // Fallback to genre list if no channels
            try {
                channelToGenreFocus.value = true
                // Request focus back to the channel list so its first item is focused.
                sharedViewModel._currentFocusedGenreSelection.value = true
                try {
                    genreFocusRequesters?.getOrNull(selectedGenreIndex.value)?.requestFocus()
                } catch (ex: Exception) {
                }
            } catch (e: Exception) {
                loge("FocusError", "Genre focus failed: ${e.message}")
            }
        }
    }


    LaunchedEffect(currentFocusedGenreSelection) {
        if(currentFocusedGenreSelection){
            try {
                genreFocusRequesters?.getOrNull(selectedGenreIndex.value)?.requestFocus()
            } catch (ex: Exception) {
            }
        }else{
            try {
                channelListFocusRequester.requestFocus()
            } catch (ex: Exception) {
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
                        Box(modifier = Modifier
                            .weight(.45f)) {
                            GenreMenuDesign(
                                genres = dynamicGenres,
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
                                    sharedViewModel.updateGenreScreenSelectedGenre(genreName)
                                }
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        // Middle: Channel List
                        Box(modifier = Modifier
                            .weight(.55f)){
                            ChannelMenuDesign(
                                sharedViewModel= sharedViewModel,
                                selectedChannelIndex= selectedChannelIndex,
                                channelListFocusRequester = channelListFocusRequester,
                                channelToGenreFocus = channelToGenreFocus,
                                onNavigateToGenre = {
                                    channelToGenreFocus.value = true

                                    val targetIndex = selectedGenreIndex.value.coerceIn(0, dynamicGenres.lastIndex)

                                    // Only update if changed to prevent unnecessary recompositions
                                    if (selectedGenreIndex.value != targetIndex) {
                                        selectedGenreIndex.value = targetIndex
                                    }

                                    // Check if target item is already visible
                                    val visibleItems = genreListState.layoutInfo.visibleItemsInfo
                                    val isItemVisible = visibleItems.any { it.index == targetIndex }

                                    if (!isItemVisible) {
                                        // Item is not visible, scroll to it
                                        scope.launch {
                                            genreListState.scrollToItem(targetIndex)
                                            // Small delay to ensure layout is stable after scroll
                                            delay(10)
                                            // Request focus after scroll is complete
                                            genreFocusRequesters.requestFocusSafely(targetIndex=targetIndex, isFocusEnabled = true )
                                        }
                                    } else {
                                        // Item is already visible, just request focus
                                        genreFocusRequesters.requestFocusSafely(targetIndex=targetIndex, isFocusEnabled = true )
                                    }
                                },
                                onVideoChange =  onVideoChange,
                                onPlayerScreenIntent = { channelInfo ->
                                    epgList?.find { it.videoUrl == channelInfo.videoUrl }
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
                                SafeZoomInOutSwitcher(
                                    models = listOf(R.drawable.panmetro_logo_new, R.drawable.panmetro_brand),
                                    modifier = Modifier.fillMaxSize(),
                                    cornerRadius = 10.dp
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
                dynamicGenres?.getOrNull(selectedGenreIndex.value)?.name?.let { PreferenceManager.saveGenre(it) }
                filteredChannels?.getOrNull<EPGDataItem>(selectedChannelIndex.value)?.channelId?.let { PreferenceManager.saveChannel(it) }
            }else if (event == Lifecycle.Event.ON_START) {
                val savedGenre = PreferenceManager.getSavedGenre()
                if (savedGenre.isNotNullOrEmpty()) {
                    try {
                        //Find and set genre index
                        selectedGenreIndex.value = dynamicGenres
                            ?.indexOfFirst { it.name == savedGenre } ?: 0
                        sharedViewModel.updateGenreScreenLastGenreIndex(selectedGenreIndex.value)
                    } catch (e: Exception) {
                        loge("ChannelRestore", "Error restoring channel state $e")
                    } finally {
                        PreferenceManager.clearSaveGenre()
                    }
                }

                PreferenceManager.getUsername()?.let {
                    sharedViewModel.checkFavorites(it)
                }

                scope.launch {
                    val idx = selectedGenreIndex.value.coerceIn(0, (dynamicGenres.size - 1).coerceAtLeast(0))
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SafeZoomInOutSwitcher(
    models: List<Any?> = listOf(
        R.drawable.panmetro_logo_new,
        R.drawable.panmetro_brand
    ),
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp,
    switchMillis: Int = 5_000,      // same behavior as “normal” switcher
    fadeMillis: Int = 1200,
    zoomMin: Float = 1.0f,          // subtle Ken-Burns zoom
    zoomMax: Float = 1.06f,
    backgroundColor: Color = Color.White
) {
    // Current page index
    var page by remember { mutableStateOf(0) }

    // Auto-advance like your old one
    LaunchedEffect(models) {
        if (models.isEmpty()) return@LaunchedEffect
        while (true) {
            kotlinx.coroutines.delay(switchMillis.toLong())
            page = (page + 1) % models.size
        }
    }

    // Gentle continuous zoom
    val zoom by rememberInfiniteTransition(label = "kb").animateFloat(
        initialValue = zoomMin,
        targetValue = zoomMax,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "zoom"
    )

    AnimatedContent(
        targetState = page,
        transitionSpec = {
            (scaleIn(initialScale = 0.92f, animationSpec = tween(fadeMillis)) +
                    fadeIn(tween(fadeMillis))) with
                    (scaleOut(targetScale = 0.92f, animationSpec = tween(fadeMillis)) +
                            fadeOut(tween(fadeMillis)))
        },
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .graphicsLayer {
                // prevent huge offscreen layers on N/O
                clip = true
                transformOrigin = TransformOrigin(0.5f, 0.5f)
            }
    ) { idx ->
        val model = models.getOrNull(idx)
        SafeFillImage(
            model = model,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // per-page Ken-Burns zoom
                    scaleX = zoom
                    scaleY = zoom
                },
            backgroundColor = backgroundColor
        )
    }
}
/** Decodes strictly to the container size to avoid gigantic bitmaps. */
@Composable
private fun SafeFillImage(
    model: Any?,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White
) {
    var size by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier.onSizeChanged { size = it }
    ) {
        if (size.width <= 0 || size.height <= 0) {
            // first frame placeholder
            Box(Modifier.fillMaxSize().background(backgroundColor))
            return@Box
        }


        val request = ImageRequest.Builder(LocalContext.current)
            .data(model ?: R.drawable.panmetro_logo_new)     // fallback small asset if needed
            .size(size.width, size.height)                   // bound decode to view
            .precision(Precision.INEXACT)
            .scale(Scale.FILL)
            .allowHardware(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            .bitmapConfig(Bitmap.Config.RGB_565)             // memory-friendly
            .build()

        SubcomposeAsyncImage(
            model = request,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,                // never FillBounds
            loading = { Box(Modifier.fillMaxSize().background(backgroundColor)) },
            error   = { Box(Modifier.fillMaxSize().background(backgroundColor)) }
        )
    }
}
