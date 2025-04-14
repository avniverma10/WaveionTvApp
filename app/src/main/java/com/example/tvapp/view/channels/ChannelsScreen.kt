package com.example.tvapp.view.channels

import android.util.Log
import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.tvapp.extensions.appGenreLiveData
import com.example.tvapp.extensions.appLanguageLiveData
import com.example.tvapp.model.data.banner.Banner
import com.example.tvapp.model.data.epgdata.Channel
import com.example.tvapp.view.navigationhelper.CategoryMenu
import com.example.tvapp.view.navigationhelper.Destination
import com.example.tvapp.view.navigationhelper.ExpandableNavigationMenu
import com.example.tvapp.view.navigationhelper.LanguageMenu
import com.example.tvapp.viewmodels.SharedViewModel

@Composable
fun ChannelScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    showBanner: Boolean = true,
    bannerList: List<Banner> = emptyList()
) {
    val context = LocalContext.current
    val categories = sharedViewModel.provideApplicationContext()
        .appGenreLiveData().value.orEmpty()
    val languages = sharedViewModel.provideApplicationContext()
        .appLanguageLiveData().value.orEmpty()
    val filterState by sharedViewModel.filterState.collectAsState()
    val filteredContent by sharedViewModel.filteredEPGList.collectAsState(emptyList())

    Log.d("AVNI", "Channel List --> $filteredContent")
    val categorySelectedIndex = remember { mutableStateOf(0) }
    val languageSelectedIndex = remember { mutableStateOf(0) }
    val firstChannelFocusRequester = remember { FocusRequester() }
    val isBannerVisible = showBanner && bannerList.isNotEmpty()

    // Create one FocusRequester per item for categories and languages.
    val categoryFocusRequesters = remember(categories) { List(categories.size) { FocusRequester() } }
    val languageFocusRequesters = remember(languages) { List(languages.size) { FocusRequester() } }

    // Set selected index only if there is data
    LaunchedEffect(filterState, categories, languages) {
        if (categories.isNotEmpty()) {
            categorySelectedIndex.value =
                categories.indexOfFirst { it.name == filterState.genre }.takeIf { it >= 0 } ?: 0
        } else {
            categorySelectedIndex.value = -1
        }
        if (languages.isNotEmpty()) {
            languageSelectedIndex.value =
                languages.indexOfFirst { it.name == filterState.language }.takeIf { it >= 0 } ?: 0
        } else {
            languageSelectedIndex.value = -1
        }
    }

    BackHandler {
        navController.navigate(Destination.homeScreen) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF14161A))
    ) {
        ExpandableNavigationMenu(
            navController = navController,
            sharedViewModel = sharedViewModel,
            onNavMenuIntent = { tabInfo, _ -> }
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF161D25))
                .zIndex(1f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Render navigation menus only when data exists.
                if (categories.isNotEmpty() && languages.isNotEmpty()) {
                    CategoryMenu(
                        sharedViewModel = sharedViewModel,
                        selectedIndex = categorySelectedIndex,
                        categoryFocusRequesters = categoryFocusRequesters,
                        languageFocusRequesters = languageFocusRequesters,
                        languageSelectedIndex = languageSelectedIndex
                    )
                    LanguageMenu(
                        sharedViewModel = sharedViewModel,
                        selectedIndex = languageSelectedIndex,
                        firstChannelFocusRequester = firstChannelFocusRequester,
                        languageFocusRequesters = languageFocusRequesters,
                        categoryFocusRequesters = categoryFocusRequesters,
                        categorySelectedIndex = categorySelectedIndex
                    )
                }
                // Process channel list
                val channelList = filteredContent.mapNotNull { epgItem ->
                    epgItem.tv?.channel?.copy(
                        videoUrl = epgItem.content?.videoUrl,
                        logoUrl = epgItem.content?.thumbnailUrl,
                        genreId = epgItem.content?.genreId ?: ""
                    )
                }

                // Render channel list only when non-empty; otherwise, show a loading placeholder.
                if (channelList.isNotEmpty()) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        contentPadding = PaddingValues(16.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF14161A))
                    ) {
                        itemsIndexed(channelList) { index, channel ->
                            val isFirstChannel = (index == 0)
                            val isLastChannel = (index == channelList.size - 1)
                            if (index == 0) {
                                ChannelList(
                                    sharedViewModel = sharedViewModel,
                                    channel = channel,
                                    focusRequester = firstChannelFocusRequester,
                                    isFirstChannel = isFirstChannel,
                                    isLastChannel = isLastChannel,
                                    onClick = { clickedChannel ->
                                        sharedViewModel.wtvEPGList.value?.find { it.content?.videoUrl == channel.videoUrl }
                                            ?.let { channelItem ->
                                                sharedViewModel.updateSelectedChannel(channelItem)
                                                navController.navigate(Destination.panMetroScreen)
                                            }
                                    },
                                    languageFocusRequesters = languageFocusRequesters,
                                    languageSelectedIndex = languageSelectedIndex,
                                    categoryFocusRequesters = categoryFocusRequesters,
                                    categorySelectedIndex = categorySelectedIndex
                                )
                            } else {
                                ChannelList(
                                    sharedViewModel = sharedViewModel,
                                    channel = channel,
                                    onClick = { clickedChannel ->
                                        sharedViewModel.wtvEPGList.value?.find { it.content?.videoUrl == channel.videoUrl }
                                            ?.let { channelItem ->
                                                sharedViewModel.updateSelectedChannel(channelItem)
                                                navController.navigate(Destination.panMetroScreen)
                                            }
                                    },
                                    isFirstChannel = isFirstChannel,
                                    isLastChannel = isLastChannel,
                                    languageFocusRequesters = languageFocusRequesters,
                                    languageSelectedIndex = languageSelectedIndex,
                                    categoryFocusRequesters = categoryFocusRequesters,
                                    categorySelectedIndex = categorySelectedIndex
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Placeholder when channels are loading
                        androidx.tv.material3.Text("Loading Channels...", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ChannelList(
    sharedViewModel: SharedViewModel,
    channel: Channel,
    focusRequester: FocusRequester? = null,
    onClick: (Channel) -> Unit = {},
    isFirstChannel: Boolean,
    isLastChannel: Boolean,
    languageSelectedIndex: MutableState<Int>,
    categoryFocusRequesters: List<FocusRequester>,
    languageFocusRequesters: List<FocusRequester>,
    categorySelectedIndex: MutableState<Int>
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.1f else 1f,
        animationSpec = tween(durationMillis = 150)
    )

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .focusable(interactionSource = interactionSource)
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.nativeKeyEvent.keyCode) {
                        KeyEvent.KEYCODE_DPAD_CENTER -> {
                            onClick(channel)
                            true
                        }
                        KeyEvent.KEYCODE_DPAD_UP -> {
                            if (isFirstChannel) {
                                // Safely request focus on the language list (if available)
                                if (languageFocusRequesters.isNotEmpty() && languageSelectedIndex.value >= 0) {
                                    languageFocusRequesters.getOrNull(languageSelectedIndex.value)?.requestFocus()
                                }
                                true
                            } else false
                        }
                        KeyEvent.KEYCODE_DPAD_DOWN -> isLastChannel
                        else -> false
                    }
                } else false
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(Color(0xFF262C36))
                .border(
                    width = if (isFocused) 2.dp else 0.dp,
                    color = if (isFocused) Color(0xFF49FEDD) else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(4.dp)
        ) {
            AsyncImage(
                model = channel.logoUrl,
                contentDescription = channel.displayName,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }
    }
}
