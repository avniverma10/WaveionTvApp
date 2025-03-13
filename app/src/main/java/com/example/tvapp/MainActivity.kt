package com.example.tvapp

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import com.example.tvapp.components.ExpandableNavigationMenu
import com.example.tvapp.navigation.AppNavGraph
import com.example.tvapp.screens.EPGContent
import com.example.tvapp.screens.EPGScreen
import com.example.tvapp.screens.LoginScreen
import com.example.tvapp.screens.SearchScreen
import com.example.tvapp.ui.screens.HomeScreen
import com.example.tvapp.ui.theme.TVAppTheme
import com.example.tvapp.utils.RootCheckUtil
import com.example.tvapp.viewmodels.HomeViewModel
import com.example.tvapp.viewmodels.OtpScreen1
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
                  LoginScreen(navController)
                }
            }
        }
        //Root check of the device
        if (RootCheckUtil.isDeviceRooted()) {
            Log.e("RootCheck", "Device is rooted!")
            Toast.makeText(this, "Warning: This device is rooted!", Toast.LENGTH_LONG).show()
            // You can take additional actions here, like restricting access.
        } else {
            Log.i("RootCheck", "Device is NOT rooted, proceeding normally.")
            // Continue with normal app flow
        }

        //TODO put this at login page, this is jsyt for testing
        lifecycleScope.launch {
            DeviceInfoService.sendDeviceInfo(applicationContext, "rishi")
        }
    }
}
