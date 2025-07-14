package com.example.tvapp.utils.network

import okhttp3.HttpUrl

object UrlManager {
    private val primaryBaseUrl = "https://api-demo.caastv.com/api/"//https://api-demo.caastv.com/api/"
    private val alternateBaseUrl = "https://api-demo.caastv.com/api/"

    @Volatile
    private var currentBaseUrl = primaryBaseUrl

    fun getCurrentBaseUrl(): String = currentBaseUrl

    fun shouldSwitchUrl(responseCode: Int): Boolean {
        return responseCode in listOf(
            204, 205, 400, 401, 403, 404, 405, 408,
            444, 495, 496, 500, 502, 503, 504,
            520, 521, 522, 523, 524, 525, 526, 530
        )
    }

    fun switchBaseUrl() {
        currentBaseUrl = if (currentBaseUrl == primaryBaseUrl) alternateBaseUrl else primaryBaseUrl
    }

    fun replaceBaseUrl(originalUrl: HttpUrl): String {
        val oldBaseUrl = originalUrl.scheme + "://" + originalUrl.host
        return originalUrl.toString().replace(oldBaseUrl, currentBaseUrl)
    }
}