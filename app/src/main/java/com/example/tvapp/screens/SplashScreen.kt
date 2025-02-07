package com.example.tvapp.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import coil3.compose.AsyncImage
import com.example.tvapp.viewmodels.SplashViewModel


@Composable
fun SplashScreen(navController: NavController,viewModel: SplashViewModel = hiltViewModel()) {
    val logoUrl by viewModel.logoUrl.collectAsState()

    Log.d("AVNI", "Logo URL: $logoUrl")

    // Delay before navigating to Home Screen
    LaunchedEffect(Unit) {
        delay(2000) // Show splash for 2 seconds

        navController.navigate("login_screen") {
            popUpTo("splash_screen") { inclusive = true } // Remove splash from backstack
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)  // Default background while loading
    ) {
        // Logo
        if (logoUrl!= null) {
            AsyncImage(
                model = logoUrl?:"https://waveiontechnologies.com/wp-content/uploads/2021/01/logo-header2.png",
                contentDescription = "Logo",
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(250.dp)
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        }
    }
}
