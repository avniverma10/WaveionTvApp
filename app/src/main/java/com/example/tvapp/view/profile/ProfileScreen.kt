
package com.example.tvapp.view.profile


import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.android.caastv.R
import com.example.tvapp.extensions.appManifestLiveData
import com.example.tvapp.extensions.capitalizeFirstLetter
import com.example.tvapp.extensions.hideKeyboard
import com.example.tvapp.extensions.loge
import com.example.tvapp.model.data.manifest.EPGCategory
import com.example.tvapp.utils.theme.screen_bg_color
import com.example.tvapp.utils.uistate.PreferenceManager
import com.example.tvapp.view.navigationhelper.ExpandableNavigationMenu
import com.example.tvapp.view.uicomponent.error.CommonDialog
import com.example.tvapp.view.uicomponent.keyboard.HideKeyboardOnEnter
import com.example.tvapp.viewmodels.SharedViewModel


@Composable
fun ProfileScreen(navController: NavController, sharedViewModel: SharedViewModel) {

    HideKeyboardOnEnter()
    val context = LocalContext.current
    var showExitDialog by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val appManifestData = sharedViewModel.provideApplicationContext().appManifestLiveData()
    var menuItems by remember { mutableStateOf<List<EPGCategory>>(appManifestData.value?.tab?.get(0)?.categories ?: emptyList()) }
    var backPressCount by remember { mutableStateOf(0) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        // Move focus to the first channel in your EPG content
        try {
            focusRequester.requestFocus()
        } catch (e: IllegalStateException) {
            loge("FocusError", "FocusRequester not initialized ${e.message}")
        }
    }


    BackHandler {
        backPressCount++

        if (backPressCount >= 2) {
            // Show exit confirmation if pressed back twice
            showExitDialog = true
        } else {
            // First back: just clear focus and move left as before
            focusManager.clearFocus(force = true)
            focusManager.moveFocus(FocusDirection.Left)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screen_bg_color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF161D25))
                .focusRequester(focusRequester)
                .padding(start = 70.dp)
                .zIndex(1f)
        ) {
            AccountScreen(
                focusRequester= focusRequester,
                profiles = listOf(
                    Profile(PreferenceManager.getUsername()?:"CaasTV", R.drawable.user_icon),
                ),
                selectedProfile = 0,
                onProfileClick = {},
                onEditProfile = {},
                subscription = SubscriptionInfo(PreferenceManager.getLoginResponse()?.loginData?.packages?.getOrNull(0)?.packageName?.capitalizeFirstLetter()?:"Super Annual Plan", PreferenceManager.getLoginResponse()?.loginData?.packageExpiryDate?:"10 Nov, 2025", {}),
                registeredMobile = PreferenceManager.getLoginResponse()?.loginData?.mobileNo?:"+91 ********",
                onUpdateMobile = {},
                thisDevice = DeviceInfo(Build.MODEL, "Today", R.drawable.tv),
                otherDevices = listOf(
                    DeviceInfo("LG TV", "2 Days Ago", R.drawable.tv)
                ),
                onLogout = {logoutInfo->
                        if(logoutInfo.lastUsed.equals("Today",true)){
                            showExitDialog = true
                        }

                }
            )
        }
        ExpandableNavigationMenu(
            navController   = navController,
            sharedViewModel = sharedViewModel,
            onNavMenuIntent = { tabInfo, _ ->
                menuItems = tabInfo.categories ?: emptyList()
            },
            modifier = Modifier.align(Alignment.CenterStart)  // ← overlay
        )
    }



    if (showExitDialog) {
        CommonDialog(
            showDialog = true,
            title = "Logout App",
            message = "Are you sure you want to logout exit the app?",
            painter = painterResource(id = R.drawable.logout_icon),
            errorCode = null,
            errorMessage = null,
            borderColor = Color.Transparent,
            confirmButtonText ="Yes" ,
            onConfirm =  {
                sharedViewModel.clearRecentlyWatched()
                PreferenceManager.clearLogin()
                showExitDialog = false
                context.hideKeyboard()
                (context as? Activity)?.finishAffinity()
            },
            dismissButtonText = "No",
            onDismiss = { showExitDialog = false }
        )
    }
}
