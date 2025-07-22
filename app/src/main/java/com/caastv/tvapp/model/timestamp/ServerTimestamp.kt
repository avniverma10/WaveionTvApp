
package com.caastv.tvapp.model.timestamp

import com.caastv.tvapp.extensions.loge
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class ServerTimeStamp(
    val status: String,
    val timestamp: String
){
    fun provideTimeStampValue(timestamp: String?): Long? {
        return try {
            // 1) Build a formatter matching your input
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            // 2) Parse into a Date
            val date: Date? = sdf.parse(timestamp)
            // 3) Return millis, or null if parse failed
            date?.time
        } catch (e: ParseException) {
            loge("provideTimeStampValue", "Failed to parse timestamp: $timestamp ${e.message}")
            null
        }
    }

    private fun parseIsoUtc(iso: String): Long {
        // SimpleDateFormat approach
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
        return sdf.parse(iso)!!.time
    }
}