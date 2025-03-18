package com.example.tvapp.view.splash

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.example.tvapp.R
import com.example.tvapp.extensions.appManifestLiveData
import com.example.tvapp.extensions.showToastS
import com.example.tvapp.view.navigationhelper.Destination
import com.example.tvapp.viewmodels.SharedViewModel


@Composable
fun SplashScreen(sharedViewModel: SharedViewModel, navController: NavController) {
    val appManifestData = sharedViewModel.provideApplicationContext().appManifestLiveData().value
    val errorLoadingData by sharedViewModel.errorLoadingData.collectAsState()
    val authToken by sharedViewModel.authToken.collectAsState(initial = null)

    val context = LocalContext.current

    LaunchedEffect(errorLoadingData,authToken) {
        if (errorLoadingData != null) {
            context.showToastS(errorLoadingData)
            return@LaunchedEffect
        }
        Handler(Looper.getMainLooper()).postDelayed({
            if (authToken.isNullOrEmpty()) {
                navController.navigate(Destination.loginScreen) {
                    popUpTo(Destination.splashScreen) { inclusive = true }
                }
            } else {
                navController.navigate(Destination.epgScreen) { // Navigate to EPG or Player Screen
                    popUpTo(Destination.splashScreen) { inclusive = true }
                }
            }
        },3000)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                //.data(appManifestData?.splashUrl?:"https://waveiontechnologies.com/wp-content/uploads/2021/01/logo-header2.png")
                .diskCachePolicy(CachePolicy.ENABLED)    // cache image on disk
                .memoryCachePolicy(CachePolicy.ENABLED)  // cache image in memory
                .build(),
            contentDescription = "Default Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
            error = painterResource(R.drawable.splash_background),        // Error state
            placeholder = painterResource(R.drawable.splash_background)   // Loading state
        )
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(appManifestData?.logo?:"https://waveiontechnologies.com/wp-content/uploads/2021/01/logo-header2.png")
                .diskCachePolicy(CachePolicy.ENABLED)    // cache image on disk
                .memoryCachePolicy(CachePolicy.ENABLED)  // cache image in memory
                .build(),
            contentDescription = "Fallback Logo",
            modifier = Modifier
                .align(Alignment.Center)
                .size(250.dp)
        )
    }
}
