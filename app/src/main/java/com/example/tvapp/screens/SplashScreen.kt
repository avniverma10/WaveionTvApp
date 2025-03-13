package com.example.tvapp.screens
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.tvapp.R
import com.example.tvapp.viewmodels.SplashViewModel
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.M)
@Composable
fun SplashScreen(navController: NavController, viewModel: SplashViewModel = hiltViewModel()) {

    val logoUrl by viewModel.logoUrl.collectAsState()
//    val backgroundUrl by viewModel.backgroundUrl.collectAsState() // Background image from API
    val errorMessage by viewModel.errorMessage.collectAsState()
    val authToken by viewModel.authToken.collectAsState(initial = null)
    val isDataLoaded by viewModel.isDataLoaded.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(isDataLoaded) {
        if (isDataLoaded) {
            navController.navigate("epg")
        }
    }

    LaunchedEffect(errorMessage, authToken) {
        if (errorMessage != null) {
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
            return@LaunchedEffect
        }

        delay(4000) // Optional delay for splash screen duration
        if (isDataLoaded) {
            val destination = if (authToken.isNullOrEmpty()) "home_screen" else "home_screen"
            navController.navigate(destination) {
                popUpTo("splash_screen") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Image(
            painter = painterResource(id = R.drawable.splash_background), // Fallback image
            contentDescription = "Default Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
////        // **Check if Background URL is Available**
//        if (backgroundUrl.isNullOrEmpty()) {
//            // **Load Hardcoded Drawable as Background**
//            Image(
//                painter = painterResource(id = R.drawable.splash_background), // Fallback image
//                contentDescription = "Default Background",
//                modifier = Modifier.fillMaxSize(),
//                contentScale = ContentScale.Crop
//            )
//        } else {
//            // **Load Background from API**
//            AsyncImage(
//                model = backgroundUrl,
//                contentDescription = "API Background Image",
//                modifier = Modifier.fillMaxSize()
//            )
//        }

        // **Center Logo**
        AsyncImage(
            model = logoUrl ?: "https://waveiontechnologies.com/wp-content/uploads/2021/01/logo-header2.png",
            contentDescription = "Logo",
            modifier = Modifier
                .align(Alignment.Center)
                .size(250.dp)
        )
    }
}
