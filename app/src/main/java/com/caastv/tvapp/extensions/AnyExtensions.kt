package com.caastv.tvapp.extensions

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Calendar
import java.util.Collections
import java.util.Date
import java.util.Locale
import java.util.Random
import java.util.concurrent.Executor
import java.util.concurrent.Executors


fun Any?.hashCodeString() = Integer.toHexString(System.identityHashCode(this))


fun Any.toGson() = Gson().toJsonTree(this)

fun Bitmap.getResizeImage(maxSize: Int): Bitmap {

    val bitmapRatio = this.width.toFloat() / this.height.toFloat()
    if (bitmapRatio > 1) {
        width = maxSize
        height = width / bitmapRatio.toInt()
    } else {
        height = maxSize
        width = height * bitmapRatio.toInt()
    }

    val bitmap = this
    return Bitmap.createScaledBitmap(bitmap, width, height, true)
}


fun Any.toJSONObject(): JSONObject? {
    var appJsonData: JSONObject? = null
    val gson = provideGsonWithCoreJsonString()
    try {
        val stringData = gson.toJson(this) //convert
        appJsonData = JSONObject(stringData)
        return appJsonData

    } catch (je: Throwable) {
        je.printStackTrace()
    }
    return appJsonData
}

fun Any.toJSONArray(): JSONArray? {
    var appJsonData: JSONArray? = null
    val gson = provideGsonWithCoreJsonString()
    try {
        val stringData = gson.toJson(this) //convert
        appJsonData = JSONArray(stringData)
        return appJsonData

    } catch (je: Throwable) {
        je.printStackTrace()
    }
    return appJsonData
}

fun Any.logReport(message: String?, error: Throwable? = null) {
    error?.printStackTrace()
    loge(value = message)
}

fun Any.logReport(pageId: String?, message: String, error: Throwable? = null) {
    error?.printStackTrace()
        loge(value = "pageId=$pageId, message=$message")
}

fun Any.mainThreadExecutor(): Executor {
    val handler = Handler(Looper.getMainLooper())
    return Executor { command -> handler.post(command) }
}

fun Any.computationExecutor(threadCount: Int = 4): Executor {
    return Executors.newFixedThreadPool(if (threadCount > 0) threadCount else 2)
}


enum class CoreDays {
    SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY;

    companion object Factory {
        fun getToday(day: Int = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)): CoreDays {
            return when (day) {
                1 -> SUNDAY
                2 -> MONDAY
                3 -> TUESDAY
                4 -> WEDNESDAY
                5 -> THURSDAY
                6 -> FRIDAY
                7 -> SATURDAY
                else -> SUNDAY
            }
        }

        fun fromDate(date: Date): CoreDays {
            val currentCalendar = Calendar.getInstance()
            currentCalendar.time = date
            return getToday(day = currentCalendar.get(Calendar.DAY_OF_WEEK))
        }

        fun fromDayName(name: String?): CoreDays {
            return when (name) {
                "Sunday" -> SUNDAY
                "Monday" -> MONDAY
                "Tuesday" -> TUESDAY
                "Wednesday" -> WEDNESDAY
                "Thursday" -> THURSDAY
                "Friday" -> FRIDAY
                "Saturday" -> SATURDAY
                else -> {
                    SUNDAY
                }
            }
        }
    }

    fun toIdString(): String {
        return when (this) {
            SUNDAY -> "0"
            MONDAY -> "1"
            TUESDAY -> "2"
            WEDNESDAY -> "3"
            THURSDAY -> "4"
            FRIDAY -> "5"
            SATURDAY -> "6"
        }
    }


    fun nameFirstCap(): String {
        return when (this) {
            SUNDAY -> "Sunday"
            MONDAY -> "Monday"
            TUESDAY -> "Tuesday"
            WEDNESDAY -> "Wednesday"
            THURSDAY -> "Thursday"
            FRIDAY -> "Friday"
            SATURDAY -> "Saturday"
        }
    }

    fun calendarDayId() = when (this) {
        SUNDAY -> 1
        MONDAY -> 2
        TUESDAY -> 3
        WEDNESDAY -> 4
        THURSDAY -> 5
        FRIDAY -> 6
        SATURDAY -> 7
    }

}


fun isValidIp4Address(hostName: String?): Boolean {
    return try {
        Inet4Address.getByName(hostName) != null
    } catch (ex: java.lang.Exception) {
        false
    }
}


fun Any.proceedWithCatch(
    expression: () -> Any,
    catchExpression: ((e: java.lang.Exception) -> Any)? = null,
    pageId: String? = "",
    fallbackErrorMessage: String? = ""
) {
    try {
        expression()
    } catch (e1: java.lang.Exception) {
        catchExpression?.invoke(e1)
        logReport(
            pageId = pageId ?: "",
            message = e1.message ?: fallbackErrorMessage ?: "",
            error = e1
        )
    }
}

fun File?.getFileSizeInMb(): Double {
    val size = this?.length()?.toString().getDoubleValue()

    return size / (1024 * 1024)//1024 to Kb and then 1024 for MB
}

fun getIPAddress(useIPv4: Boolean=true): String {
    try {
        val interfaces: List<NetworkInterface> =
            Collections.list(NetworkInterface.getNetworkInterfaces())
        for (networkInterface in interfaces) {
            val addrs: List<InetAddress> = Collections.list(networkInterface.inetAddresses)
            for (addr in addrs) {
                if (!addr.isLoopbackAddress) {
                    val sAddr = addr.hostAddress.toUpperCase(Locale.US)
                    val isIPv4 = addr is Inet4Address
                    if (useIPv4) {
                        if (isIPv4) return sAddr
                    } else {
                        if (!isIPv4) {
                            val delim = sAddr.indexOf('%') // drop ip6 port suffix
                            return if (delim < 0) sAddr else sAddr.substring(0, delim)
                        }
                    }
                }
            }
        }
    } catch (ex: java.lang.Exception) {
    } // for now eat exceptions
    return ""
}

fun Any.generateUniqueRandomLong(): Long {
    val random = Random()
    val uniqueId = System.currentTimeMillis()
    val randomLong = random.nextLong()
    return randomLong xor uniqueId
}

// Extension function to format time (mm:ss)
fun Long.toFormattedTime(): String {
    val minutes = (this / 1000) / 60
    val seconds = (this / 1000) % 60
    return String.format("%02d:%02d", minutes, seconds)
}