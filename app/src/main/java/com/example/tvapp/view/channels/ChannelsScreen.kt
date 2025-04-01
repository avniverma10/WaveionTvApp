package com.example.tvapp.view.channels

import android.util.Log
import android.view.KeyEvent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.tvapp.extensions.isNotNullOrEmpty
import com.example.tvapp.model.data.banner.Banner
import com.example.tvapp.model.data.epgdata.Channel
import com.example.tvapp.utils.Constants
import com.example.tvapp.view.navigationhelper.CategoryMenu
import com.example.tvapp.view.navigationhelper.Destination
import com.example.tvapp.view.navigationhelper.ExpandableNavigationMenu
import com.example.tvapp.view.navigationhelper.LanguageMenu
import com.example.tvapp.viewmodels.SharedViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun ChannelScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    showBanner: Boolean = true,
    bannerList: List<Banner> = emptyList()
) {
    val context = LocalContext.current
    val filteredContent by sharedViewModel.filteredEPGList.collectAsState(emptyList())


    // Setup state for Category and Language menus.
    val categorySelectedIndex = remember { mutableStateOf(0) }
    val languageSelectedIndex = remember { mutableStateOf(0) }
    val categoryFocusRequesters = remember { mutableMapOf<Int, FocusRequester>() }
    val languageFocusRequesters =
        remember { mutableStateOf(mutableMapOf<Int, FocusRequester>()) }.value
    val firstChannelFocusRequester = remember { FocusRequester() }

    // Determine banner visibility.
    val isBannerVisible = showBanner && bannerList.isNotEmpty()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF14161A))
    ) {
        // Side navigation menu.
        ExpandableNavigationMenu(
            navController = navController,
            sharedViewModel = sharedViewModel,
            onNavMenuIntent = { tabInfo, _ ->
                // Update categories if needed based on the tab info.
            }
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF161D25))
                .zIndex(1f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // CategoryMenu calls filterChannelsByGenre on selection change.
                CategoryMenu(
                    sharedViewModel = sharedViewModel,
                    selectedIndex = categorySelectedIndex,
                    categoryFocusRequesters = categoryFocusRequesters,
                    languageFocusRequesters = languageFocusRequesters,
                    languageSelectedIndex = languageSelectedIndex
                )

                // LanguageMenu calls filterChannelsByLanguage on selection change.
                LanguageMenu(
                    sharedViewModel = sharedViewModel,
                    selectedIndex = languageSelectedIndex,
                    firstChannelFocusRequester = firstChannelFocusRequester,
                    languageFocusRequesters = languageFocusRequesters,
                    categoryFocusRequesters = categoryFocusRequesters,
                    categorySelectedIndex = categorySelectedIndex
                )

                // Map filtered EPG items to Channel objects.
                val channelList = filteredContent.mapNotNull { epgItem ->
                    epgItem.tv?.channel?.copy(
                        videoUrl = epgItem.content?.videoUrl,
                        logoUrl = epgItem.content?.thumbnailUrl,
                        genreId = epgItem.content?.genreId ?: ""
                    )
                }.ifEmpty { emptyList() }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF14161A))
                ) {
                    itemsIndexed(channelList) { index, channel ->
                        // Attach the focusRequester only to the first item.
                        val isFirstChannel = (index == 0)
                        val isLastChannel = (index == channelList.size - 1)
                        if (index == 0) {
                            ChannelList(
                                sharedViewModel= sharedViewModel,
                                channel = channel,
                                focusRequester = firstChannelFocusRequester,
                                isFirstChannel = isFirstChannel,
                                isLastChannel = isLastChannel,
                                onClick = { clickedChannel ->
                                    Constants.selectedChannelUrl = channel.videoUrl?:""
                                    navController.navigate(Destination.panMetroScreen) {
                                        popUpTo(Destination.channel) { inclusive = true }
                                    }

                                   /* navController.navigate(
                                        "homeplayer/${URLEncoder.encode(channel.videoUrl ?: "", StandardCharsets.UTF_8.toString())}" +
                                                "?categoryIds="
                                    )*/


                                },
                                languageFocusRequesters = languageFocusRequesters,
                                languageSelectedIndex = languageSelectedIndex,
                                categoryFocusRequesters = categoryFocusRequesters,
                                categorySelectedIndex = categorySelectedIndex
                            )
                        } else {
                            ChannelList(
                                sharedViewModel= sharedViewModel,
                                channel = channel,
                                onClick = { clickedChannel ->
                                    Constants.selectedChannelUrl = channel.videoUrl?:""
                                    navController.navigate(Destination.panMetroScreen) {
                                        popUpTo(Destination.channel) { inclusive = true }
                                    }
                                    /*navController.navigate(
                                        "homeplayer/${URLEncoder.encode(channel.videoUrl ?: "", StandardCharsets.UTF_8.toString())}" +
                                                "?categoryIds="
                                    )*/

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
    languageFocusRequesters: Map<Int, FocusRequester>,
    languageSelectedIndex: MutableState<Int>,
    categoryFocusRequesters: Map<Int, FocusRequester>,
    categorySelectedIndex: MutableState<Int>
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Animate scaling when focused
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
                                languageFocusRequesters[languageSelectedIndex.value]?.requestFocus()
                                true
                            } else false
                        }
                        KeyEvent.KEYCODE_DPAD_DOWN -> isLastChannel
                        else -> false
                    }
                } else false
            }
        ,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // ✅ No shadow
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
                    .height(90.dp)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }
    }
}

