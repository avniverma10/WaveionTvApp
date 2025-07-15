package com.example.tvapp.view.search

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.android.caastv.R
import com.example.tvapp.model.data.epgdata.Channel
import com.example.tvapp.utils.theme.*
import com.example.tvapp.utils.uistate.PreferenceManager
import com.example.tvapp.view.navigationhelper.Destination
import com.example.tvapp.view.navigationhelper.ExpandableNavigationMenu
import com.example.tvapp.view.uicomponent.error.CommonDialog
import com.example.tvapp.viewmodels.SharedViewModel
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel
) {
    var searchText by remember { mutableStateOf("") }
    val epgData       by sharedViewModel.wtvEPGList.collectAsState()
    val searchResults by sharedViewModel.searchResults.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager  = LocalFocusManager.current
    val context       = LocalContext.current
    val menuFocusRequester = remember { FocusRequester() }

    var backCount by remember { mutableStateOf(0) }
    var showExit by remember { mutableStateOf(false) }

    val displayed = if (searchText.isEmpty()) {
        epgData.mapNotNull { item ->
            item.tv?.channel?.copy(
                logoUrl   = item.content?.thumbnailUrl,
                videoUrl  = item.content?.videoUrl,
                genreId   = item.content?.genreId ?: "Unknown",
                channelNo = item.content?.channelNo,
                bgGradient = item.content?.bgGradient
            )
        }.distinctBy { it._id }
    } else {
        searchResults.mapNotNull { result ->
            val source = epgData.find { it.tv?.channel?._id == result._id }
            result.copy(
                bgGradient = source?.content?.bgGradient
            )
        }.distinctBy { it._id }
    }
    val lastIdx by sharedViewModel.lastSearchSelectedIndex.collectAsState()
    var isFirst by rememberSaveable { mutableStateOf(true) }
    val requesters = remember(displayed) {
        displayed.map { FocusRequester() }
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF14161A))
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(screen_bg_color)
                .padding(start = 70.dp)
                .padding(16.dp)
        ) {
            // — Search field —
            OutlinedTextField(
                value = searchText,
                onValueChange = { txt ->
                    searchText = txt
                    coroutineScope.launch {
                        sharedViewModel.searchChannels(txt)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF2A2D32))
                    .padding(horizontal = 8.dp)
                    .onKeyEvent {
                        if (it.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_DOWN &&
                            it.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN
                        ) {
                            requesters.firstOrNull()?.requestFocus()
                            true
                        } else false
                    },
                placeholder = { Text("Movies, TV Shows and more", color = Color.Gray, fontSize = 16.sp) },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    disabledTextColor = Color.Gray,
                    errorTextColor = Color.Red,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    cursorColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(Modifier.height(18.dp))

            Text(
                text = if (searchText.isEmpty()) "Trending in India" else "Search Results",
                style = TextStyle(
                    fontSize   = 18.sp,
                    fontFamily = FontFamily(Font(R.font.figtree_light)),
                    fontWeight = FontWeight(600),
                    color      = Color.White
                ),
                modifier = Modifier.padding(start = 30.dp)
            )

            LazyVerticalGrid(
                columns        = GridCells.Fixed(5),
                contentPadding = PaddingValues(16.dp),
                modifier       = Modifier.fillMaxSize()
            ) {
                itemsIndexed(
                    items = displayed,
                    key   = {idx, ch  ->
                        val k = "${ch._id}-${idx}"
                        k
                    }
                ) { idx, channel ->
                    val shouldFocus = remember(isFirst, displayed) {
                        // on first compose → idx==0, else idx==lastIdx
                        if (isFirst) idx == 0 else idx == lastIdx
                    }

                    Box(
                        modifier = Modifier
                            .focusRequester(requesters.getOrNull(idx) ?: FocusRequester())
                            .padding(8.dp)
                    ) {
                        // immediately request when this box is in composition
                        if (shouldFocus) {
                            LaunchedEffect(Unit) {
                                requesters[idx].requestFocus()
                                isFirst = false
                            }
                        }

                        ChannelThumbnail(
                            channel = channel,
                            onChannelClick = { url ->
                                sharedViewModel.updateLastSearchSelectedIndex(idx)
                                // your existing navigation logic:
                                val allChannels = epgData.mapNotNull { item ->
                                    item.tv?.channel?.copy(
                                        logoUrl   = item.content?.thumbnailUrl,
                                        videoUrl  = item.content?.videoUrl,
                                        genreId   = item.content?.genreId.orEmpty(),
                                        channelNo = item.content?.channelNo
                                    )
                                }
                                val playlistItems = displayed
                                    .mapNotNull { ch ->
                                        epgData.firstOrNull { it.content?.videoUrl == ch.videoUrl }
                                    }
                                val title = if (searchText.isEmpty()) "Trending in India"
                                else "Search Results"
                                sharedViewModel.setCurrentPlaylist(
                                    playlistItems,
                                    title
                                )
                                sharedViewModel.updateLanguage(null)
                                epgData.find { it.content?.videoUrl == url }?.let {
                                    sharedViewModel.updateSelectedChannel(it)
                                    if(PreferenceManager.getAppSettings()?.isPlayerAnimationOverlay == true){
                                        navController.navigate(Destination.animationPlayer)
                                    }else{
                                        navController.navigate(Destination.panMetroScreen)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // — side menu & exit dialog —
        ExpandableNavigationMenu(
            navController   = navController,
            sharedViewModel = sharedViewModel,
            onNavMenuIntent = { _, _ -> },
            modifier        = Modifier.align(Alignment.CenterStart),
            menuFocusRequester = menuFocusRequester,
            onBackPressed = {
                menuFocusRequester.requestFocus()
            }
        )
        if (showExit) {
            CommonDialog(
                showDialog        = true,
                title             = "Exit App",
                painter           = painterResource(id = R.drawable.exit_icon),
                message           = "Are you sure you want to exit?",
                confirmButtonText = "Yes",
                onConfirm         = { (context as? Activity)?.finishAffinity() },
                dismissButtonText = "No",
                onDismiss         = { showExit = false }
            )
        }
    }
}

@Composable
fun ChannelThumbnail(
    channel: Channel,
    onChannelClick: (String) -> Unit
) {
    val brush = channel.bgGradient?.let { grad ->
        val colors = grad.colors.map {
            Color(android.graphics.Color.parseColor(it.color))
        }
        Brush.linearGradient(colors = colors)
    } ?: Brush.verticalGradient(
        colors = listOf(bg_card_color, bg_card_color)
    )
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        Modifier
            .fillMaxWidth()
            .focusable(interactionSource = interactionSource)
            .background(brush, shape = RoundedCornerShape(8.dp))
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) base_color else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { channel.videoUrl?.let(onChannelClick) }
            .focusable(interactionSource = interactionSource)
    ) {
        Box {
            AsyncImage(
                model = channel.logoUrl,
                contentDescription = channel.displayName,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            channel.channelNo?.let { no ->
                Text(
                    text = no.toString(),
                    color = Color.White, // or Black if that’s more visible
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.8f),    // 50% black
                            shape = RoundedCornerShape(4.dp)
                        )
                )
            }
        }
    }
}
