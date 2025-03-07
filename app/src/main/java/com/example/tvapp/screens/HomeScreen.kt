package com.example.tvapp.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.tvapp.models.HomeContent
import com.example.tvapp.viewmodels.HomeViewModel
import kotlinx.coroutines.delay
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun HomeScreen(navController: NavController,viewModel: HomeViewModel = hiltViewModel()) {
    val homeContent by viewModel.homeContent.collectAsState()

    Log.d("HOME", "Home content -->$homeContent")



    LaunchedEffect(Unit) {
        viewModel.loadHomeContent()
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
                if (homeContent.isNotEmpty()) {
                    HeroCarousel(homeContent, navController)
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Loading...", color = Color.White)
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
fun HeroCarousel(homeContent: List<HomeContent>, navController: NavController) {
    if (homeContent.isNotEmpty()) {
        var selectedIndex by remember { mutableStateOf(0) }

        // Auto-scroll logic
        LaunchedEffect(homeContent) {
            while (true) {
                delay(5000) // Change slide every 3 seconds
                selectedIndex = (selectedIndex + 1) % homeContent.size
            }
        }

        val selectedContent = homeContent[selectedIndex]
        val encodedUrl = URLEncoder.encode(selectedContent.videoUrl, StandardCharsets.UTF_8.toString())

        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = rememberAsyncImagePainter(selectedContent.thumbnailUrl),
                contentDescription = selectedContent.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clickable {navController.navigate("homeplayer/$encodedUrl")},
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = selectedContent.title,
                    modifier = Modifier.padding(8.dp),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall
                )

                Button(
                    onClick = { navController.navigate("homeplayer/$encodedUrl") },
                    modifier = Modifier
                        .padding(8.dp)  .clip(RoundedCornerShape(4.dp)),
                    shape = RectangleShape, // Ensures sharp corners

                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black) // Black Button
                ) {
                    Text(text = "Watch Now",color = Color.White)
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
    Column(
        modifier = Modifier
            .padding(8.dp)
            .width(160.dp)
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
            style = MaterialTheme.typography.bodySmall
        )
    }
}
