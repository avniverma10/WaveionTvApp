package com.android.panmetroiptv.model.repository.common

import androidx.annotation.Keep
import com.android.panmetroiptv.extensions.convertIntoLoginResponse
import com.android.panmetroiptv.extensions.convertIntoModel
import com.android.panmetroiptv.extensions.convertIntoModels
import com.android.panmetroiptv.extensions.logReport
import com.android.panmetroiptv.extensions.loge
import com.android.panmetroiptv.extensions.toJSONArray
import com.android.panmetroiptv.extensions.toJSONObject
import com.android.panmetroiptv.model.data.appupdate.AppUpdateResponse
import com.android.panmetroiptv.model.data.banner.Banner
import com.android.panmetroiptv.model.data.epgdata.EPGDataItem
import com.android.panmetroiptv.model.data.genre.WTVGenre
import com.android.panmetroiptv.model.data.hash.HashInfo
import com.android.panmetroiptv.model.data.health.HealthAPIResponse
import com.android.panmetroiptv.model.data.home.HomeData
import com.android.panmetroiptv.model.data.language.WTVLanguage
import com.android.panmetroiptv.model.data.login.ChannelResult
import com.android.panmetroiptv.model.data.login.CustomerChannelsInfo
import com.android.panmetroiptv.model.data.login.CustomerPackageInfo
import com.android.panmetroiptv.model.data.login.DRMUserInfo
import com.android.panmetroiptv.model.data.login.LoginInfo
import com.android.panmetroiptv.model.data.manifest.WTVManifest
import com.android.panmetroiptv.model.notification.NotificationItem
import com.android.panmetroiptv.model.home.WTVHomeCategory
import com.android.panmetroiptv.utils.Constants
import com.android.panmetroiptv.utils.network.NetworkApiCallInterface
import com.android.panmetroiptv.utils.sealed.WTVListResponse
import com.android.panmetroiptv.utils.sealed.WTVResponse
import com.android.panmetroiptv.utils.uistate.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.io.IOException
import java.util.Collections
import javax.inject.Inject

