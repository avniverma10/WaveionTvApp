package com.caastv.tvapp.extensions


import android.app.ActivityManager
import android.content.Context
import android.media.MediaDrm
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.Base64
import androidx.media3.common.C
import java.util.Locale


data class AndroidTvDrmInfo(
    var userName:String?=null,
    var userPassword:String?=null,
    val drmScheme: String?=null,
    val widevineSecurityLevel: String?=null,
    val macId: String?=null,
    val deviceUniqueId: String?=null,
    val manufacturer: String?=null,
    val model: String?=null,
    val androidVersion: String?=null,
    val totalMemory: String?=null,
    val storageInfo: String?=null,
    val availableStorage: String?=null
)

fun Context.getAndroidTvDrmInfo(): AndroidTvDrmInfo? {
    return try {
        // DRM UUID for Widevine
        val mediaDrm = MediaDrm(C.WIDEVINE_UUID)

        val securityLevel = mediaDrm.getPropertyString("securityLevel")

        val deviceIdBytes: ByteArray = mediaDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)

        val deviceIdBase64 = Base64.encodeToString(deviceIdBytes, Base64.NO_WRAP)

        AndroidTvDrmInfo(
            drmScheme = "Widevine",
            widevineSecurityLevel = securityLevel,
            deviceUniqueId = deviceIdBase64,
            macId= C.WIDEVINE_UUID.toString(),
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE,
            availableStorage = getAvailableStorage(),
            totalMemory = this.getTotalMemory(),
            storageInfo = getStorageInfo()
        )

    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


fun getAvailableStorage(): String {
    val stat = StatFs(Environment.getDataDirectory().path)

    val bytesAvailable = stat.blockSizeLong * stat.availableBlocksLong
    val availableStorageInGB = bytesAvailable.toDouble() / (1024 * 1024 * 1024)

    return String.format(Locale.US, "%.2f GB", availableStorageInGB)
}

fun getStorageInfo(): String {
    val stat = StatFs(Environment.getDataDirectory().path)

    val bytesAvailable = stat.blockSizeLong * stat.blockCountLong
    val totalStorageInGB = bytesAvailable.toDouble() / (1024 * 1024 * 1024)

    return String.format(Locale.US, "%.2f GB", totalStorageInGB)
}

fun Context.getTotalMemory(): String {
    val memoryInfo = ActivityManager.MemoryInfo()
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    activityManager.getMemoryInfo(memoryInfo)

    val totalMemInGB = memoryInfo.totalMem.toDouble() / (1024 * 1024 * 1024)
    return String.format(Locale.US, "%.2f GB", totalMemInGB)
}

fun AndroidTvDrmInfo.toHashMap(): HashMap<String, String?> {
    return hashMapOf(
        "userName" to (userName ?: ""),
        "userPassword" to (userPassword ?: ""),
        "drmScheme" to drmScheme,
        "widevineSecurityLevel" to widevineSecurityLevel,
        "macId" to macId,
        "deviceUniqueId" to deviceUniqueId,
        "manufacturer" to manufacturer,
        "model" to model,
        "androidVersion" to androidVersion,
        "totalMemory" to totalMemory,
        "storageInfo" to storageInfo,
        "availableStorage" to availableStorage
    )
}