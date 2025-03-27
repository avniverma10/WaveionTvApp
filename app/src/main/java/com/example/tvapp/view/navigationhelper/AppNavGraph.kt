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
import com.example.tvapp.otp.OtpScreen1
import com.example.tvapp.search.SearchScreen
import com.example.tvapp.view.epg.EPGScreen
import com.example.tvapp.view.home.HomePlayer
import com.example.tvapp.view.home.HomePlayerScreen
import com.example.tvapp.view.home.HomeScreen
import com.example.tvapp.view.login.LoginScreen1
import com.example.tvapp.view.splash.SplashScreen
import com.example.tvapp.viewmodels.SharedViewModel
import java.net.URLDecoder
import java.net.URLEncoder
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
            LoginScreen1(navController)
        }

        composable(Destination.epgScreen) {
            EPGScreen(navController,sharedViewModel)
        }
        composable(Destination.otpScreen) {
            OtpScreen1(navController)
        }
        composable(Destination.searchScreen) {
            SearchScreen(navController,sharedViewModel)
        }

        composable(
            route =  Destination.playerScreen +"/{videoUrl}?categoryIds={categoryIds}",
            arguments = listOf(
                navArgument("videoUrl") { type = NavType.StringType },
                navArgument("categoryIds") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val videoUrl = backStackEntry.arguments?.getString("videoUrl") ?: ""
            val categoryIds = backStackEntry.arguments?.getString("categoryIds")?.split(",") ?: emptyList()
            val filteredEPGList = sharedViewModel.filteredEPGList.collectAsState().value
            val categoryEPGItems = filteredEPGList.filter { categoryIds.contains(it.channelId ?: "") }

            HomePlayerScreen(
                initialVideoUrl = videoUrl,
                allChannels = categoryEPGItems,
                onBack = { navController.popBackStack() },
                onVideoChange = { newUrl ->
                    navController.navigate("homeplayer/${URLEncoder.encode(newUrl, StandardCharsets.UTF_8.toString())}?categoryIds=${categoryIds.joinToString(",")}") {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Destination.homeScreen) {
            HomeScreen(navController,sharedViewModel)
        }

    }

}