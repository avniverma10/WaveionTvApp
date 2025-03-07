package com.example.tvapp.screens

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import com.example.tvapp.viewmodels.SplashViewModel


@RequiresApi(Build.VERSION_CODES.M)
@Composable
fun SplashScreen(navController: NavController,viewModel: SplashViewModel = hiltViewModel()) {
    val logoUrl by viewModel.logoUrl.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    Log.d("AVNI", "SplashScreen: ERROr  is ${errorMessage}")
    val authToken by viewModel.authToken.collectAsState(initial = null)


    val context = LocalContext.current

    Log.d("AVNI", "Logo URL: $logoUrl")


    LaunchedEffect(errorMessage,authToken) {
        if (errorMessage != null) {
            // Show error message
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
            return@LaunchedEffect
        }

        delay(2000)
//        if (authToken.isNullOrEmpty()) {
//            navController.navigate("login_screen") {
//                popUpTo("splash_screen") { inclusive = true }
//            }
//        } else {
            navController.navigate("epg") { // Navigate to EPG or Player Screen
                popUpTo("splash_screen") { inclusive = true }
            }
//        }

    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
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
