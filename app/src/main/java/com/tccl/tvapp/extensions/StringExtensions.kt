package com.tccl.tvapp.extensions

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import org.threeten.bp.Duration
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.io.File
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.math.BigDecimal
import java.math.BigInteger
import java.net.NetworkInterface
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern


fun String?.isNotNullOrEmpty(): Boolean = this != null && this.trim().isNotEmpty()

fun String?.getColor(): Int {
    if (this == "#00000000" || this.isNullOrEmpty() || this.startsWith("#") && this.length > 9) {
        return Color.TRANSPARENT
    }
    return try {
        val color = this?.let {
            if (this.isEmpty()) -1 else this.getOctColor()
        } ?: kotlin.run {
            -1
        }
        color
    } catch (e: Exception) {
        -1
    }
}

private fun String.getOctColor(): Int {
    if (this.contains("rgba")) {
        var tempstr = this.split("rgba\\(".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()//.split(",");
        tempstr = tempstr[1].split("\\)".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        tempstr = tempstr[0].split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()


        if (!this.equals("rgba(255,255,255,0)", ignoreCase = true)) {
            return Color.parseColor(
                String.format(
                    "#%02x%02x%02x%02x",
                    Math.round(java.lang.Float.parseFloat(tempstr[3].trim()) * 255),
                    Integer.parseInt(tempstr[0].trim()),
                    Integer.parseInt(tempstr[1].trim()),
                    Integer.parseInt(tempstr[2].trim())
                )
            )
        }
        return Color.parseColor("#00000000")
    } else {
        return this.getObjColor()
    }

}


private fun String.getObjColor(): Int {

    try {
        if (this.contains("rgb")) {
            var tempstr = this.split("rgb\\(".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()//.split(",");
            tempstr =
                tempstr[1].split("\\)".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            tempstr = tempstr[0].split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            return Color.rgb(
                Integer.parseInt(tempstr[0].trim { it <= ' ' }),
                Integer.parseInt(tempstr[1].trim { it <= ' ' }),
                Integer.parseInt(tempstr[2].trim { it <= ' ' })
            )

        } else if (this.contains("#")) {
            if (this.length < 5) {
                val color = this.replace("#", "")
                var tempColor = ""
                for (i in 0..2) tempColor = color[i].toString() + color[i].toString() + tempColor
                //logReport("AppCompactView", "received color #$tempColor")
                return Color.parseColor("#$tempColor")

            } else return Color.parseColor(this)
        }
    } catch (e: Exception) {
        return Color.parseColor("#000000")
    }

    return Color.parseColor("#000000")
}

fun String?.getToolBarTextSize(): Float {
    return if (this.equals("largeHeaderBar", ignoreCase = true)) 26f
    else if (this.equals("mediumHeaderBar", ignoreCase = true)) 20f
    else if (this.equals("smallHeaderBar", ignoreCase = true)) 14f
    else if (this.equals("xlargeHeaderBar", ignoreCase = true)) 36f
    else 20f
}

fun String?.getFloatValue(defaultValue: Float = 0f): Float {
    return try {
        this?.toFloatOrNull() ?: defaultValue
    } catch (e: java.lang.Exception) {
        // logReport(e.message)
        0f
    }
}

fun String?.getDoubleValue(): Double {
    return try {
        this?.toDoubleOrNull() ?: 0.toDouble()
    } catch (e: java.lang.Exception) {
        // logReport(e.message)
        0.toDouble()
    }
}


fun String?.getIntValue(defaultValue: Int = 0): Int {
    return try {
        this?.trim()?.toIntOrNull() ?: defaultValue
    } catch (e: java.lang.Exception) {
        // logReport(e.message)
        defaultValue
    }
}

fun String?.getLongValue(defaultValue: Long = 0): Long {
    return try {
        this?.toLongOrNull() ?: defaultValue
    } catch (e: java.lang.Exception) {
        // logReport(e.message)
        defaultValue
    }
}

fun String?.getBigDecimalValue(defaultValue: BigDecimal = BigDecimal(0)): BigDecimal {
    return try {
        this?.toBigDecimal() ?: defaultValue
    } catch (e: java.lang.Exception) {
        // logReport(e.message)
        defaultValue
    }
}

fun BigDecimal?.getIntWithBigDecimal(defaultValue: Int = 0): Int {
    return try {
        this?.toInt() ?: defaultValue
    } catch (e: java.lang.Exception) {
        // logReport(e.message)
        defaultValue
    }
}

fun String?.getBooleanValue(): Boolean {
    return try {
        return this?.trim() == "true" || this?.trim() == "True" || this?.trim() == "TRUE" || this?.trim() == "1" || this?.trim() == "Yes" || this?.trim() == "YES" || this?.trim() == "yes"
    } catch (e: java.lang.Exception) {
        // logReport(e.message)
        false
    }
}


fun String?.validateEmail(): Boolean = this?.let {
    Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE)
        .matcher(this).find()
} ?: kotlin.run { false }


fun String?.isPhoneInput(): Boolean {
    this?.let {
        val doubleValue: Double = this.trim().toDoubleOrNull() ?: 0.toDouble()
        return (doubleValue > 0.toDouble())
    } ?: kotlin.run {
        return false
    }
}


fun String?.isMobileInput(): Boolean {
    this?.let {
        val doubleValue: Double = this.trim().toDoubleOrNull() ?: 0.toDouble()
        return (doubleValue > 0.toDouble()) && this.trim().length >= 10
    } ?: kotlin.run {
        return false
    }
}


fun String?.stableId() = this?.hashCode()?.toLong() ?: 0L


fun String.toJsonObject(): JsonObject? {
    return try {
        Gson().fromJson(this, JsonObject::class.java)
    } catch (e: Throwable) {
        loge("","${e.message}")
        null
    }
}

fun String.toJSONObject(): JSONObject? {
    return try {
        JSONObject(this)
    } catch (e: Throwable) {
        loge("","${e.message}")
        null
    }
}

fun String.toJsonArray(): JsonArray? {
    return try {
        Gson().fromJson(this, JsonArray::class.java)
    } catch (e: Throwable) {
        loge("","${e.message}")
        null
    }
}

fun String.getSharableIntent(): Intent {
    val intent = Intent("android.intent.action.SEND")
    intent.type = "text/plain"
    intent.putExtra("android.intent.extra.TEXT", this)
    return intent
}


fun getDateInInputPattern(pattern: String): String {
    val calendar = Calendar.getInstance()
    val time = calendar.time
    val outputFmt = SimpleDateFormat(pattern, Locale.US)
    return outputFmt.format(time)
}


fun String.encodeURIComponent(): String {
    val result: String? = try {
        URLEncoder.encode(this, "UTF-8").replace("\\+".toRegex(), "%20")
            .replace("\\%21".toRegex(), "!").replace("\\%27".toRegex(), "'")
            .replace("\\%28".toRegex(), "(").replace("\\%29".toRegex(), ")")
            .replace("\\%7E".toRegex(), "~")
    } catch (e: UnsupportedEncodingException) {
        this
    }
    return result ?: ""
}



fun String.convertStringToDateDDMMMYYYY(): String {
    val date = SimpleDateFormat("dd-MM-yyyy").parse(this)
    val format = SimpleDateFormat("dd-MMM-yyyy")
    return format.format(date)
}

fun String.convertSimpleDateFormat(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        android.icu.text.SimpleDateFormat(this, Locale.getDefault()).format(Date())
    } else {
        SimpleDateFormat(this, Locale.getDefault()).format(Date())
    }
}

fun String.toUrlUtf(): String {
    return try {
        URLEncoder.encode(this, "UTF-8")
    } catch (e: Throwable) {
        loge("","${e.message}")
        ""
    }
}

fun String.decodeUrlUTF(): String {
    return try {
        URLDecoder.decode(this, "UTF-8")
    } catch (e: Throwable) {
        loge("","${e.message}")
        ""
    }
}


fun String.getMD5(): String {
    return try {
        val md = MessageDigest.getInstance("MD5")
        val messageDigest = md.digest(this.toByteArray())
        val number = BigInteger(1, messageDigest)
        var hashtext = number.toString(16)
        // Now we need to zero pad it if you actually want the full 32 chars.
        while (hashtext.length < 32) {
            hashtext = "0$hashtext"
        }
        hashtext
    } catch (e: NoSuchAlgorithmException) {
        this
    }

}

fun String.getSHA1(): String {
    return try {
        val md = MessageDigest.getInstance("SHA-1")
        val messageDigest = md.digest(this.toByteArray())
        val number = BigInteger(1, messageDigest)
        var hashtext = number.toString(16)
        // Now we need to zero pad it if you actually want the full 32 chars.
        while (hashtext.length < 32) {
            hashtext = "0$hashtext"
        }
        hashtext
    } catch (e: NoSuchAlgorithmException) {
        this
    }
}

@SuppressLint("SimpleDateFormat")
fun String?.getDate(format: String, locale: Locale? = null): Date? {
    return try {
        val date = this ?: return null
        val dateFormat: SimpleDateFormat =
            locale?.let { SimpleDateFormat(format, locale) } ?: SimpleDateFormat(format)
        dateFormat.parse(date)
    } catch (e: Exception) {
        this?.loge("","${e.message}")
        null
    }
}

@SuppressLint("SimpleDateFormat")
fun convertDateYYYYMMDDtoMMDDYYYY(time: String): String? {
    val inputPattern = "yyyy-MM-dd"
    val outputPattern = "MM-dd-yyyy"
    val inputFormat = SimpleDateFormat(inputPattern)
    val outputFormat = SimpleDateFormat(outputPattern)
    try {
        val date = inputFormat.parse(time) ?: return null
        return outputFormat.format(date)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return null
}

fun String?.getQueryParamFromUrl(key: String): String? {
    this?.let {
        try {
            val videoUri = Uri.parse(it)
            return videoUri.getQueryParameter(key)
        } catch (e: java.lang.Exception) {
            loge("","${e.message}")
        }
    }
    return null
}

fun <T> String?.convertIntoModel(classRef: Class<T>): T? {
    return try {
        convertIntoModel(classRef = classRef, gson = this!!.provideGsonWithCoreJsonString())
    } catch (ex: java.lang.Exception) {
        ex.printStackTrace()
        null
    }
}

fun <T> String?.convertIntoModels(type: TypeToken<T>): T? {
    return try {
        this?.provideGsonWithCoreJsonString()?.fromJson(this, type.type)
    } catch (ex: java.lang.Exception) {
        ex.printStackTrace()
        null
    }
}


fun <T> String?.convertIntoModel(classRef: Class<T>, gson: Gson): T? {
    return try {
        gson.fromJson(this, classRef)
    } catch (ex: java.lang.Exception) {
        ex.printStackTrace()
        null
    }
}


fun <T> String?.convertIntoModels(type: TypeToken<T>, gson: Gson): T? {
    return try {
        gson.fromJson(this, type.type)
    } catch (ex: java.lang.Exception) {
        ex.printStackTrace()
        null
    }

}
fun String?.getContactNo(): String = this?.replace("[^0-9]".toRegex(), "") ?: this ?: "qwerty"

fun String.isValidPassword(): Boolean {
    val password = this.trim()
    val hasUpperCase = password.any { it.isUpperCase() }
    val hasLowerCase = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecialChar = password.any { !it.isLetterOrDigit() }
    return hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar
}

// Helper function to convert program time string to milliseconds.
fun String.provideTimeInMillis(): Long {
    return try {
        val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss Z")
        val zdt = ZonedDateTime.parse(this, formatter)
        zdt.toInstant().toEpochMilli()
    } catch (e: Exception) {
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            val zdt = ZonedDateTime.parse("$this+0000", DateTimeFormatter.ofPattern("yyyyMMddHHmmss Z"))
            zdt.toInstant().toEpochMilli()
        } catch (ex: Exception) {
            System.currentTimeMillis()
        }
    }
}
fun String.toTimestamp(): Long {
    // Define the formatter according to the input pattern.
    // "yyyyMMddHHmmss Z" expects a space before the timezone offset.
    val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss Z")
    // Parse the string into a ZonedDateTime and convert to epoch milliseconds.
    return ZonedDateTime.parse(this.trim(), formatter)
        .toInstant().toEpochMilli()
}


fun calculateProgramWidth(startTime:String, endTime:String,widthPerBlock: Dp = 50.dp): Dp {
    val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss Z")
    val startDateTime = ZonedDateTime.parse(startTime, formatter)
    val endDateTime = ZonedDateTime.parse(endTime, formatter)

    val durationInMinutes = Duration.between(startDateTime, endDateTime).toMinutes()
    val blocks = durationInMinutes / 30.0

    return (blocks.toFloat() * widthPerBlock.value).dp
}

fun calculateProgramsWidth(startTime:Long, endTime:Long,widthPerBlock: Dp = 50.dp): Dp {
    val durationMillis = endTime - startTime
    // Convert milliseconds to minutes
    val durationInMinutes = durationMillis / 60000.0
    // Calculate the number of 30-minute blocks (each block represents 30 minutes)
    val blocks = durationInMinutes / 30.0
    // Calculate and return the width based on the number of blocks
    return (blocks.toFloat() * widthPerBlock.value).dp
}


fun provideProgramWidth(startTime: String, endTime: String, widthPerBlock: Dp = 50.dp): Dp {
    val format = SimpleDateFormat("HH:mm", Locale.US)
    val start = format.parse(startTime)
    val end = format.parse(endTime)

    val durationInMinutes = (end.time - start.time) / (1000 * 60)
    val blocks = durationInMinutes / 30.0f

    return (blocks * widthPerBlock.value).dp
}


fun String.provideProgramTime(): Date {
    val format = SimpleDateFormat("HH:mm", Locale.US)
    return format.parse(this)
}

fun String.decodeJwtToken(): String? {
    return try {
        val parts = this.split(".") // JWT consists of header, payload, signature
        if (parts.size < 2) return null // Ensure it's a valid token

        val payload = String(Base64.decode(parts[1], Base64.URL_SAFE)) // Decode payload
        val jsonObject = JSONObject(payload)

        // Log full JWT payload for debugging
        Log.d("HASH", "Decoded Payload: $payload")


        // Extract phone number (fallback to `sub` if no phone field exists)
        jsonObject.optString("phone", jsonObject.optString("sub", null.toString())) //  Now extracts `sub`

    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Convert a date/time string to a formatted time "HH:mm:ss"
fun String.toFormattedTime(): String {
    val inputFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss Z")
    val outputFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    // Parse and then format the date/time
    return ZonedDateTime.parse(this.trim(), inputFormatter)
        .format(outputFormatter)
}

// Extension function to produce "HH:mm:ss-HH:mm:ss" from a start and an end time string.
fun String.formatStartEndTime(endTime: String): String {
    val startFormatted = this.toFormattedTime()
    val endFormatted = endTime.toFormattedTime()
    return "$startFormatted-$endFormatted"
}

fun Date.formatToCustom(): String {
    val formatter = SimpleDateFormat("hh:mma | dd MMM", Locale.ENGLISH)
    return formatter.format(this)
}

fun String.toBase64Encoded(): String {
    return Base64.encodeToString(this.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
}

fun String.toBase64UrlSafe(): String =
    Base64.encodeToString(
        this.toByteArray(Charsets.UTF_8),
        Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
    )


/**
 * Maps common API status codes to user‐friendly messages.
 */
fun String.toResponseMessage(): String = when (this) {
    "0"   -> "Login Successfully"
    "405" -> "Method Not Allowed"
    "401" -> "Unauthorized Data"
    "103" -> "Missing mandatory fields"
    "102" -> "Operation failed: Account is locked by Admin"
    "101" -> "Invalid Password"
    "100" -> "User Not Present on System"
    else -> "Unknown response code: $this"
}

/*
fun playerErrorHandling(errorCode: Int): Pair<Int, String> =
    when (errorCode) {
        400 -> 601 to "Bad request - Bad input to server"
        403 -> 602 to "Forbidden - user does not have permission, or invalid login data"
        404 -> 603 to "This channel is temporarily unavailable.We apologize for the inconvenience.Please check back later or contact your service provider for assistance."
        429 -> 604 to "Too many request– max concurrent streams reached"
        451 -> 605 to "Unavailable for legal reasons– geo blocking"
        500 -> 606 to "Internal error"
        2001 -> 607 to "Source error"
        6004 -> 608 to "You are not authorized to view this content.\nPlease contact your service provider for assistance."
        4001 -> 609 to "This channel is temporarily unavailable.\nWe apologize for the inconvenience.Please check back later or contact your service provider for assistance."
        2000 -> 610 to "This channel is temporarily unavailable.We apologize for the inconvenience. Please check back later or contact your service provider for assistance."
        else -> errorCode to "An unknown error occurred"
    }*/


// from Pair<Int,String> to Triple<Int,String,String>
fun playerErrorHandling(errorCode: Int): Triple<Int, String, String> =
    when (errorCode) {
        400  -> Triple(601,
            "Bad request",
            "Bad input to server")
        403  -> Triple(602,
            "Forbidden",
            "User does not have permission, or invalid login data")
        404  -> Triple(603,
            "This channel is temporarily unavailable.",
            "We apologize for the inconvenience. Please check back later or contact your service provider for assistance.")
        429  -> Triple(604,
            "Too many requests",
            "Max concurrent streams reached")
        451  -> Triple(605,
            "Unavailable for legal reasons",
            "Geo-blocking")
        500  -> Triple(606,
            "Internal error",
            "An internal server error occurred")
        2001 -> Triple(607,
            "Source error",
            "The media source could not be loaded")
        6004 -> Triple(608,
            "DRM licence server request failed",
            "Please try again later.")
        4001 -> Triple(609,
            "This channel is temporarily unavailable.",
            "We apologize for the inconvenience. Please check back later or contact your service provider for assistance.")
        2000 -> Triple(610,
            "This channel is temporarily unavailable.",
            "We apologize for the inconvenience. Please check back later or contact your service provider for assistance.")
        6006 -> Triple(611,
            "License Error",
            "We apologize for the inconvenience. Please check back later or contact your service provider for assistance.")
        2002 -> Triple(612,
            "Timeout Error",
            "We apologize for the inconvenience. Please check back later or contact your service provider for assistance.")
        3003 -> Triple(613,
            "Video Source Error",
            "Please try again later.")
        else -> Triple(errorCode,
            "Unknown error",
            "An unknown error occurred")
    }


/**
 * Maps HTTP, NGINX, and Cloudflare status/error *[errorCode]*s to a
 * Triple of **internal code**, **title**, and **human‑readable description** that can be
 * displayed in the player UI.
 *
 * The internal codes here start at **701** to stay clear of the 6xx range already used by
 */
fun serverErrorHandling(errorCode: Int): Triple<Int, String, String> = when (errorCode) {
    204 -> Triple(701, "No Content", "The server processed the request but returned no content.")
    205 -> Triple(702, "Reset Content", "Please refresh or reset the view and try again.")
    400 -> Triple(703, "Bad Request", "The request was malformed or invalid.")
    401 -> Triple(704, "Unauthorized", "Authentication failed or is missing.")
    403 -> Triple(705, "Forbidden", "You don't have permission to access this resource.")
    404 -> Triple(706, "Not Found", "The requested resource couldn't be located.")
    405 -> Triple(707, "Method Not Allowed", "This request method isn't supported for the resource.")
    408 -> Triple(708, "Request Timeout", "The server timed out waiting for the request.")
    500 -> Triple(709, "Internal Server Error", "The server encountered an unexpected condition.")
    502 -> Triple(710, "Bad Gateway", "Invalid response from an upstream server.")
    503 -> Triple(711, "Service Unavailable", "The server is temporarily unable to handle the request.")
    504 -> Triple(712, "Gateway Timeout", "No timely response from an upstream server.")
    // NGINX‑specific codes
    444 -> Triple(713, "No Response", "The server closed the connection without a response.")
    495 -> Triple(714, "SSL Certificate Error", "The client certificate is invalid.")
    496 -> Triple(715, "SSL Certificate Required", "A valid client certificate is required.")
    // Cloudflare codes
    520 -> Triple(716, "Unknown Error", "Unexpected response from the origin server.")
    521 -> Triple(717, "Web Server Down", "The origin server refused connections.")
    522 -> Triple(718, "Connection Timed Out", "Cloudflare couldn't reach the origin server in time.")
    523 -> Triple(719, "Origin Unreachable", "The origin server could not be reached.")
    524 -> Triple(720, "Timeout Occurred", "The origin didn't send a timely HTTP response.")
    525 -> Triple(721, "SSL Handshake Failed", "Cloudflare couldn't negotiate SSL/TLS with the origin server.")
    526 -> Triple(722, "Invalid SSL Certificate", "The origin's SSL certificate is invalid.")
    530 -> Triple(723, "Invalid Hostname", "Cloudflare couldn't resolve the origin hostname.")
    else -> Triple(errorCode, "Unknown Server Error", "An unrecognized error occurred (code $errorCode).")
}

// Usage:
val wifiMac = "wlan0".getMacAddress()      // Wi‑Fi
val ethMac  = "eth0".getMacAddress()       // Ethernet
fun String.getMacAddress(): String? {
    return try {
        val nif = NetworkInterface.getByName(this) ?: return null
        val macBytes = nif.hardwareAddress ?: return null
        macBytes.joinToString(separator = ":") { byte ->
            String.format("%02X", byte)
        }
    } catch (e: Exception) {
        null
    }
}
//provide interfaceName like "wlan0"/"eth0" to get specific macAddress
fun String.readMacFromSysfs(): String? {
    return try {
        val path = "/sys/class/net/$this/address"
        val mac = File(path).readText().toUpperCase(Locale.ROOT).substring(0, 17)
        mac.ifEmpty { null }
    } catch (e: Exception) {
        null
    }
}


/*
private fun getMacAddress(): String? {
    try {
        return loadFileAsString("/sys/class/net/eth0/address").toUpperCase().substring(0, 17)
    } catch (e: IOException) {
        e.printStackTrace()
        return null
    }
}*/



/**
 * Reads the entire contents of this String (treated as a file path) into a String,
 * or throws IOException if it fails.
 */
@Throws(IOException::class)
private fun String.readFile(): String =
    File(this).bufferedReader().use { it.readText() }

/**
 * Attempts to read the MAC address from the given interface path,
 * e.g. "/sys/class/net/eth0/address" or "/sys/class/net/wlan0/address".
 *
 * @receiver the interface path to read
 * @return the MAC address in upper‑case "XX:XX:XX:XX:XX:XX" format, or null if it fails
 */
fun String.macAddress(): String? = try {
    this
        .readFile()
        .trim()                    // remove newline
        .uppercase()               // upper‑case
        .takeIf { it.length >= 17 }
        ?.substring(0, 17)         // "XX:XX:XX:XX:XX:XX"
} catch (e: IOException) {
    e.printStackTrace()
    null
}


fun String.isCurrentTimeAfter(): Boolean {
    val format = SimpleDateFormat("HH:mm", Locale.US)
    val now = Calendar.getInstance()
    val target = Calendar.getInstance()

    // Parse "16:20" and set to today
    val parsed = format.parse(this)
    target.time = parsed ?: return false

    // Set target to today's date + parsed time
    target.set(Calendar.YEAR, now.get(Calendar.YEAR))
    target.set(Calendar.MONTH, now.get(Calendar.MONTH))
    target.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))

    return now.after(target) || now.before(target)
}
fun currentProgramFilter(start:String?,end:String?): Boolean {
    val format = SimpleDateFormat("HH:mm", Locale.US)
    val now = Calendar.getInstance()
    val target = Calendar.getInstance()

    // Parse "16:20" and set to today
    val parsed = format.parse(start)
    target.time = parsed ?: return false

    // Set target to today's date + parsed time
    target.set(Calendar.YEAR, now.get(Calendar.YEAR))
    target.set(Calendar.MONTH, now.get(Calendar.MONTH))
    target.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))

    return target.before(now)

   // (start <= now && now < end) || (now < start)
}

/**
 * Capitalizes the very first character of this string, lower-cases all the rest.
 * If the string is empty, returns it unchanged.
 */
fun String.capitalizeFirstLetter(): String =
    this.lowercase(Locale.getDefault())
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }