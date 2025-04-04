package com.example.tvapp.utils

import com.example.tvapp.model.data.epgdata.EPGDataItem
import com.example.tvapp.model.data.genre.WTVGenre
import com.example.tvapp.model.data.language.WTVLanguage
import com.example.tvapp.model.data.manifest.WTVManifest

object Constants {
    var manifest:WTVManifest? =null
    var genreList:List<WTVGenre>? =null
    var languageList:List<WTVLanguage>? =null
    var epgItemList:List<EPGDataItem> = arrayListOf()
    const val BASE_URL_API_1 = "https://nextwave.waveiontechnologies.com:5000/api/tabs"
    const val BASE_URL_API_2 = "https://cpaas.messagecentral.com/"
    const val AUTH_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJDLUE2OTBBODkwNDVCODRFOCIsImlhdCI6MTczOTI1MzI4NCwiZXhwIjoxODk2OTMzMjg0fQ.Zudk_A33gyu_raBnzrR0nGFkvYsQPiiuM_lSR3Hr0-0OCHoONeMlIffZResiQlmPbPF0LSsc_fKuJKVqszoRNQ"
}