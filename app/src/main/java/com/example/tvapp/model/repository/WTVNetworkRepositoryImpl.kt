package com.example.tvapp.model.repository

import com.example.tvapp.extensions.convertIntoModel
import com.example.tvapp.extensions.convertIntoModels
import com.example.tvapp.extensions.logReport
import com.example.tvapp.extensions.toJSONArray
import com.example.tvapp.extensions.toJSONObject
import com.example.tvapp.utils.sealed.WTVListResponse
import com.example.tvapp.utils.sealed.WTVResponse
import com.example.tvapp.model.data.banner.Banner
import com.example.tvapp.model.data.home.HomeData
import com.example.tvapp.model.data.epgdata.EPGDataItem
import com.example.tvapp.model.data.genre.WTVGenre
import com.example.tvapp.model.data.language.WTVLanguage
import com.example.tvapp.model.data.manifest.WTVManifest
import com.example.tvapp.utils.network.NetworkApiCallInterface
import com.example.tvapp.utils.sealed.LoginResponse
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class WTVNetworkRepositoryImpl @Inject constructor(private val networkApiCallInterface: NetworkApiCallInterface) {
    suspend fun provideWTVManifest(manifestUrl: String): Flow<WTVResponse<WTVManifest>> = flow {
        try {
            val response = networkApiCallInterface.makeHttpGetRequest(manifestUrl).execute()
            if (response.isSuccessful && response.body() != null) {
                val manifest = response.body()?.toJSONObject()?.toString()
                    .convertIntoModel(WTVManifest::class.java)
                manifest?.let {
                    // Optionally save manifest data into ContentProvider or DB here
                    emit(WTVResponse.Success(it))
                } ?: throw Exception("Failed to parse manifest")
            } else {
                emit(WTVResponse.Failure(Throwable("Invalid response received")))
            }
        } catch (e: Exception) {
            emit(WTVResponse.Failure(e))
        }
    }.flowOn(Dispatchers.IO)


    suspend fun provideWTVEPGData(epgContentUrl: String): Flow<WTVListResponse<EPGDataItem>> = flow {
        try {
            val response = networkApiCallInterface.makeHttpGetRequest(epgContentUrl).execute()
            if (response.isSuccessful && response.body() != null) {
                val epgData: List<EPGDataItem>? = response.body()?.toJSONArray().toString()
                    .convertIntoModels(object : TypeToken<List<EPGDataItem>>() {})
                // Optionally save EPG data into ContentProvider or DB here
                emit(WTVListResponse.Success(epgData!!))
            } else {
                emit(WTVListResponse.Failure(Throwable("Invalid response received")))
            }
        } catch (e: Exception) {
            emit(WTVListResponse.Failure(e))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun provideWTVGenreData(genreUrl: String): Flow<WTVListResponse<WTVGenre>> = flow {
        try {
            val response = networkApiCallInterface.makeHttpGetRequest(genreUrl).execute()
            if (response.isSuccessful && response.body() != null) {
                val epgData: List<WTVGenre>? = response.body()?.toJSONArray().toString()
                    .convertIntoModels(object : TypeToken<List<WTVGenre>>() {})
                // Optionally save EPG data into ContentProvider or DB here
                emit(WTVListResponse.Success(epgData!!))
            } else {
                emit(WTVListResponse.Failure(Throwable("Invalid response received")))
            }
        } catch (e: Exception) {
            emit(WTVListResponse.Failure(e))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun provideWTVLanguageData(languageUrl: String): Flow<WTVListResponse<WTVLanguage>> = flow {
        try {
            val response = networkApiCallInterface.makeHttpGetRequest(languageUrl).execute()
            if (response.isSuccessful && response.body() != null) {
                val epgData: List<WTVLanguage>? = response.body()?.toJSONArray().toString()
                    .convertIntoModels(object : TypeToken<List<WTVLanguage>>() {})
                // Optionally save EPG data into ContentProvider or DB here
                emit(WTVListResponse.Success(epgData!!))
            } else {
                emit(WTVListResponse.Failure(Throwable("Invalid response received")))
            }
        } catch (e: Exception) {
            emit(WTVListResponse.Failure(e))
        }
    }.flowOn(Dispatchers.IO)


    suspend fun getBanners(bannerUrl: String): Flow<WTVListResponse<Banner>> = flow {
        try {
            val response = networkApiCallInterface.makeHttpGetRequest(bannerUrl).execute()
            if (response.isSuccessful && response.body() != null) {
                val banners = response.body()?.toString()?.convertIntoModels(object : TypeToken<List<Banner>>() {})
                banners?.let { data ->
                    emit(WTVListResponse.Success(data))
                } ?: emit(WTVListResponse.Failure(Throwable("Parsing error: data is null")))
            } else {
                emit(WTVListResponse.Failure(Throwable("Invalid response received")))
            }
        } catch (e: Exception) {
            emit(WTVListResponse.Failure(e))
        }
    }


    suspend fun provideHomeContent(homeContentUrl:String): Flow<List<HomeData>> = flow {
        try {
            val response = networkApiCallInterface.makeHttpGetRequest(homeContentUrl).execute()
            if (response.isSuccessful && response.body() != null) {
                response.body()?.toString().convertIntoModels(object : TypeToken<List<HomeData>>() {})?.let{ data->
                    // Optionally save EPG data into ContentProvider or DB here
                    emit(data)
                }
            } else {
                logReport("Banner", "Error fetching Banner content")
            }
        } catch (e: Exception) {
            logReport("Banner", "Error fetching Banner content", e)
        }
    }.flowOn(Dispatchers.IO) // <-- This moves the emission to the IO thread

    suspend fun sendOtp(
        url: String,
        authToken: String,
        countryCode: String = "+91",
        customerId: String = "C-A690A89045B84E8",
        flowType: String = "SMS",
        mobileNumber: String = "7838115152"
    ): Flow<LoginResponse> = flow {
        try {

            logReport("AVNI", "sendOtp: Inside the try block")

            val response = networkApiCallInterface.sendOtp(
                url = url,
                authToken = authToken,
                countryCode = countryCode,
                customerId = customerId,
                flowType = flowType,
                mobileNumber = mobileNumber
            )

            logReport("AVNI", "sendOtp: $authToken")

            if (response.isSuccessful) {
                response.body()?.let { otpResponse ->
                    emit(LoginResponse.Success(otpResponse.data.verificationId)) // Emit success
                } ?: emit(LoginResponse.OnFailure("Response body is null"))
            } else {
                val errorBody = response.errorBody()?.string()
                logReport("AVNI", "Error Response: $errorBody")
                emit(LoginResponse.OnFailure("Failed to send OTP: ${response.message()}"))
            }
        } catch (e: Exception) {
            logReport("AVNI", "Exception: ${e.localizedMessage}")
            emit(LoginResponse.OnFailure(e.localizedMessage ?: "Unknown error occurred"))
        }
    }.flowOn(Dispatchers.IO) // Ensures network call runs on IO thread

    suspend fun validateOtp(
        url: String,
        currentVerificationId: Long,
        otpCode: String,
        authToken: String
    ): Flow<LoginResponse> = flow {
        try {

            logReport("AVNI", "sendOtp: Inside the try block")

            val response = networkApiCallInterface.validateOtp(
                url = url,
                authToken = authToken,
                verificationId = currentVerificationId,
                code = otpCode,
            )
            if (response.isSuccessful) {
                val validationData = response.body()?.data
                if (validationData?.verificationStatus == "VERIFICATION_COMPLETED") {
                    //dataStoreManager.saveLoginState(true, authToken)
                    emit(LoginResponse.Success(authToken))

                } else {
                    emit(LoginResponse.OnFailure("OTP validation failed. Status: ${validationData?.verificationStatus}"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                logReport("AVNI", "Error Response: $errorBody")
                emit(LoginResponse.OnFailure( "Failed to send OTP: ${response.message()}"))
            }
        } catch (e: Exception) {
            logReport("AVNI", "Exception: ${e.localizedMessage}")
            emit(LoginResponse.OnFailure(e.localizedMessage ?: "Unknown error occurred"))
        }
    }.flowOn(Dispatchers.IO) // Ensures network call runs on IO thread

}