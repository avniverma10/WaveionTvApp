package com.example.tvapp.di

import com.example.tvapp.model.repository.WTVNetworkRepositoryImpl
import com.example.tvapp.utils.network.LoggingInterceptor
import com.example.tvapp.utils.network.NetworkApiCallInterface
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private var CONNECT_TIMEOUT: Long = 3 * 60L
    private var READ_TIMEOUT: Long = 3 * 60L
    private var WRITE_TIMEOUT: Long = 3 * 60L

    @Singleton
    @Provides
    fun okHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .addInterceptor(LoggingInterceptor())
            .cache(null)
            .build()
    }

    @Singleton
    @Provides
    fun retrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://example.com")
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


    @Provides
    @Singleton
    fun provideNetworkAPIService(retrofit: Retrofit): NetworkApiCallInterface {
        return retrofit.create(NetworkApiCallInterface::class.java)
    }

    @Provides
    @Singleton
    fun provideWTVNetworkRepository(networkApiCallInterface: NetworkApiCallInterface):WTVNetworkRepositoryImpl{
        return WTVNetworkRepositoryImpl(networkApiCallInterface)
    }

}
