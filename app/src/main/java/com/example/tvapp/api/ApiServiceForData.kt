package com.example.tvapp.api

import com.example.tvapp.models.ContentResponse
import com.example.tvapp.models.EpgFile
import com.example.tvapp.models.Banner
import com.example.tvapp.models.SplashApiResponse
import com.example.tvapp.models.Tab
import kotlinx.coroutines.flow.Flow
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming

interface ApiServiceForData {

    @GET("android/content")
    suspend fun getVideoContent(): ContentResponse

    @GET("android/logo_path")
    suspend fun getLogo(): SplashApiResponse

    @GET("epg-files/join-epg-content")
    suspend fun getEpgFiles(): List<EpgFile>

    @GET("tabs")
    suspend fun getTabs(): List<Tab>

    @GET("banners")
    suspend fun getBanners(): List<Banner>

    //TODO: currently we are not using this because are using live update, we need to impl. this here later.
    @Streaming
    @GET("tabs/sse-tabs")
    fun streamTabs(): Flow<ResponseBody>

}