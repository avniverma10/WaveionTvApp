package com.example.tvapp.screens

import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.platform.LocalContext
import coil3.Uri
import coil3.compose.AsyncImage
import com.example.tvapp.viewmodels.SplashViewModel


@RequiresApi(Build.VERSION_CODES.M)
@Composable
fun SplashScreen(navController: NavController,viewModel: SplashViewModel = hiltViewModel()) {
    val logoUrl by viewModel.logoUrl.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    Log.d("AVNI", "SplashScreen: ERROr  is ${errorMessage}")
    val updateUrl by viewModel.updateUrl.collectAsState()

    val context = LocalContext.current

    Log.d("AVNI", "Logo URL: $logoUrl")


    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            // Show error message
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
            return@LaunchedEffect
        }

//        if (updateUrl != null) {
//            // Navigate to update screen or open update link
//            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl))
//            startActivity(intent)
//            return@LaunchedEffect
//        }

        delay(2000)
        navController.navigate("login_screen") {
            popUpTo("splash_screen") { inclusive = true }
        }
    }

    // Delay before navigating to Home Screen
//    LaunchedEffect(Unit) {
//        delay(2000) // Show splash for 2 seconds
//
//        navController.navigate("login_screen") {
//            popUpTo("splash_screen") { inclusive = true } // Remove splash from backstack
//        }
//    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)  // Default background while loading
    ) {
        // Logo
        if (logoUrl!= null) {
            AsyncImage(
                model = logoUrl,
                contentDescription = "Logo",
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(250.dp)
            )
        } else {
                AsyncImage(
                    model = "https://waveiontechnologies.com/wp-content/uploads/2021/01/logo-header2.png",
                    contentDescription = "Fallback Logo",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(250.dp)
                )
        }
    }
}
