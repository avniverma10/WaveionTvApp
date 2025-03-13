package com.example.tvapp.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.tvapp.R
import com.example.tvapp.components.ExpandableNavigationMenu
import com.example.tvapp.models.EPGChannel
import com.example.tvapp.viewmodels.EPGViewModel
import kotlinx.coroutines.launch


@Composable
fun SearchScreen(navController: NavController, viewModel: EPGViewModel = hiltViewModel()) {
    var searchText by remember { mutableStateOf("") }
    val allChannels = viewModel.epgChannels.collectAsState().value  // Fetch all channels initially
    val searchResults = viewModel.searchResults.collectAsState().value  // Fetch search results
    val coroutineScope = rememberCoroutineScope()

    Row(modifier = Modifier.fillMaxSize().background(Color(0xFF14161A))) { // Background Color
        ExpandableNavigationMenu(navController)
        Column(modifier = Modifier.fillMaxSize().padding(16.dp))
        {
            // Search Box
            OutlinedTextField(
                value = searchText,
                onValueChange = { newText ->
                    searchText = newText
                    coroutineScope.launch {
                        viewModel.searchChannels(newText)  // Call search function when typing
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp)) // Rounded corners
                    .background(Color(0xFF2A2D32)) // Dark Gray Background
                    .padding(horizontal = 8.dp),
                placeholder = { Text("Movies, TV Shows and more", color = Color.Gray, fontSize = 16.sp) },
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
                    cursorColor =  Color.Transparent,
                    focusedIndicatorColor =  Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )


            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Trending in India",

                // Title-Large-Semibold
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
                columns = GridCells.Fixed(4), // Adjust grid columns as per UI reference
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                val displayedChannels =
                    if (searchText.isEmpty()) allChannels else searchResults  // Show all channels if search is empty
                items(displayedChannels) { channel ->
                    ChannelThumbnail(channel)
                }
            }
        }
    }
}

@Composable
fun ChannelThumbnail(channel: EPGChannel) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp), // Rounded corners
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Small shadow effect
    ) {
        AsyncImage(
            model = channel.logoUrl,
            contentDescription = channel.name,
            contentScale = ContentScale.Crop, // Crop image to fill the space
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f) // Matches the aspect ratio of thumbnails
        )
    }
}
