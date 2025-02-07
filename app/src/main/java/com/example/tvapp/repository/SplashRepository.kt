package com.example.tvapp.repository

import com.example.tvapp.api.ApiService2
import com.example.tvapp.models.SplashApiResponse
import javax.inject.Inject

class SplashRepository @Inject constructor(private val apiService: ApiService2) {
    suspend fun getLogo(): SplashApiResponse {
        return apiService.getLogo()
    }
}