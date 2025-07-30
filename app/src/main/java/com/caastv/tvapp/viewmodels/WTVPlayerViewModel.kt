package com.caastv.tvapp.viewmodels


import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager
import androidx.media3.exoplayer.drm.FrameworkMediaDrm
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.caastv.tvapp.extensions.findMyDeviceId
import com.caastv.tvapp.extensions.toBase64Encoded
import com.caastv.tvapp.model.data.DataStoreManager
import com.caastv.tvapp.model.repository.common.WTVNetworkRepositoryImpl
import com.caastv.tvapp.view.uicomponent.generateWatermark
import com.caastv.tvapp.view.wtvplayer.WidevineMediaDrmCallback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
open class WTVPlayerViewModel @Inject constructor(
    private val wtvNetworkRepositoryImpl: WTVNetworkRepositoryImpl,
    private val application: Application,
    private val dataStoreManager: DataStoreManager) : WTVViewModel(application= application,networkApiCallInterfaceImpl= wtvNetworkRepositoryImpl) {
    // Mutable StateFlow to store the mobile number
    private var _mobileNumber = MutableLiveData<String?>(null)
    companion object {
        private const val KEY_GENRE = "selectedGenreIndex"
        private const val KEY_CHANNEL = "selectedChannelIndex"
        private const val SELECTED_CHANNEL = "selectedChannel"
    }




    // State to trigger video playback for a particular channel.
    private val _selectedVideoUrl = MutableStateFlow<String?>(null)
    val selectedVideoUrl: StateFlow<String?> = _selectedVideoUrl.asStateFlow()

    // Observe mobile number
    init {
        // Collect mobile number from DataStore
        viewModelScope.launch {
            dataStoreManager.authToken.collect { number ->
                _mobileNumber.value = number
            }
        }
    }



    @OptIn(UnstableApi::class)
    fun provideMediaSourceFactory(context: Context,defaultLicenseUrl:String="https://license-staging.sigmadrm.com/license/verify/widevine"):DefaultMediaSourceFactory{
        //userInfo:UserInfo?=null
        // Create a default DataSource.Factory (Media3 version).
        val defaultDataSourceFactory = DefaultDataSource.Factory(context)
        // Create a DefaultHttpDataSource.Factory from Media3.
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        // Create your WidevineMediaDrmCallback.
        // (Ensure you have implemented WidevineMediaDrmCallback as per your requirements.)
        val drmCallback = WidevineMediaDrmCallback(
            userSigmaDetail = null,
            defaultLicenseUrl = defaultLicenseUrl,
            forceDefaultLicenseUrl = false,
            dataSourceFactory = httpDataSourceFactory
        )

        // Create a DRM session manager using Media3's DefaultDrmSessionManager.Builder.
        val drmSessionManager = DefaultDrmSessionManager.Builder()
            .setUuidAndExoMediaDrmProvider(C.WIDEVINE_UUID) { uuid ->
                FrameworkMediaDrm.newInstance(uuid)
            }.build(drmCallback)

        // Create a media source factory with DRM integration.
        val mediaSourceFactory = DefaultMediaSourceFactory(defaultDataSourceFactory)
            .setDrmSessionManagerProvider { drmSessionManager }

        return mediaSourceFactory
    }


    @SuppressLint("HardwareIds")
    fun provideWatermarkHash(context: Context):String{
        // Device ID
        val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return generateWatermark(_mobileNumber.value, deviceId)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun provideMediaSourceFactory(contentUrl:String, context: Context){
            // Base URL without the trailing '?' because HttpUrl.Builder will add it
            val baseUrl = "https://cryptoguard.waveiontechnologies.com:4443"

            // Build URL with query parameters using OkHttp's HttpUrl builder.
            val httpUrl = baseUrl.toHttpUrlOrNull()?.newBuilder()
                ?.addQueryParameter("PlayState", "1")
                ?.addQueryParameter("DrmSystem", "Widevine")
                ?.addQueryParameter("LoginName", "am9zaXA=".toBase64Encoded())
                ?.addQueryParameter("Password", "Y3J5cHRvZ3VhcmQ=".toBase64Encoded())
                ?.addQueryParameter("KeyId", "NTQ2NDY1ZjEtYTU0Yy00MTQ2LWI0MTctYzVkNWFjMGQwODAy".toBase64Encoded())
                ?.addQueryParameter("UniqueDeviceId", (context.findMyDeviceId()?:"").toBase64Encoded())
                ?.addQueryParameter("ContentUrl", contentUrl.toBase64Encoded())
                ?.addQueryParameter("DeviceTypeName", "android".toBase64Encoded())
                ?.build()

            if (httpUrl == null) {
                println("Invalid URL.")
                return
            }

            // Create the GET request
            val request = Request.Builder()
                .url(httpUrl)
                .get()
                .build()


            // Asynchronously execute the request
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // Handle failure, e.g. log or update UI accordingly
                    println("DRM request failed: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) {
                            println("Unexpected response code: ${response.code}")
                        } else {
                            // Process the response (for example, parse the DRM license)
                            val responseBody = response.body?.string()
                            println("DRM License Response: $responseBody")
                            // TODO: Implement DRM license handling logic here.
                        }
                    }
                }
            })
        }

    }
