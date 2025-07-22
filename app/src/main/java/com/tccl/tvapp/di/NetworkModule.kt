package com.tccl.tvapp.di

import android.content.Context
import com.tccl.tvapp.model.repository.common.WTVNetworkRepositoryImpl
import com.tccl.tvapp.utils.crash.logs.CrashLogger
import com.tccl.tvapp.utils.crash.logs.LogUploader
import com.tccl.tvapp.utils.network.BaseUrlSwitcherInterceptor
import com.tccl.tvapp.utils.network.LoggingInterceptor
import com.tccl.tvapp.utils.network.NetworkApiCallInterface
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val CONNECT_TIMEOUT = 15L
    private const val READ_TIMEOUT    = 10L
    private const val WRITE_TIMEOUT   = 10L
    private const val API_KEY_HEADER = "x-api-key"
    private const val API_KEY_VALUE  = "BUAA8JJkzfMI56y4BhEhU"

    @Singleton
    @Provides
    fun okHttpClient(@ApplicationContext context: Context): OkHttpClient {
        // Interceptor that adds the API key header:
        val headerInterceptor = Interceptor { chain ->
            val original = chain.request()
            val builder = original.newBuilder()
                .header("Accept", "application/json")
                .header(API_KEY_HEADER, API_KEY_VALUE)
            val requestWithHeaders = builder.build()
            chain.proceed(requestWithHeaders)
        }
        // Build and return the OkHttpClient
        return OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .addInterceptor(headerInterceptor)               // header
            .addInterceptor(BaseUrlSwitcherInterceptor())
            .addInterceptor(LoggingInterceptor())
            .cache(null)
            .build()
    }

    @Singleton
    @Provides
    fun retrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api-demo.caastv.com/") // replace with your production base URL
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


    @Provides
    @Singleton
    fun provideCrashLogger(context: Context): CrashLogger = CrashLogger(context)

    @Provides
    @Singleton
    fun provideLogUploader(
        context: Context,
        api: NetworkApiCallInterface
    ): LogUploader = LogUploader(context, api)

}
