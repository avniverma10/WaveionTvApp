package com.caastv.tvapp.view.home

import android.app.Activity
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.android.caastv.R
import com.caastv.tvapp.utils.theme.base_color
import com.caastv.tvapp.view.navigationhelper.Destination
import com.caastv.tvapp.view.navigationhelper.ExpandableNavigationMenu
import com.caastv.tvapp.view.uicomponent.error.CommonDialog
import com.caastv.tvapp.viewmodels.SharedViewModel
import kotlinx.coroutines.delay
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class Banner(
    @DrawableRes val image: Int,
    val title: String,
    val description: String,
    val genres: List<String>
)

@Composable
fun DemoHomeScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel
) {
    val menuWidth = 70.dp
    val banners = listOf(
        Banner(R.drawable.movie_26,  "Wicked",   "An epic journey across oceans.",       listOf("Action","Drama")),
        Banner(R.drawable.movie_31,  "X-MEN", "Mutants fight for survival.",    listOf("Sci-Fi","Adventure","Drama")),
        Banner(R.drawable.movie_29, "Master", "Mystery of an ancient bridge.",     listOf("Fantasy","History")),
        Banner(R.drawable.movie_30,  "TRON", "Underdog boxers battle for glory.", listOf("Fantasy","Sci-Fi","Inspiration"))
    )
    var currentBanner by remember { mutableStateOf(0) }
    var backPressCount by remember { mutableStateOf(0) }
    var showExitDialog by remember { mutableStateOf(false) }
    val watchNowRequester = remember { FocusRequester() }
    val context = LocalContext.current

    val fixedUrl = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"
    val encodedUrl = URLEncoder.encode(fixedUrl, StandardCharsets.UTF_8.toString())
    val menuFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(100)
        watchNowRequester.requestFocus()
    }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
        ) {
            BannerCarousel(
                imageResIds   = banners.map { it.image },
                modifier      = Modifier.matchParentSize(),
                switchMillis  = 5_000L,
                onIndexChange = { currentBanner = it }
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(200.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Black, Color.Transparent)
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black),
                            startY = 0f,
                            endY   = with(LocalDensity.current) { 200.dp.toPx() }
                        )
                    )
            )
        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = menuWidth, top = 24.dp, end = 24.dp, bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Bottom
        ) {
            val banner = banners[currentBanner]

            Text(
                text = banner.title,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = banner.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                banner.genres.forEach { GenreChip(it) }
            }

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                val playInteraction = remember { MutableInteractionSource() }
                val playFocused by playInteraction.collectIsFocusedAsState()
                Button(
                    onClick = { navController.navigate("${Destination.demoplayer}/$encodedUrl") },
                    interactionSource = playInteraction,
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (playFocused) Color(0xFF50776E) else Color.White,
                        contentColor   = if (playFocused) Color.White      else Color.Black
                    ),
                    modifier = Modifier
                        .height(48.dp)
                        .focusRequester(watchNowRequester)
                        .then(
                            if (playFocused)
                                Modifier.border(2.dp, base_color, RoundedCornerShape(4.dp))
                            else Modifier
                        )
                        .focusable(interactionSource = playInteraction)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Watch Now", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.width(16.dp))

                val favInteraction = remember { MutableInteractionSource() }
                val favFocused by favInteraction.collectIsFocusedAsState()
                IconButton(
                    onClick = { /* TODO */ },
                    interactionSource = favInteraction,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (favFocused) Color(0xFF50776E) else Color(0x33FFFFFF),
                        contentColor   = Color.White
                    ),
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .then(
                            if (favFocused)
                                Modifier.border(2.dp, base_color, RoundedCornerShape(4.dp))
                            else Modifier
                        )
                        .focusable(interactionSource = favInteraction)
                ) {
                    Icon(Icons.Default.Favorite, contentDescription = null)
                }
            }

            Spacer(Modifier.height(32.dp))

            CategorySection(navController, "Trending", continueWatchingRes)
            Spacer(Modifier.height(24.dp))
            CategorySection(navController, "Comedy",    topTvRes)
            Spacer(Modifier.height(24.dp))
            CategorySection(navController, "Romantic",  trending)
            Spacer(Modifier.height(24.dp))
            CategorySection(navController, "Latest Movies", latest)
            Spacer(Modifier.height(24.dp))
            CategorySection(navController, "Action", action)
            Spacer(Modifier.height(24.dp))
            CategorySection(navController, "Top Movies", topMovies)

            Spacer(Modifier.height(10.dp))
        }

        ExpandableNavigationMenu(
            navController   = navController,
            sharedViewModel = sharedViewModel,
            onNavMenuIntent = { _, _ -> },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(menuWidth),
            menuFocusRequester = menuFocusRequester,
            onBackPressed = {
                menuFocusRequester.requestFocus()
            }
        )

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
//                    Process.killProcess(Process.myPid())
                },
                dismissButtonText = "No",
                onDismiss = { showExitDialog = false }
            )

        }
    }
}

