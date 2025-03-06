//package com.example.tvapp.utils
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.net.wifi.WifiManager
//import android.os.Build
//import android.util.Log
//import androidx.annotation.RequiresPermission
//import java.net.Inet4Address
//import java.net.NetworkInterface
//import android.provider.Settings
//import java.net.URL
//
//object DeviceInfoHelper {
//
//    fun getDeviceModel(): String = Build.MODEL
//
//    fun getManufacturer(): String = Build.MANUFACTURER
//
//    fun getOSVersion(): String = Build.VERSION.RELEASE
//
//    fun getSDKVersion(): Int = Build.VERSION.SDK_INT
//
////    fun getIPAddress(): String? {
////        return try {
////            val interfaces = NetworkInterface.getNetworkInterfaces()
////            interfaces.toList().flatMap { it.inetAddresses.toList() }
////                .firstOrNull { !it.isLoopbackAddress && it is Inet4Address }?.hostAddress
////        } catch (e: Exception) {
////            Log.e("DeviceInfoHelper", "Error getting IP Address", e)
////            null
////        }
////    }
//
//    /** Get Android ID */
//    @SuppressLint("HardwareIds")
//    fun getAndroidId(context: Context): String {
//        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
//    }
//
//
//    /** Get IP Address */
//    fun getIPAddress(): String? {
//        return try {
//            val interfaces = NetworkInterface.getNetworkInterfaces()
//            interfaces.toList().flatMap { it.inetAddresses.toList() }
//                .firstOrNull { !it.isLoopbackAddress && it is Inet4Address }?.hostAddress
//        } catch (e: Exception) {
//            Log.e("DeviceInfoHelper", "Error getting IP Address", e)
//            null
//        }
//    }
//
//    /** Get Public IP Address */
//    fun getPublicIPAddress(): String? {
//        return try {
//            val url = URL("https://checkip.amazonaws.com")
//            url.readText().trim()
//        } catch (e: Exception) {
//            Log.e("DeviceInfoHelper", "Error getting public IP", e)
//            null
//        }
//    }
//    /** Get MAC Address */
//    @SuppressLint("HardwareIds", "MissingPermission")
//    @RequiresPermission(android.Manifest.permission.ACCESS_WIFI_STATE)
//    fun getMacAddress(context: Context): String? {
//        return try {
//            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
//            wifiManager.connectionInfo.macAddress
//        } catch (e: Exception) {
//            Log.e("DeviceInfoHelper", "Error getting MAC Address", e)
//            null
//        }
//    }
//    /** Get Serial Number (Requires Android 9 and below) */
//    @SuppressLint("HardwareIds")
//    fun getSerialNumber(): String {
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            Build.getSerial() // Requires "READ_PHONE_STATE" permission
//        } else {
//            Build.SERIAL
//        }
//    }
//
//    @RequiresPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
//    fun getWifiInfo(context: Context): Pair<String, String>? {
//        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
//        val wifiInfo = wifiManager.connectionInfo
//        return Pair(wifiInfo.ssid, wifiInfo.bssid)
//    }
//
//    fun getDeviceInfo(context: Context): Map<String, String?> {
//        val wifiInfo = getWifiInfo(context)
//        return mapOf(
//            "Device Model" to getDeviceModel(),
//            "Manufacturer" to getManufacturer(),
//            "OS Version" to getOSVersion(),
//            "SDK Version" to getSDKVersion().toString(),
//            "IP Address" to getIPAddress(),
//            "Public IP Address" to getPublicIPAddress(),
//            "MAC Address" to getMacAddress(context),
//            "Serial Number" to getSerialNumber(),
//            "Wi-Fi SSID" to wifiInfo?.first,
//            "Wi-Fi BSSID" to wifiInfo?.second,
//
//        )
//    }
//}
