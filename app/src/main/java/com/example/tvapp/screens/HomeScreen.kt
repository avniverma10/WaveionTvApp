package com.example.tvapp.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.tvapp.components.ExpandableNavigationMenu
import com.example.tvapp.models.Banner
import com.example.tvapp.models.HomeContent
import com.example.tvapp.viewmodels.EPGViewModel
import com.example.tvapp.viewmodels.HomeViewModel
import kotlinx.coroutines.delay
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun HomeScreen(navController: NavController,
               homeViewModel: HomeViewModel = hiltViewModel(),
               epgViewModel: EPGViewModel = hiltViewModel()) {

    val homeContent by homeViewModel.homeContent.collectAsState()
    val bannerList by epgViewModel.bannerList.collectAsState() // ✅ Get banners from EPGViewModel

    Log.d("HOME", "Home content -->$homeContent")
    Log.d("EPG", "Banner content -->$bannerList")

    // Load banners from API
    LaunchedEffect(Unit) {
        epgViewModel.fetchBanners()
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // Left Side: Expandable Navigation Menu
        ExpandableNavigationMenu(navController)

        // Right Side: Scrollable Home Content
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        ) {
            item {
                if (bannerList.isNotEmpty()) {
                    HeroCarousel(bannerList, navController) // ✅ Use banners instead of `homeContent`
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Loading Banners...", color = Color.White)
                    }
                }
            }

            if (homeContent.isNotEmpty()) {
                item { CategorySection("Continue Watching", homeContent, navController) }
                item { CategorySection("Trending", homeContent, navController) }
                item { CategorySection("TV Shows", homeContent, navController) }
                item { CategorySection("Movies", homeContent, navController) }
            }
        }
    }
}


@Composable
fun HeroCarousel(bannerList: List<Banner>, navController: NavController) {
    if (bannerList.isNotEmpty()) {
        var selectedIndex by remember { mutableStateOf(0) }
        var isButtonFocused by remember { mutableStateOf(false) }

        // Auto-scroll logic for Hero Carousel
        LaunchedEffect(bannerList) {
            while (true) {
                delay(5000) // Auto-scroll every 5 seconds
                selectedIndex = (selectedIndex + 1) % bannerList.size
            }
        }

        val selectedBanner = bannerList[selectedIndex]
        val encodedUrl = URLEncoder.encode(selectedBanner.bannerContentLink, StandardCharsets.UTF_8.toString())

        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = rememberAsyncImagePainter(selectedBanner.bannerUrl),
                contentDescription = "",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        Log.d("HeroCarousel", "Navigating to video: ${selectedBanner.bannerContentLink}")
                        navController.navigate("homeplayer/$encodedUrl")
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .onFocusChanged { isButtonFocused = it.isFocused }
                        .focusable()
                        .border(
                            width = if (isButtonFocused) 3.dp else 0.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clip(RoundedCornerShape(4.dp)),
                    shape = RectangleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text(text = "Watch Now", color = Color.White)
                }
            }
        }
    }
}



@Composable
fun CategorySection(title: String, contentList: List<HomeContent>,  navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black) // Set background to black
            .padding(vertical = 12.dp)

    ) {
        Text(
            text = title,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
        LazyRow {
            items(contentList) { content ->
                val encodedUrl = URLEncoder.encode(content.videoUrl, StandardCharsets.UTF_8.toString())
                VideoThumbnail(content) {
                    navController.navigate("homeplayer/$encodedUrl")
                }
            }
        }
    }
}


@Composable
fun VideoThumbnail(content: HomeContent, onVideoClick: (String) -> Unit) {
    var isFocused by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(8.dp)
            .width(160.dp)
            .onFocusChanged { focusState -> isFocused = focusState.isFocused } // Track focus state
            .focusable() // Make the item navigable
            .border(
                width = if (isFocused) 4.dp else 0.dp, // Show border only when focused
                color = Color.White,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onVideoClick(content.videoUrl) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberAsyncImagePainter(content.thumbnailUrl),
            contentDescription = content.title,
            modifier = Modifier
                .height(120.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Crop
        )
        Text(
            text = content.title,
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodySmall,
           // Optional: Change text color when focused
        )
    }
}


