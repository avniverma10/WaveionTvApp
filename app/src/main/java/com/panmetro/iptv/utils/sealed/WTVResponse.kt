package com.panmetro.iptv.utils.sealed

import androidx.annotation.Keep
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

@Keep
sealed class WTVResponse<T> {
    data class Success<T>(val data: T) : WTVResponse<T>()
    data class Failure<T>(val error: Throwable) : WTVResponse<T>()
}

@Keep
suspend fun <T> Flow<WTVResponse<T>>.firstOrNullSuccess(): T? =
    firstOrNull { it is WTVResponse.Success }?.let { (it as WTVResponse.Success).data }
