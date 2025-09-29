package com.panmetro.iptv.extensions

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.media.MediaDrm
import android.media.UnsupportedSchemeException
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.media3.common.C
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections
import java.util.Locale
import java.util.UUID
import kotlin.text.uppercase


@SuppressLint("HardwareIds", "MissingPermission")
fun Context.getIptvDeviceInfo(): Map<String, String?> {
    val info = mutableMapOf<String, String?>()

    try {
        val build = Build::class.java
        info["Brand"] = Build.BRAND
        info["Manufacturer"] = Build.MANUFACTURER
        info["Model"] = Build.MODEL
        info["Product"] = Build.PRODUCT
        info["Device"] = Build.DEVICE
        info["Board"] = Build.BOARD
        info["Hardware"] = Build.HARDWARE
        info["Bootloader"] = Build.BOOTLOADER
        info["Host"] = Build.HOST
        info["Fingerprint"] = Build.FINGERPRINT
        info["Display"] = Build.DISPLAY
        info["Build ID"] = Build.ID
        info["Build Time"] = Build.TIME.toString()
        info["SDK Version"] = Build.VERSION.SDK_INT.toString()
        info["Release"] = Build.VERSION.RELEASE
        info["Serial"] = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Build.getSerial()
        } else {
            Build.SERIAL
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    // Network Info
    try {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm.activeNetwork
        } else {
            null
        }

        val capabilities = activeNetwork?.let { cm.getNetworkCapabilities(it) }

        info["Network Type"] = when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "WiFi"
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "Ethernet"
            else -> "Unknown"
        }

        info["MAC Address"] = provideMacAddress()//getDeviceMacAddress()
        //info["IP Address"] = getLocalIpAddress()
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return info
}


fun getSystemAllInfo(): String{
    val sb = StringBuilder()
    try {
        sb.appendLine("Brand: ${Build.BRAND}")
        sb.appendLine("Manufacturer: ${Build.MANUFACTURER}")
        sb.appendLine("Model: ${Build.MODEL}")
        sb.appendLine("PRODUCT: ${Build.PRODUCT}")
        sb.appendLine("DEVICE: ${Build.DEVICE}")
        sb.appendLine("BOARD: ${Build.BOARD}")
        sb.appendLine("HARDWARE: ${Build.HARDWARE}")
        sb.appendLine("BOOTLOADER: ${Build.BOOTLOADER}")
        sb.appendLine("HOST: ${Build.HOST}")
        sb.appendLine("FINGERPRINT: ${Build.FINGERPRINT}")
        sb.appendLine("DISPLAY: ${Build.DISPLAY}")
        sb.appendLine("Build ID: ${Build.ID}")
        sb.appendLine("Build TIME: ${Build.TIME.toString()}")
        sb.appendLine("Build VERSION: ${ Build.VERSION.SDK_INT.toString()}")
        sb.appendLine("Build RELEASE: ${ Build.VERSION.RELEASE}")
        sb.appendLine("Serial: ${ if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Build.getSerial()
        } else {
            Build.SERIAL
        }}")
    } catch (e: Exception) {
        sb.appendLine("HDMI Info: Not available")
    }

    return sb.toString()
}

fun Context.networkType():String?{
    // Network Info
    try {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm.activeNetwork
        } else {
            null
        }

        val capabilities = activeNetwork?.let { cm.getNetworkCapabilities(it) }

        val networkType = when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "wlan0"
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "eth0"
            else -> "Unknown"
        }
      return networkType
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}


