package com.panmetro.iptv.view.navigationhelper

import ForceMessageDialog
import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.panmetro.iptv.NotificationBanner
import com.panmetro.iptv.extensions.hideKeyboard
import com.panmetro.iptv.model.data.login.LoginInfo
import com.panmetro.iptv.model.data.sseresponse.Fingerprint
import com.panmetro.iptv.model.data.sseresponse.ScrollMessage
import com.panmetro.iptv.utils.network.scheduler.ServerGSSECheckWorker
import com.panmetro.iptv.utils.uistate.PreferenceManager
import com.panmetro.iptv.view.epg.EPGScreen
import com.panmetro.iptv.view.panmetro.genre.PanmetroGenreScreen
import com.panmetro.iptv.view.panmetro.login.PanmetroLoginScreen
import com.panmetro.iptv.view.panmetro.player.CaastvVideoPlayer
import com.panmetro.iptv.view.panmetro.settings.SettingsScreen
import com.panmetro.iptv.view.profile.ProfileScreen
import com.panmetro.iptv.view.splash.SplashScreen
import com.panmetro.iptv.view.uicomponent.fingerprint.GlobalFingerprintOverlay
import com.panmetro.iptv.view.uicomponent.fingerprint.ScrollingMessageOverlay
import com.panmetro.iptv.view.uicomponent.fingerprint.state.ForceMessageDialogState
import com.panmetro.iptv.viewmodels.SharedViewModel

