package com.example.tvapp.viewmodels


import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.provider.Settings
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager
import androidx.media3.exoplayer.drm.FrameworkMediaDrm
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.tvapp.extensions.decodeJwtToken
import com.example.tvapp.model.data.DataStoreManager
import com.example.tvapp.model.repository.WTVNetworkRepositoryImpl
import com.example.tvapp.view.player.generateWatermark
import com.example.tvapp.view.wtvplayer.WidevineMediaDrmCallback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class WTVPlayerViewModel @Inject constructor(
    private val wtvNetworkRepositoryImpl: WTVNetworkRepositoryImpl,
    private val application: Application,
    private val dataStoreManager: DataStoreManager) : WTVViewModel(application= application,networkApiCallInterfaceImpl= wtvNetworkRepositoryImpl) {
    // Mutable StateFlow to store the mobile number
    private var _mobileNumber = MutableLiveData<String?>(null)
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







}
