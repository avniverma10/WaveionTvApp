import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tvapp.database.EPGEntity
import com.example.tvapp.viewmodels.EPGViewModel

//package com.example.tvapp.database
//
//import androidx.compose.animation.animateContentSize
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxHeight
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.ArrowForward
//import androidx.compose.material3.Divider
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.tv.material3.Icon
//import androidx.tv.material3.IconButton
//import androidx.tv.material3.Text
//import coil3.compose.AsyncImage
//import com.example.tvapp.R
//import com.example.tvapp.viewmodels.EPGViewModel
//import com.google.accompanist.pager.ExperimentalPagerApi
//import com.google.accompanist.pager.HorizontalPager
//import com.google.accompanist.pager.rememberPagerState
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//
//@Composable
//fun HomeScreen(viewModel: EPGViewModel = hiltViewModel()) {
//    val epgData by viewModel.epgList.collectAsState()
//
//
//    Row(modifier = Modifier.fillMaxSize()) {
//        // Left Navigation Menu
//        ExpandableNavigationMenu()
//
//        Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
//            // Top Banner
//            AdvertisementBanner()
//
//            Row(modifier = Modifier.fillMaxSize()) {
//                // Left Navigation Menu
//                NavigationMenu()
//
//                Column(modifier = Modifier.weight(1f)) {
//                    // Timeline
//                    TimeHeader()
//
////                // EPG List
////                LazyColumn {
////                    items(epgData) { channel ->
////                        ChannelRow(channel)
////                    }
////                }
//                }
//            }
//        }
//    }
//}
//@Composable
//fun ExpandableNavigationMenu() {
//    var expanded by remember { mutableStateOf(false) }
//
//    Column(
//        modifier = Modifier
//            .width(if (expanded) 180.dp else 80.dp)
//            .fillMaxHeight()
//            .background(Color.Black)
//            .clickable { expanded = !expanded }
//            .animateContentSize()
//            .padding(8.dp)
//    ) {
//        val menuItems = listOf(
//            MenuItem(R.drawable.user, "Profile") // First item (User)
//        )
//
//        val otherMenuItems = listOf(
//            MenuItem(R.drawable.search_svgrepo_com, "Search"),
//            MenuItem(R.drawable.tv, "TV Guide"),
//            MenuItem(R.drawable.play, "Play"),
//            MenuItem(R.drawable.language, "Languages"),
//            MenuItem(R.drawable.setting, "Settings")
//        )
//
//        // First item (User) at the top
//        menuItems.forEach { item ->
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(8.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Icon(
//                    painter = painterResource(id = item.icon),
//                    contentDescription = item.label,
//                    tint = Color.White,
//                    modifier = Modifier.size(32.dp)
//                )
//                if (expanded) {
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text(
//                        text = item.label,
//                        color = Color.White,
//                        fontSize = 14.sp
//                    )
//                }
//            }
//        }
//
//        // Divider after User icon
//        Divider(
//            color = Color.Gray, // Customize the color
//            thickness = 1.dp,
//            modifier = Modifier.padding(vertical = 8.dp)
//        )
//
//        // Spacer to create space between "User" and other items
//        Spacer(modifier = Modifier.height(20.dp))
//
//
//        // Other menu items
//        otherMenuItems.forEach { item ->
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(8.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Icon(
//                    painter = painterResource(id = item.icon),
//                    contentDescription = item.label,
//                    tint = Color.White,
//                    modifier = Modifier.size(32.dp)
//                )
//                if (expanded) {
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text(
//                        text = item.label,
//                        color = Color.White,
//                        fontSize = 14.sp
//                    )
//                }
//            }
//        }
//    }
//}
//
//
//data class MenuItem(val icon: Int, val label: String)
//
//@OptIn(ExperimentalPagerApi::class)
//@Composable
//fun AdvertisementBanner() {
//    val images = listOf(
//        R.drawable.news5,
//        R.drawable.news6,
//        R.drawable.news4
//    )
//
//    val pagerState = rememberPagerState(initialPage = 0)
//    val coroutineScope = rememberCoroutineScope()
//
//    // Auto-scroll every 3 seconds
//    LaunchedEffect(pagerState) {
//        while (true) {
//            delay(3000)
//            coroutineScope.launch {
//                val nextPage = (pagerState.currentPage + 1) % images.size
//                pagerState.animateScrollToPage(nextPage)
//            }
//        }
//    }
//
//    Box(modifier = Modifier.fillMaxWidth().height(130.dp).width(120.dp)) {
//        HorizontalPager(
//            count = images.size,
//            state = pagerState,
//            modifier = Modifier.fillMaxSize()
//        ) { page ->
//            Image(
//                painter = painterResource(id = images[page]),
//                contentDescription = "Advertisement",
//                modifier = Modifier.fillMaxSize(),
//                contentScale = ContentScale.Crop
//            )
//        }
//
//        // Left Button
//        IconButton(
//            onClick = {
//                coroutineScope.launch {
//                    val prevPage = (pagerState.currentPage - 1 + images.size) % images.size
//                    pagerState.animateScrollToPage(prevPage)
//                }
//            },
//            modifier = Modifier.align(Alignment.CenterStart)
//                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
//        ) {
//            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Previous", tint = Color.White)
//        }
//
//        // Right Button
//        IconButton(
//            onClick = {
//                coroutineScope.launch {
//                    val nextPage = (pagerState.currentPage + 1) % images.size
//                    pagerState.animateScrollToPage(nextPage)
//                }
//            },
//            modifier = Modifier.align(Alignment.CenterEnd)
//                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
//        ) {
//            Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = "Next", tint = Color.White)
//        }
//    }
//}
//
//
//@Composable
//fun ChannelRow(program: EPGEntity) {
//    Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
//        // Channel Name
//        Text(
//            text = program.serviceName,
//            color = Color.White,
//            fontSize = 16.sp,
//            modifier = Modifier.weight(1f)
//        )
//
//        // Program Details
//        Box(
//            modifier = Modifier
//                .width(120.dp)
//                .height(50.dp)
//                .background(Color.DarkGray)
//                .padding(4.dp),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(program.eventName, color = Color.White, fontSize = 14.sp)
//        }
//    }
//}
//
//
//@Composable
//fun NavigationMenu() {
//    val menuItems = listOf(
//        MenuItem(R.drawable.recent, "Recently Watched"),
//        MenuItem(R.drawable.news, "News"),
//        MenuItem(R.drawable.face, "Entertainment"),
//        MenuItem(R.drawable.music, "Music"),
//        MenuItem(R.drawable.kid, "Kids"),
//        MenuItem(R.drawable.spirit, "Spiritual"),
//        MenuItem(R.drawable.movie, "Movies"),
//        MenuItem(R.drawable.star, "Lifestyle")
//    )
//
//    Column(
//        modifier = Modifier
//            .width(200.dp) // Fixed width
//            .fillMaxHeight()
//            .background(Color.DarkGray)
//            .padding(top = 16.dp)
//    ) {
//        menuItems.forEach { item ->
//            Row(  // Use Row to align the icon and text horizontally
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 12.dp, horizontal = 16.dp)
//                    .clickable { /* Handle Click */ },
//                verticalAlignment = Alignment.CenterVertically // Align icon and text
//            ) {
//                Icon(
//                    painter = painterResource(id = item.icon), // Load icon from resources
//                    contentDescription = item.label,
//                    tint = Color.White,
//                    modifier = Modifier.size(24.dp) // Adjust icon size
//                )
//
//                Spacer(modifier = Modifier.width(8.dp)) // Space between icon and text
//
//                Text(
//                    text = item.label,
//                    color = Color.White,
//                    fontSize = 14.sp,
//                    fontWeight = FontWeight.Medium
//                )
//            }
//        }
//    }
//}
//
//
//
////@Composable
////fun NavigationMenu() {
////    val menuItems = listOf("Recently Watched", "News", "Entertainment", "Music", "Kids", "Spiritual", "Movies", "Lifestyle")
////
////    Column(
////        modifier = Modifier
////            .width(120.dp)
////            .fillMaxHeight()
////            .background(Color.DarkGray)
////            .padding(2.dp)
////    ) {
////        menuItems.forEach { item ->
////            Box(
////                modifier = Modifier
////                    .fillMaxWidth()
////                    .padding(8.dp)
////                    .clickable { /* Handle Click */ },
////                contentAlignment = Alignment.Center
////            ) {
////                Text(text = item, color = Color.White, fontSize = 14.sp)
////            }
////        }
////    }
////}
//
//@Composable
//fun TimeHeader() {
//    val timeSlots = listOf("05:00 PM", "05:30 PM", "06:00 PM", "06:30 PM")
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(40.dp)
//            .background(Color.Black)
//            .padding(8.dp),
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        timeSlots.forEach { time ->
//            Text(text = time, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
//        }
//    }
//}
////
////@Preview(
////    showBackground = true, widthDp = 960,  // Adjust for your target resolution (e.g., 1920x1080 TV)
////    heightDp = 540
////)
////@Composable
////fun PreviewExpandableNavigationMenu() {
////    HomeScreen()
////}
////
//

@Composable
fun EPGScreen(viewModel: EPGViewModel = hiltViewModel()) {
    val epgList by viewModel.epgList.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "EPG Data", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        if (epgList.isEmpty()) {
            Text(text = "No EPG Data Found", color = Color.Red)
        } else {
            LazyColumn {
                items(epgList) { program ->
                    EPGItem(program)
                }
            }
        }
    }
}

@Composable
fun EPGItem(program: EPGEntity) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Channel: ${program.serviceName}", fontWeight = FontWeight.Bold)
            Text(text = "Program: ${program.eventName}")
            Text(text = "Time: ${program.startTime} - ${program.endTime}")
            Text(text = "Description: ${program.eventDescription}")
        }
    }
}

