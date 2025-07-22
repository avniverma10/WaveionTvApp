package com.caastv.tvapp.utils.network
import com.caastv.tvapp.extensions.loge
import okhttp3.Interceptor
import okhttp3.Response

//It is use to intercept API's request and response
class NetworkInterceptor:Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        loge("Request::","${request}")
        val response = chain.proceed(request)
        // loge("Response::", response.body?.string().toString())
        return response
    }
}