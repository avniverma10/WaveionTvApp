package com.example.tvapp.view.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import com.example.tvapp.R
import com.example.tvapp.view.navigationhelper.ExpandableNavigationMenu
import com.example.tvapp.viewmodels.SharedViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun HomeScreen(navController: NavController,sharedViewModel:SharedViewModel) {
    val bannerList = listOf(
        Banner(R.drawable.banner4, "Star News", "Watch the latest breaking news", "Watch Now","https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"),
        Banner(R.drawable.banner1, "AajTak", "Watch the latest breaking news", "Watch Now","https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"),
        Banner(R.drawable.banner2, "News18", "Watch the latest breaking news", "Watch Now","https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"),
        Banner(R.drawable.banner3, "Zee News", "Watch the latest breaking news", "Watch Now","https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"),

    )

    val staticHomeContent = listOf(
        HomeContent("Avatar", R.drawable.news3, "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"),
        HomeContent("The Rings of Power", R.drawable.news5, "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"),
        HomeContent("Squid Game 2", R.drawable.news6, "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"),
        HomeContent("Superhero India", R.drawable.news7, "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"),
        HomeContent("Movie X", R.drawable.news9, "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"),
        HomeContent("TV Show Y", R.drawable.news10, "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"),
        HomeContent("Squid Game", R.drawable.news1, "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"),
        HomeContent("Avatar", R.drawable.news3, "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"),
        HomeContent("The Rings of Power", R.drawable.news5, "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"),
    )

    Row(modifier = Modifier.fillMaxSize()) {
        // Left Side: Expandable Navigation Menu
        // Left Navigation Menu
        ExpandableNavigationMenu(navController, sharedViewModel, onNavMenuIntent = { tabInfo, selectedTabIndex ->
            //menuItems = tabInfo.categories ?: emptyList()
        })

        LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { HeroCarousel(bannerList, navController) }
        item { CategorySection("Continue Watching", staticHomeContent, navController) }
        item { CategorySection("Trending", staticHomeContent, navController) }
        item { CategorySection("TV Shows", staticHomeContent, navController) }
        item { CategorySection("Movies", staticHomeContent, navController) }
    }
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

    // Auto-scroll logic for Hero Carousel
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
                    fontWeight = FontWeight(700),
                    color = Color(0xFFFFFFFF)
                ),
                modifier = Modifier.padding(8.dp)
            )

            Text(
                text = selectedBanner.description,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.figtree_light)),
                    fontWeight = FontWeight(600),
                ),
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    navController.navigate("homeplayer/$encodedUrl")
                },
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
                        fontWeight = FontWeight(600),
                        color = Color(0xFF000000),
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Image(
                    painter = painterResource(id = R.drawable.play), // Load the icon
                    contentDescription = "Play Icon",
                    modifier = Modifier.size(18.dp) // Adjust icon size
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



@Composable
fun VideoThumbnail(content: HomeContent, onVideoClick: (String) -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    val animatedSize by animateDpAsState(if (isFocused) 200.dp else 180.dp, animationSpec = tween(150))

    Column(
        modifier = Modifier
            .padding(4.dp)
            .width(animatedSize)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { val encodedUrl = URLEncoder.encode(content.videoUrl, StandardCharsets.UTF_8.toString())
                onVideoClick(encodedUrl)  },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = content.videoResId),
            contentDescription = content.title,
            modifier = Modifier
                .width(animatedSize)
                .height(animatedSize * 0.6f)
                .clip(RoundedCornerShape(8.dp))
                .border(
                    if (isFocused) 2.dp else 0.dp,
                    if (isFocused) Color(0xFF49FEDD) else Color.Transparent,
                    shape = RoundedCornerShape(4.dp),
                ),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun CategorySection(title: String, contentList: List<HomeContent>, navController: NavController) {
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
                fontWeight = FontWeight(600),
                color = Color.White,
            )
        )

        LazyRow(
            modifier = Modifier.padding(start = 16.dp)
        ) {
            itemsIndexed(contentList) { _, content ->
                VideoThumbnail(content) { videoUrl ->
                    navController.navigate("homeplayer/$videoUrl") // Pass encoded URL
                }
            }
        }
    }
}


// Data Classes
data class Banner(
    val bannerResId: Int,
    val title: String,
    val description: String,
    val buttonText: String,
    val videoUrl: String
)

data class HomeContent(
    val title: String,
    val videoResId: Int,
    val videoUrl: String
)
