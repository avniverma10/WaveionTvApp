package com.android.panmetroiptv.utils

import com.android.panmetroiptv.model.data.genre.WTVGenre

object Constants {
    var genre:List<WTVGenre>?= null
    const val BUILD_TYPE = "release"//"debug"//
    const val HEADER_TOKEN = "BUAA8JJkzfMI56y4BhEhU"
    const val DRM_LICENSE_BASE = "https://drm.panmetroconvergence.com:4443"//"https://10.22.254.46:4443"//
    const val LOGIN_SMS_BASE = "https://10.22.254.46:9443/"//"https://iptvtest.panmetro.in/"//
    var BASE_URL = "http://10.22.254.46:7443/api/"//
}
