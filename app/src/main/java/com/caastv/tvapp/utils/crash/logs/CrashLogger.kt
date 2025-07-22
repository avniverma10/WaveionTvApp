package com.caastv.tvapp.utils.crash.logs

import android.content.Context
import android.os.Build
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class CrashLogger(private val context: Context) {

    companion object {
        private const val LOG_DIR = "crash_logs"
        const val LOG_FILE_NAME = "error_logs.txt"
        private const val MAX_LOG_AGE_HOURS = 24
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    }

    fun logCrash(throwable: Throwable) {
        val logEntry = createLogEntry(throwable)
        saveLogEntry(logEntry)
        cleanupOldLogs()
    }

    private fun createLogEntry(throwable: Throwable): String {
        return """
            |===== CRASH REPORT =====
            |Timestamp: ${dateFormat.format(Date())}
            |App Version: ${getAppVersion()}
            |Device Info: ${getDeviceInfo()}
            |Exception: ${throwable.javaClass.name}
            |Message: ${throwable.message}
            |Stack Trace:
            |${getStackTrace(throwable)}
            |===== END REPORT =====
            """.trimMargin()
    }

    private fun getAppVersion(): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${pInfo.versionName} (${pInfo.versionCode})"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun getDeviceInfo(): String {
        return "Android ${Build.VERSION.RELEASE} (${Build.MANUFACTURER} ${Build.MODEL})"
    }

    private fun getStackTrace(throwable: Throwable): String {
        return StringWriter().use { sw ->
            PrintWriter(sw).use { pw ->
                throwable.printStackTrace(pw)
                sw.toString()
            }
        }
    }

    private fun saveLogEntry(logEntry: String) {
        try {
            val logDir = File(context.filesDir, LOG_DIR).apply { mkdirs() }
            File(logDir, LOG_FILE_NAME).appendText("$logEntry\n")
        } catch (e: Exception) {
            // Log to system if file logging fails
            android.util.Log.e("CrashLogger", "Failed to save log entry", e)
        }
    }

    private fun cleanupOldLogs() {
        try {
            val logFile = getLogFile()
            if (!logFile.exists()) return

            val currentTime = System.currentTimeMillis()
            val maxAgeMillis = TimeUnit.HOURS.toMillis(MAX_LOG_AGE_HOURS.toLong())

            val filteredLines = logFile.readLines()
                .filter { line ->
                    if (line.startsWith("Timestamp: ")) {
                        val timestampStr = line.substringAfter("Timestamp: ")
                        try {
                            val date = dateFormat.parse(timestampStr)
                            currentTime - date.time <= maxAgeMillis
                        } catch (e: Exception) {
                            true // Keep if we can't parse
                        }
                    } else true
                }

            if (filteredLines.size < logFile.readLines().size) {
                logFile.writeText(filteredLines.joinToString("\n"))
            }
        } catch (e: Exception) {
            android.util.Log.e("CrashLogger", "Failed to clean up logs", e)
        }
    }

    fun getLogFile(): File {
        return File(File(context.filesDir, LOG_DIR), LOG_FILE_NAME)
    }

    fun clearLogs() {
        try {
            getLogFile().delete()
        } catch (e: Exception) {
            android.util.Log.e("CrashLogger", "Failed to clear logs", e)
        }
    }
}