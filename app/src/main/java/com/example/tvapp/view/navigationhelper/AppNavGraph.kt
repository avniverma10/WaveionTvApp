package com.example.tvapp.view.navigationhelper

import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tvapp.view.epg.EPGScreen
import com.example.tvapp.view.home.HomePlayer
import com.example.tvapp.view.home.HomeScreen
import com.example.tvapp.view.login.LoginScreen
import com.example.tvapp.view.splash.SplashScreen
import com.example.tvapp.viewmodels.SharedViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WTVPlayerApp(sharedViewModel: SharedViewModel) {
    val navController = rememberNavController()
    WTVPlayerNavHost(
        navController = navController,
        sharedViewModel = sharedViewModel
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("ContextCastToActivity")
@Composable
fun WTVPlayerNavHost(navController: NavHostController, sharedViewModel: SharedViewModel) {
    val activity = (LocalContext.current as ComponentActivity)
    val authToken by sharedViewModel.authToken.collectAsState(initial = null)
    NavHost(navController = navController, startDestination = Destination.splashScreen) {
        composable(route = Destination.splashScreen) {
            SplashScreen(sharedViewModel = sharedViewModel,navController)
        }
        composable(Destination.loginScreen) {
            LoginScreen(navController)
        }

        composable(Destination.epgScreen) {
            EPGScreen(navController,sharedViewModel)
        }
        composable(Destination.searchScreen) {
           // SearchScreen(navController)
        }
        composable(
            route = Destination.playerScreen +"/{videoUrl}",
            arguments = listOf(navArgument("videoUrl") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("videoUrl") ?: ""
            val videoUrl = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())
            HomePlayer(navController, videoUrl)
        }

        composable(Destination.homeScreen) {
            HomeScreen(navController,sharedViewModel)
        }
    }

}