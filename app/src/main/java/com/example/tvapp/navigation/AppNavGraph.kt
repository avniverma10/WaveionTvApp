package com.example.tvapp.navigation

import LoginScreen
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tvapp.screens.EPGScreen
import com.example.tvapp.screens.SplashScreen
import com.example.tvapp.viewmodels.SplashViewModel


@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash_screen") {
        composable(route = "splash_screen") {
            val splashViewModel: SplashViewModel = hiltViewModel() // Get the ViewModel using Hilt
            SplashScreen(navController, splashViewModel)
        }
        composable("login_screen") {
            LoginScreen(navController)
        }
//        composable("player") {
//            ExoPlayerScreen()
//        }
        composable("epg") {
            EPGScreen()
        }
    }
}