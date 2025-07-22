package com.caastv.tvapp.utils.sealed

sealed class LoginResponse {
    data class Success(val data: String) : LoginResponse()
    data class OnFailure(val message:String):LoginResponse()
}