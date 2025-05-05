package com.example.tvapp.utils.network
import android.util.Log
import com.example.tvapp.extensions.toGson
import okhttp3.Interceptor
import okhttp3.Response

//It is use to intercept API's request and response
class LoggingInterceptor:Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
       val request = chain.request()
        Log.e("Request::","${request}")
       val response = chain.proceed(request)
       // Log.e("Response::", response.body?.string().toString())
      return response
    }
}