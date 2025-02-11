//package com.example.tvapp.screens
//
//import android.content.Context
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import com.example.tvapp.utils.DeviceInfoHelper
////import com.example.tvapp.utils.DeviceInfoHelper
//import com.example.tvapp.viewmodels.ExoPlayerViewModel
//
//
//@Composable
//fun MainScreen() {
//    val context = LocalContext.current
//    DeviceInfoScreen(context)
//}
//
//@Composable
//fun DeviceInfoScreen(context: Context) {
//    val deviceInfo = remember { DeviceInfoHelper.getDeviceInfo(context) }
//
//    Box(modifier = Modifier.fillMaxSize()) {
//        Column(modifier = Modifier.background(Color.White).fillMaxSize()) {
//            Text(text = "Device Information", fontSize = 20.sp, fontWeight = FontWeight.Bold)
//
//            deviceInfo.forEach { (key, value) ->
//                Text(text = "$key: $value", fontSize = 16.sp)
//            }
//        }
//    }
//}