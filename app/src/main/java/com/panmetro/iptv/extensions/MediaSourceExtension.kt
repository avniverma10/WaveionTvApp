package com.panmetro.iptv.extensions

import android.content.Context
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager
import androidx.media3.exoplayer.drm.FrameworkMediaDrm
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.panmetro.iptv.model.data.epgdata.EPGDataItem
import com.panmetro.iptv.utils.Constants
import com.panmetro.iptv.utils.mediahelper.CryptoguardDrmCallback
import com.panmetro.iptv.view.wtvplayer.WidevineMediaDrmCallback
import kotlinx.coroutines.flow.first
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import com.panmetro.iptv.utils.uistate.PreferenceManager

/**
 * Returns a MediaSourceFactory configured with a DRM session manager if needed.
 */
fun Context.createWTVMediaSourceFactory(epgDataItem: EPGDataItem): DefaultMediaSourceFactory {
    return when (epgDataItem?.drmType) {
        "sigma" -> {
            // Replace with the actual SIGMA DRM UUID and callback implementation.
            this.provideSigmaSourceFactory()
        }
        "cryptoguard" -> {
            // Replace with the actual CryptoGuard DRM UUID and callback implementation.
            provideCryptoGuardSourceFactory(contentUrl="https://admin.cryptoplay.tv/content/live/MTV_Live_HD/master.mpd", contentId = "546465f1-a54c-4146-b417-c5d5ac0d0802")
        }
        else -> {
            // No DRM configuration.
            DefaultMediaSourceFactory(this)
        }
    }
}


//for Sigma DRM
@OptIn(UnstableApi::class)
fun Context.provideSigmaSourceFactory(defaultLicenseUrl:String="https://license-staging.sigmadrm.com/license/verify/widevine"): DefaultMediaSourceFactory {
    //userInfo:UserInfo?=null
    // Create a default DataSource.Factory (Media3 version).
    val defaultDataSourceFactory = DefaultDataSource.Factory(this)
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


//for Cryptoguard DRM
@OptIn(UnstableApi::class)
fun Context.provideCryptoGuardSourceFactory(defaultLicenseUrl:String="https://drm.panmetroconvergence.com:4443/?",contentUrl:String?="https://nextwave.waveiontechnologies.com:8447/ottproxy/live/disk0/BHARAT_24/CG_DASH/BHARAT_24.mpd",contentId:String?="a9e277d2-7e1a-4bbb-9443-731a921d9ff0"): DefaultMediaSourceFactory {
    // Build URL with query parameters using OkHttp's HttpUrl builder.
    val httpUrl = defaultLicenseUrl.toHttpUrlOrNull()?.newBuilder()
        ?.addQueryParameter("PlayState", "1")
        ?.addQueryParameter("DrmSystem", "Widevine")
        ?.addQueryParameter("LoginName", "josip".toBase64Encoded())
        ?.addQueryParameter("Password", "cryptoguard".toBase64Encoded())
        ?.addQueryParameter("KeyId", contentId?.toBase64Encoded())
        ?.addQueryParameter("UniqueDeviceId", findMyDeviceId()?.toBase64Encoded())
        ?.addQueryParameter("ContentUrl", contentUrl?.toBase64Encoded())
        ?.addQueryParameter("DeviceTypeName", "Android TV".toBase64Encoded())
        ?.build()
    val licenseUrl = httpUrl.toString().replace("https://drm.panmetroconvergence.com:4443/?&","https://drm.panmetroconvergence.com:4443/?")

    // Create a default DataSource.Factory (Media3 version).
    val defaultDataSourceFactory = DefaultDataSource.Factory(this)
    // Create a DefaultHttpDataSource.Factory from Media3.
    val httpDataSourceFactory = DefaultHttpDataSource.Factory()
    // Create your WidevineMediaDrmCallback.
    // (Ensure you have implemented WidevineMediaDrmCallback as per your requirements.)
    val drmCallback = CryptoguardDrmCallback(
        defaultLicenseUrl = licenseUrl,
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

//for Cryptoguard DRM
@OptIn(UnstableApi::class)
suspend fun Context.provideCryptoGuardMediaSource(defaultLicenseUrl:String= Constants.DRM_LICENSE_BASE+"/", contentUrl:String?=null, contentId:String?=null, logData:HashMap<String,String>?=null): MediaItem {
    val uNamme = PreferenceManager.getUsername()?:""
    val pwd = PreferenceManager.getPassword()

    val macAddress = provideMacAddress()
    loge("loginInfo>>","${uNamme},${pwd},>${macAddress}")
    // Build URL with query parameters using OkHttp's HttpUrl builder.
    val httpUrl = defaultLicenseUrl.toUri().buildUpon()
        .appendQueryParameter("PlayState",      "1")
        .appendQueryParameter("DrmSystem",      "Widevine")
        .appendQueryParameter("LoginName",      uNamme.toBase64UrlSafe())
        .appendQueryParameter("Password",       pwd?.toBase64UrlSafe())
        .appendQueryParameter("KeyId",          contentId?.toBase64UrlSafe())
        .appendQueryParameter("UniqueDeviceId", macAddress?.toBase64UrlSafe())
        .appendQueryParameter("ContentUrl",     contentUrl?.toBase64UrlSafe())
        .appendQueryParameter("DeviceTypeName", "Android TV".toBase64UrlSafe())
        .build()
    val licenseUrl = httpUrl.toString().replace(Constants.DRM_LICENSE_BASE+"/?",Constants.DRM_LICENSE_BASE+"?")
    logData?.put("licenseUrl",licenseUrl)
    loge("loginInfo>",contentUrl.toString())
    loge("loginInfo>",licenseUrl)

    return MediaItem.Builder()
        .setUri(contentUrl)
        .setDrmConfiguration(
            MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
                .setLicenseUri(licenseUrl) // Base license URL (will be modified in callback)
                .build()
        ).build()
}



