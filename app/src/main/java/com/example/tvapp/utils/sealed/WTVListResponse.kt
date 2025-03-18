package com.example.tvapp.utils.sealed

sealed class WTVListResponse<T> {
    data class Success<T>(val data: List<T>) : WTVListResponse<T>()
    data class Failure<T>(val error: Throwable) : WTVListResponse<T>()
}