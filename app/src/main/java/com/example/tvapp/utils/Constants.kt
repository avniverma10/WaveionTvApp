package com.example.tvapp.utils

import com.example.tvapp.model.data.genre.WTVGenre

object Constants {
    var genre:List<WTVGenre>?= null

    const val HEADER_TOKEN = "BUAA8JJkzfMI56y4BhEhU"
    const val DEV_BASE_URL = "https://api-dev.caastv.com/api/"//"http://192.168.1.4:3001/api/"
    const val BASE_URL = "https://api-demo.caastv.com/api/"//"http://192.168.1.4:3001/api/"
    const val BASE_URL_API_1 = "https://api-demo.caastv.com/api/tabs"
    const val BASE_URL_API_2 = "https://cpaas.messagecentral.com/"
    const val AUTH_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJDLUE2OTBBODkwNDVCODRFOCIsImlhdCI6MTczOTI1MzI4NCwiZXhwIjoxODk2OTMzMjg0fQ.Zudk_A33gyu_raBnzrR0nGFkvYsQPiiuM_lSR3Hr0-0OCHoONeMlIffZResiQlmPbPF0LSsc_fKuJKVqszoRNQ"


    fun provideBaseUrl(isDevEnable: Boolean?=false): String{
          if (isDevEnable == true)
              return DEV_BASE_URL
          else
              return BASE_URL
    }

}