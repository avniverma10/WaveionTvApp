package com.caastv.tvapp.utils.mediahelper

import android.net.Uri
import android.text.TextUtils
import android.util.Base64
import androidx.annotation.OptIn
import androidx.media3.common.util.Assertions
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DataSourceInputStream
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.drm.ExoMediaDrm
import androidx.media3.exoplayer.drm.MediaDrmCallback
import com.caastv.tvapp.extensions.loge
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.HashMap
import java.util.UUID


@UnstableApi
class SigmaDrmCallback(
    private val userSigmaDetail:JSONObject?=null,
    private val defaultLicenseUrl: String,
    private val forceDefaultLicenseUrl: Boolean = false,
    private val dataSourceFactory: HttpDataSource.Factory) : MediaDrmCallback {

    private val keyRequestProperties = HashMap<String, String>()

    fun setKeyRequestProperty(name: String, value: String) {
        Assertions.checkNotNull(name)
        Assertions.checkNotNull(value)
        keyRequestProperties[name] = value
    }

    fun clearKeyRequestProperty(name: String) {
        Assertions.checkNotNull(name)
        keyRequestProperties.remove(name)
    }

    fun clearAllKeyRequestProperties() {
        keyRequestProperties.clear()
    }

    @Throws(IOException::class)
    override fun executeProvisionRequest(
        uuid: UUID,
        request: ExoMediaDrm.ProvisionRequest
    ): ByteArray {
        val url = request.defaultUrl + "&signedRequest=" + Util.fromUtf8Bytes(request.data)
        return executePost(dataSourceFactory, url, Util.EMPTY_BYTE_ARRAY, null)
    }

    @Throws(Exception::class)
    override fun executeKeyRequest(uuid: UUID, request: ExoMediaDrm.KeyRequest): ByteArray {
        var url = request.licenseServerUrl
        if (forceDefaultLicenseUrl || TextUtils.isEmpty(url)) {
            url = defaultLicenseUrl
        }
        val requestProperties = HashMap<String, String>()
        val contentType = "application/octet-stream"
        requestProperties["Content-Type"] = contentType
        requestProperties["custom-data"] = getCustomData()

        synchronized(keyRequestProperties) {
            requestProperties.putAll(keyRequestProperties)
        }
        val responseData = executePost(dataSourceFactory, url, request.data, requestProperties)
        try {
            val jsonObject = JSONObject(String(responseData))
            // If you use feature license encryption, uncomment the next three lines:
            // val licenseInBase64 = SigmaDrmPacker.extractLicense(jsonObject.getString("license"))
            // loge("WidevineDRM", "License Data: $licenseInBase64")
            // return Base64.decode(licenseInBase64, Base64.DEFAULT)
            // Otherwise, simply decode the license:
            return Base64.decode(jsonObject.getString("license"), Base64.DEFAULT)
        } catch (e: JSONException) {
            loge("WidevineDRM", "Error parsing DRM response: " + String(responseData)+ " " + e.message)
            throw RuntimeException("Error parsing DRM response", e)
        }
    }

    @OptIn(UnstableApi::class)
    @Throws(IOException::class)
    private fun executePost(
        dataSourceFactory: HttpDataSource.Factory,
        url: String,
        data: ByteArray,
        requestProperties: Map<String, String>?
    ): ByteArray {
        val dataSource = dataSourceFactory.createDataSource()
        requestProperties?.forEach { (key, value) ->
            dataSource.setRequestProperty(key, value)
        }
        while (true) {
            val dataSpec = DataSpec.Builder()
                .setUri(Uri.parse(url))
                .setHttpMethod(DataSpec.HTTP_METHOD_POST)
                .setHttpBody(data)
                .setPosition(0)
                .setLength(-1)
                .setFlags(DataSpec.FLAG_ALLOW_GZIP)
                .build()

            val inputStream = DataSourceInputStream(dataSource, dataSpec)
            try {
                return Util.toByteArray(inputStream)
            } catch (e: Exception) {
                throw e
            } finally {
                Util.closeQuietly(inputStream)
            }
        }
    }


    @Throws(Exception::class)
    private fun getCustomData(): String {
        val userSigmaDetail = userSigmaDetail ?: kotlin.run {
            val customData = JSONObject()
            customData.put("userId", "1-6849382")
            customData.put("sessionId", "exoplayer_sessionId_123456")
            customData.put("merchantId", "waveion")
            customData.put("appId", "test_app")
            customData
        }

        // If you use feature license encryption with SigmaDrmPacker, uncomment the following lines:
        // val requestInfo: com.sigma.packer.RequestInfo? = SigmaDrmPacker.requestInfo()
        // if (requestInfo != null) {
        //     customData.put("reqId", requestInfo.requestId)
        //     customData.put("deviceInfo", requestInfo.deviceInfo)
        // }
        return Base64.encodeToString(userSigmaDetail.toString().toByteArray(), Base64.NO_WRAP)
    }
}