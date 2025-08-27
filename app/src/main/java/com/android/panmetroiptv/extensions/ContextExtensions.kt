package com.android.panmetroiptv.extensions

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.datastore.preferences.preferencesDataStore
import com.android.panmetroiptv.di.CoreComponentProvider
import com.android.panmetroiptv.model.data.epgdata.EPGDataItem
import com.android.panmetroiptv.model.data.genre.WTVGenre
import com.android.panmetroiptv.model.data.language.WTVLanguage
import com.android.panmetroiptv.model.data.manifest.WTVManifest
import com.android.panmetroiptv.model.home.WTVHomeCategory
import java.io.File


val Context.dataStore by preferencesDataStore(name = "user_prefs")



fun Context.coreEPGLiveData() =
    (applicationContext as? CoreComponentProvider)?.provideEPGLiveData()
        ?: throw IllegalStateException("EPG is null: $applicationContext")


fun Context.provideMacAddrLiveData() =
    (applicationContext as? CoreComponentProvider)?.provideMacAddr()
        ?: throw IllegalStateException("provideMacAddr is null: $applicationContext")


fun Context.applyEPGData(data: List<EPGDataItem>) =
    (applicationContext as? CoreComponentProvider)?.initializeEPGData(data)


fun Context.appManifestLiveData() =
    (applicationContext as? CoreComponentProvider)?.provideAppManifestLiveData()
        ?: throw IllegalStateException("Manifest is null: $applicationContext")


fun Context.applyAppManifest(data: WTVManifest) =
    (applicationContext as? CoreComponentProvider)?.initializeAppManifest(data)


fun Context.appPkgChannelsLiveData() =
    (applicationContext as? CoreComponentProvider)?.providePkgChannel()
        ?: throw IllegalStateException("appPkgChannelsLiveData is null: $applicationContext")


fun Context.updatePkgChannels(pkg:String,channels: MutableSet<String>) =
    (applicationContext as? CoreComponentProvider)?.updatePkgChannel(pkg,channels)

fun Context.isUserBlockedLiveData() =
    (applicationContext as? CoreComponentProvider)?.provideIsUserBlocked()
        ?: throw IllegalStateException("appIsUserBlockedLiveData is null: $applicationContext")


fun Context.updatesUserBlocked(username:String,isUserBlocked: Boolean) =
    (applicationContext as? CoreComponentProvider)?.updateUserBlocked(username,isUserBlocked)


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
    } catch (e: java.lang.Exception) {
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
    } catch (ex: java.lang.Exception) {
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