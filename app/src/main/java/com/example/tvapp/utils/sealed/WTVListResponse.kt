package com.example.tvapp.utils.sealed

import androidx.annotation.Keep
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

@Keep
sealed class WTVListResponse<T> {
    data class Success<T>(val data: List<T>) : WTVListResponse<T>()
    data class Failure<T>(val error: Throwable) : WTVListResponse<T>()
}

@Keep
suspend fun <T> Flow<WTVListResponse<T>>.firstOrNullSuccess(): List<T>? =
    firstOrNull { it is WTVListResponse.Success }?.let { (it as WTVListResponse.Success).data }