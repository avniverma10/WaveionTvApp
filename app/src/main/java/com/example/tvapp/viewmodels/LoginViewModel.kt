package com.example.tvapp.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.tvapp.extensions.AndroidTvDrmInfo
import com.example.tvapp.model.data.login.WTVLogin
import com.example.tvapp.model.repository.common.WTVNetworkRepositoryImpl
import com.example.tvapp.model.repository.login.LoginPrefsRepository
import com.example.tvapp.model.repository.login.LoginRepositoryImpl
import com.example.tvapp.utils.sealed.LoginResponse
import com.example.tvapp.utils.sealed.WTVResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val wtvNetworkRepositoryImpl: WTVNetworkRepositoryImpl,
    private val application: Application, private val loginRepositoryImpl: LoginRepositoryImpl, private val loginPrefsRepository: LoginPrefsRepository) : WTVViewModel(application = application, networkApiCallInterfaceImpl = wtvNetworkRepositoryImpl,loginPrefsRepository=loginPrefsRepository, okHttpClient = OkHttpClient()) {
    var verificationId: String? = "000000"

    fun saveLogin(username: String, password: String) {
        viewModelScope.launch {
            loginPrefsRepository.saveLoginInfo(username, password)
        }
    }

    fun validateUserLogin(androidTvDrmInfo: AndroidTvDrmInfo,onLoginResponse:(WTVLogin?,String?)->Unit){
        viewModelScope.launch {
            // 3. Prepare headers and body
            val headers = mapOf(
                "Authorization" to "56fdsr237df325fv454v3v4532drferh",
                "Content-Type"  to "application/json"
            )
            // "uname":"PAN000014","paswrd":"1234566","macaddr":"123456789"
            Log.e("MAC ID", "$androidTvDrmInfo.macId")
            val requestBody = hashMapOf(
                "uname" to (androidTvDrmInfo.userName ?: ""),
                "paswrd" to (androidTvDrmInfo.userPassword ?: ""),
                "macaddr" to (androidTvDrmInfo.macId)
            )
            //loginRepositoryImpl.provideUserLogin("https://nextwave.waveiontechnologies.com:5000/api/android/appLogin",androidTvDrmInfo.toHashMap()).collect { response ->
            loginRepositoryImpl.provideUserLogin(
                loginUrl = "https://iptvtest.panmetro.in/osmsapi/cryptodrm/logincheck",
                headers = headers,
                requestBody = requestBody).collect { response ->
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
