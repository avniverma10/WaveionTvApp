package com.example.tvapp.view

import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.SoundEffectConstants
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.tvapp.extensions.hideKeyboard
import com.example.tvapp.extensions.showToastS
import com.example.tvapp.view.uicomponent.fingerprint.globalfingerprint.OverlayPermissionDialog
import com.example.tvapp.view.uicomponent.fingerprint.globalfingerprint.OverlayPermissionHelper
import com.example.tvapp.utils.theme.TVAppTheme
import com.example.tvapp.utils.uistate.PreferenceManager
import com.example.tvapp.view.navigationhelper.WTVPlayerApp
import com.example.tvapp.viewmodels.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var overlayHelper: OverlayPermissionHelper
    private val sharedViewModel: SharedViewModel by viewModels()

    private val isFireTv: Boolean
        get() = Build.MANUFACTURER.equals("Amazon", ignoreCase = true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideKeyboard()
        PreferenceManager.init(applicationContext)
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
}

