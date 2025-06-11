package com.example.tvapp.search

import android.app.Activity
import android.os.Process
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.android.tccl.R
import com.example.tvapp.extensions.loge
import com.example.tvapp.model.data.epgdata.Channel
import com.example.tvapp.ui.theme.bg_card_color
import com.example.tvapp.ui.theme.base_color
import com.example.tvapp.ui.theme.screen_bg_color
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
    val searchFieldFocusRequester = remember { FocusRequester() }
    val firstThumbnailFocusRequester = remember { FocusRequester() }

    var searchText by remember { mutableStateOf("") }
    val epgData by sharedViewModel.wtvEPGList.collectAsState()
    val searchResults by sharedViewModel.searchResults.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var backPressCount by remember { mutableStateOf(0) }
    var showExitDialog by remember { mutableStateOf(false) }


    BackHandler {
        backPressCount++

        if (backPressCount >= 2) {
            // Show exit confirmation if pressed back twice
            showExitDialog = true
        } else {
            // First back: just clear focus and move left as before
            focusManager.clearFocus(force = true)
            focusManager.moveFocus(FocusDirection.Left)
        }
    }
    LaunchedEffect(Unit) {
        backPressCount = 0
        val listToFocus = if (searchText.isNotEmpty()) searchResults else epgData
        if (listToFocus?.isNotEmpty() == true) {
            try {
                firstThumbnailFocusRequester.requestFocus()
            } catch (e: IllegalStateException) {
                loge("FocusError", "FocusRequester not initialized ${e.message}")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF14161A))
    ) {
        // Main content inset by collapsed menu width (70.dp)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(screen_bg_color)
                .padding(start = 70.dp)  // <-- inset so it never shifts
                .padding(16.dp)         // your existing padding
        ) {
            // Search Box
            OutlinedTextField(
                value = searchText,
                onValueChange = { newText ->
                    searchText = newText
                    coroutineScope.launch {
                        sharedViewModel.searchChannels(newText)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF2A2D32))
                    .padding(horizontal = 8.dp)
                    .focusRequester(searchFieldFocusRequester)
                    .onKeyEvent { event ->
                        if (event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_DOWN &&
                            event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN
                        ) {
                            firstThumbnailFocusRequester.requestFocus()
                            true
                        } else false
                    },
                placeholder = {
                    Text(
                        "Movies, TV Shows and more",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search Icon",
                        tint = Color.Gray
                    )
                },
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

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (searchText.isEmpty()) "Trending in India" else "Search Results",
                style = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 28.01.sp,
                    fontFamily = FontFamily(Font(com.android.tccl.R.font.figtree_light)),
                    fontWeight = FontWeight(600),
                    color = Color.White
                ),
                modifier = Modifier.padding(start = 30.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                val channelsForEmpty = epgData?.mapNotNull { epgItem ->
                    epgItem.tv?.channel?.copy(
                        logoUrl = epgItem.content?.thumbnailUrl,
                        videoUrl = epgItem.content?.videoUrl,
                        genreId = epgItem.content?.genreId ?: "Unknown"
                    )
                }
                val displayedChannels =
                    if (searchText.isEmpty()) channelsForEmpty else searchResults
                itemsIndexed(displayedChannels ?: emptyList()) { index, channel ->
                    ChannelThumbnail(
                        channel = channel,
                        modifier = if (index == 0) Modifier.focusRequester(
                            firstThumbnailFocusRequester
                        ) else Modifier,
                        onChannelClick = { url ->
                            epgData?.find { it.content?.videoUrl == url }?.let { item ->
                                sharedViewModel.updateSelectedChannel(item)
                                navController.navigate(Destination.panMetroScreen)
                            }
                        }
                    )
                }
            }
        }

        // Overlay navigation menu:
        ExpandableNavigationMenu(
            navController = navController,
            sharedViewModel = sharedViewModel,
            onNavMenuIntent = { tabInfo, selectedIndex ->
                loge("SEARCH", "Selected Tab: ${tabInfo.displayName}, Index: $selectedIndex")
            },
            modifier = Modifier.align(Alignment.CenterStart)
        )

        // Exit confirmation dialog
        if (showExitDialog) {
            CommonDialog(
                showDialog = true,
                title = "Exit App",
                borderColor = Color.Transparent,
                painter = painterResource(id = R.drawable.exit_icon),
                message = "Are you sure you want to exit the app?",
                confirmButtonText = "Yes",
                onConfirm = {
                    (context as? Activity)?.finishAffinity()
//                    Process.killProcess(Process.myPid())
                },
                dismissButtonText = "No",
                onDismiss = { showExitDialog = false }
            )

        }
    }
}

@Composable
fun ChannelThumbnail(
    channel: Channel,
    onChannelClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        modifier = modifier
            .padding(8.dp)
            .fillMaxSize()
            .fillMaxWidth()
            .focusable(interactionSource = interactionSource)
            .background(color = bg_card_color, shape = RoundedCornerShape(8.dp))
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) base_color else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { channel.videoUrl?.let(onChannelClick) }
    ) {
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
    }
}
