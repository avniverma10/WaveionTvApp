package com.example.tvapp.utils

import com.example.tvapp.model.data.genre.WTVGenre

object Constants {
    var genre:List<WTVGenre>?= null
    const val BUILD_TYPE = "release"//"debug"//
    const val HEADER_TOKEN = "BUAA8JJkzfMI56y4BhEhU"
    const val DRM_LICENSE_BASE = "https://172.23.140.4:4443"//"https://drm.panmetroconvergence.com:4443/"//
    const val LOGIN_SMS_BASE = "https://172.23.140.4:9443/"//"https://iptvtest.panmetro.in"//
    var BASE_URL = "https://172.23.140.4:7443/api/"//"https://api-panmetro.caastv.com/api/"//
    var isServerRunning = false
}
