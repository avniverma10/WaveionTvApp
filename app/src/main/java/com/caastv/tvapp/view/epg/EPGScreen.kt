
package com.caastv.tvapp.view.epg


import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.android.caastv.R
import com.caastv.tvapp.extensions.hideKeyboard
import com.caastv.tvapp.extensions.loge
import com.caastv.tvapp.model.data.banner.Banner
import com.caastv.tvapp.model.data.manifest.EPGCategory
import com.caastv.tvapp.model.data.manifest.TabInfo
import com.caastv.tvapp.utils.theme.screen_bg_color
import com.caastv.tvapp.view.navigationhelper.CategoryMenu
import com.caastv.tvapp.view.navigationhelper.ExpandableNavigationMenu
import com.caastv.tvapp.view.navigationhelper.LanguageMenu
import com.caastv.tvapp.view.uicomponent.error.CommonDialog
import com.caastv.tvapp.viewmodels.SharedViewModel
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun EPGScreen(navController: NavController, sharedViewModel: SharedViewModel) {


    val context = LocalContext.current


    val appManifestData = sharedViewModel.manifestData.collectAsState()
    var menuItems by remember { mutableStateOf<List<EPGCategory>>(appManifestData.value?.tab?.get(0)?.categories ?: emptyList()) }
    val tabItems by remember { mutableStateOf<List<TabInfo>>(appManifestData.value?.tab ?: emptyList()) }
    val menuFocusRequester = remember { FocusRequester() }
    val tabItemsData by sharedViewModel.tabItemsFlow.collectAsState()
    val bannerList by sharedViewModel.bannerList.collectAsState(initial = emptyList())
    val firstChannelFocusRequester = remember { FocusRequester() }



    LaunchedEffect(Unit) {
        context.hideKeyboard()
        // Move focus to the first channel in your EPG content
        try {
            firstChannelFocusRequester.requestFocus()
        } catch (e: IllegalStateException) {
            loge("FocusError", "FocusRequester not initialized ${e.message}")
        }

        //request for user hash
        sharedViewModel?.provideUserHash()

        //register scroll message request
        sharedViewModel.provideGlobalSSERequest()
    }
    val categories = appManifestData.value?.genre?: arrayListOf()
    val languages = appManifestData.value?.language?: arrayListOf()
    val filterState by sharedViewModel.filterState.collectAsState()

    if (categories.isEmpty() || languages.isEmpty()) {
        return
    }

    // A FocusRequester per item
    val categoryFocusRequesters = remember(categories) {
        List(categories.size) { FocusRequester() }
    }
    val languageFocusRequesters = remember(languages) {
        List(languages.size) { FocusRequester() }
    }


    val categorySelectedIndex = remember { mutableStateOf(0) }
    val languageSelectedIndex = remember { mutableStateOf(0) }

    LaunchedEffect(filterState, categories, languages) {
        categorySelectedIndex.value = categories.indexOfFirst { it.name == filterState.genre }
            .takeIf { it >= 0 } ?: 0
        languageSelectedIndex.value = languages.indexOfFirst { it.name == filterState.language }
            .takeIf { it >= 0 } ?: 0
    }



    val genreSelectedIndex = remember { mutableStateOf(0) }

    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler {
        showExitDialog = true
    }


    // Exit confirmation dialog
    if (showExitDialog) {
        CommonDialog(
            showDialog = true,
            title = "Exit App",
            message = "Are you sure you want to exit the app?",
            borderColor = Color.Transparent,
            painter = painterResource(id = R.drawable.exit_icon),
            errorCode = null,
            errorMessage = null,
            confirmButtonText = "Yes",
            onConfirm = {
                (context as? Activity)?.finishAffinity()
//                Process.killProcess(Process.myPid())
            },
            dismissButtonText = "No",
            onDismiss = {
                showExitDialog = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screen_bg_color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF161D25))
                .padding(start = 70.dp)
                .zIndex(1f)
        ) {
            /*if (appManifestData.value?.tab?.find { it.name =="epg" }?.components?.get(0)?.isVisible == true || (tabItemsData.find{it.name == "home"}?.components?.get(0)?.isVisible == true)) {
                AdvertisementBanner(bannerList = bannerList)
            } else {
                logReport("EPGScreen", "Advertisement banner is not displayed due to visibility settings or missing data.")
            }*/

            Column(modifier = Modifier.fillMaxSize()) {
                CategoryMenu(
                    sharedViewModel = sharedViewModel,
                    selectedIndex = categorySelectedIndex,
                    categoryFocusRequesters = categoryFocusRequesters,
                    languageFocusRequesters = languageFocusRequesters,
                    languageSelectedIndex = languageSelectedIndex
                )

                LanguageMenu(
                    sharedViewModel = sharedViewModel,
                    selectedIndex = languageSelectedIndex,
                    firstChannelFocusRequester = firstChannelFocusRequester,
                    languageFocusRequesters = languageFocusRequesters,
                    categoryFocusRequesters = categoryFocusRequesters,
                    categorySelectedIndex = categorySelectedIndex
                )

                EPGContent(
                    navController,
                    sharedViewModel,
                    firstChannelFocusRequester,
                    languageFocusRequesters,
                    languageSelectedIndex,
                    categoryFocusRequesters,
                    genreSelectedIndex
                )
            }
        }
        ExpandableNavigationMenu(
            navController   = navController,
            sharedViewModel = sharedViewModel,
            menuFocusRequester = menuFocusRequester,
            onNavMenuIntent = { tabInfo, _ ->
                menuItems = tabInfo.categories ?: emptyList()
            },
            modifier = Modifier.align(Alignment.CenterStart),
            onBackPressed = {
                menuFocusRequester.requestFocus()
            }
        )
    }
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
                AsyncImage(
                    model = bannerList[page].bannerUrl,
                    contentDescription = "Advertisement",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

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



