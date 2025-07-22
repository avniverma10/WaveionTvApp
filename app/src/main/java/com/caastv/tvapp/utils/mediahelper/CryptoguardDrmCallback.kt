package com.caastv.tvapp.utils.mediahelper


import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DataSourceInputStream
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.drm.ExoMediaDrm
import androidx.media3.exoplayer.drm.MediaDrmCallback
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.UUID

@UnstableApi
class CryptoguardDrmCallback(
    private val defaultLicenseUrl: String,
    private val dataSourceFactory: HttpDataSource.Factory
) : MediaDrmCallback {
    private val client = OkHttpClient()

    @Throws(IOException::class)
    override fun executeKeyRequest(uuid: UUID, request: ExoMediaDrm.KeyRequest): ByteArray {
        // Use the default license URL provided (ensure that it includes all required query parameters).
        // If necessary, you can append additional data from request.data.
        val url = defaultLicenseUrl
        val httpRequest = Request.Builder()
            .url(url)
            .build()

        client.newCall(httpRequest).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected response code ${response.code}")
            }
            return response.body?.bytes() ?: throw IOException("Empty response body")
        }
    }

    @Throws(IOException::class)
    override fun executeProvisionRequest(uuid: UUID, request: ExoMediaDrm.ProvisionRequest): ByteArray {
        // Construct the provisioning URL by appending the signed request.
        val url = request.defaultUrl + "&signedRequest=" + Util.fromUtf8Bytes(request.data)
        return executePost(dataSourceFactory, url, Util.EMPTY_BYTE_ARRAY, null)
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
        val dataSpec = DataSpec.Builder()
            .setUri(Uri.parse(url))
            .setHttpMethod(DataSpec.HTTP_METHOD_POST)
            .setHttpBody(data)
            .setPosition(0)
            .setLength(-1)
            .setFlags(DataSpec.FLAG_ALLOW_GZIP)
            .build()

        DataSourceInputStream(dataSource, dataSpec).use { inputStream ->
            return Util.toByteArray(inputStream)
        }
    }
}