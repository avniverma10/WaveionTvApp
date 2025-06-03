package com.example.tvapp.model.repository.login

import android.util.Log
import com.example.tvapp.extensions.convertIntoModel
import com.example.tvapp.extensions.logReport
import com.example.tvapp.extensions.loge
import com.example.tvapp.extensions.toJSONObject
import com.example.tvapp.model.data.login.LoginResponseData
import com.example.tvapp.model.data.login.WTVLogin
import com.example.tvapp.utils.network.NetworkApiCallInterface
import com.example.tvapp.utils.sealed.LoginResponse
import com.example.tvapp.utils.sealed.WTVResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class LoginRepositoryImpl @Inject constructor(private val networkApiCallInterface: NetworkApiCallInterface) {
    suspend fun provideUserLogin(
        loginUrl: String,
        requestBody: HashMap<String, String?>
    ): Flow<WTVResponse<LoginResponseData>> = flow {
        try {
            loge("url:","$loginUrl ${requestBody}")
            val response = networkApiCallInterface.makeHttpPostRequest(url=loginUrl,body = requestBody).execute()
            if (response.isSuccessful && response.body() != null) {
                loge("response:","${response.body()}")

                val manifest = response.body()?.toJSONObject()?.toString()
                    .convertIntoModel(LoginResponseData::class.java)
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