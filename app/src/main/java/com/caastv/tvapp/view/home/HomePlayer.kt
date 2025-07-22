package com.caastv.tvapp.view.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.caastv.tvapp.viewmodels.SharedViewModel
import com.caastv.tvapp.viewmodels.WTVPlayerViewModel

@Composable
fun HomePlayer(
    navController: NavController,
    videoUrl: String,
    sharedViewModel: SharedViewModel = hiltViewModel(),
    wtvPlayerViewModel: WTVPlayerViewModel = hiltViewModel()
) {
    val epgDataItems by sharedViewModel.filteredEPGList.collectAsState()

    if (epgDataItems.isNotEmpty()) {
        HomePlayerScreen(
            initialVideoUrl = videoUrl,
            allChannels = epgDataItems,
            onBack = { navController.popBackStack() },
            onVideoChange = { newUrl ->
                navController.navigate("homeplayer/${newUrl}") {
                    launchSingleTop = true
                }
            },
            wtvPlayerViewModel = wtvPlayerViewModel
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    }
}
