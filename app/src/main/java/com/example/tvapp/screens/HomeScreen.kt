package com.example.tvapp.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
import androidx.tv.material3.IconButton
import kotlinx.coroutines.launch


@Composable
fun EpgScreen() {
    var isMenuExpanded by remember { mutableStateOf(false) } // State for Sidebar
    val pagerState = rememberPagerState(pageCount = { 3 }) // Example with 3 banners

    Row(Modifier.fillMaxSize()) {
        // Collapsible Sidebar
        AnimatedVisibility(visible = isMenuExpanded) {
            SideNavigationMenu(
                onClose = { isMenuExpanded = false } // Close when clicked
            )
        }

        Column(
            modifier = Modifier
            .fillMaxSize()
            .clickable { isMenuExpanded = false } // Collapse menu when clicking outside
        ) {
            // Top Pager Section
            TopPagerSection()

            // EPG Grid
            EpgGrid(
                onMenuClick = { isMenuExpanded = true }
            )
        }
    }
}

// Sidebar Navigation
@Composable
fun SideNavigationMenu(onClose: () -> Unit) {
    Column(
        Modifier
            .width(200.dp)
            .background(Color.DarkGray)
            .clickable { onClose() } // Close menu when clicked
    ) {
        val menuItems = listOf("Search", "News", "Entertainment", "Music", "Movies")
        menuItems.forEach {
            Text(
                text = it,
                color = Color.White,
                modifier = Modifier
                    .padding(16.dp)
                    .clickable { /* Handle Click */ }
            )
        }
    }
}
@Composable
fun TopPagerSection() {
    val pagerState = rememberPagerState(pageCount = { 3 })

    Column(modifier = Modifier.fillMaxWidth()) {
        BannerPager(pagerState)
    }
}

@Composable
fun BannerPager(pagerState: PagerState) {
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        // Horizontal Pager
        HorizontalPager(state = pagerState) { page ->
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        when (page) {
                            0 -> Color.Red
                            1 -> Color.Blue
                            else -> Color.Green
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Banner ${page + 1}",
                    color = Color.White
                )
            }
        }

        // Left Arrow Button
        IconButton(
            onClick = {
                coroutineScope.launch {
                    val prevPage = (pagerState.currentPage - 1).coerceAtLeast(0)
                    pagerState.animateScrollToPage(prevPage) // Smooth scroll
                }
            },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
        ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Previous", tint = Color.White)
        }

        // Right Arrow Button
        IconButton(
            onClick = {
                coroutineScope.launch {
                    val nextPage = (pagerState.currentPage + 1).coerceAtMost(2)
                    pagerState.animateScrollToPage(nextPage) // Smooth scroll
                }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
        ) {
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Next", tint = Color.White)
        }
    }
}



// EPG Grid (Simplified)
@Composable
fun EpgGrid(onMenuClick: () -> Unit) {
    val channels = listOf("Aaj Tak", "Republic Bharat", "India TV", "ABP News")
    val programs = listOf("News", "Debate", "Interview", "Documentary")

    LazyColumn {
        items(channels.size) { channelIndex ->
            Row(Modifier.fillMaxWidth()) {
                // Channel Name
                Box(
                    Modifier
                        .width(120.dp)
                        .height(50.dp)
                        .background(Color.Gray)
                        .clickable { onMenuClick() }
                ) {
                    Text(
                        text = channels[channelIndex],
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                // Program Schedule
                LazyRow {
                    items(programs.size) { programIndex ->
                        Box(
                            Modifier
                                .width(150.dp)
                                .height(50.dp)
                                .background(if (programIndex == 0) Color.Yellow else Color.LightGray)
                        ) {
                            Text(
                                text = programs[programIndex],
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }
    }
}
@Preview(
    showBackground = true,
    widthDp = 960,
    heightDp = 540
)
@Composable
fun PreviewTvLoginScreen() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            EpgScreen()
        }
    }
}

