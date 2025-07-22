package com.caastv.tvapp.utils.crash.logs

import android.content.Context
import androidx.work.*
import com.caastv.tvapp.utils.network.NetworkApiCallInterface
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LogUploader @Inject constructor(
    private val context: Context,
    private val networkApiCallInterface: NetworkApiCallInterface
) {
    companion object {
        private const val UPLOAD_WORK_NAME = "auto_log_upload"
        private const val MIN_UPLOAD_INTERVAL = 24L // Minimum 24 hours between uploads
    }

    fun scheduleAutoUpload() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val uploadRequest = PeriodicWorkRequestBuilder<LogUploadWorker>(
            MIN_UPLOAD_INTERVAL,
            TimeUnit.HOURS,
            // Flex interval (last 1 hour of the period)
            MIN_UPLOAD_INTERVAL - 1, TimeUnit.HOURS
        ).setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UPLOAD_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            uploadRequest
        )
    }

    suspend fun uploadLogs(): Boolean {
        return try {
            val logFile = CrashLogger(context).getLogFile()
            if (!logFile.exists() || logFile.length() == 0L) return false

            val requestFile = logFile.asRequestBody("text/plain".toMediaType())
            val body = MultipartBody.Part.createFormData(
                "logfile",
                "error_logs_${System.currentTimeMillis()}.txt", // Unique filename
                requestFile
            )

            val response = networkApiCallInterface.uploadLogs(body)
            if (response.isSuccessful) {
                logFile.delete()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}

class LogUploadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @Inject
    lateinit var networkApiCallInterface: NetworkApiCallInterface

    override suspend fun doWork(): Result {
        return try {
            val uploader = LogUploader(applicationContext, networkApiCallInterface)
            if (uploader.uploadLogs()) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}