package com.example.tvapp.navigation

import LoginScreen
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tvapp.screens.ExoPlayerScreen
import com.example.tvapp.screens.SplashScreen
import com.example.tvapp.screens.VideoListScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "splash_screen") {
        composable(route = "splash_screen") {
            SplashScreen(navController)
        }
        composable("login_screen") {
            LoginScreen(navController)
        }
        composable("videoList") {
            VideoListScreen(navController)
        }
        composable("player/{index}") { backStackEntry ->
            val index = backStackEntry.arguments?.getString("index")?.toInt() ?: 0
            ExoPlayerScreen(navController, index)
        }
//        composable("signup") {
//            SignUpScreen(navController)
//        }
    }
}