
package com.example.tvapp.screens


import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.example.tvapp.R
import com.example.tvapp.components.ExpandableNavigationMenu
import com.example.tvapp.models.Tab
import com.example.tvapp.viewmodels.EPGViewModel
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun EPGScreen(viewModel: EPGViewModel = hiltViewModel()) {
    val epgChannels by viewModel.epgChannels.collectAsState()
    val bannerList by viewModel.bannerList.collectAsState(initial = emptyList())
    val tabsList by viewModel.tabs.collectAsState(initial = emptyList())
    val showBanner = isBannerVisible(tabsList)

    Row(modifier = Modifier.fillMaxSize()) {
        // Left Navigation Menu
        ExpandableNavigationMenu()

        Column(modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)) {
            // Top Banner
            if (showBanner) {
                AdvertisementBanner(bannerList = bannerList)
            } else {
                Log.i("EPGScreen", "Advertisement banner is not displayed due to visibility settings or missing data.")
            }

            Row(modifier = Modifier.fillMaxSize()) {
                // Left Navigation Menu
                NavigationMenu()
                EPGContent()

                // Log tabs data for debugging purposes
                LaunchedEffect(tabsList) {
                    Log.i("RISHI", "Tab list updated: $tabsList")
                }
            }
        }
    }
}

fun isBannerVisible(tabsList: List<Tab>?): Boolean {
    if (tabsList.isNullOrEmpty()) {
        Log.i("EPGScreen", "No tabs data available or tabs list is empty.")
        return false
    }
    val bannerVisible = tabsList.any { tab ->
        tab.components.any { component ->
            component.name == "Banner" && component.isVisible
        }
    }
    if (!bannerVisible) {
        Log.i("EPGScreen", "Banner component not visible or not found in any tab.")
    }
    return bannerVisible
}







data class MenuItem(val icon: Int, val label: String)

@Composable
fun AdvertisementBanner(bannerList: List<com.example.tvapp.models.Banner>) {
    if (bannerList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(Color.Gray)
        ) {
            Text(
                text = "No Banner Available",
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        }
    } else {
        val pagerState = rememberPagerState(initialPage = 0)
        val coroutineScope = rememberCoroutineScope()

        // Auto-scroll every 3 seconds
        LaunchedEffect(pagerState) {
            while (true) {
                delay(3000)
                coroutineScope.launch {
                    val nextPage = (pagerState.currentPage + 1) % bannerList.size
                    pagerState.animateScrollToPage(nextPage)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            HorizontalPager(
                count = bannerList.size,
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                // Load the image from the URL using Coil's AsyncImage
                AsyncImage(
                    model = bannerList[page].bannerUrl,
                    contentDescription = "Advertisement",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Left Button
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        val prevPage = (pagerState.currentPage - 1 + bannerList.size) % bannerList.size
                        pagerState.animateScrollToPage(prevPage)
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Previous",
                    tint = Color.White
                )
            }

            // Right Button
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        val nextPage = (pagerState.currentPage + 1) % bannerList.size
                        pagerState.animateScrollToPage(nextPage)
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = "Next",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun NavigationMenu() {
    val menuItems = listOf(
        MenuItem(R.drawable.recent, "Recently Watched"),
        MenuItem(R.drawable.news, "News"),
        MenuItem(R.drawable.face, "Entertainment"),
        MenuItem(R.drawable.music, "Music"),
        MenuItem(R.drawable.kid, "Kids"),
        MenuItem(R.drawable.spirit, "Spiritual"),
        MenuItem(R.drawable.movie, "Movies"),
        MenuItem(R.drawable.star, "Lifestyle")
    )

    Column(
        modifier = Modifier
            .width(200.dp) // Fixed width
            .fillMaxHeight()
            .background(Color.DarkGray)
            .padding(top = 16.dp)
    ) {
        menuItems.forEach { item ->
            Row(  // Use Row to align the icon and text horizontally
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp)
                    .clickable { /* Handle Click */ },
                verticalAlignment = Alignment.CenterVertically // Align icon and text
            ) {
                Icon(
                    painter = painterResource(id = item.icon), // Load icon from resources
                    contentDescription = item.label,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp) // Adjust icon size
                )

                Spacer(modifier = Modifier.width(8.dp)) // Space between icon and text

                Text(
                    text = item.label,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
