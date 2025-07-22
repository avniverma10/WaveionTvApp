package com.caastv.tvapp.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaDrm
import android.media.UnsupportedSchemeException
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.media3.common.C
import java.net.NetworkInterface
import java.util.Collections
import java.util.Locale

import android.net.LinkAddress
import java.net.Inet4Address
import java.net.Inet6Address


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
        /*info["Serial"] = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Build.getSerial()
        } else {
            Build.SERIAL
        }*/
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
    return try { getVendorMacSuffixDecimal()?.buildFullMac()?: getMacAddress()?: Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
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
        // Try Wi-Fi manager (may return 02:00:00:00:00:00 on Android 6+)
        //    Only works if the app has ACCESS_WIFI_STATE and Wi-Fi is enabled.
        val wifiMgr = getApplicationContext().getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val wifiInfo = wifiMgr?.connectionInfo
        val macFromWifi = wifiInfo?.macAddress
        if (!macFromWifi.isNullOrBlank() && macFromWifi != "02:00:00:00:00:00") {
            return macFromWifi.uppercase(Locale.US)
        }

        // Fall back to NetworkInterface enumeration
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


fun Context.getIPAddress(): String? {
    try {
        val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
        for (intf in interfaces) {
            for (addr in Collections.list(intf.inetAddresses)) {
                if (!addr.isLoopbackAddress) {
                    val sAddr = addr.hostAddress?.uppercase(Locale.US)
                    if (sAddr != null) {
                        // Prefer IPv4 for simplicity, but you can remove this check if you want IPv6
                        val isIPv4 = sAddr.indexOf(':') < 0
                        if (isIPv4) {
                            return sAddr
                        }
                    }
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

// For Wi-Fi IP address specifically
fun Context.getWifiIPAddress(): String? {
    try {
        val wifiMgr = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiMgr.connectionInfo
        val ip = wifiInfo.ipAddress
        return String.format(
            Locale.US,
            "%d.%d.%d.%d",
            ip and 0xff,
            ip shr 8 and 0xff,
            ip shr 16 and 0xff,
            ip shr 24 and 0xff
        )
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}


/**
 * Best-effort IP detector.
 * Returns the first non-loopback, non-link-local address—preferring IPv4.
 */
fun Context.getDeviceIpAddress(): String? {
    val cm = getSystemService(ConnectivityManager::class.java) ?: return null
    val lp = cm.getLinkProperties(cm.activeNetwork) ?: return null

    // 1️⃣ Prefer non-link-local IPv4
    lp.linkAddresses
        .map(LinkAddress::getAddress)
        .firstOrNull { it is Inet4Address && !it.isLinkLocalAddress && !it.isLoopbackAddress }
        ?.let { return it.hostAddress }

    // 2️⃣ Fallback to non-link-local IPv6
    lp.linkAddresses
        .map(LinkAddress::getAddress)
        .firstOrNull { it is Inet6Address && !it.isLinkLocalAddress && !it.isLoopbackAddress }
        ?.let { addr ->
            return addr.hostAddress.substringBefore('%') // strip scope id
        }

    return null
}