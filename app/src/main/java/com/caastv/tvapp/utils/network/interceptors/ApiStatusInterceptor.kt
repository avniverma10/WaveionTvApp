package com.caastv.tvapp.utils.network.interceptors

import com.caastv.tvapp.utils.network.ApiStatusObserver
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class ApiStatusInterceptor private constructor() : Interceptor {
    private val observers = mutableListOf<ApiStatusObserver>()
    private var lastKnownStatus: Boolean? = null

    companion object {
        @Volatile
        private var instance: ApiStatusInterceptor? = null

        fun getInstance(): ApiStatusInterceptor {
            return instance ?: synchronized(this) {
                instance ?: ApiStatusInterceptor().also { instance = it }
            }
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        try {
            val response = chain.proceed(request)
            
            // Check if response is successful (2xx)
            if (response.isSuccessful) {
                notifyObservers(true)
            } else {
                notifyObservers(false)
            }
            
            return response
        } catch (e: IOException) {
            notifyObservers(false)
            throw e
        }
    }

    fun addObserver(observer: ApiStatusObserver) {
        observers.add(observer)
        lastKnownStatus?.let { observer.onApiStatusChanged(it) }
    }

    fun removeObserver(observer: ApiStatusObserver) {
        observers.remove(observer)
    }

    private fun notifyObservers(isApiWorking: Boolean) {
        if (lastKnownStatus != isApiWorking) {
            lastKnownStatus = isApiWorking
            observers.forEach { it.onApiStatusChanged(isApiWorking) }
        }
    }
}