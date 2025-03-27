package com.example.tvapp.view.home

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.tvapp.R
import com.example.tvapp.extensions.appHomeLiveData
import com.example.tvapp.model.data.banner.Banner
import com.example.tvapp.model.data.epgdata.Channel
import com.example.tvapp.view.navigationhelper.Destination
import com.example.tvapp.view.navigationhelper.ExpandableNavigationMenu
import com.example.tvapp.viewmodels.SharedViewModel
import kotlinx.coroutines.delay
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun HomeScreen(navController: NavController, sharedViewModel: SharedViewModel) {
    val homeCategories by sharedViewModel.provideApplicationContext().appHomeLiveData().observeAsState(initial = emptyList())
    val epgChannels by sharedViewModel.epgChannels.collectAsState()
    val banners by sharedViewModel.bannerList.collectAsState()

    BackHandler {
        navController.navigate(Destination.epgScreen) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF14161A))
    ) {
        ExpandableNavigationMenu(navController, sharedViewModel, onNavMenuIntent = { _, _ -> })

        LazyColumn(modifier = Modifier.fillMaxSize()) {

            item {
                if (banners.isNotEmpty()) {
                    HeroCarousel(bannerList = banners, navController = navController)
                } else {
                    // Show a loading indicator while banners are loading.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }
            // For each dynamic category, map channel IDs to detailed Channel objects from epgChannels.
            items(homeCategories) { category ->
                val channelsForCategory = category.channels.mapNotNull { channelId ->
                    epgChannels.find { it._id?.equals(channelId, ignoreCase = true) == true }
                }
                if (channelsForCategory.isNotEmpty()) {
                    CategorySection(
                        title = category.name,
                        channels = channelsForCategory,
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
fun CategorySection(title: String, channels: List<Channel>, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF14161A))
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(start = 16.dp, bottom = 5.dp),
            style = TextStyle(
                fontSize = 22.sp,
                fontFamily = FontFamily(Font(R.font.figtree_light)),
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(channels) { channel ->
                ChannelBox(channel = channel) { videoUrl ->
                    val encodedUrl = URLEncoder.encode(videoUrl, StandardCharsets.UTF_8.toString())
                    navController.navigate("homeplayer/$encodedUrl")
                }
            }
        }
    }
}

@Composable
fun ChannelBox(channel: Channel, onChannelClick: (String) -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.1f else 1f,
        animationSpec = tween(durationMillis = 150)
    )
    Box(
        modifier = Modifier
            .width(140.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            }
            .focusable()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black)
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) Color(0xFF49FEDD) else Color.Transparent,
                shape = RoundedCornerShape(2.dp)
            )
            .clickable {
                channel.videoUrl?.let { onChannelClick(it) }
            }
    ) {
        AsyncImage(
            model = channel.logoUrl,
            contentDescription = channel.displayName,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(8.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun HeroCarousel(bannerList: List<Banner>, navController: NavController) {
    var selectedIndex by remember { mutableStateOf(0) }
    var isButtonFocused by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition()
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    // Auto-scroll logic.
    LaunchedEffect(bannerList) {
        while (true) {
            delay(5000)
            selectedIndex = (selectedIndex + 1) % bannerList.size
        }
    }
    val selectedBanner = bannerList[selectedIndex]
    // Use a default video URL (adjust as needed)
    val videoUrl = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"
    val encodedUrl = URLEncoder.encode(videoUrl, StandardCharsets.UTF_8.toString())
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        // Load the banner image using AsyncImage.
        AsyncImage(
            model = selectedBanner.bannerUrl,
            contentDescription = null,
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
            Text(
                text = selectedBanner.name,
                style = TextStyle(
                    fontSize = 35.sp,
                    fontFamily = FontFamily(Font(R.font.figtree_medium)),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                modifier = Modifier.padding(8.dp)
            )
            Text(
                text = "Watch the latest breaking news", // default description
                style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.figtree_light)),
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { navController.navigate("homeplayer/$encodedUrl") },
                modifier = Modifier
                    .padding(8.dp)
                    .onFocusChanged { isButtonFocused = it.isFocused }
                    .focusable()
                    .border(
                        if (isButtonFocused) 2.dp else 0.dp,
                        if (isButtonFocused) Color(0xFF49FEDD) else Color.Transparent,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clip(RoundedCornerShape(6.dp)),
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text(
                    text = "Watch Now", // default button text
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontFamily = FontFamily(Font(R.font.figtree_light)),
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Image(
                    painter = painterResource(id = R.drawable.play),
                    contentDescription = "Play Icon",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            bannerList.indices.forEach { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == selectedIndex) 12.dp else 8.dp)
                        .background(
                            color = if (index == selectedIndex) Color.White else Color.Gray.copy(alpha = dotAlpha),
                            shape = RoundedCornerShape(50)
                        )
                )
            }
        }
    }
}
