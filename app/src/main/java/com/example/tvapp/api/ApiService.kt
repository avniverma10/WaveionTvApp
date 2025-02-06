package com.example.tvapp.api

import com.example.applicationscreens.models.ContentResponse
import com.example.tvapp.models.SplashApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @GET("android/content")
    suspend fun getVideoContent(): ContentResponse

    @GET("android/logo_path")
    suspend fun getLogo(): SplashApiResponse

//    @POST("auth/api-login")
//    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
//
//    @POST("auth/api-signup")
//    suspend fun signUp(@Body signupRequest: SignUpRequest): Response<LoginResponse>
}
