package com.panmetro.iptv.model.repository.login

import androidx.annotation.Keep
import com.panmetro.iptv.extensions.logReport
import com.panmetro.iptv.utils.network.NetworkApiCallInterface
import com.panmetro.iptv.utils.sealed.LoginResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@Keep
class LoginRepositoryImpl @Inject constructor(private val networkApiCallInterface: NetworkApiCallInterface) {

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