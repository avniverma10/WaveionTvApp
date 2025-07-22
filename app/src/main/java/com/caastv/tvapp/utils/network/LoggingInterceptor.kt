package com.caastv.tvapp.utils.network
import com.caastv.tvapp.extensions.loge
import com.caastv.tvapp.utils.Constants
import okhttp3.Interceptor
import okhttp3.Response

class LoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestWithKey = chain.request()
            .newBuilder()
            .addHeader("x-api-key", Constants.HEADER_TOKEN)
            .build()
        loge("Request →", "${requestWithKey.method} ${requestWithKey.url}")
        val response = chain.proceed(requestWithKey)
        loge("Response ←", "${response.code} ${response.request.url}")
        return response
    }
}