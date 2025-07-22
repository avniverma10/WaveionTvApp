package com.caastv.tvapp.utils.crash

import android.content.Context
import android.os.Build
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CustomCrashHandler(context: Context) : Thread.UncaughtExceptionHandler {
    private val defaultHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()
    private val context: Context? = context.applicationContext

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        // Prepare crash report
        val report = CrashReport()
        report.timestamp = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.getDefault()
        ).format(Date())
        report.stackTrace = Log.getStackTraceString(throwable)
        report.deviceInfo = Build.MODEL//getDeviceInfo()
        report.appVersion = android.os.Build.VERSION.SDK_INT.toString()//getAppVersion()
       // report.userActions = getRecentUserActions()


        // Send crash report synchronously
       // sendCrashReportSync(report)


        // Call default handler
        defaultHandler?.uncaughtException(thread, throwable)
    }

    /*private fun sendCrashReportSync(report: CrashReport?) {
        try {
            val service: LogService = ApiClient.getClient().create(LogService::class.java)
            val response: Response<Void?> = service.sendCrashReport(report).execute()
            if (!response.isSuccessful()) {
                Log.e("CrashHandler", "Failed to send crash report: " + response.code())
            }
        } catch (e: IOException) {
            Log.e("CrashHandler", "Error sending crash report", e)
        }
    } // Helper methods to gather device info, etc.*/
}