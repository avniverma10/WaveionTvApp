package com.example.tvapp.repository

import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.example.applicationscreens.models.ContentResponse
import com.example.tvapp.api.ApiService2
import javax.inject.Inject

class ExoPlayerRepository @Inject constructor(private val apiService: ApiService2) {

    @UnstableApi
    suspend fun getVideoContent(): ContentResponse {
        Log.d("AVNI","1. response  in repo --> ${apiService.getVideoContent()}")
        return apiService.getVideoContent()
    }
}