package com.caastv.tvapp.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.datastore.preferences.preferencesDataStore
import com.caastv.tvapp.di.CoreComponentProvider
import com.caastv.tvapp.model.data.customapp.InventoryApp
import com.caastv.tvapp.model.data.epgdata.EPGDataItem
import com.caastv.tvapp.model.data.genre.WTVGenre
import com.caastv.tvapp.model.data.language.WTVLanguage
import com.caastv.tvapp.model.data.login.LoginResponseData
import com.caastv.tvapp.model.data.manifest.WTVManifest
import com.caastv.tvapp.model.home.WTVHomeCategory
import java.io.File
import java.lang.Exception


val Context.dataStore by preferencesDataStore(name = "user_prefs")



fun Context.coreEPGLiveData() =
    (applicationContext as? CoreComponentProvider)?.provideEPGLiveData()
        ?: throw IllegalStateException("EPG is null: $applicationContext")


fun Context.provideMacAddrLiveData() =
    (applicationContext as? CoreComponentProvider)?.provideMacAddr()
        ?: throw IllegalStateException("provideMacAddr is null: $applicationContext")


fun Context.applyEPGData(data: List<EPGDataItem>) =
    (applicationContext as? CoreComponentProvider)?.initializeEPGData(data)

/*
fun Context.appManifestLiveData() =
    (applicationContext as? CoreComponentProvider)?.provideAppManifestLiveData()
        ?: throw IllegalStateException("Manifest is null: $applicationContext")*/


fun Context.applyAppManifest(data: WTVManifest) =
    (applicationContext as? CoreComponentProvider)?.initializeAppManifest(data)

fun Context.userInfo() =
    (applicationContext as? CoreComponentProvider)?.provideUserInfo()
        ?: throw IllegalStateException("userInfo is null: $applicationContext")


fun Context.applyUserInfo(data: LoginResponseData) =
    (applicationContext as? CoreComponentProvider)?.initializeUserInfo(data)

fun Context.appGenreLiveData() =
    (applicationContext as? CoreComponentProvider)?.provideGenreLiveData()
        ?: throw IllegalStateException("Manifest is null: $applicationContext")


fun Context.applyAppGenre(data: List<WTVGenre>) =
    (applicationContext as? CoreComponentProvider)?.initializeGenre(data)


fun Context.appHomeLiveData() =
    (applicationContext as? CoreComponentProvider)?.provideHomeLiveData()
        ?: throw IllegalStateException("Manifest is null: $applicationContext")


fun Context.applyAppHome(data: List<WTVHomeCategory>) =
    (applicationContext as? CoreComponentProvider)?.initializeHome(data)

fun Context.applyAppInventoryApp(data: List<InventoryApp>) =
    (applicationContext as? CoreComponentProvider)?.provideInventoryApps(data)

fun Context.appLanguageLiveData() =
    (applicationContext as? CoreComponentProvider)?.provideLanguageLiveData()
        ?: throw IllegalStateException("Manifest is null: $applicationContext")


fun Context.applyAppLanguage(data: List<WTVLanguage>) =
    (applicationContext as? CoreComponentProvider)?.initializeLanguage(data)


fun Context.isInternetOn(): Boolean {
    val connectivityManager: ConnectivityManager =
        getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    if (Build.VERSION.SDK_INT >= 29) {
        val capabilities: NetworkCapabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                ?: return false
        when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> return true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> return true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> return true
        }
    } else {
        try {
            val activeNetworkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            return activeNetworkInfo?.isConnected ?: false
        } catch (e: Throwable) {
            loge("",e.message?:"")
        }
    }
    return false
}

fun Context.dpToPx(dp: Int = 16): Int {
    return (dp * resources.displayMetrics.density).toInt()
}

fun Context?.showToastL(message: String?) {
    this?.let { context ->
        message?.let { text ->
            Toast.makeText(context, text, Toast.LENGTH_LONG).show()
        }
    }
}

fun Context?.showToastS(message: String?) {
    this?.let { context ->
        message?.let { text ->
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }
    }
}

fun Context?.showToastServer(
    message: String?,
    duration: Int = Toast.LENGTH_LONG // Longer duration for server errors
) {
    this?.let { context ->
        message?.let { text ->
            // Create styled message with red dot and error code
            val styledMessage = SpannableStringBuilder().apply {
                // Add red dot (Unicode + color)
                append("● ") // Red dot symbol
                setSpan(
                    ForegroundColorSpan(android.graphics.Color.RED),
                    0, 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                // Add error code if provided
                /*errorCode?.let {
                    append("$it | ")
                    setSpan(
                        ForegroundColorSpan(android.graphics.Color.RED),
                        2, 2 + it.length + 2,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }*/

                // Add main message (white)
                append(text)
            }

            val toast = Toast.makeText(context, styledMessage, duration)
            // Customize toast appearance
            toast.apply {
                // Position (default to top-right)
                setGravity(
                    Gravity.TOP or Gravity.RIGHT,
                    0,
                    dpToPx(50)
                )

                view?.let { view ->
                    // Dark semi-transparent background
                    view.setBackgroundColor(android.graphics.Color.parseColor("#99000000"))

                    // TextView styling
                    view.findViewById<TextView>(android.R.id.message)?.apply {
                        setTextColor(android.graphics.Color.WHITE)
                        textSize = 14f
                        setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8))
                        gravity = Gravity.CENTER_VERTICAL
                    }
                }
                show()
            }
        }
    }
}



