package com.example.tvapp.search

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.tvapp.R
import com.example.tvapp.model.data.epgdata.Channel
import com.example.tvapp.ui.theme.bg_card_color
import com.example.tvapp.ui.theme.base_color
import com.example.tvapp.ui.theme.screen_bg_color
import com.example.tvapp.view.navigationhelper.Destination
import com.example.tvapp.view.navigationhelper.ExpandableNavigationMenu
import com.example.tvapp.viewmodels.SharedViewModel
import kotlinx.coroutines.launch


@Composable
fun SearchScreen(navController: NavController, sharedViewModel: SharedViewModel) {

    val searchFieldFocusRequester = remember { FocusRequester() }
    val firstThumbnailFocusRequester = remember { FocusRequester() }

    var searchText by remember { mutableStateOf("") }
    val epgData by sharedViewModel.wtvEPGList.collectAsState()
    Log.d("SEARCH", "All Channels coming ---> ${epgData}")// Fetch all channels initially
    val searchResults by sharedViewModel.searchResults.collectAsState()  // Fetch search results
    Log.d("SEARCH","Searched ones ---> $searchResults")
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current


    BackHandler {
        navController.navigate(Destination.homeScreen) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    Row(modifier = Modifier.fillMaxSize().background(Color(0xFF14161A))) { // Background Color
        ExpandableNavigationMenu(navController,sharedViewModel,  onNavMenuIntent = { tabInfo, selectedIndex ->
            Log.d("SEARCH", "Selected Tab: ${tabInfo.displayName}, Index: $selectedIndex")
        })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = screen_bg_color) // Background Color
                .padding(16.dp)
        ) {
            // Search Box
            OutlinedTextField(
                value = searchText,
                onValueChange = { newText ->
                    searchText = newText
                    coroutineScope.launch {
                        sharedViewModel.searchChannels(context, newText)
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
                    fontFamily = FontFamily(Font(R.font.figtree_light)),
                    fontWeight = FontWeight(600),
                    color = Color(0xFFFFFFFF),
                ),
                modifier = Modifier.padding(start = 30.dp)
            )

            // Display Channel Thumbnails in a Grid (All channels by default, filtered when searching)
            LazyVerticalGrid(
                columns = GridCells.Fixed(5), // Adjust grid columns as per UI reference
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
                val displayedChannels = if (searchText.isEmpty()) channelsForEmpty else searchResults
                items(displayedChannels?: arrayListOf()) { channel ->
                    ChannelThumbnail(channel){
                        epgData?.find { it.content?.videoUrl == channel.videoUrl }?.let {channelItem->
                            sharedViewModel.updateSelectedChannel(channelItem)
                            navController.navigate(Destination.panMetroScreen)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChannelThumbnail(channel: Channel, onChannelClick: (String) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Box(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .focusable(interactionSource = interactionSource)
            .background(color = bg_card_color, shape = RoundedCornerShape(12.dp))
            .padding(4.dp)
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) base_color else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable {
                channel.videoUrl?.let { onChannelClick(it) }
            },
    ) {
        AsyncImage(
            model = channel.logoUrl,
            contentDescription = channel.displayName,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(10.dp)
        )
    }
}

