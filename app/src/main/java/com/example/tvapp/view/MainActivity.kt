package com.example.tvapp.view

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.example.tvapp.utils.theme.TVAppTheme
import com.example.tvapp.view.epg.EPGScreen
import com.example.tvapp.view.home.HomeScreen
import com.example.tvapp.view.navigationhelper.WTVPlayerApp
import com.example.tvapp.viewmodels.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val sharedViewModel: SharedViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install Splash Screen
        //installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            TVAppTheme {
                WTVPlayerApp(sharedViewModel=sharedViewModel)
            }
        }
    }
}
