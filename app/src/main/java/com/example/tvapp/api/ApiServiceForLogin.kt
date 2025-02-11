package com.example.tvapp.api

import com.example.tvapp.models.SendOTPRequest
import com.example.tvapp.models.ValidateOtpRequest
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

import retrofit2.http.POST
import retrofit2.http.Query

interface ApiServiceForLogin {

    @POST("verification/v3/send")
    suspend fun sendOtp(
        @Header("authToken") authToken: String,
        @Query("countryCode") countryCode: String = "91",
        @Query("customerId") customerId: String,
        @Query("flowType") flowType: String = "SMS",
        @Query("mobileNumber") mobileNumber: String
    ): Response<SendOTPRequest>

    @GET("verification/v3/validateOtp/")
    suspend fun validateOtp(
        @Header("authToken") authToken: String,
        @Query("verificationId") verificationId:Long,
        @Query("code") code: String
    ): Response<ValidateOtpRequest>

}
