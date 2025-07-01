package com.example.tvapp.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.tvapp.extensions.AndroidTvDrmInfo
import com.example.tvapp.model.data.login.LoginResponseData
import com.example.tvapp.model.data.login.WTVLogin
import com.example.tvapp.model.repository.common.WTVNetworkRepositoryImpl
import com.example.tvapp.model.repository.login.LoginPrefsRepository
import com.example.tvapp.model.repository.login.LoginRepositoryImpl
import com.example.tvapp.utils.Constants
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

    fun validateUserLogin(androidTvDrmInfo: AndroidTvDrmInfo,onLoginResponse:(LoginResponseData?, String?)->Unit){
        viewModelScope.launch {
            val requestBody = hashMapOf(
                "username" to (androidTvDrmInfo.userName ?: ""),
                "password" to (androidTvDrmInfo.userPassword ?: ""),
                "macId" to (androidTvDrmInfo.macId)
            )
            loginRepositoryImpl.provideUserLogin(
                loginUrl = Constants.provideBaseUrl()+"app/tv-users/login",
                requestBody = requestBody).collect { response ->
                when (response) {
                    is WTVResponse.Success -> onLoginResponse(response.data,null)//_bannerList.value = response.data
                    is WTVResponse.Failure -> onLoginResponse(null,response.error.message) //logReport("_bannerList:${response.error.message}")
                }
            }
        }
    }
}
