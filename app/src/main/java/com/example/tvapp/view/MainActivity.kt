package com.example.tvapp.view

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.tvapp.extensions.showToastS
import com.example.tvapp.utils.theme.TVAppTheme
import com.example.tvapp.utils.uistate.PreferenceManager
import com.example.tvapp.view.navigationhelper.WTVPlayerApp
import com.example.tvapp.viewmodels.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val sharedViewModel: SharedViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install Splash Screen
        super.onCreate(savedInstanceState)
        //Init the singleton
        PreferenceManager.init(applicationContext)
        //init app data
        sharedViewModel.initializeAppRequiredData()
        // in onCreate of your ComponentActivity
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        )

        setContent {
            TVAppTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    WTVPlayerApp(sharedViewModel=sharedViewModel)
                }
//                val navController = rememberNavController()
//                ChannelScreen(navController ,sharedViewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
       // showToastS("MainActivity>onDestroy")
    }
}
