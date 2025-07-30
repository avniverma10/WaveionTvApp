package com.caastv.tvapp.view

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.SoundEffectConstants
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewModelScope
import com.caastv.tvapp.extensions.hideKeyboard
import com.caastv.tvapp.extensions.showToastS
import com.caastv.tvapp.utils.network.ApiStatusObserver
import com.caastv.tvapp.utils.network.interceptors.ApiStatusInterceptor
import com.caastv.tvapp.utils.network.scheduler.ServerHealthCheckWorker
import com.caastv.tvapp.view.uicomponent.fingerprint.globalfingerprint.OverlayPermissionHelper
import com.caastv.tvapp.utils.theme.TVAppTheme
import com.caastv.tvapp.utils.uistate.PreferenceManager
import com.caastv.tvapp.view.navigationhelper.WTVPlayerApp
import com.caastv.tvapp.viewmodels.SharedViewModel
import com.caastv.tvapp.viewmodels.WTVViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity(),ApiStatusObserver {
    private lateinit var overlayHelper: OverlayPermissionHelper
    private val sharedViewModel: SharedViewModel by viewModels()

    private val isFireTv: Boolean
        get() = Build.MANUFACTURER.equals("Amazon", ignoreCase = true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideKeyboard()
        PreferenceManager.init(applicationContext)
        ApiStatusInterceptor.getInstance().addObserver(this)
        PreferenceManager.preferredAudio       = null
        PreferenceManager.preferredSubtitle    = null
        PreferenceManager.preferredVideoQuality = null
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        /*overlayHelper = OverlayPermissionHelper(this).apply {
            registerLauncher()
        }*/

        setContent {
            TVAppTheme {
                // observe overlay‐permission state
                Box(modifier = Modifier.fillMaxSize()) {
                    WTVApp()
                }

                hideKeyboard()
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
                  }*/
            }
        }
    }



    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_UP,
                KeyEvent.KEYCODE_DPAD_DOWN,
                KeyEvent.KEYCODE_DPAD_LEFT,
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    window.decorView.playSoundEffect(SoundEffectConstants.NAVIGATION_DOWN)
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    @Composable
    private fun WTVApp() {
        Box(modifier = Modifier.fillMaxSize()) {
            WTVPlayerApp(sharedViewModel = sharedViewModel)
        }
    }

    override fun onApiStatusChanged(isApiWorking: Boolean) {
        runOnUiThread {
            if (!isApiWorking) {
                // API is back online, notify user
                sharedViewModel.enableOffline(true)
                // Server went offline - start health checks
                ServerHealthCheckWorker.schedule(this,sharedViewModel)
            } else {
                // Server is back online - cancel health checks
                ServerHealthCheckWorker.cancel(this)
                // API is back online, notify user
                sharedViewModel.enableOffline(false)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        ApiStatusInterceptor.getInstance().removeObserver(this)
    }
}

