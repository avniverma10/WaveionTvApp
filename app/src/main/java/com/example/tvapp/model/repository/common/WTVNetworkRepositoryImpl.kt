package com.example.tvapp.model.repository.common

import com.example.tvapp.extensions.convertIntoModel
import com.example.tvapp.extensions.convertIntoModels
import com.example.tvapp.extensions.logReport
import com.example.tvapp.extensions.toJSONArray
import com.example.tvapp.extensions.toJSONObject
import com.example.tvapp.model.data.appupdate.AppUpdateResponse
import com.example.tvapp.model.data.banner.Banner
import com.example.tvapp.model.data.epgdata.EPGDataItem
import com.example.tvapp.model.data.genre.WTVGenre
import com.example.tvapp.model.data.home.HomeData
import com.example.tvapp.model.data.language.WTVLanguage
import com.example.tvapp.model.data.manifest.WTVManifest
import com.example.tvapp.model.home.WTVHomeCategory
import com.example.tvapp.utils.network.NetworkApiCallInterface
import com.example.tvapp.utils.sealed.WTVListResponse
import com.example.tvapp.utils.sealed.WTVResponse
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class WTVNetworkRepositoryImpl @Inject constructor(private val networkApiCallInterface: NetworkApiCallInterface) {
    suspend fun provideWTVManifest(manifestUrl: String): Flow<WTVResponse<WTVManifest>> = flow {
        try {
            val response = networkApiCallInterface.makeHttpGetRequest(manifestUrl).execute()
            if (response.isSuccessful && response.body() != null) {
                val manifest = response.body()?.toJSONObject()?.toString()
                    .convertIntoModel(WTVManifest::class.java)
                manifest?.let {
                    // Optionally save manifest data into ContentProvider or DB here
                    emit(WTVResponse.Success(it))
                } ?: throw Exception("Failed to parse manifest")
            } else {
                emit(WTVResponse.Failure(Throwable("Invalid response received")))
            }
        } catch (e: Exception) {
            emit(WTVResponse.Failure(e))
        }
    }.flowOn(Dispatchers.IO)


    suspend fun provideWTVEPGData(epgContentUrl: String): Flow<WTVListResponse<EPGDataItem>> = flow {
        try {
            val response = networkApiCallInterface.makeHttpGetRequest(epgContentUrl).execute()
            if (response.isSuccessful && response.body() != null) {
                val epgData: List<EPGDataItem>? = response.body()?.toJSONArray().toString()
                    .convertIntoModels(object : TypeToken<List<EPGDataItem>>() {})
                // Optionally save EPG data into ContentProvider or DB here
                emit(WTVListResponse.Success(epgData!!))
            } else {
                emit(WTVListResponse.Failure(Throwable("Invalid response received")))
            }
        } catch (e: Exception) {
            emit(WTVListResponse.Failure(e))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun provideWTVGenreData(genreUrl: String): Flow<WTVListResponse<WTVGenre>> = flow {
        try {
            val response = networkApiCallInterface.makeHttpGetRequest(genreUrl).execute()
            if (response.isSuccessful && response.body() != null) {
                val epgData: List<WTVGenre>? = response.body()?.toJSONArray().toString()
                    .convertIntoModels(object : TypeToken<List<WTVGenre>>() {})
                // Optionally save EPG data into ContentProvider or DB here
                emit(WTVListResponse.Success(epgData!!))
            } else {
                emit(WTVListResponse.Failure(Throwable("Invalid response received")))
            }
        } catch (e: Exception) {
            emit(WTVListResponse.Failure(e))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun provideWTVLanguageData(languageUrl: String): Flow<WTVListResponse<WTVLanguage>> = flow {
        try {
            val response = networkApiCallInterface.makeHttpGetRequest(languageUrl).execute()
            if (response.isSuccessful && response.body() != null) {
                val epgData: List<WTVLanguage>? = response.body()?.toJSONArray().toString()
                    .convertIntoModels(object : TypeToken<List<WTVLanguage>>() {})
                // Optionally save EPG data into ContentProvider or DB here
                emit(WTVListResponse.Success(epgData!!))
            } else {
                emit(WTVListResponse.Failure(Throwable("Invalid response received")))
            }
        } catch (e: Exception) {
            emit(WTVListResponse.Failure(e))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun provideWTVHomeData(homeUrl: String): Flow<WTVListResponse<WTVHomeCategory>> = flow {
        try {
            val response = networkApiCallInterface.makeHttpGetRequest(homeUrl).execute()
            if (response.isSuccessful && response.body() != null) {
                val epgData: List<WTVHomeCategory>? = response.body()?.toJSONArray().toString()
                    .convertIntoModels(object : TypeToken<List<WTVHomeCategory>>() {})
                // Optionally save EPG data into ContentProvider or DB here
                emit(WTVListResponse.Success(epgData!!))
            } else {
                emit(WTVListResponse.Failure(Throwable("Invalid response received")))
            }
        } catch (e: Exception) {
            emit(WTVListResponse.Failure(e))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getBanners(bannerUrl: String): Flow<WTVListResponse<Banner>> = flow {
        try {
            val response = networkApiCallInterface.makeHttpGetRequest(bannerUrl).execute()
            if (response.isSuccessful && response.body() != null) {
                val banners = response.body()?.toJSONArray()?.toString()?.convertIntoModels(object : TypeToken<List<Banner>>() {})
                banners?.let { data ->
                    emit(WTVListResponse.Success(data))
                } ?: emit(WTVListResponse.Failure(Throwable("Parsing error: data is null")))
            } else {
                emit(WTVListResponse.Failure(Throwable("Invalid response received")))
            }
        } catch (e: Exception) {
            emit(WTVListResponse.Failure(e))
        }
    }.flowOn(Dispatchers.IO)


    suspend fun provideHomeContent(homeContentUrl:String): Flow<List<HomeData>> = flow {
        try {
            val response = networkApiCallInterface.makeHttpGetRequest(homeContentUrl).execute()
            if (response.isSuccessful && response.body() != null) {
                response.body()?.toString().convertIntoModels(object : TypeToken<List<HomeData>>() {})?.let{ data->
                    // Optionally save EPG data into ContentProvider or DB here
                    emit(data)
                }
            } else {
                logReport("Banner", "Error fetching Banner content")
            }
        } catch (e: Exception) {
            logReport("Banner", "Error fetching Banner content", e)
        }
    }.flowOn(Dispatchers.IO) // <-- This moves the emission to the IO thread


    suspend fun provideAppUpdateInfo(updateUrl: String): Flow<WTVResponse<AppUpdateResponse>> = flow {
        try {
            val resp = networkApiCallInterface.makeHttpGetRequest(updateUrl).execute()
            if (resp.isSuccessful && resp.body() != null) {
                val body = resp.body()!!.toJSONObject().toString()
                val parsed = body.convertIntoModel(AppUpdateResponse::class.java)
                parsed?.let { emit(WTVResponse.Success(it)) }
                    ?: emit(WTVResponse.Failure(Throwable("Parsing error")))
            } else {
                emit(WTVResponse.Failure(Throwable("HTTP ${resp.code()}")))
            }
        } catch (e: Exception) {
            emit(WTVResponse.Failure(e))
        }
    }.flowOn(Dispatchers.IO)


}