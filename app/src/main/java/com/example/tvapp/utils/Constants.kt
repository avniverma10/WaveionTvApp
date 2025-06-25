package com.example.tvapp.utils

import com.example.tvapp.model.data.genre.WTVGenre

object Constants {
    var genre:List<WTVGenre>?= null

    const val BUILD_TYPE = "debug"//"release"//
    const val HEADER_TOKEN = "BUAA8JJkzfMI56y4BhEhU"
    const val DEV_BASE_URL = "https://api-dev.caastv.com/api/"
    var BASE_URL = "https://api-panmetro.caastv.com/api/"
    var isServerRunning = false

    fun provideBaseUrl(isDevEnable: Boolean?=false): String{
          if (isDevEnable == true)
              return DEV_BASE_URL
          else
              return BASE_URL
    }


    fun applyBaseUrl(isMainServerRunning: Boolean=true){
        if(!isMainServerRunning){
            BASE_URL = "https://api-panmetro.caastv.com/api/"
        }else{
            BASE_URL = "https://api-panmetro.caastv.com/api/1"
        }

    }

}