fun Context.showTopRightToast(
    message: String,
    duration: Int = Toast.LENGTH_SHORT,
    xOffset: Int = 16,    // in dp
    yOffset: Int = 50      // in dp
) {
    val toast = Toast.makeText(this, message, duration)

    // Position the toast (top-right)
    toast.setGravity(
        Gravity.TOP or Gravity.END,
        dpToPx(xOffset),   // Convert dp to pixels
        dpToPx(yOffset)    // Convert dp to pixels
    )

    // Customize toast appearance
    toast.view?.apply {
        setBackgroundColor(android.graphics.Color.parseColor("#CC1A1A1A")) // Semi-transparent dark background

        findViewById<TextView>(android.R.id.message)?.apply {
            setTextColor(android.graphics.Color.WHITE)
            textSize = 14f
            setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8))
        }
    }

    toast.show()
}

/**
 * Formats time in milliseconds to hh:mm:ss string format.
 */
fun Int.formatMillis(): String {
    var millis = this
    var result = ""
    val hr = millis / 3600000
    millis %= 3600000
    val min = millis / 60000
    millis %= 60000
    val sec = millis / 1000
    if (hr > 0) {
        result += "$hr:"
    }
    if (min >= 0) {
        result += if (min > 9) {
            "$min:"
        } else {
            "0$min:"
        }
    }
    if (sec > 9) {
        result += sec
    } else {
        result += "0$sec"
    }
    return result
}

fun Context.showSimpleDialog(title: String?, message: String?) {
    val builder = AlertDialog.Builder(this)
    builder.setTitle(title)
    builder.setMessage(message)
    builder.setPositiveButton("OK", null)
    val dialog: Dialog = builder.create()
    dialog.show()
}

@SuppressLint("HardwareIds")
fun Context.findMyDeviceId(): String? {
    try {
        return Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )
    } catch (e: Exception) {
        return ""
    }
}


// Extension function for Activity context
fun Activity.hideKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    currentFocus?.let { view ->
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}


fun Context.createFile(extension: String=".apk"): File {
    val storageDir = this.filesDir
    return File.createTempFile("FILE_${System.currentTimeMillis()}_", ".${extension}", storageDir)
}


fun Context.provideFileFromUri(uri: Uri?): File? {
    if (uri == null) return null
    try {
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val file = createFile()
        inputStream.copyTo(file.outputStream())
        inputStream.close()
        return file
    } catch (ex: Exception) {
        return null
    }
}

/**
 * Hides the software keyboard if any view in the current Activity has focus.
 */
fun Context.hideKeyboard() {
    // Try to get the InputMethodManager
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        ?: return

    // Find the currently focused view, or use a fallback token
    val windowToken = (this as? Activity)
        ?.currentFocus
        ?.windowToken
    // fallback to the window token of the Activity's root view
        ?: (this as? Activity)
            ?.window
            ?.decorView
            ?.rootView
            ?.windowToken

    // If we have a valid token, request the keyboard to hide
    windowToken?.let { token ->
        imm.hideSoftInputFromWindow(token, 0)
    }
}

/*

@Composable
fun Context.provideGif(fileName:String, type:String):Painter?{
   return when(type) {
        "url" -> rememberAsyncImagePainter(
            model = ImageRequest.Builder(this)
                .data(fileName)
                .decoderFactory(GifDecoder.Factory())
                .listener(
                    onError = { _, result ->
                        // onGifLoadingFailed(result.throwable)
                    }
                )
                .build(),
            placeholder = painterResource(R.drawable.caastv_icon_foreground),
            error = painterResource(R.drawable.caastv_icon_foreground)
        )

        "drawable" -> painterResource(id = gifResId)

        "asset" -> rememberAsyncImagePainter(
            model = ImageRequest.Builder(this)
                .data("file:///android_asset/$fileName")
                .decoderFactory(GifDecoder.Factory())
                .listener(
                    onError = { _, result ->
                        // onGifLoadingFailed(result.throwable)
                    }
                )
                .build()
        )

        else -> null
    }
}*/


/**
 * Launches the given package if it is installed and has a launcher activity.
 *
 * @return true if we launched it, false if the app isn’t present / launchable.
 */
fun Context.launchPackageIfInstalled(packageName: String): Boolean {
    val pm: PackageManager = packageManager

    // ── Option A: quickest attempt — returns null if not installed *or* not visible
    val launchIntent: Intent? = pm.getLaunchIntentForPackage(packageName)

    if (launchIntent != null) {
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(launchIntent)
        return true
    }

    // ── Option B: double-check with PackageManager in case the app simply has no LAUNCHER
    val installed = try {
        if (Build.VERSION.SDK_INT >= 33) {
            pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(packageName, 0)
        }
        true   // no NameNotFoundException → package exists
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }

    // You might show a toast/dialog if !installed
    return false
}

