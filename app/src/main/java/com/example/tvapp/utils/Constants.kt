package com.example.tvapp.utils

import com.example.tvapp.model.data.genre.WTVGenre

object Constants {
    var genre:List<WTVGenre>?= null
    var isDevEnable: Boolean?=false

    const val BUILD_TYPE = "debug"//"release"
    const val HEADER_TOKEN = "BUAA8JJkzfMI56y4BhEhU"
    const val DEV_BASE_URL = "https://api-dev.caastv.com/api/"//"http://192.168.1.4:3001/api/"
    const val BASE_ICON_URL = "https://api-demo.caastv.com/"

}