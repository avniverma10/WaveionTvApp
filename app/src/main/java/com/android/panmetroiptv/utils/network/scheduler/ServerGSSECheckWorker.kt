package com.android.panmetroiptv.utils.network.scheduler

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.android.panmetroiptv.viewmodels.SharedViewModel
import kotlinx.coroutines.delay
import java.security.SecureRandom

class ServerGSSECheckWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private var sharedViewModel: SharedViewModel?=null
        const val WORK_TAG = "SERVER_GLOBAL_SSE_CHECK"

        fun schedule(context: Context, sharedViewModel: SharedViewModel) {
            // Store ViewModel reference in ViewModelStore
            this.sharedViewModel = sharedViewModel
            val workRequest = OneTimeWorkRequestBuilder<ServerGSSECheckWorker>()
                .addTag(WORK_TAG)
                .build()

            WorkManager.getInstance(context)
                .enqueue(workRequest)
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context)
                .cancelAllWorkByTag(WORK_TAG)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        // Check server sse
        sharedViewModel?.provideGlobalSSERequest()

        if (sharedViewModel?.isGlobalSSEClosed?.value == true) {
            // If server is still down, reschedule in 1 second
            delay(secureRandomLong())
            sharedViewModel?.let {
                schedule(applicationContext, it)
            }
            return Result.retry()
        }

        return Result.success()
    }
}

fun secureRandomLong(): Long {
    return 10000L + SecureRandom().nextInt(20001).toLong()
}