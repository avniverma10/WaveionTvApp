package com.example.tvapp.extensions

import android.annotation.SuppressLint
import android.util.Log
import com.android.panmetroiptv.BuildConfig
import com.example.tvapp.utils.Constants

@SuppressLint("LogNotTimber")
fun Any.loge(tag: String = "", value: String?) {
    if (Constants.BUILD_TYPE.equals("release")) return
    //if (BuildConfig.BUILD_TYPE.equals("release")) return
    val customTag = if (tag.isNotEmpty()) tag else this.javaClass.simpleName
    val messageToDisplay = value ?: "empty message"
    Log.d(customTag, if (tag.isNotEmpty()) "${this.javaClass.simpleName} >> $messageToDisplay" else messageToDisplay)
}

@SuppressLint("LogNotTimber")
fun Any.logd(tag: String = "", value: String?) {
    if (BuildConfig.BUILD_TYPE.equals("release",true)) return
    val customTag = if (tag.isNotEmpty()) tag else this.javaClass.simpleName
    val messageToDisplay = value ?: "empty message"
    Log.d(customTag, if (tag.isNotEmpty()) "${this.javaClass.simpleName} >> $messageToDisplay" else messageToDisplay)
}

@SuppressLint("LogNotTimber")
fun Any.logi(tag: String = "", value: String?) {
    if (BuildConfig.BUILD_TYPE.equals("release",true)) return
    val customTag = if (tag.isNotEmpty()) tag else this.javaClass.simpleName
    val messageToDisplay = value ?: "empty message"
    Log.i(customTag, if (tag.isNotEmpty()) "${this.javaClass.simpleName} >> $messageToDisplay" else messageToDisplay)
}