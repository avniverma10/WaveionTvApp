package com.panmetro.iptv.model.repository.common

import androidx.annotation.Keep
import com.panmetro.iptv.extensions.convertIntoModel
import com.panmetro.iptv.extensions.convertIntoModels
import com.panmetro.iptv.extensions.logReport
import com.panmetro.iptv.extensions.loge
import com.panmetro.iptv.extensions.toJSONArray
import com.panmetro.iptv.extensions.toJSONObject
import com.panmetro.iptv.model.data.appupdate.AppUpdateResponse
import com.panmetro.iptv.model.data.banner.Banner
import com.panmetro.iptv.model.data.epgdata.EPGDataItem
import com.panmetro.iptv.model.data.genre.WTVGenre
import com.panmetro.iptv.model.data.hash.HashInfo
import com.panmetro.iptv.model.data.health.HealthAPIResponse
import com.panmetro.iptv.model.data.home.HomeData
import com.panmetro.iptv.model.data.language.WTVLanguage
import com.panmetro.iptv.model.data.login.CustomerChannelsInfo
import com.panmetro.iptv.model.data.login.CustomerPackageInfo
import com.panmetro.iptv.model.data.login.DRMUserInfo
import com.panmetro.iptv.model.data.login.LoginInfo
import com.panmetro.iptv.model.data.manifest.WTVManifest
import com.panmetro.iptv.model.home.WTVHomeCategory
import com.panmetro.iptv.model.notification.NotificationItem
import com.panmetro.iptv.utils.Constants
import com.panmetro.iptv.utils.network.NetworkApiCallInterface
import com.panmetro.iptv.utils.sealed.WTVListResponse
import com.panmetro.iptv.utils.sealed.WTVResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.panmetro.iptv.model.data.epgdata.EPGContentInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import org.json.JSONException
import org.json.JSONObject
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

    suspend fun provideWTVEPGData(epgContentUrl: String): Flow<WTVResponse<EPGContentInfo>> = flow {
        try {
            val response = networkApiCallInterface.makeHttpGetRequest(epgContentUrl).execute()
            if (response.isSuccessful && response.body() != null) {
                loge("API:","Url:${epgContentUrl}>${response.body()}")
                val epgData: EPGContentInfo? = response.body()?.toJSONObject()?.toString()
                    .convertIntoModel(EPGContentInfo::class.java)
                // Optionally save EPG data into ContentProvider or DB here
                emit(WTVResponse.Success(epgData!!))
            } else {
                emit(WTVResponse.Failure(Throwable("Invalid response received")))
            }
        } catch (e: Exception) {
            emit(WTVResponse.Failure(e))
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
            }else{
                val errorBody = response.errorBody()?.string()
                if (!errorBody.isNullOrEmpty()) {
                    try {
                        val jsonObject = JSONObject(errorBody)
                        val message = jsonObject.getString("message")
                        // Use the message: "Password incorrect"

                        emit(WTVResponse.Failure(Throwable(message)))
                    } catch (e: JSONException) {
                        // Handle JSON parsing error
                        loge("Error", "Failed to parse error JSON: $errorBody")
                    }
                }
            }
        } catch (e: Exception) {
            emit(WTVResponse.Failure(Throwable(e.message)))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getCustomerChannelInfo(pkgName: String) : Flow<WTVResponse<Set<String>>> = flow {
        try {
            val requestUrl = "${Constants.LOGIN_SMS_BASE}src/api/v1/services-assets/livechannels/$pkgName?page=1&limit=1000"
            val response = withContext(Dispatchers.IO) {
                networkApiCallInterface.makeDRMHttpGetRequest(requestUrl).execute()
            }

            if (response.isSuccessful) {
                val channels = response.body()?.let { body ->
                    try {
                        val jsonObject = body.toJSONObject()
                        val channelInfo = jsonObject?.toString()?.convertIntoModel(CustomerChannelsInfo::class.java)
                        channelInfo?.results?.mapNotNull { it.contentId }?.toSet() ?: emptySet()
                    } catch (e: Exception) {
                        loge("ChannelFetch", "JSON parsing error for $pkgName: ${e.message}")
                        emptySet()
                    }
                } ?: emptySet()

                emit(WTVResponse.Success(channels))
            }else{
                val errorBody = response.errorBody()?.string()
                if (!errorBody.isNullOrEmpty()) {
                    try {
                        val jsonObject = JSONObject(errorBody)
                        val message = jsonObject.getString("message")
                        loge("message", message)
                        emit(WTVResponse.Failure(Throwable(message)))
                    } catch (e: JSONException) {
                        // Handle JSON parsing error
                        loge("Error", "Failed to parse error JSON: $errorBody")
                    }
                }
            }
        } catch (e: Exception) {
            val errorMessage = "Network error fetching channels for package $pkgName: ${e.message}"
            loge("ChannelFetch", errorMessage)
            emit(WTVResponse.Failure(Throwable(e.message)))
        }
    }.flowOn(Dispatchers.IO)

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
            }else{
                val errorBody = response.errorBody()?.string()
                if (!errorBody.isNullOrEmpty()) {
                    try {
                        val jsonObject = JSONObject(errorBody)
                        val message = jsonObject.getString("message")
                        // Use the message: "Password incorrect"

                        emit(WTVResponse.Failure(Throwable(message)))
                    } catch (e: JSONException) {
                        // Handle JSON parsing error
                        loge("Error", "Failed to parse error JSON: $errorBody")
                    }
                }
            }
        } catch (e: Exception) {
            emit(WTVResponse.Failure(Throwable(e.message)))
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