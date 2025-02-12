package com.example.tvapp.di


import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.tvapp.models.DataStoreManager
import com.example.tvapp.utils.Constants
import com.example.tvapp.api.ApiServiceForLogin
import com.example.tvapp.api.ApiServiceForData
import com.example.tvapp.database.EPGDao
import com.example.tvapp.database.EPGDatabase
import com.example.tvapp.database.EPGRepository
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
//    @Provides
//    fun provideApplication(@ApplicationContext context: Context): Application {
//        return context as Application
//    }
//
//    @Provides
//    fun provideEPGRepository(dao: EPGDao): EPGRepository {
//        return EPGRepository(dao)
//    }

    @Provides
    @Singleton
    fun provideEPGDatabase(@ApplicationContext context: Context): EPGDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            EPGDatabase::class.java,
            "epg_database"
        ).build()
    }

    @Provides
    fun provideEPGDao(database: EPGDatabase): EPGDao {
        return database.epgDao()
    }

    @Provides
    @Singleton
    fun provideEPGRepository(dao: EPGDao): EPGRepository {
        return EPGRepository(dao)
    }
}
