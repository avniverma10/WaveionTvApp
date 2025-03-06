package com.example.tvapp.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.Log
import com.example.tvapp.models.DataStoreManager
import com.example.tvapp.api.ApiServiceForLogin
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.provider.Settings
import android.provider.Settings.Secure.getString
import android.util.Base64
import org.json.JSONObject

@HiltViewModel
class LoginViewModel @Inject constructor(private val apiService: ApiServiceForLogin, private val dataStoreManager: DataStoreManager) : ViewModel() {

    var verificationId: String? = null





   // Send OTP
    fun sendOtp( authToken: String,phoneNumber: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                android.util.Log.d("AVNI", "sendOtp: Inside the try block ")
                val response = apiService.sendOtp(
                    authToken = authToken,
                    countryCode = "91",
                    customerId = "C-A690A89045B84E8",
                    flowType = "SMS",// Replace with actual customer ID
                    mobileNumber = phoneNumber
                )
                android.util.Log.d("AVNI", "sendOtp: ${authToken}")
                android.util.Log.d("AVNI", "sendOtp:Responsse isss --> $response ")
                if (response.isSuccessful) {
                    Log.d("AVNI","Inside the iff")
                    val otpResponse = response.body()!!
                    if (otpResponse != null) {
                        verificationId = otpResponse.data.verificationId  // Extract verification ID
                    }
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("AVNI", "Error Response: $errorBody")
                    onFailure("Failed to send OTP: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("AVNI", "Exception: ${e.localizedMessage}")
                onFailure(e.localizedMessage ?: "Unknown error occurred")
            }
        }
    }

    fun validateOtp(
        otpCode: String,
        authToken: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {

        val currentVerificationId = verificationId?.toLong() ?: return onFailure("Verification ID is missing")

        viewModelScope.launch {
            try {
                val response = apiService.validateOtp(
                    authToken = authToken,
                    verificationId = currentVerificationId,
                    code = otpCode,
                )

                if (response.isSuccessful) {
                    val validationData = response.body()?.data
                    if (validationData?.verificationStatus == "VERIFICATION_COMPLETED") {
                        dataStoreManager.saveLoginState(true, authToken)
                        onSuccess()
                    } else {
                        onFailure("OTP validation failed. Status: ${validationData?.verificationStatus}")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    onFailure("Failed to validate OTP: $errorBody")
                }
            } catch (e: Exception) {
                onFailure(e.localizedMessage ?: "Unknown error occurred")
            }
        }
    }

}
