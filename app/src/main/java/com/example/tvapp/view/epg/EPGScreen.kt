
package com.example.tvapp.view.epg


import android.app.Activity
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.example.tvapp.extensions.appManifestLiveData
import com.example.tvapp.extensions.showToastS
import com.example.tvapp.model.data.banner.Banner
import com.example.tvapp.model.data.manifest.EPGCategory
import com.example.tvapp.model.data.manifest.TabInfo
import com.example.tvapp.view.navigationhelper.ExpandableNavigationMenu
import com.example.tvapp.view.navigationhelper.CategoryMenu
import com.example.tvapp.view.navigationhelper.LanguageMenu
import com.example.tvapp.viewmodels.SharedViewModel
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun EPGScreen(navController:NavController,sharedViewModel: SharedViewModel) {
    val context = LocalContext.current

    val appManifestData = sharedViewModel.provideApplicationContext().appManifestLiveData()
    var menuItems by remember { mutableStateOf<List<EPGCategory>>(appManifestData.value?.tab?.get(0)?.categories?: emptyList()) }
    val tabItems by remember { mutableStateOf<List<TabInfo>>(appManifestData.value?.tab?: emptyList()) }
    val bannerList by sharedViewModel.bannerList.collectAsState(initial = emptyList())
    val showBanner = isBannerVisible(tabItems)
    val firstChannelFocusRequester = remember { FocusRequester() }

    val genreSelectedIndex = remember { mutableStateOf(0) }
    val languageSelectedIndex = remember { mutableStateOf(0) }


    var lastBackPressedTime by remember { mutableStateOf(0L) }
    var showExitDialog by remember { mutableStateOf(false) }

    // Intercept back press on landing screen to show exit confirmation
    BackHandler {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPressedTime < 2000) {
            // Double back press detected, show exit confirmation dialog
            showExitDialog = true
        } else {
            // Update the time and prompt the user
            lastBackPressedTime = currentTime
            context.showToastS("Press back again to exit")
        }
    }

    // Exit confirmation dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit App") },
            text = { Text("Do you want to close the app?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        (context as? Activity)?.finish()
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    Row(modifier = Modifier.fillMaxSize().background(Color(0xFF14161A))) {
        // Left Navigation Menu
        ExpandableNavigationMenu(navController, sharedViewModel, onNavMenuIntent = { tabInfo, selectedTabIndex ->
            menuItems = tabInfo.categories ?: emptyList()
        })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF161D25))
                .zIndex(1f)
        ) {
            // Top Banner
            if (showBanner) {
                AdvertisementBanner(bannerList = bannerList)
            } else {
                Log.i("EPGScreen", "Advertisement banner is not displayed due to visibility settings or missing data.")
            }

            // Main content area
            Column(modifier = Modifier.fillMaxSize()){
                // Left Navigation Menu (optional duplicate, remove if ExpandableNavigationMenu is sufficient)
                 CategoryMenu(sharedViewModel,genreSelectedIndex)
                  LanguageMenu(sharedViewModel,languageSelectedIndex,firstChannelFocusRequester)
                // EPG Content
                EPGContent(sharedViewModel,firstChannelFocusRequester)
            }
        }
    }

}


fun isBannerVisible(tabsList: List<TabInfo>?): Boolean {
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






@Composable
fun AdvertisementBanner(bannerList: List<Banner>) {
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



