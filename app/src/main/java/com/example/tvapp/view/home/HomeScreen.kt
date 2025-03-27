package com.example.tvapp.view.home

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import com.example.tvapp.model.data.epgdata.Channel
import com.example.tvapp.view.navigationhelper.Destination
import com.example.tvapp.view.navigationhelper.ExpandableNavigationMenu
import com.example.tvapp.viewmodels.SharedViewModel
import kotlinx.coroutines.delay
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun HomeScreen(navController: NavController, sharedViewModel: SharedViewModel) {
    // Observe your dynamic home categories and EPG channels (like in your first home screen)
    val homeCategories by sharedViewModel.provideApplicationContext().appHomeLiveData().observeAsState(initial = emptyList())
    val epgChannels by sharedViewModel.epgChannels.collectAsState()

    // Back handler: navigate to EPG screen when back is pressed.
    BackHandler {
        navController.navigate(Destination.epgScreen) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    // (Optional) Keep your static banner list for the Hero Carousel.
    // You could also choose to make this dynamic if needed.
    val bannerList = listOf(
        Banner(R.drawable.banner4, "Zee News", "Watch the latest breaking news", "Watch Now",
            "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"),
        Banner(R.drawable.banner5, "AajTak", "Watch the latest breaking news", "Watch Now",
            "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"),
        Banner(R.drawable.dd, "News18", "Watch the latest breaking news", "Watch Now",
            "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"),
        Banner(R.drawable.banner15, "Star News", "Watch the latest breaking news", "Watch Now",
            "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8")
    )

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF14161A))
    ) {
        // Left Navigation Menu (kept from your second layout)
        ExpandableNavigationMenu(navController, sharedViewModel, onNavMenuIntent = { _, _ -> })

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Hero carousel at the top
            item {
                HeroCarousel(bannerList = bannerList, navController = navController)
            }
            // For each dynamic category, map the channels and display a section.
            items(homeCategories) { category ->
                // Map channel IDs from the category to detailed Channel objects from epgChannels.
                val channelsForCategory = category.channels.mapNotNull { channelId ->
                    epgChannels.find { it._id?.equals(channelId, ignoreCase = true) == true }
                }
                if (channelsForCategory.isNotEmpty()) {
                    CategorySectionDynamic(
                        title = category.name,
                        channels = channelsForCategory,
                        navController = navController
                    )
                }
            }
        }
    }
}

// A composable for displaying a category section using dynamic channel data.
@Composable
fun CategorySectionDynamic(title: String, channels: List<Channel>, navController: NavController) {
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
//            modifier = Modifier
////                .padding(start = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(channels) { channel ->
                // Reuse your ChannelCard from the first home screen for each channel.
                ChannelBox(channel = channel) { videoUrl ->
                    Log.d("AVNIV","VideoUrl --->$videoUrl")
                    val encodedUrl = URLEncoder.encode(videoUrl, StandardCharsets.UTF_8.toString())
                    navController.navigate("homeplayer/$encodedUrl")
                }
            }
        }
    }
}

@Composable
fun ChannelBox(channel: Channel, onChannelClick: (String) -> Unit) {
    // Track whether this box is currently focused
    var isFocused by remember { mutableStateOf(false) }

    // Animate scale when focused/unfocused
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.1f else 1f,
        animationSpec = tween(durationMillis = 150)
    )

    Box(
        modifier = Modifier
            .width(140.dp)
            // Apply the scale animation
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            // Detect focus changes
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            }
            // Make this composable focusable
            .focusable()
            // Rounded corners
            .clip(RoundedCornerShape(8.dp))
            // Background color (could be Color.Transparent if you prefer)
            .background(Color.Black)
            // Show border only when focused
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) Color(0xFF49FEDD) else Color.Transparent,
                shape = RoundedCornerShape(2.dp)
            )
            // Clickable logic
            .clickable {
                channel.videoUrl?.let { onChannelClick(it) }
            }
    ) {
        // Only show the channel logo
        AsyncImage(
            model = channel.logoUrl,
            contentDescription = channel.displayName,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp).padding(8.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Fit
        )
    }
}




// Hero Carousel from your second home screen.
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

    // Auto-scroll logic for the hero carousel.
    LaunchedEffect(bannerList) {
        while (true) {
            delay(5000)
            selectedIndex = (selectedIndex + 1) % bannerList.size
        }
    }

    val selectedBanner = bannerList[selectedIndex]
    val encodedUrl = URLEncoder.encode(selectedBanner.videoUrl, StandardCharsets.UTF_8.toString())

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        Image(
            painter = painterResource(id = selectedBanner.bannerResId),
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
                text = selectedBanner.title,
                style = TextStyle(
                    fontSize = 35.sp,
                    fontFamily = FontFamily(Font(R.font.figtree_medium)),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                modifier = Modifier.padding(8.dp)
            )

            Text(
                text = selectedBanner.description,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.figtree_light)),
                    fontWeight = FontWeight.SemiBold,
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
                    text = selectedBanner.buttonText,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontFamily = FontFamily(Font(R.font.figtree_light)),
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
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

// Data classes remain the same.
data class Banner(
    val bannerResId: Int,
    val title: String,
    val description: String,
    val buttonText: String,
    val videoUrl: String
)
