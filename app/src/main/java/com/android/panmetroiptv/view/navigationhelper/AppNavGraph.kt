package com.android.panmetroiptv.view.navigationhelper

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
import com.android.panmetroiptv.NotificationBanner
import com.android.panmetroiptv.utils.uistate.PreferenceManager
import com.android.panmetroiptv.view.epg.EPGScreen
import com.android.panmetroiptv.view.panmetro.genre.PanmetroGenreScreen
import com.android.panmetroiptv.view.panmetro.login.PanmetroLoginScreen
import com.android.panmetroiptv.view.panmetro.player.CaastvVideoPlayer
import com.android.panmetroiptv.view.panmetro.player.PanMetroVideoPlayer
import com.android.panmetroiptv.view.panmetro.settings.SettingsScreen
import com.android.panmetroiptv.view.profile.ProfileScreen
import com.android.panmetroiptv.view.splash.SplashScreen
import com.android.panmetroiptv.view.uicomponent.fingerprint.GlobalFingerprintOverlay
import com.android.panmetroiptv.view.uicomponent.fingerprint.ScrollingMessageOverlay
import com.android.panmetroiptv.view.uicomponent.fingerprint.state.ForceMessageDialogState
import com.android.panmetroiptv.viewmodels.SharedViewModel

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
                        uName = PreferenceManager.getUsername()?:"",
                        paswrd = PreferenceManager.getUsername()?:"",
                        macId = sharedViewModel.deviceMacAddr.value,
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
            PanmetroLoginScreen(sharedViewModel= sharedViewModel,navController = navController)
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
           // PanMetroVideoPlayer(navController,sharedViewModel)
           CaastvVideoPlayer(navController,sharedViewModel)
        }
    }

}