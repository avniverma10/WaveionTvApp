package com.example.tvapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tvapp.model.data.DataStoreManager
import com.example.tvapp.model.repository.WTVNetworkRepositoryImpl
import com.example.tvapp.utils.sealed.LoginResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val wtvNetworkRepositoryImpl: WTVNetworkRepositoryImpl,private val dataStoreManager: DataStoreManager) : ViewModel() {
    var verificationId: String? = "000000"
   // Send OTP
    fun sendOtp( authToken: String,phoneNumber: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            wtvNetworkRepositoryImpl.sendOtp(
                url="https://cpaas.messagecentral.com/verification/v3/send",
                authToken = authToken,
                countryCode = "91",
                customerId = "C-A690A89045B84E8",
                flowType = "SMS",// Replace with actual customer ID
                mobileNumber = phoneNumber
            ).collect{ status ->
                when(status){
                    is LoginResponse.Success -> {
                        verificationId = status.data  // Extract verification ID
                        onSuccess()
                    }

                    is LoginResponse.OnFailure -> {
                        onFailure(status.message)
                    }
                }
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
            wtvNetworkRepositoryImpl.validateOtp(
                url="https://cpaas.messagecentral.com/verification/v3/send",
                otpCode =otpCode,
                currentVerificationId =currentVerificationId,
                authToken = authToken
            ).collect{ status ->
                when(status){
                    is LoginResponse.Success -> {
                        dataStoreManager.saveLoginState(true, status.data)
                    }

                    is LoginResponse.OnFailure -> {
                        onFailure(status.message)
                    }
                }
            }
        }
    }

}
