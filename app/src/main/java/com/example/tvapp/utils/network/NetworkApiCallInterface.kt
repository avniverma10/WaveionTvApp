package com.example.tvapp.utils.network

import com.example.tvapp.model.data.validation.SendOTPRequest
import com.example.tvapp.model.data.validation.ValidateOtpRequest
import com.google.gson.JsonObject
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url


interface NetworkApiCallInterface {
    @GET
    suspend fun makeHttpSingleDataRequest( @Url url: String): String

    @GET
    fun makeHttpGetRequest(@Url url: String): Call<Any>

    @POST
    fun makeHttpPostRequest(@Url url: String, @Body body: HashMap<String, String>): Call<Any>

    @POST
    fun makeMultipartJsonResRequest(@Url url: String, @Body requestBody: RequestBody): Call<JsonObject>

    @GET
    @Headers("Content-Type:application/json")
    fun getClientIpInfo(@Url url: String): Call<JsonObject>



    @POST
    suspend fun sendOtp(
        @Url url: String,
        @Header("authToken") authToken: String,
        @Query("countryCode") countryCode: String = "91",
        @Query("customerId") customerId: String,
        @Query("flowType") flowType: String = "SMS",
        @Query("mobileNumber") mobileNumber: String
    ): Response<SendOTPRequest>

    @GET("verification/v3/validateOtp/")
    suspend fun validateOtp(
        @Url url: String,
        @Header("authToken") authToken: String,
        @Query("verificationId") verificationId:Long,
        @Query("code") code: String
    ): Response<ValidateOtpRequest>
}