@Keep
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


    fun provideNotificationSSE(sseUrl: String): Flow<NotificationItem> = callbackFlow {
        val client = OkHttpClient.Builder()
            .readTimeout(0, java.util.concurrent.TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true)
            .build()

        val request = Request.Builder()
            .url(sseUrl)
            .addHeader("Accept", "text/event-stream")
            .build()

        val gson = Gson()
        val listener = object : EventSourceListener() {
            override fun onEvent(
                eventSource: okhttp3.sse.EventSource,
                id: String?, type: String?, data: String
            ) {
                // parse JSON array of NotificationItem
                val listType = object : TypeToken<List<NotificationItem>>() {}.type
                val items: List<NotificationItem> = gson.fromJson(data, listType)
                items.forEach { trySend(it).isSuccess }
            }

            override fun onFailure(
                eventSource: okhttp3.sse.EventSource,
                t: Throwable?, response: okhttp3.Response?
            ) {
                // close the flow on error
                close(t ?: RuntimeException("SSE failure"))
            }
        }

        val source = EventSources.createFactory(client)
            .newEventSource(request, listener)

        // tear down when the collector disappears
        awaitClose { source.cancel() }
    }.flowOn(Dispatchers.IO)

    suspend fun provideWTVEPGData(epgContentUrl: String): Flow<WTVListResponse<EPGDataItem>> = flow {
        try {
            val response = networkApiCallInterface.makeHttpGetRequest(epgContentUrl).execute()
            if (response.isSuccessful && response.body() != null) {
                loge("API:","Url:${epgContentUrl}>${response.body()}")
                val epgData: List<EPGDataItem>? = response.body()?.toJSONArray().toString()
                    .convertIntoModels(object : TypeToken<List<EPGDataItem>>() {})
                // Optionally save EPG data into ContentProvider or DB here
                emit(WTVListResponse.Success(epgData!!))
            } else {
                emit(WTVListResponse.Failure(Throwable("Invalid response received")))
            }
        } catch (e: Exception) {
            emit(WTVListResponse.Failure(e))
            loge("API:","Url:${epgContentUrl}>${e.message}")
        }
    }.flowOn(Dispatchers.IO)

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


    suspend fun getUserInfo(requestUrl: String,headers: Map<String, String>): Flow<WTVResponse<DRMUserInfo>> = flow {
        try {
            val response = networkApiCallInterface.makeDRMHttpGetRequest(requestUrl).execute()
            if (response.isSuccessful && response.body() != null) {
                val banners = response.body()?.toString()?.convertIntoModel(DRMUserInfo::class.java)
                banners?.let { data ->
                    emit(WTVResponse.Success(data))
                } ?: emit(WTVResponse.Failure(Throwable("Parsing error: data is null")))
            } else {
                emit(WTVResponse.Failure(Throwable("Invalid response received")))
            }
        } catch (e: Exception) {
            emit(WTVResponse.Failure(e))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getCustomerPackageInfo(requestUrl: String): Flow<WTVResponse<CustomerPackageInfo>> = flow {
        try {
            val response = networkApiCallInterface.makeDRMHttpGetRequest(requestUrl).execute()
            if (response.isSuccessful && response.body() != null) {
                response.body()?.toJSONObject()?.toString().convertIntoModel(CustomerPackageInfo::class.java)?.let {
                    emit(WTVResponse.Success(it))
                }
            }
        } catch (e: Exception) {
            emit(WTVResponse.Failure(e))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getCustomerChannelInfo(pkgName: List<Int>) = coroutineScope {
        // Create a mutable list to store all channels
        val allChannels = Collections.synchronizedList(mutableListOf<ChannelResult>())

        try{
            pkgName.map { pkg ->
                async(Dispatchers.IO) {  // Explicitly use IO dispatcher for network calls
                    try {
                        val requestUrl = "${Constants.LOGIN_SMS_BASE}src/api/v1/services-assets/livechannels/$pkg?page=1&limit=1000"
                        val response = networkApiCallInterface.makeDRMHttpGetRequest(requestUrl).execute()

                        if (response.isSuccessful) {
                            response.body()?.let { body ->
                                // Use proper JSON parsing instead of manual conversion
                                body.toJSONObject()?.toString().convertIntoModel(
                                    CustomerChannelsInfo::class.java)?.let {
                                    synchronized(allChannels) {
                                        allChannels.addAll(it.results.map { it})
                                    }
                                }
                            }
                        } else {
                            loge("ChannelFetch", "Failed for $pkg: ${response.code()}")
                        }
                    } catch (e: Exception) {
                        loge("ChannelFetch", "Error fetching $pkg >>${e.message}")
                    }
                }
            }.awaitAll() // Wait for all requests to complete
            Constants.userChannelResult = allChannels
        } catch (e: Exception) {
           loge("",e.message)
        }

        // Return the combined list of all channels
       // return@coroutineScope allChannels
    }

    suspend fun provideServerTimeStamp(healthUrl: String): Flow<WTVResponse<HealthAPIResponse>> =
        flow {
            try {
                val response = networkApiCallInterface
                    .makeHttpGetRequest(healthUrl)
                    .execute()
                if (response.isSuccessful) {
                    emit(WTVResponse.Success(HealthAPIResponse()))
                } else {
                    emit(WTVResponse.Failure(Throwable("Invalid response received")))
                }
            } catch (e: Exception) {
                emit(WTVResponse.Failure(e))
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


    suspend fun provideUserHash(hashUrl: String): Flow<WTVResponse<HashInfo>> =
        flow {
            try {
                val response = networkApiCallInterface
                    .makeHttpGetRequest(hashUrl)
                    .execute()
                if (response.isSuccessful) {
                    val hashInfo = response.body()?.toJSONObject()?.toString()
                        .convertIntoModel(HashInfo::class.java)
                    hashInfo?.let {
                        loge("hashUrl>",hashUrl+hashInfo.toString())

                        // Optionally save manifest data into ContentProvider or DB here
                        emit(WTVResponse.Success(hashInfo))
                    } ?: throw Exception("Failed to parse hashInfo")
                } else {
                    emit(WTVResponse.Failure(Throwable("Invalid response received")))
                }
            } catch (e: Exception) {
                emit(WTVResponse.Failure(e))
            }
        }.flowOn(Dispatchers.IO)


    suspend fun registerUserHash(hashUrl: String,requestBody: HashMap<String, String>): Flow<WTVResponse<HashInfo>> =
        flow {
            try {
                val response = networkApiCallInterface
                    .makeHttpPostRequest(hashUrl,requestBody)
                    .execute()
                if (response.isSuccessful) {
                    val hashInfo = response.body()?.toJSONObject()?.toString()
                        .convertIntoModel(HashInfo::class.java)
                    hashInfo?.let {
                        loge("hashUrl>",hashUrl+hashInfo.toString())

                        // Optionally save manifest data into ContentProvider or DB here
                        emit(WTVResponse.Success(hashInfo))
                    } ?: throw Exception("Failed to parse hashInfo")
                } else {
                    emit(WTVResponse.Failure(Throwable("Invalid response received")))
                }
            } catch (e: Exception) {
                emit(WTVResponse.Failure(e))
            }
        }.flowOn(Dispatchers.IO)


    suspend fun provideUserLogin(
        loginUrl: String,
        requestBody: HashMap<String, String>
    ): Flow<WTVResponse<LoginInfo>> = flow {
        try {
            loge("response:","$loginUrl ${requestBody}")
            val response = networkApiCallInterface.makeHttpPostLoginRequest(
                url = loginUrl,
                body = requestBody
            ).execute()
            if(response.isSuccessful && response.body() !=null){
                response.body()?.toJSONObject()?.toString().convertIntoModel(LoginInfo::class.java)?.let {
                    emit(WTVResponse.Success(it))
                }
            }
        } catch (e: Exception) {
            emit(WTVResponse.Failure(e))
        }
    }.flowOn(Dispatchers.IO)


    suspend fun provideUserProfileCMS(
        requestBody: HashMap<String, Any>
    ): Flow<WTVResponse<Boolean>> = flow {
        try {
            loge("url:","app/package-update> ${requestBody}")
            val response = networkApiCallInterface.makeHttpAnyPostRequest(url= Constants.BASE_URL +"app/package-update", body = requestBody).execute()
            if (response.isSuccessful  && response.body() != null) {
                emit(WTVResponse.Success(true))
            } else {
                emit(WTVResponse.Failure(Throwable("Invalid response received")))
            }
        } catch (e: Exception) {
            emit(WTVResponse.Failure(e))
        }
    }.flowOn(Dispatchers.IO)
}