package com.caastv.tvapp.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.caastv.tvapp.utils.Constants
import com.caastv.tvapp.utils.crash.logs.CrashLogger

@SuppressLint("LogNotTimber")
fun Any.loge(tag: String = "", value: String?) {
    if (Constants.BUILD_TYPE.equals("release")) return
    val customTag = if (tag.isNotEmpty()) tag else this.javaClass.simpleName
    val messageToDisplay = value ?: "empty message"
    //loge(customTag, if (tag.isNotEmpty()) "${this.javaClass.simpleName} >> $messageToDisplay" else messageToDisplay)
    Log.e(customTag, if (tag.isNotEmpty()) "${this.javaClass.simpleName} >> $messageToDisplay" else messageToDisplay)
}



fun logException(context: Context, throwable: Throwable, additionalInfo: String = "") {
    try {
        val crashLogger = CrashLogger(context)
        val enhancedThrowable = EnhancedThrowable(throwable, additionalInfo)
        crashLogger.logCrash(enhancedThrowable)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

class EnhancedThrowable(
    private val original: Throwable,
    private val additionalInfo: String
) : Throwable(original.message, original) {
    override fun toString(): String {
        return "${super.toString()}\nAdditional Info: $additionalInfo"
    }
}