@Composable
fun BannerCarousel(
    @DrawableRes imageResIds: List<Int>,
    modifier: Modifier = Modifier,
    switchMillis: Long = 5_000L,
    onIndexChange: (Int) -> Unit
) {
    var current by remember { mutableStateOf(0) }
    LaunchedEffect(imageResIds) {
        while (true) {
            delay(switchMillis)
            current = (current + 1) % imageResIds.size
            onIndexChange(current)
        }
    }
    Box(modifier = modifier) {
        Image(
            painter           = painterResource(id = imageResIds[current]),
            contentDescription = null,
            contentScale      = ContentScale.Crop,
            modifier          = Modifier.matchParentSize()
        )
        DotsIndicator(
            count   = imageResIds.size,
            current = current,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 12.dp)
        )
    }
}

@Composable
private fun DotsIndicator(
    count: Int,
    current: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier            = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        repeat(count) { i ->
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (i == current) Color.White else Color(0x66FFFFFF))
            )
        }
    }
}

@Composable
fun GenreChip(text: String) {
    Box(
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x66FFFFFF))
            .padding(vertical = 6.dp, horizontal = 12.dp)
    ) {
        Text(text, color = Color.White, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun CategorySection(
    navController: NavController,
    title: String,
    items: List<Int>
) {
    val fixedUrl = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8"
    val encodedUrl = URLEncoder.encode(fixedUrl, StandardCharsets.UTF_8.toString())

    Text(title, style = MaterialTheme.typography.headlineSmall, color = Color.White)
    Spacer(Modifier.height(8.dp))
    LazyRow(
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(items) { _, resId ->
            val interaction = remember { MutableInteractionSource() }
            val focused by interaction.collectIsFocusedAsState()
            val scale by animateFloatAsState(
                targetValue = if (focused) 1.05f else 1f,
                animationSpec = tween(150)
            )
            Box(
                modifier = Modifier
                    .size(200.dp, 120.dp)
                    .graphicsLayer { scaleX = scale; scaleY = scale }
                    .clip(RoundedCornerShape(8.dp))
                    .then(if (focused) Modifier.border(2.dp, base_color, RoundedCornerShape(8.dp)) else Modifier)
                    .focusable(interactionSource = interaction)
                    .clickable { navController.navigate("${Destination.demoplayer}/$encodedUrl") }
            ) {
                Image(
                    painter           = painterResource(resId),
                    contentDescription = null,
                    contentScale      = ContentScale.Crop,
                    modifier          = Modifier.matchParentSize()
                )
            }
        }
    }
}

// Sample data
private val continueWatchingRes = listOf(
    R.drawable.movie_8, R.drawable.movie_15, R.drawable.movie_3,
    R.drawable.movie_12, R.drawable.movie_5, R.drawable.movie_6,
    R.drawable.movie_10, R.drawable.movie_28, R.drawable.movie_23,
    R.drawable.movie_13, R.drawable.movie_15, R.drawable.movie_10,
    R.drawable.movie_6, R.drawable.movie_9
)
private val topTvRes = listOf(
    R.drawable.movie_9, R.drawable.movie_18, R.drawable.movie_13,
    R.drawable.movie_12, R.drawable.movie_14,
    R.drawable.movie_10, R.drawable.movie_11, R.drawable.movie_3,
    R.drawable.movie_7, R.drawable.movie_4
)
private val trending = listOf(
    R.drawable.movie_22, R.drawable.movie_13, R.drawable.movie_12,
    R.drawable.movie_17, R.drawable.movie_10, R.drawable.movie_15,
    R.drawable.movie_23, R.drawable.movie_4,
    R.drawable.movie_8, R.drawable.movie_15, R.drawable.movie_3,
    R.drawable.movie_12, R.drawable.movie_5, R.drawable.movie_6,
)
private val latest = listOf(
    R.drawable.movie_17, R.drawable.movie_13, R.drawable.movie_12,
    R.drawable.movie_22, R.drawable.movie_25, R.drawable.movie_7,
    R.drawable.movie_1, R.drawable.movie_10,
    R.drawable.movie_10, R.drawable.movie_11, R.drawable.movie_3,
    R.drawable.movie_7, R.drawable.movie_4
)
private val action = listOf(
    R.drawable.movie_8, R.drawable.movie_9, R.drawable.movie_11,
    R.drawable.movie_13, R.drawable.movie_15, R.drawable.movie_10,
    R.drawable.movie_6, R.drawable.movie_9,
    R.drawable.movie_22, R.drawable.movie_13, R.drawable.movie_12,
    R.drawable.movie_17, R.drawable.movie_10, R.drawable.movie_15,
)
private val topMovies = listOf(
    R.drawable.movie_21, R.drawable.movie_22, R.drawable.movie_26,
    R.drawable.movie_27, R.drawable.movie_28, R.drawable.movie_30,
    R.drawable.movie_4, R.drawable.movie_10,
    R.drawable.movie_10, R.drawable.movie_11, R.drawable.movie_3,
    R.drawable.movie_7, R.drawable.movie_4
)
