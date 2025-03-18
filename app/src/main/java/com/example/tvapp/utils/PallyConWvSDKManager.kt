package com.example.tvapp.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import androidx.media3.exoplayer.source.MediaSource
import com.example.tvapp.extensions.showSimpleDialog
import com.pallycon.widevine.exception.PallyConException
import com.pallycon.widevine.exception.PallyConException.ContentDataException
import com.pallycon.widevine.exception.PallyConException.DetectedDeviceTimeModifiedException
import com.pallycon.widevine.exception.PallyConException.DrmException
import com.pallycon.widevine.exception.PallyConLicenseServerException
import com.pallycon.widevine.model.ContentData
import com.pallycon.widevine.model.DownloadState
import com.pallycon.widevine.model.PallyConDrmConfigration
import com.pallycon.widevine.model.PallyConEventListener
import com.pallycon.widevine.sdk.PallyConWvSDK
import com.pallycon.widevine.track.PallyConDownloaderTracks
import java.util.concurrent.Callable


class PallyConWvSDKManager {
    private var context: Context? = null
    private var activity: Activity? = null
    private var wvSDK: PallyConWvSDK? = null
    private val drmListener: PallyConEventListener = object : PallyConEventListener {
        override fun onFailed(contentData: ContentData, e: PallyConLicenseServerException?) {
            e?.printStackTrace()
            activity?.showSimpleDialog("onFailed", e?.message)
        }

        override fun onFailed(contentData: ContentData, e: PallyConException?) {
            e?.printStackTrace()
            activity?.showSimpleDialog("onFailed", e?.message)
        }

        override fun onPaused(contentData: ContentData) {
        }

        override fun onRemoved(contentData: ContentData) {
        }

        override fun onRestarting(contentData: ContentData) {
        }

        override fun onStopped(contentData: ContentData) {
        }

        override fun onProgress(contentData: ContentData, percent: Float, downloadedBytes: Long) {
        }

        override fun onCompleted(contentData: ContentData) {
        }
    }

    private object LazyHolder {
        @SuppressLint("StaticFieldLeak")
        val INSTANCE: PallyConWvSDKManager = PallyConWvSDKManager()
    }


    fun createSDK(context: Context) {

        // Enter DRM related information.
        val config = PallyConDrmConfigration(
            "site id",
            "site key", // Set to an empty string if you don't know
            "content token",
            "custom data",
            mutableMapOf(), // custom header
            "cookie"
        )

        /*
        val data = ContentData(
            contentId = "content id",
            url = "content URL",
            localPath = "Download location where content will be stored",
            drmConfig = config,
            cookie = null,
        )
        */

        val data = ContentData(
            url = "content URL",
            drmConfig = config,
            cookie = null,
        )


        wvSDK = PallyConWvSDK.createPallyConWvSDK(
            context,
            data
        )
        PallyConWvSDK.addPallyConEventListener(drmListener)
    }

    fun downloadLicense(onSuccess: Callable<Void?>) {
        wvSDK?.downloadLicense(
            null, {
                try {
                    onSuccess.call()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                null
            },
            { e: PallyConException ->
                e.printStackTrace()
                activity?.showSimpleDialog("content data is not correct", e.message)
                null
            })
    }

    fun download(tracks: PallyConDownloaderTracks) {
        try {
            wvSDK?.download(tracks)
        } catch (e: ContentDataException) {
            e.printStackTrace()
            activity?.showSimpleDialog( "content data error", e.message)
        } catch (e: PallyConException.DownloadException) {
            e.printStackTrace()
            activity?.showSimpleDialog("download exception", e.message)
        } catch (e: DrmException) {
            e.printStackTrace()
            activity?.showSimpleDialog( "drm exception", e.message)
        }
    }

    val mediaSource: MediaSource?
        get() {
            var mediaSource: MediaSource? = null
            try {
                mediaSource = wvSDK?.getMediaSource()
            } catch (e: DetectedDeviceTimeModifiedException) {
                e.printStackTrace()
                activity?.showSimpleDialog( "modified device time", e.message)
            } catch (e: ContentDataException) {
                e.printStackTrace()
                activity?.showSimpleDialog( "content data error", e.message)
            } finally {
                return mediaSource
            }
        }

    val downloadState: DownloadState?
        get() = wvSDK?.getDownloadState()

    fun defaultDownload() {
        wvSDK?.getContentTrackInfo({ tracks: PallyConDownloaderTracks ->
            download(tracks)
            null
        }, { e: PallyConException ->
            e.printStackTrace()
            activity?.showSimpleDialog("PallyConException", e.message)
            null
        })
    }

    fun removeLicense() {
        try {
            wvSDK!!.removeLicense()
        } catch (e: DrmException) {
            e.printStackTrace()
            activity?.showSimpleDialog("DrmException", e.message)
        }
    }

    fun remove() {
        try {
            wvSDK!!.remove()
        } catch (e: ContentDataException) {
            e.printStackTrace()
            activity?.showSimpleDialog( "content data error", e.message)
        } catch (e: PallyConException.DownloadException) {
            e.printStackTrace()
            activity?.showSimpleDialog("DownloadException", e.message)
        }
    }

    fun release() {
        wvSDK!!.release()
    }

    companion object {
        fun getInstance(activity: Activity): PallyConWvSDKManager {
            LazyHolder.INSTANCE.activity = activity
            LazyHolder.INSTANCE.context = activity.applicationContext
            return LazyHolder.INSTANCE
        }
    }
}