package com.panmetro.iptv.utils.network

//import com.android.tvapp.model.crash.LogEntry
import com.panmetro.iptv.model.data.login.CustomerChannelsInfo
import com.panmetro.iptv.model.data.login.CustomerPackageInfo
import com.panmetro.iptv.model.data.validation.SendOTPRequest
import com.panmetro.iptv.model.data.validation.ValidateOtpRequest
import com.panmetro.iptv.model.timestamp.ServerTimeStamp
import com.panmetro.iptv.utils.Constants
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

    /*@GET
    @Headers
    fun makeHttpGetRequest(@Url url: String): Call<Any>*/

    @GET
    fun makeHttpGetRequest(
        @Url url: String,
        @Header("x-api-key") apiKey: String = Constants.HEADER_TOKEN,
        @Header("Content-Type") contentType: String = "application/json"
    ): Call<Any>


    @POST
    fun makeHttpAnyPostRequest(@Url url: String, @Body body: HashMap<String, Any>): Call<Any>

    @GET
    fun makeDRMHttpGetRequest(
        @Url url: String,
        @Header("x-api-key") apiKey: String = Constants.DRM_HEADER_TOKEN,
        @Header("Content-Type") contentType: String = "application/json"
    ): Call<Any>

    @GET
    fun makeDRMPKGHttpGetRequest(
        @Url url: String,
        @Header("x-api-key") apiKey: String = Constants.DRM_HEADER_TOKEN,
        @Header("Content-Type") contentType: String = "application/json"
    ): Response<CustomerPackageInfo>

    @GET
    fun makeDRMChannelsHttpGetRequest(
        @Url url: String,
        @Header("x-api-key") apiKey: String = Constants.DRM_HEADER_TOKEN,
        @Header("Content-Type") contentType: String = "application/json"
    ): Response<CustomerChannelsInfo>

    @GET
    fun makeTimestampRequest(@Url url: String): ServerTimeStamp


    /**
     * Make a POST request to a dynamic URL, with a JSON body and custom headers.
     * @param url the full endpoint URL
     * @param headers a map of header names to values
     * @param body a map of key/value pairs to send as JSON in the request body
     */
    @POST
    fun makeHttpPostRequest(
        @Url url: String,
        @Body body: HashMap<String, String>,
        @Header("x-api-key") apiKey: String = Constants.HEADER_TOKEN,
        @Header("Content-Type") contentType: String = "application/json",
    ): Call<Any>

    @POST
    fun makeHttpPostLoginRequest(
        @Url url: String,
        @Body body: HashMap<String, String>,
        @Header("x-api-key") apiKey: String? = Constants.DRM_HEADER_TOKEN,
        @Header("Content-Type") contentType: String? = "application/json"
    ): Call<Any>//: Response<LoginApiResponse> // Changed from Call<Any> to Response<LoginResponse>

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
