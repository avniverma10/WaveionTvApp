package com.example.tvapp.view

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import com.example.tvapp.utils.network.heper.NetworkStatus
import com.example.tvapp.utils.theme.TVAppTheme
import com.example.tvapp.utils.uistate.PreferenceManager
import com.example.tvapp.view.navigationhelper.WTVPlayerApp
import com.example.tvapp.view.network.NetworkUnstableScreen
import com.example.tvapp.viewmodels.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@RequiresApi(Build.VERSION_CODES.M)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val sharedViewModel: SharedViewModel by viewModels()
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
                    WTVPlayerApp(sharedViewModel = sharedViewModel)
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
