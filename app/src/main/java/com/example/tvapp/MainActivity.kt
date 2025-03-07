package com.example.tvapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.lifecycleScope
import androidx.tv.material3.Surface
import com.example.tvapp.api.DeviceInfoService
import com.example.tvapp.navigation.AppNavGraph
import com.example.tvapp.screens.EPGScreen
import com.example.tvapp.ui.screens.HomeScreen
import com.example.tvapp.ui.theme.TVAppTheme
import com.example.tvapp.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TVAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape
                ) {
                    val navController = rememberNavController() // Create NavController
                  AppNavGraph()
                }
            }
        }
        //TODO put this at login page, this is jsyt for testing
        lifecycleScope.launch {
            DeviceInfoService.sendDeviceInfo(applicationContext, "rishi")
        }
    }
}
