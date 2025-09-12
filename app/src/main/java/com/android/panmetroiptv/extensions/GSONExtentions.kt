package com.android.panmetroiptv.extensions

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Add this extension function
inline fun <reified T> Gson.fromJson(json: String): T? {
    return try {
        fromJson(json, object : TypeToken<T>() {}.type)
    } catch (e: Exception) {
        null
    }
}