package com.example.tvapp.view.home

import android.app.Activity
import android.os.Process
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.android.caastv.R
import com.example.tvapp.extensions.appHomeLiveData
import com.example.tvapp.extensions.appManifestLiveData
import com.example.tvapp.model.data.banner.Banner
import com.example.tvapp.model.data.epgdata.Channel
import com.example.tvapp.ui.theme.base_color
import com.example.tvapp.ui.theme.bg_card_color
import com.example.tvapp.ui.theme.screen_bg_color
import com.example.tvapp.utils.Constants
import com.example.tvapp.view.navigationhelper.Destination
import com.example.tvapp.view.navigationhelper.ExpandableNavigationMenu
import com.example.tvapp.view.uicomponent.ExitDialog
import com.example.tvapp.view.uicomponent.error.CommonDialog
import com.example.tvapp.viewmodels.SharedViewModel
import kotlinx.coroutines.delay
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun HomeScreen(navController: NavController, sharedViewModel: SharedViewModel) {
    val homeCategories by sharedViewModel.provideApplicationContext().appHomeLiveData().observeAsState(initial = emptyList())
    val epgChannels by sharedViewModel.wtvEPGList.collectAsState()
    val banners by sharedViewModel.bannerList.collectAsState()
    val context = LocalContext.current
    var showExitDialog by remember { mutableStateOf(false) }
    // Observe the SSE event flow.
    val tabItemsData by sharedViewModel.tabItemsFlow.collectAsState()
    val appManifestData = sharedViewModel.provideApplicationContext().appManifestLiveData()

    // 1) remember a state for your column
    val columnState = rememberLazyListState()

    val firstChannelFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        columnState.scrollToItem(0)
        firstChannelFocusRequester.requestFocus()
    }
    BackHandler {
        showExitDialog = true
    }


    // Exit confirmation dialog
    if (showExitDialog) {
        CommonDialog(
            showDialog = true,
            title = "Exit App",
            borderColor = Color.Transparent,
            painter = painterResource(id = R.drawable.exit_icon),
            message = "Are you sure you want to exit the app?",
            confirmButtonText = "Yes",
            onConfirm = {
                (context as? Activity)?.finishAffinity()
                Process.killProcess(Process.myPid())
            },
            dismissButtonText = "No",
            onDismiss = { showExitDialog = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screen_bg_color)
    ) {

        LazyColumn( state = columnState,modifier = Modifier.fillMaxSize(). padding(start = 70.dp)) {
            // ③ Switch to itemsIndexed so we know when it's the first category
            itemsIndexed(homeCategories) { catIndex, category ->
                val epgList = epgChannels
                    ?.filter { it.channelId in category.channels }
                val channelsForCategory = epgList
                    ?.mapNotNull { epgItem ->
                        epgItem.tv?.channel?.copy(
                            logoUrl  = epgItem.content?.thumbnailUrl,
                            videoUrl = epgItem.content?.videoUrl,
                            genreId  = epgItem.content?.genreId ?: "Unknown"
                        )
                    } ?: emptyList()

                if (channelsForCategory.isNotEmpty()) {
                    CategorySection(
                        title                    = category.name,
                        channels                 = channelsForCategory,
                        navController            = navController,
                        sharedViewModel          = sharedViewModel,
                        // ④ Pass down our focusRequester only on the *very first* category
                        firstChannelFocusRequester = if (catIndex == 0) firstChannelFocusRequester else null,
                        isFirstCategory          = (catIndex == 0)
                    )
                }
            }
        }
        ExpandableNavigationMenu(
            navController      = navController,
            sharedViewModel    = sharedViewModel,
            onNavMenuIntent    = { _, _ -> },
            modifier           = Modifier.align(Alignment.CenterStart)
        )
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategorySection(
    title: String,
    channels: List<Channel>,
    navController: NavController,
    sharedViewModel: SharedViewModel,
    firstChannelFocusRequester: FocusRequester? = null,
    isFirstCategory: Boolean = false
) {
    val rowState = rememberLazyListState()
    // 1) Create a BringIntoViewRequester
    val bringRequester = remember { BringIntoViewRequester() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = screen_bg_color)
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(start = 16.dp, bottom = 5.dp),
            style = TextStyle(
                fontSize = 19.sp,
                fontFamily = FontFamily(Font(R.font.figtree_light)),
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(
            state = rowState,
            contentPadding       = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ⑤ Use itemsIndexed so we know when it's the first channel
            itemsIndexed(channels) { idx, channel ->
                // only the first item of the first category gets our focusRequester
                val modifier = if (isFirstCategory && idx == 0 && firstChannelFocusRequester != null) {
                    Modifier.focusRequester(firstChannelFocusRequester) .bringIntoViewRequester(bringRequester)
                } else {
                    Modifier
                }

                ChannelBox(
                    channel  = channel,
                    modifier = modifier,         // new slot
                ) { videoUrl ->
                    sharedViewModel.wtvEPGList.value
                        ?.find { it.content?.videoUrl == videoUrl }
                        ?.let { channelItem ->
                            sharedViewModel.updateSelectedChannel(channelItem)
                            navController.navigate(Destination.panMetroScreen)
                        }
                }
                if (isFirstCategory && firstChannelFocusRequester != null && channels.isNotEmpty()) {
                    LaunchedEffect(channels) {
                        // scroll horizontally to the very first channel
                        rowState.scrollToItem(0)
                        firstChannelFocusRequester.requestFocus()
                    }
                }
            }
        }
    }
}
@Composable
fun ChannelBox(
    channel: Channel,
    modifier: Modifier = Modifier,   // ← new
    onChannelClick: (String) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue   = if (isFocused) 1.1f else 1f,
        animationSpec = tween(durationMillis = 150)
    )

    Box(
        modifier = modifier                 // ← apply before the rest
            .width(140.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clip(RoundedCornerShape(8.dp))
            .background(bg_card_color)
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) base_color else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { channel.videoUrl?.let(onChannelClick) }
    ) {
        AsyncImage(
            model         = channel.logoUrl,
            contentDescription = channel.displayName,
            contentScale  = ContentScale.Fit,
            modifier      = Modifier
                .aspectRatio(16f / 9f)
                .padding(10.dp)
                .clip(RoundedCornerShape(8.dp))
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
                onClick = {
                    navController.navigate("homeplayer/$encodedUrl") },
                modifier = Modifier
                    .padding(8.dp)
                    .onFocusChanged { isButtonFocused = it.isFocused }
                    .focusable()
                    .border(
                        if (isButtonFocused) 2.dp else 0.dp,
                        if (isButtonFocused) base_color else Color.Transparent,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clip(RoundedCornerShape(6.dp)),
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text(
                    text = "Watch Now",
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
