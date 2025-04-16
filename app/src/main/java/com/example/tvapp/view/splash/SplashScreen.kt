package com.example.tvapp.view.splash

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.example.tvapp.R
import com.example.tvapp.extensions.appManifestLiveData
import com.example.tvapp.extensions.showToastS
import com.example.tvapp.utils.Constants
import com.example.tvapp.view.navigationhelper.Destination
import com.example.tvapp.viewmodels.LoginViewModel
import com.example.tvapp.viewmodels.SharedViewModel
import kotlinx.coroutines.delay


@Composable
fun SplashScreen(sharedViewModel: SharedViewModel, navController: NavController,) {
    val loginInfo = sharedViewModel.loginInfo?.collectAsState()?.value
    val errorLoadingData by sharedViewModel.errorLoadingData.collectAsState()
    val isInitializeData by sharedViewModel.isInitializeData.collectAsState()
    val context = LocalContext.current


    Log.d("AVNI", "loginInfo in splash: $loginInfo")

    LaunchedEffect(loginInfo, errorLoadingData, isInitializeData) {
        // Wait until initialization is complete so we know we have the correct login state.
        if (!isInitializeData) return@LaunchedEffect

        errorLoadingData?.let {
            context.showToastS(it)
            return@LaunchedEffect
        }

        // Check that valid credentials exist and that the user chose "Remember me"
        if (
            loginInfo!!.username.isNotEmpty()) {
            navController.navigate(Destination.genreScreen) {
                popUpTo(Destination.splashScreen) { inclusive = true }
            }
        } else {
            navController.navigate(Destination.loginScreen) {
                popUpTo(Destination.splashScreen) { inclusive = true }
            }
        }
    }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                //.data(appManifestData?.splashUrl?:"https://waveiontechnologies.com/wp-content/uploads/2021/01/logo-header2.png")
                .diskCachePolicy(CachePolicy.ENABLED)    // cache image on disk
                .memoryCachePolicy(CachePolicy.ENABLED)  // cache image in memory
                .build(),
            contentDescription = "Default Background",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.align(Alignment.Center),
            error = painterResource(R.drawable.gtpl_logo),        // Error state
            placeholder = painterResource(R.drawable.gtpl_logo)   // Loading state
        )
       /* AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(appManifestData?.logo?:"https://waveiontechnologies.com/wp-content/uploads/2021/01/logo-header2.png")
                .diskCachePolicy(CachePolicy.ENABLED)    // cache image on disk
                .memoryCachePolicy(CachePolicy.ENABLED)  // cache image in memory
                .build(),
            contentDescription = "Fallback Logo",
            modifier = Modifier
                .align(Alignment.Center)
                .size(250.dp)
        )*/
    }
}
