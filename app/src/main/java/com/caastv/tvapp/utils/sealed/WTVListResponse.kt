package com.caastv.tvapp.utils.sealed

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

sealed class WTVListResponse<T> {
    data class Success<T>(val data: List<T>) : WTVListResponse<T>()
    data class Failure<T>(val error: Throwable) : WTVListResponse<T>()
}

suspend fun <T> Flow<WTVListResponse<T>>.firstOrNullSuccess(): List<T>? =
    firstOrNull { it is WTVListResponse.Success }?.let { (it as WTVListResponse.Success).data }