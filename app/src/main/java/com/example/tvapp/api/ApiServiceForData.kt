package com.example.tvapp.api

import com.example.applicationscreens.models.ContentResponse
import com.example.tvapp.models.Banner
import com.example.tvapp.models.SplashApiResponse
import retrofit2.http.GET

interface ApiServiceForData {

    @GET("android/content")
    suspend fun getVideoContent(): ContentResponse

    @GET("android/logo_path")
    suspend fun getLogo(): SplashApiResponse

    @GET("banners")
    suspend fun getBanners(): List<Banner>

}