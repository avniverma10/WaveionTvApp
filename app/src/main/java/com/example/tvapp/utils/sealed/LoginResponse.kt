package com.example.tvapp.utils.sealed

import androidx.annotation.Keep


@Keep
sealed class LoginResponse {
    data class Success(val data: String) : LoginResponse()
    data class OnFailure(val message:String):LoginResponse()
}