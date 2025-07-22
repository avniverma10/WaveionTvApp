package com.caastv.tvapp.utils.network

import com.caastv.tvapp.extensions.loge
import com.caastv.tvapp.extensions.serverErrorHandling
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


class BaseUrlSwitcherInterceptor : Interceptor {
    companion object {
        private const val MAX_RETRIES = 2
        private const val INITIAL_RETRY_DELAY_MS = 2_500L
    }
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        var response: Response? = null
        var retryCount = 0

        try {
            response = chain.proceed(originalRequest)
            if (UrlManager.shouldSwitchUrl(response.code)) {
                response.close()
                //UrlManager.switchBaseUrl()
                if (retryCount < MAX_RETRIES) {
                    retryCount++
                    return retryWithNewUrl(chain, originalRequest, retryCount)
                }else{
                    val (code, title, message) = serverErrorHandling(response.code)
                    ErrorHandler.setError(
                        ErrorHandler.ErrorState(
                            code = code,
                            title = title,
                            message = message
                        )
                    )
                }
                return response
            }
            return response
        } catch (e: Exception) {
            if (isNetworkError(e)) {
                loge("shouldSwitchUrl::","${e}")
                if (retryCount < MAX_RETRIES) {
                    retryCount++
                    return retryWithNewUrl(chain, originalRequest, retryCount)
                }
                throw e
            }
            throw e
        }
    }

    /*private fun retryWithNewUrl(chain: Interceptor.Chain, originalRequest: Request): Response {
        val newUrl = UrlManager.replaceBaseUrl(originalRequest.url)
        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()
        return chain.proceed(newRequest)
    }*/

    private fun retryWithNewUrl(chain: Interceptor.Chain, originalRequest: Request, retryCount: Int): Response {
        try {
            // Exponential backoff with jitter
            val delayMs = (INITIAL_RETRY_DELAY_MS * retryCount).coerceAtMost(10000L)
            Thread.sleep(delayMs + (0..1000).random())
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IOException("Interrupted during retry delay", e)
        }

        val newRequest = originalRequest.newBuilder()
            .url(UrlManager.getCurrentBaseUrl())  // Make sure to use the updated URL
            .build()
        return chain.proceed(newRequest)
    }

    private fun isNetworkError(e: Exception): Boolean {
        return e is SocketTimeoutException ||
                e is ConnectException ||
                e is UnknownHostException
    }
}