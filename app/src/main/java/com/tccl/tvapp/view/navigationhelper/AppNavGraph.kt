package com.tccl.tvapp.view.navigationhelper

import ForceMessageDialog
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tccl.tvapp.view.appscreen.AppsScreen
import com.tccl.tvapp.view.notificationbanner.NotificationBanner
import com.tccl.tvapp.view.otp.OtpScreen1
import com.tccl.tvapp.view.search.SearchScreen
import com.tccl.tvapp.utils.network.error.GlobalErrorHandler
import com.tccl.tvapp.utils.uistate.PreferenceManager
import com.tccl.tvapp.view.channels.ChannelScreen
import com.tccl.tvapp.view.epg.EPGScreen
import com.tccl.tvapp.view.home.DemoHomeScreen
import com.tccl.tvapp.view.home.DemoPlayerScreen
import com.tccl.tvapp.view.home.HomePlayerScreen
import com.tccl.tvapp.view.home.HomeScreen
import com.tccl.tvapp.view.panmetro.NewPanMetroSettingsScreen
import com.tccl.tvapp.view.panmetro.genre.PanmetroGenreScreen
import com.tccl.tvapp.view.panmetro.login.PanmetroLoginScreen
import com.tccl.tvapp.view.panmetro.player.CaastvVideoPlayer
import com.tccl.tvapp.view.panmetro.player.PanMetroVideoPlayer
import com.tccl.tvapp.view.profile.ProfileScreen
import com.tccl.tvapp.view.splash.SplashScreen
import com.tccl.tvapp.view.uicomponent.fingerprint.GlobalFingerprintOverlay
import com.tccl.tvapp.view.uicomponent.fingerprint.ScrollingMessageOverlay
import com.tccl.tvapp.view.uicomponent.fingerprint.state.ForceMessageDialogState
import com.tccl.tvapp.viewmodels.SharedViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@SuppressLint("UnrememberedMutableState")
@Composable
fun WTVPlayerApp(sharedViewModel: SharedViewModel) {
    val navController = rememberNavController() // This is the one you'll use everywhere.
    val bannerMsg by sharedViewModel.bannerMessage.collectAsState()
    val globalSSERules by sharedViewModel.globalSSERules.collectAsState()
    //In your composable function or ViewModel
    var dialogStates = remember { mutableStateListOf<ForceMessageDialogState>()}

    LaunchedEffect(globalSSERules) {
        if((globalSSERules?.forceMessages?.size ?: 0) > 0){
            dialogStates.clear()
            globalSSERules?.forceMessages?.forEach { message ->
                dialogStates.add(ForceMessageDialogState(message,true))
            }
        }else{
            dialogStates = mutableStateListOf<ForceMessageDialogState>()
        }
    }

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

        //Show dialogs
        if((globalSSERules?.forceMessages?.size ?: 0) > 0){
            dialogStates.forEachIndexed { index, dialogState ->
                if(dialogStates[index].show) {
                    ForceMessageDialog(
                        showDialog = true,
                        forceMessage = dialogState.message,
                        onConfirm = {
                            // Mark this dialog as dismissed
                            dialogStates[index] = dialogState.copy(show = false)
                        }
                    )
                }
            }
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


        if((globalSSERules?.packageUpdates?.size ?: 0) > 0){
            globalSSERules?.packageUpdates?.forEach {
                if(it.packageUpdate == 1){
                    sharedViewModel.validateUserLogin(
                        userName = PreferenceManager.getUsername()?:"",
                        userPassword = PreferenceManager.getUsername()?:"",
                        onLoginResponse = { response, errorMsg ->
                            if (response != null) {
                                PreferenceManager.saveUserInfo(response)
                                sharedViewModel.provideGlobalSSERequest()
                                sharedViewModel.packageUpdate()
                            }

                        })
                }
            }
        }

        // Global error handler (will show on top when needed)
        GlobalErrorHandler()
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
            CaastvVideoPlayer(navController,sharedViewModel)
        }
        composable(Destination.animationPlayer) {backStackEntry ->
            PanMetroVideoPlayer(navController,sharedViewModel)
        }
        composable(Destination.demoHome) {
            DemoHomeScreen(
                navController   = navController,
                sharedViewModel = sharedViewModel
            )
        }
        composable(
            route = Destination.demoplayer + "/{videoUrl}",
            arguments = listOf(
                navArgument("videoUrl") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val videoUrl = backStackEntry.arguments?.getString("videoUrl") ?: ""
            DemoPlayerScreen(
                url = videoUrl,
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
        composable(Destination.appsScreen) {
            AppsScreen(navController, sharedViewModel)
        }
    }

}