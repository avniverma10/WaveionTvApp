package com.example.tvapp.view

import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.tvapp.extensions.provideMacAddress
import com.example.tvapp.extensions.showToastS
import com.example.tvapp.globalFingerprintService.OverlayPermissionDialog
import com.example.tvapp.globalFingerprintService.OverlayPermissionHelper
import com.example.tvapp.utils.uistate.PreferenceManager
import com.example.tvapp.view.navigationhelper.WTVPlayerApp
import com.example.tvapp.viewmodels.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@RequiresApi(Build.VERSION_CODES.M)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
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

        overlayHelper = OverlayPermissionHelper(this).apply {
            registerLauncher()
        }

        setContent {
            val hasOverlayPermission by overlayHelper.hasOverlayPermissionState
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
}

