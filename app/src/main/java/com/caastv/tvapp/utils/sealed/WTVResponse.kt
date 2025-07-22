package com.caastv.tvapp.utils.sealed

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

sealed class WTVResponse<T> {
    data class Success<T>(val data: T) : WTVResponse<T>()
    data class Failure<T>(val error: Throwable) : WTVResponse<T>()
}

suspend fun <T> Flow<WTVResponse<T>>.firstOrNullSuccess(): T? =
    firstOrNull { it is WTVResponse.Success }?.let { (it as WTVResponse.Success).data }
