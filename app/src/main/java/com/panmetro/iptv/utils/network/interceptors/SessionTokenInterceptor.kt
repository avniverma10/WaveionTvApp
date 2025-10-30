package com.panmetro.iptv.utils.network.interceptors

import com.panmetro.iptv.utils.uistate.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class SessionTokenInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val isAuthHost = req.url.host.contains("api-demo.caastv.com")
        val isRefreshPath = req.url.encodedPath.contains("/" +"" )//ApiConst.PATH_REFRESH.trim('/'))

        // Only attach sessionToken to DATA calls (epg, etc.), not to refresh
        if (!isAuthHost && !isRefreshPath) {
            val session = tokenManager.currentSessionTokenBlocking()
            if (!session.isNullOrBlank()) {
                val withSession = req.newBuilder()
                    .header("sessionToken", session)
                    .build()
                return chain.proceed(withSession)
            }
        }
        return chain.proceed(req)
    }
}