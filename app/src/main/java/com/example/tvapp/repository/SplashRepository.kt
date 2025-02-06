package com.example.tvapp.repository

import android.util.Log
import com.example.applicationscreens.models.ContentResponse
import com.example.tvapp.api.ApiService
import com.example.tvapp.models.SplashApiResponse
import javax.inject.Inject

class SplashRepository @Inject constructor(private val apiService: ApiService) {
    suspend fun getLogo(): SplashApiResponse {
        return apiService.getLogo()
    }
}