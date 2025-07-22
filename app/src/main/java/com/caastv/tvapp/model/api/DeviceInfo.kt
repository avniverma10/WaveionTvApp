package com.caastv.tvapp.model.api

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DeviceInfo(
    val userId: String,
    val deviceType: String,
    val os: String,
    val osVersion: String,
    val deviceId: String,
    val macAddress: String,
    val lastLogin: String,
    val active: Boolean
)

interface ApiServiceForDeviceInfo {
    @POST("deviceInfo")
    suspend fun sendDeviceInfo(@Body deviceInfo: DeviceInfo): Response<Void>
}


object DeviceInfoService {
    @SuppressLint("HardwareIds")
    fun collectDeviceInfo(context: Context, userId: String, isActive: Boolean = true): DeviceInfo {
        val deviceType =
            if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)) {
                "AndroidTV"
            } else {
                "AndroidPhone"
            }
        val os = "Android"
        val osVersion = Build.VERSION.RELEASE

        // Get unique device identifier.
        val deviceId =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val macAddress = wifiManager.connectionInfo.macAddress ?: "unknown"

        val lastLogin = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault()).format(Date())

        Log.i("rishi -DeviceInfo", "collectDeviceInfo: userId=$userId, deviceType=$deviceType, os=$os, osVersion=$osVersion, deviceId=$deviceId, macAddress=$macAddress, lastLogin=$lastLogin, active=$isActive")

        return DeviceInfo(
            userId = userId,
            deviceType = deviceType,
            os = os,
            osVersion = osVersion,
            deviceId = deviceId,
            macAddress = macAddress,
            lastLogin = lastLogin,
            active = isActive
        )
    }
}
