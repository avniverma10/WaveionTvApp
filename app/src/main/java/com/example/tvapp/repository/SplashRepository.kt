package com.example.tvapp.repository

import com.example.tvapp.api.ApiServiceForData
import com.example.tvapp.models.SplashApiResponse
import javax.inject.Inject

class SplashRepository @Inject constructor(private val apiService: ApiServiceForData) {
    suspend fun getLogo(): SplashApiResponse {
        return apiService.getLogo()
    }
}