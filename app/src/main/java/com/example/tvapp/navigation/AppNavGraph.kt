package com.example.tvapp.navigation

import LoginScreen
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tvapp.screens.EPGScreen
import com.example.tvapp.screens.HomePlayer
import com.example.tvapp.screens.SearchScreen
import com.example.tvapp.screens.SplashScreen
import com.example.tvapp.ui.screens.HomeScreen
import com.example.tvapp.viewmodels.SplashViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets


@RequiresApi(Build.VERSION_CODES.M)
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

        composable("epg") {
            EPGScreen(navController)
        }
        composable("search_screen") {
            SearchScreen(navController)
        }

        composable(
            route = "homeplayer/{videoUrl}",
            arguments = listOf(navArgument("videoUrl") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("videoUrl") ?: ""
            val videoUrl = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())
            HomePlayer(navController, videoUrl)
        }

        composable("home_screen") {
            HomeScreen(navController)
        }
    }

}