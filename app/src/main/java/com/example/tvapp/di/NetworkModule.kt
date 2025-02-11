package com.example.tvapp.di


import android.content.Context
import com.example.tvapp.models.DataStoreManager
import com.example.tvapp.utils.Constants
import com.example.tvapp.api.ApiServiceForLogin
import com.example.tvapp.api.ApiServiceForData
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {


    @Provides
    @Singleton
    @Named("API1")
    fun provideRetrofitApi1(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL_API_1)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("API2")
    fun provideRetrofitApi2(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL_API_2)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


    @Provides
    @Singleton
    fun provideApiService1( @Named("API2")retrofit: Retrofit): ApiServiceForLogin {
        return retrofit.create(ApiServiceForLogin::class.java)
    }

    @Provides
    @Singleton
    fun provideApiService2(@Named("API1") retrofit: Retrofit): ApiServiceForData {
        return retrofit.create(ApiServiceForData::class.java)
    }

    @Provides
    @Singleton
    fun provideDataStoreManager(@ApplicationContext context: Context): DataStoreManager {
        return DataStoreManager(context)
    }
}
