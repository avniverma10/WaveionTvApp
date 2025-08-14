package com.android.panmetroiptv.di

import android.content.Context
import com.android.panmetroiptv.model.repository.common.WTVNetworkRepositoryImpl
import com.android.panmetroiptv.utils.network.NetworkApiCallInterface
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val CONNECT_TIMEOUT = 10 * 60L
    private const val READ_TIMEOUT    = 3 * 60L
    private const val WRITE_TIMEOUT   = 3 * 60L
    private const val API_KEY_HEADER = "x-api-key"
    private const val API_KEY_VALUE  = "BUAA8JJkzfMI56y4BhEhU"

    @Singleton
    @Provides
    fun okHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val cacheSize = 10L * 1024 * 1024
        val cacheDir  = File(context.cacheDir, "http_cache")
        val cache     = Cache(cacheDir, cacheSize)
        //Network interceptor: tag fresh responses with max-age
        val networkCacheInterceptor = Interceptor { chain ->
            val response = chain.proceed(chain.request())
            // If the server gave no caching headers, add one for 60s
            response.newBuilder()
                .header("Cache-Control", "public, max-age=60")
                .build()
        }
        // Offline interceptor: on any IOException or 5xx, force only-if-cached
        val offlineInterceptor = Interceptor { chain ->
            var request = chain.request()
            try {
                val response = chain.proceed(request)
                // If server error, drop that response and try cache instead
                if (response.code in 500..599) {
                    response.close()
                    request = request.newBuilder()
                        .header(
                            "Cache-Control",
                            "public, only-if-cached, max-stale=${7 * 24 * 60 * 60}"
                        )
                        .build()
                    return@Interceptor chain.proceed(request)
                }
                return@Interceptor response
            } catch (ioEx: IOException) {
                // Network error or timeout => serve stale cache
                request = request.newBuilder()
                    .header(
                        "Cache-Control",
                        "public, only-if-cached, max-stale=${7 * 24 * 60 * 60}"
                    )
                    .build()
                return@Interceptor chain.proceed(request)
            }
        }
        // Create a TrustManager that does not validate certificate chains , only for testing and development purpose only
        // TODO("Add trust manager that validate certificate chains")
        val trustAllCerts = arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit
                override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
            }
        )

        // Install the all-trusting trust manager into an SSLContext
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, trustAllCerts, SecureRandom())
        }
        val sslSocketFactory = sslContext.socketFactory
        // Interceptor that adds the API key header:
        /*val headerInterceptor = Interceptor { chain ->
            val original = chain.request()
            val builder = original.newBuilder()
                .header("Accept", "application/json")
                .header(API_KEY_HEADER, API_KEY_VALUE)
            val requestWithHeaders = builder.build()
            chain.proceed(requestWithHeaders)
        }*/
        // Build and return the OkHttpClient
        return OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            //.addInterceptor(headerInterceptor)               // header
            //Trust all SSL certificates (for debug/development only)
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }// Bypass hostname verification
            .cache(null)
            .build()
    }

    @Singleton
    @Provides
    fun retrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api-panmetro.caastv.com/") // replace with your production base URL
            .client(okHttpClient)
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder()
                        .setLenient()
                        .create()
                )
            )
            .build()
    }

    @Singleton
    @Provides
    fun provideNetworkAPIService(retrofit: Retrofit): NetworkApiCallInterface {
        return retrofit.create(NetworkApiCallInterface::class.java)
    }

    @Singleton
    @Provides
    fun provideWTVNetworkRepository(
        networkApiCallInterface: NetworkApiCallInterface
    ): WTVNetworkRepositoryImpl {
        return WTVNetworkRepositoryImpl(networkApiCallInterface)
    }
}
