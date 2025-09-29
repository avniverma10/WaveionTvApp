package com.panmetro.iptv.view

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.panmetro.iptv.extensions.loge
import com.panmetro.iptv.extensions.provideMacAddress
import com.panmetro.iptv.globalFingerprintService.OverlayPermissionHelper
import com.panmetro.iptv.utils.network.ApiStatusObserver
import com.panmetro.iptv.utils.uistate.PreferenceManager
import com.panmetro.iptv.view.navigationhelper.WTVPlayerApp
import com.panmetro.iptv.viewmodels.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.M)
@AndroidEntryPoint
class MainActivity : ComponentActivity(),ApiStatusObserver  {
    private lateinit var overlayHelper: OverlayPermissionHelper
    private val sharedViewModel: SharedViewModel by viewModels()


    private val isFireTv: Boolean
        get() = Build.MANUFACTURER.equals("Amazon", ignoreCase = true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.init(applicationContext)
        //Init macId
        sharedViewModel.deviceMacAddr.value = provideMacAddress().toString()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)



        /*overlayHelper = OverlayPermissionHelper(this).apply {
            registerLauncher()
        }*/

        setContent {
            WTVApp()
            /*val hasOverlayPermission by overlayHelper.hasOverlayPermissionState
                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        isFireTv -> {
                            WTVApp()
                        }

                        hasOverlayPermission -> {
                            WTVApp()
                            LaunchedEffect(Unit) {
                                overlayHelper.startOverlayServiceIfNeeded()
                                overlayHelper.requestIgnoreBatteryOptimizationsIfNeeded()
                            }
                        }

                        else -> {
                            OverlayPermissionDialog {
                                overlayHelper.requestOverlayPermission()
                            }
                        }
                    }
                }
            }*/
        }
    }


    /*override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            // Optional: Handle at activity level
            showToastS("Please Exist the App First!")
            return true
        }
        *//*if (event?.action == KeyEvent.ACTION_UP) {
            when (event.keyCode) {
                *//**//* Most AOSP remotes use 289 for “TV Home”.
                   Replace / add the code(s) your box spits out (adb logcat). *//**//*
                KeyEvent.KEYCODE_HOME, 289        -> bus.post(RemoteKey.TV_HOME)
                KeyEvent.KEYCODE_DPAD_CENTER,
                KeyEvent.KEYCODE_ENTER               -> bus.post(RemoteKey.OK)
            }
        }*//*
        return super.onKeyDown(keyCode, event)
    }*/

    @Composable
    private fun WTVApp() {
        Box(modifier = Modifier.fillMaxSize()) {
            WTVPlayerApp(sharedViewModel = sharedViewModel)
        }
    }

    override fun onApiStatusChanged(isApiWorking: Boolean) {
        runOnUiThread {
            if (!isApiWorking) {
                loge("SSE isApiWorking", "Connection failed")
                // API is back online, notify user
                sharedViewModel.isGlobalSSEClosed.value = true
            } else {
                loge("SSE isApiWorking", "Connection Success")
                sharedViewModel.isGlobalSSEClosed.value = false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}

