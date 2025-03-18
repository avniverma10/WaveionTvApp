package com.example.tvapp.utils.sealed

sealed class WTVResponse<T> {
    data class Success<T>(val data: T) : WTVResponse<T>()
    data class Failure<T>(val error: Throwable) : WTVResponse<T>()
}