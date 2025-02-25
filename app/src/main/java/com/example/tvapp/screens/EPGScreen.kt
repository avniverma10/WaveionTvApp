
package com.example.tvapp.screens


import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Divider
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
import com.example.tvapp.R
import com.example.tvapp.viewmodels.EPGViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



@Composable
fun EPGScreen(viewModel: EPGViewModel = hiltViewModel()) {
    val epgChannels by viewModel.epgChannels.collectAsState()

    Row(modifier = Modifier.fillMaxSize()) {
        // Left Navigation Menu
        ExpandableNavigationMenu()

        Column(modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)) {
            // Top Banner
            AdvertisementBanner()

            Row(modifier = Modifier.fillMaxSize()) {
                // Left Navigation Menu
                NavigationMenu()
                EPGContent()

                }
            }
        }
    }

@Composable
fun ExpandableNavigationMenu() {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .width(if (expanded) 180.dp else 80.dp)
            .fillMaxHeight()
            .background(Color.Black)
            .clickable { expanded = !expanded }
            .animateContentSize()
            .padding(8.dp)
    ) {
        val menuItems = listOf(
            MenuItem(R.drawable.user, "Profile") // First item (User)
        )

        val otherMenuItems = listOf(
            MenuItem(R.drawable.search_svgrepo_com, "Search"),
            MenuItem(R.drawable.tv, "TV Guide"),
            MenuItem(R.drawable.play, "Play"),
            MenuItem(R.drawable.language, "Languages"),
            MenuItem(R.drawable.setting, "Settings")
        )

        // First item (User) at the top
        menuItems.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = item.icon),
                    contentDescription = item.label,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                if (expanded) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.label,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Divider after User icon
        Divider(
            color = Color.Gray, // Customize the color
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Spacer to create space between "User" and other items
        Spacer(modifier = Modifier.height(20.dp))


        // Other menu items
        otherMenuItems.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = item.icon),
                    contentDescription = item.label,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                if (expanded) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.label,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}


data class MenuItem(val icon: Int, val label: String)

@OptIn(ExperimentalPagerApi::class)
@Composable
fun AdvertisementBanner() {
    val images = listOf(
        R.drawable.news5,
        R.drawable.news6,
        R.drawable.news4
    )

    val pagerState = rememberPagerState(initialPage = 0)
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll every 3 seconds
    LaunchedEffect(pagerState) {
        while (true) {
            delay(3000)
            coroutineScope.launch {
                val nextPage = (pagerState.currentPage + 1) % images.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Box(modifier = Modifier
        .fillMaxWidth()
        .height(130.dp)
        .width(120.dp)) {
        HorizontalPager(
            count = images.size,
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            Image(
                painter = painterResource(id = images[page]),
                contentDescription = "Advertisement",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Left Button
        IconButton(
            onClick = {
                coroutineScope.launch {
                    val prevPage = (pagerState.currentPage - 1 + images.size) % images.size
                    pagerState.animateScrollToPage(prevPage)
                }
            },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
        ) {
            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Previous", tint = Color.White)
        }

        // Right Button
        IconButton(
            onClick = {
                coroutineScope.launch {
                    val nextPage = (pagerState.currentPage + 1) % images.size
                    pagerState.animateScrollToPage(nextPage)
                }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
        ) {
            Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = "Next", tint = Color.White)
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
