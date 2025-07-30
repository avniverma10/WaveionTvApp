package com.caastv.tvapp.utils.network.scheduler

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.caastv.tvapp.viewmodels.WTVViewModel
import kotlinx.coroutines.delay
import java.security.SecureRandom
import kotlin.random.Random

class ServerHealthCheckWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private var wtvViewModel: WTVViewModel?=null
        const val WORK_TAG = "SERVER_HEALTH_CHECK"
        private const val HEALTH_CHECK_URL = "your_health_check_endpoint" // Replace with your endpoint
        private const val HEALTH_CHECK_TIMEOUT = 3000L // 5 seconds timeout

        fun schedule(context: Context, viewModel: WTVViewModel) {
            // Store ViewModel reference in ViewModelStore
            this.wtvViewModel = viewModel
            val workRequest = OneTimeWorkRequestBuilder<ServerHealthCheckWorker>()
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
        // Check server health
        val isHealthy = wtvViewModel?.fetchServerTimeMillis() != null

        if (!isHealthy) {

            // If server is still down, reschedule in 1 second
            delay(secureRandomLong())
            wtvViewModel?.let {
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