package com.example.tvapp.view.navigationhelper

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tvapp.NotificationBanner
import com.example.tvapp.otp.OtpScreen1
import com.example.tvapp.search.SearchScreen
import com.example.tvapp.view.channels.ChannelScreen
import com.example.tvapp.view.epg.EPGScreen
import com.example.tvapp.view.home.DemoHomeScreen
import com.example.tvapp.view.home.DemoPlayerScreen
import com.example.tvapp.view.home.HomePlayerScreen
import com.example.tvapp.view.home.HomeScreen
import com.example.tvapp.view.panmetro.NewPanMetroSettingsScreen
import com.example.tvapp.view.panmetro.genre.PanmetroGenreScreen
import com.example.tvapp.view.panmetro.login.PanmetroLoginScreen
import com.example.tvapp.view.panmetro.player.PanMetroVideoPlayer
import com.example.tvapp.view.profile.Profile
import com.example.tvapp.view.profile.ProfileScreen
import com.example.tvapp.view.splash.SplashScreen
import com.example.tvapp.view.uicomponent.fingerprint.GlobalFingerprintOverlay
import com.example.tvapp.view.uicomponent.fingerprint.ScrollingMessageOverlay
import com.example.tvapp.viewmodels.SharedViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@SuppressLint("UnrememberedMutableState")
@Composable
fun WTVPlayerApp(sharedViewModel: SharedViewModel) {
    val navController = rememberNavController() // This is the one you'll use everywhere.
    val bannerMsg by sharedViewModel.bannerMessage.collectAsState()
    val globalSSERules by sharedViewModel.globalSSERules.collectAsState()
    val scrollMessageItems by sharedViewModel.scrollMessageItemsFlow.collectAsState()

    Box(Modifier.fillMaxSize()) {
        WTVPlayerNavHost(
            navController = navController,
            sharedViewModel = sharedViewModel
        )
        bannerMsg?.let { msg ->
            NotificationBanner(
                message = msg,
                visible = true,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                // you can add paddingIfNeeded here
            )
        }

        if((globalSSERules?.fingerprints?.size ?: 0) > 0){
            globalSSERules?.fingerprints?.forEach {
                GlobalFingerprintOverlay(mutableStateOf(it))
            }
        }

        if((globalSSERules?.scrollMessages?.size ?: 0) > 0){
            globalSSERules?.scrollMessages?.forEach {
                ScrollingMessageOverlay( scrollMessageInfo = mutableStateOf(it))
            }
        }
    }
}

@SuppressLint("ContextCastToActivity")
@Composable
fun WTVPlayerNavHost(navController: NavHostController, sharedViewModel: SharedViewModel) {
    NavHost(navController = navController, startDestination = Destination.splashScreen) {
        composable(route = Destination.splashScreen) {
            SplashScreen(sharedViewModel = sharedViewModel, navController)
        }
        composable(Destination.loginScreen) {
            PanmetroLoginScreen(sharedViewModel = sharedViewModel,navController = navController)
        }
        composable(Destination.epgScreen) {
            EPGScreen(navController, sharedViewModel)
        }
        composable(Destination.profile) {
            ProfileScreen(navController, sharedViewModel)
        }
        composable(Destination.genreScreen) {
            PanmetroGenreScreen(navController,sharedViewModel)
        }
        composable(Destination.settings) {
            NewPanMetroSettingsScreen(navController,sharedViewModel)
        }
        composable(Destination.panMetroScreen) {backStackEntry ->
            //val channelId = backStackEntry.arguments?.getString("channelId")
            PanMetroVideoPlayer(navController,sharedViewModel)
        }
        composable(Destination.demoHome) {
            DemoHomeScreen(
                navController   = navController,
                sharedViewModel = sharedViewModel
            )
        }
        composable(Destination.demoplayer) {
            DemoPlayerScreen(
                url    = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
                onBack = { navController.popBackStack() }
            )
        }

        /*
        composable(
             route = Destination.panMetroScreen+"{fromEpg}",
             arguments = listOf(
                 navArgument("fromEpg") {
                     type = NavType.StringType
                     defaultValue = "false"
                 }
             )
         ) { backStackEntry ->
             val fromEpg = backStackEntry.arguments?.getString("fromEpg")?.toBoolean() ?: false
             PanMetroVideoPlayer(fromEpg,navController,sharedViewModel)

         }
         */
        composable(Destination.otpScreen) {
            OtpScreen1(navController)
        }
        composable(Destination.searchScreen) {
            SearchScreen(navController, sharedViewModel)
        }
        // This route is reused across your app.
        // It expects a videoUrl (path parameter) and a categoryIds (query parameter).
        composable(
            route = Destination.playerScreen + "/{videoUrl}?categoryIds={categoryIds}",
            arguments = listOf(
                navArgument("videoUrl") { type = NavType.StringType },
                navArgument("categoryIds") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val videoUrl = backStackEntry.arguments?.getString("videoUrl") ?: ""
            val categoryIds = backStackEntry.arguments?.getString("categoryIds") ?: ""

            val filteredEPGList = sharedViewModel.filteredEPGList.collectAsState().value

            // ✅ FIX GOES HERE
            val categoryEPGItems = if (categoryIds.isBlank()) {
                filteredEPGList // Show all channels if no category filter applied
            } else {
                filteredEPGList.filter { categoryIds.contains(it.channelId ?: "") }
            }

            HomePlayerScreen(
                initialVideoUrl = videoUrl,
                allChannels = categoryEPGItems,
                onBack = { navController.popBackStack() },
                onVideoChange = { newUrl ->
                    val newEncodedUrl = URLEncoder.encode(newUrl, StandardCharsets.UTF_8.toString())
                    navController.navigate("homeplayer/$newEncodedUrl?categoryIds=$categoryIds") {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Destination.homeScreen) {
            HomeScreen(navController, sharedViewModel)
        }
        composable(Destination.channel) {
            ChannelScreen(navController = navController, sharedViewModel = sharedViewModel)
        }
    }

}