@SuppressLint("UnrememberedMutableState")
@Composable
fun WTVPlayerApp(sharedViewModel: SharedViewModel) {
    val refreshFilter by sharedViewModel.refreshFilter.collectAsState()
    val navController = rememberNavController() // This is the one you'll use everywhere.
    val bannerMsg by sharedViewModel.bannerMessage.collectAsState()
    val globalSSERules by sharedViewModel.globalSSERules.collectAsState()
    val globalSSERequestEvent by sharedViewModel.isGlobalSSEClosed.collectAsState()

    //In your composable function or ViewModel
    var userInfo = remember { mutableStateOf<LoginInfo?>(null)}
    var visibleForce = remember { mutableStateListOf<ForceMessageDialogState>()}
    val visibleMessages = remember { mutableStateListOf<ScrollMessage>() }
    val visibleFingerprint = remember { mutableStateListOf<Fingerprint>() }
    val context = LocalContext.current

    // Observer effect - triggers when dependencies change
    LaunchedEffect(refreshFilter) {
        if(refreshFilter) {
            sharedViewModel.applyFilters()
            sharedViewModel.filterPanMetroChannelsByGenre()

        }
    }


    LaunchedEffect(globalSSERequestEvent) {
        if (globalSSERequestEvent == true) {
            // Server went offline - start sse checks
            ServerGSSECheckWorker.schedule(context,sharedViewModel)
        } else {
            // Server is back online - cancel sse checks
            ServerGSSECheckWorker.cancel(context)
        }
    }


    LaunchedEffect(globalSSERules) {
        userInfo.value = PreferenceManager.getLoginResponse()
        visibleFingerprint.clear()
        visibleMessages.clear()
        visibleForce.clear()
        globalSSERules?.forceMessages?.forEach { item ->
            val messageId = item._id ?: return@forEach
            val storedTimestamp = PreferenceManager.getForceUpdatedAt(messageId)
            val currentTimestamp = item.updatedAt
            val shouldShow = when {
                currentTimestamp == null -> true
                storedTimestamp == null -> true
                currentTimestamp != storedTimestamp -> true
                else -> false
            }

            if (shouldShow) {
                visibleForce.add(ForceMessageDialogState(item,true))
            }
        }

        //handle it for scroll message
        globalSSERules?.scrollMessages?.forEach { item ->
            val messageId = item._id ?: return@forEach
            val storedTimestamp = PreferenceManager.getScrollUpdatedAt(messageId)
            val currentTimestamp = item.updatedAt
            val shouldShow = when {
                currentTimestamp == null -> true
                storedTimestamp == null -> true
                currentTimestamp != storedTimestamp -> true
                else -> false
            }

            if (shouldShow) {
                 visibleMessages.add(item)
            }
        }

        //handle it for fingerprint
        globalSSERules?.fingerprints?.forEach { item ->
            val messageId = item._id ?: return@forEach
            val storedTimestamp = PreferenceManager.getFingerUpdatedAt(messageId)
            val currentTimestamp = item.updatedAt
            val shouldShow = when {
                currentTimestamp == null -> true
                storedTimestamp == null -> true
                currentTimestamp != storedTimestamp -> true
                else -> false
            }

            if (shouldShow) {
                visibleFingerprint.add(item)
            }
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
        visibleForce.forEachIndexed { index, dialogState ->
            if(visibleForce[index].show) {
                if (visibleForce[index].message.forcePush == true){
                    ForceMessageDialog(
                        showDialog = true,
                        forceMessage = dialogState.message,
                        onConfirm = {
                        }
                    )
                }else{
                    if (visibleForce[index].message.updatedAt?.equals(PreferenceManager.getForceUpdatedAt(visibleForce[index].message._id?:""), true) != true){
                        ForceMessageDialog(
                            showDialog = true,
                            forceMessage = dialogState.message,
                            onConfirm = {

                                // Remove this message from the visible list
                                visibleMessages.removeIf { it.updatedAt == visibleForce[index].message.updatedAt }
                                // Mark this dialog as dismissed
                                visibleForce[index] = dialogState.copy(show = false)
                                visibleForce[index].message?.let {
                                    PreferenceManager.saveForceUpdatedAt((it._id?:""),(it.updatedAt?:""))
                                }
                            }
                        )
                    }
                }
            }
        }

        // Display only visible fingerprint
        visibleFingerprint.forEach { fingerprint ->
            key(fingerprint._id) { // Important for proper recomposition
                GlobalFingerprintOverlay(fingerprint,
                    onFinish = { updatedAt ->
                        // Remove this message from the visible list
                        visibleMessages.removeIf { it.updatedAt == updatedAt }

                        // Also save to preferences
                        fingerprint._id?.let { id ->
                            PreferenceManager.saveFingerUpdatedAt(id, updatedAt)
                        }
                    })
            }
        }
        // Display only visible messages
        visibleMessages.forEach { message ->
            key(message._id) { // Important for proper recomposition
                ScrollingMessageOverlay(
                    scrollMessageInfo = message,
                    onFinish = { updatedAt ->
                        // Remove this message from the visible list
                        visibleMessages.removeIf { it.updatedAt == updatedAt }

                        // Also save to preferences
                        message._id?.let { id ->
                            PreferenceManager.saveScrollUpdatedAt(id, updatedAt)
                        }
                    }
                )
            }
        }


        globalSSERules?.packageUpdates?.distinct()?.forEach {
            if(it.packageUpdate == 1){
                it.packageID?.let {pkgId->
                    sharedViewModel.customerChannelUpdates(arrayListOf(pkgId))
                }
            }
        }


        globalSSERules?.userUpdates?.forEach {
            if(PreferenceManager.getUsername()?.equals(it.username,true) == true){
                userInfo.value?.customerNumber?.let {
                    sharedViewModel.userPackageUpdate(customerNumber = it, isPkgUpdateOnly = true)
                    sharedViewModel.provideGlobalSSERequest()
                    // sharedViewModel.packageUpdate()
                }
            }
        }

        globalSSERules?.blockUser?.forEach {
            if(PreferenceManager.getUsername()?.equals(it.username) == true && it.isBlocked == 1){
                PreferenceManager.clearLogin()
                PreferenceManager.clearSaveGenre()
                PreferenceManager.clearSaveChannel()
                context.hideKeyboard()
                (context as? Activity)?.finishAffinity()
                /*BlockUserScreen(
                    showDialog = true,
                    message = "Temporarily blocked. Please contact your provider to continue.",
                    confirmButtonText = "Exit",
                    onConfirm = {
                        (context as? Activity)?.finishAffinity()
                        android.os.Process.killProcess(android.os.Process.myPid())
                    },
                    dismissButtonText = null,
                    onDismiss = {})*/
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
          //  GenreScreenView(navController,sharedViewModel)
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