package com.example.tvapp.view

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.example.tvapp.utils.theme.TVAppTheme
import com.example.tvapp.view.navigationhelper.WTVPlayerApp
import com.example.tvapp.view.panmetro.PanmetroLoginScreen
import com.example.tvapp.view.player.CommonDialog
import com.example.tvapp.viewmodels.LoginViewModel
import com.example.tvapp.viewmodels.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val sharedViewModel: SharedViewModel by viewModels()



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install Splash Screen
        super.onCreate(savedInstanceState)
        //init app data
        sharedViewModel.initializeAppRequiredData()
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

}
