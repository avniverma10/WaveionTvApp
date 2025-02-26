package com.example.tvapp.api

import com.example.applicationscreens.models.ContentResponse
import com.example.tvapp.models.EpgFile
import com.example.tvapp.models.SplashApiResponse
import com.example.tvapp.models.Tab
import retrofit2.http.GET

interface ApiServiceForData {

    @GET("android/content")
    suspend fun getVideoContent(): ContentResponse

    @GET("android/logo_path")
    suspend fun getLogo(): SplashApiResponse

    @GET("epg-files/match-epg-content")
    suspend fun getEpgFiles(): List<EpgFile>

    @GET("tabs")
    suspend fun getTabs(): List<Tab>

}