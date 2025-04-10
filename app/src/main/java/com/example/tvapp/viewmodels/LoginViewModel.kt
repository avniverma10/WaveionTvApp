package com.example.tvapp.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tvapp.extensions.AndroidTvDrmInfo
import com.example.tvapp.extensions.logReport
import com.example.tvapp.extensions.toHashMap
import com.example.tvapp.model.data.DataStoreManager
import com.example.tvapp.model.data.FilterPreferences
import com.example.tvapp.model.data.login.WTVLogin
import com.example.tvapp.model.repository.common.WTVNetworkRepositoryImpl
import com.example.tvapp.model.repository.login.LoginInfo
import com.example.tvapp.model.repository.login.LoginPrefsRepository
import com.example.tvapp.model.repository.login.LoginRepositoryImpl
import com.example.tvapp.utils.sealed.LoginResponse
import com.example.tvapp.utils.sealed.WTVListResponse
import com.example.tvapp.utils.sealed.WTVResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val wtvNetworkRepositoryImpl: WTVNetworkRepositoryImpl,
    private val application: Application, private val loginRepositoryImpl: LoginRepositoryImpl, private val loginPrefsRepository: LoginPrefsRepository) : WTVViewModel(application = application, networkApiCallInterfaceImpl = wtvNetworkRepositoryImpl,loginPrefsRepository=loginPrefsRepository) {
    var verificationId: String? = "000000"

    fun saveLogin(username: String, password: String, remember: Boolean) {
        viewModelScope.launch {
            loginPrefsRepository.saveLoginInfo(username, password, remember)
        }
    }

    fun validateUserLogin(androidTvDrmInfo: AndroidTvDrmInfo,onLoginResponse:(WTVLogin?,String?)->Unit){
        viewModelScope.launch {
            loginRepositoryImpl.provideUserLogin("https://nextwave.waveiontechnologies.com:5000/api/android/appLogin",androidTvDrmInfo.toHashMap()).collect { response ->
                when (response) {
                    is WTVResponse.Success -> onLoginResponse(response.data,null)//_bannerList.value = response.data
                    is WTVResponse.Failure -> onLoginResponse(null,response.error.message) //logReport("_bannerList:${response.error.message}")
                }
            }
        }
    }


    // Send OTP
    fun sendOtp( authToken: String,phoneNumber: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            loginRepositoryImpl.sendOtp(
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
            loginRepositoryImpl.validateOtp(
                url="https://cpaas.messagecentral.com/verification/v3/send",
                otpCode =otpCode,
                currentVerificationId =currentVerificationId,
                authToken = authToken
            ).collect{ status ->
                when(status){
                    is LoginResponse.Success -> {
                       // dataStoreManager.saveLoginState(true, status.data)
                    }

                    is LoginResponse.OnFailure -> {
                        onFailure(status.message)
                    }
                }
            }
        }
    }

}