@SuppressLint("HardwareIds")
fun Context.provideMacAddress():String?{
    return try {  getVendorMacSuffixDecimal()?.buildFullMac()?: Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID).uppercase()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


fun getVendorMacSuffixDecimal(): Long? {
    val keys = listOf(
        "ro.boot.macaddr",
        "ro.boot.mac",
    )
    return try {
        // load the hidden SystemProperties class
        val spClass   = Class.forName("android.os.SystemProperties")
        val getMethod = spClass.getMethod("get", String::class.java)

        // try each key until one returns a non‑empty string
        for (key in keys) {
            val value = (getMethod.invoke(null, key) as? String)?.trim()
            if (!value.isNullOrEmpty()) {
                // if it's already the full MAC ("00:15:C0:98:74:49") parse last 8 hex digits
                val hex = value.replace(":", "")
                if (hex.length == 12) {
                    // full 6‑bytes: just convert to decimal
                    return hex.takeLast(8).toLongOrNull(16)
                }
                // else assume it's the decimal suffix itself
                return value.toLongOrNull()
            }
        }
        null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


fun Long.buildFullMac(ouiPrefix: String?="00:15"): String? {
    // Step A: turn decimal into 8‑digit uppercase hex
    val hex8 = this
        .toULong()
        .toString(16)
        .uppercase()
        .padStart(8, '0')  // ensures we always have 4 bytes worth

    // Step B: split into 4 two‑char pairs
    val deviceBytes = hex8.chunked(2) // ["C0","98","74","49"]

    // Step C: combine your 2‑byte OUI and these 4
    return (ouiPrefix?.split(":")?.plus(deviceBytes))
        ?.joinToString(":") { it.padStart(2, '0').uppercase() }
}
fun isSchemeSupported(): Boolean =
    try {
        MediaDrm.isCryptoSchemeSupported(C.WIDEVINE_UUID)
    } catch (_: Exception) {
        false
    }


@SuppressLint("HardwareIds")
fun Context.getMacAddress(): String? {
    try {
        // 1) Try Wi-Fi manager (may return 02:00:00:00:00:00 on Android 6+)
        //    Only works if the app has ACCESS_WIFI_STATE and Wi-Fi is enabled.
        val wifiMgr = getApplicationContext().getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val wifiInfo = wifiMgr?.connectionInfo
        val macFromWifi = wifiInfo?.macAddress
        if (!macFromWifi.isNullOrBlank() && macFromWifi != "02:00:00:00:00:00") {
            return macFromWifi.uppercase(Locale.US)
        }

        // 2) Fall back to NetworkInterface enumeration
        val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
        for (intf in interfaces) {
            // prefer wlan0 (Wi-Fi) or eth0 (Ethernet) if present
            if (!intf.name.equals("wlan0", true) && !intf.name.equals("eth0", true)) continue
            val addr = intf.hardwareAddress ?: continue
            return addr.joinToString(separator = ":") { byte -> "%02X".format(byte) }
        }

        // 3) Last resort: any interface with a hardware address
        for (intf in interfaces) {
            val addr = intf.hardwareAddress ?: continue
            if (addr.isNotEmpty()) {
                return addr.joinToString(":") { byte -> "%02X".format(byte) }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

@RequiresApi(Build.VERSION_CODES.R)
fun logAllDrmInfo() {
    // On Android 12+:
    val schemes = if (isSchemeSupported()) {
        MediaDrm.getSupportedCryptoSchemes()
    } else {
        // Fallback list of known UUIDs: Widevine, PlayReady, CryptoGuard, ...
        listOf(
            C.WIDEVINE_UUID
        )
    }

    for (uuid in schemes) {
        try {
            val drm = MediaDrm(uuid)
            val vendor  = drm.getPropertyString(MediaDrm.PROPERTY_VENDOR)
            val version = drm.getPropertyString(MediaDrm.PROPERTY_VERSION)
            Log.i("DRM_INFO", "Scheme UUID=$uuid, Vendor=$vendor, Version=$version")
            drm.release()
        } catch (e: UnsupportedSchemeException) {
            // not supported—ignore
        }
    }
}


fun Context.getTestingDeviceInfo(): String {
    val sb = StringBuilder()

    // --- Basic Device Info ---
    /*sb.appendLine("📱 DEVICE INFO")
    sb.appendLine("Manufacturer: ${Build.MANUFACTURER}")
    sb.appendLine("Brand       : ${Build.BRAND}")
    sb.appendLine("Model       : ${Build.MODEL}")
    sb.appendLine("Device      : ${Build.DEVICE}")
    sb.appendLine("Product     : ${Build.PRODUCT}")
    sb.appendLine("Board       : ${Build.BOARD}")
    sb.appendLine("Hardware    : ${Build.HARDWARE}")
    sb.appendLine("Bootloader  : ${Build.BOOTLOADER}")
    sb.appendLine("Fingerprint : ${Build.FINGERPRINT}")
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        sb.appendLine("Serial      : ${Build.SERIAL}")
    }

    // --- CPU Info ---
    sb.appendLine("\n⚙ CPU INFO")
    sb.appendLine("Supported ABIs: ${Build.SUPPORTED_ABIS.joinToString()}")
    try {
        val reader = RandomAccessFile("/proc/cpuinfo", "r")
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            sb.appendLine(line)
        }
        reader.close()
    } catch (e: Exception) {
        sb.appendLine("CPU Info: Not accessible")
    }

    // --- OS Info ---
    sb.appendLine("\n🖥 OS INFO")
    sb.appendLine("Android Version : ${Build.VERSION.RELEASE}")
    sb.appendLine("SDK Int         : ${Build.VERSION.SDK_INT}")
    sb.appendLine("Security Patch  : ${Build.VERSION.SECURITY_PATCH}")
    sb.appendLine("Build ID        : ${Build.ID}")

     */
// --- Identifiers ---
    sb.appendLine("\n🔑 IDENTIFIERS")

    val androidId = Settings.Secure.getString(
        contentResolver,
        Settings.Secure.ANDROID_ID
    )
    sb.appendLine("Android ID : $androidId")

    // --- RAM Info ---
    sb.appendLine("\n💾 MEMORY")
    val actManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memInfo = ActivityManager.MemoryInfo()
    actManager.getMemoryInfo(memInfo)
    sb.appendLine("Total RAM       : ${memInfo.totalMem / (1024 * 1024)} MB")
    sb.appendLine("Available RAM   : ${memInfo.availMem / (1024 * 1024)} MB")

    // --- Storage Info ---
    val stat = Environment.getDataDirectory().usableSpace
    val total = Environment.getDataDirectory().totalSpace
    sb.appendLine("Internal Storage: ${total / (1024 * 1024)} MB")
    sb.appendLine("Available Storage: ${stat / (1024 * 1024)} MB")


    // --- Network Info ---
    sb.appendLine("\n🌐 NETWORK")
    try {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        sb.appendLine("Connection Type: ${activeNetwork?.typeName ?: "Unknown"}")

        // Local IP
        val interfaces = NetworkInterface.getNetworkInterfaces()
        interfaces.iterator().forEach { intf ->
            intf.inetAddresses.iterator().forEach { addr ->
                if (!addr.isLoopbackAddress && addr is InetAddress) {
                    sb.appendLine("IP Address: ${addr.hostAddress}")
                }
            }
        }

        // MAC (some devices restrict this)
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val mac = wifiManager.connectionInfo.macAddress
        sb.appendLine("MAC Address: $mac")

        // SSID (if Wi-Fi)
        sb.appendLine("SSID: ${wifiManager.connectionInfo.ssid}")
    } catch (e: Exception) {
        sb.appendLine("Network Info: Not accessible")
    }

    // --- DRM (Widevine) ---
    sb.appendLine("\n🔒 DRM (Widevine)")
    try {
        val widevineUUID = UUID.fromString("edef8ba9-79d6-4ace-a3c8-27dcd51d21ed")
        val drm = MediaDrm(widevineUUID)
        val widevineId = drm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)
        sb.appendLine("Widevine ID (Base64): ${Base64.encodeToString(widevineId, Base64.NO_WRAP)}")
        sb.appendLine("Vendor : ${drm.getPropertyString(MediaDrm.PROPERTY_VENDOR)}")
        sb.appendLine("Version: ${drm.getPropertyString(MediaDrm.PROPERTY_VERSION)}")
        sb.appendLine("Description: ${drm.getPropertyString(MediaDrm.PROPERTY_DESCRIPTION)}")
        drm.close()
    } catch (e: Exception) {
        sb.appendLine("Widevine: Not available")
    }

    // --- HDMI Status (Android TV/FireTV only) ---
    sb.appendLine("\n🔌 HDMI STATUS")
    try {
        val hdmiState = applicationContext.registerReceiver(null,
            android.content.IntentFilter("android.intent.action.HDMI_PLUGGED"))
        val plugged = hdmiState?.getBooleanExtra("state", false) ?: false
        sb.appendLine("HDMI Plugged: $plugged")
    } catch (e: Exception) {
        sb.appendLine("HDMI Info: Not available")
    }

    return sb.toString()
}
