package com.example.tvapp.extensions

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.example.tvapp.di.CoreComponentProvider
import com.example.tvapp.model.data.epgdata.EPGDataItem
import com.example.tvapp.model.data.genre.WTVGenre
import com.example.tvapp.model.data.language.WTVLanguage
import com.example.tvapp.model.data.manifest.WTVManifest
import com.example.tvapp.model.home.WTVHomeCategory


fun Context.coreEPGLiveData() =
    (applicationContext as? CoreComponentProvider)?.provideEPGLiveData()
        ?: throw IllegalStateException("EPG is null: $applicationContext")


fun Context.applyEPGData(data: List<EPGDataItem>) =
    (applicationContext as? CoreComponentProvider)?.initializeEPGData(data)


fun Context.appManifestLiveData() =
    (applicationContext as? CoreComponentProvider)?.provideAppManifestLiveData()
        ?: throw IllegalStateException("Manifest is null: $applicationContext")


fun Context.applyAppManifest(data: WTVManifest) =
    (applicationContext as? CoreComponentProvider)?.initializeAppManifest(data)

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
            Log.e("",e.message?:"")
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
