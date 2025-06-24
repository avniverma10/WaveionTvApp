package com.example.tvapp.view.navigationhelper

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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tvapp.NotificationBanner
import com.example.tvapp.view.epg.EPGScreen
import com.example.tvapp.view.panmetro.settings.SettingsScreen
import com.example.tvapp.view.panmetro.genre.PanmetroGenreScreen
import com.example.tvapp.view.panmetro.login.PanmetroLoginScreen
import com.example.tvapp.view.panmetro.player.PanMetroVideoPlayer
import com.example.tvapp.view.profile.ProfileScreen
import com.example.tvapp.view.splash.SplashScreen
import com.example.tvapp.view.uicomponent.fingerprint.GlobalFingerprintOverlay
import com.example.tvapp.view.uicomponent.fingerprint.ScrollingMessageOverlay
import com.example.tvapp.view.uicomponent.fingerprint.state.ForceMessageDialogState
import com.example.tvapp.viewmodels.SharedViewModel

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
            SettingsScreen(navController,sharedViewModel)
        }
        composable(Destination.panMetroScreen) { backStackEntry ->
            //val channelId = backStackEntry.arguments?.getString("channelId")
            PanMetroVideoPlayer(navController, sharedViewModel)
        }
    }

}