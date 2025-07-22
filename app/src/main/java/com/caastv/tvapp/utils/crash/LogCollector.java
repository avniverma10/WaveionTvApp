package com.caastv.tvapp.utils.crash;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.caastv.tvapp.utils.network.NetworkApiCallInterface;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LogCollector {
    private static final int MAX_LOG_ENTRIES = 50;
    private final LinkedList<LogEntry> logBuffer = new LinkedList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final NetworkApiCallInterface networkApiCallInterface;
    private static LogCollector instance;

    @Inject
    public LogCollector(NetworkApiCallInterface networkApiCallInterface) {
        this.networkApiCallInterface = networkApiCallInterface;
        instance = this;
    }

    public void initialize(Context context) {
        // Redirect standard Log output
        LoggingInterceptor.setup();
    }

    public void log(String level, String tag, String message) {
        LogEntry entry = new LogEntry();
        entry.setTimestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        entry.setLogLevel(level);
        entry.setTag(tag);
        entry.setMessage(message);
        entry.setDeviceInfo(Build.MODEL);
        entry.setAppVersion(String.valueOf(Build.VERSION.SDK_INT));

        synchronized (logBuffer) {
            logBuffer.add(entry);
            if (logBuffer.size() > MAX_LOG_ENTRIES) {
                logBuffer.removeFirst();
            }
        }

        // Send log asynchronously
        executor.execute(() -> sendLog(entry));
    }

    private void sendLog(LogEntry entry) {
        /*try {
            // Option 1: If using fixed endpoint (preferred)
            Response<Void> response = networkApiCallInterface.makeHttpPostCrashRequest("",entry).execute();

            // Option 2: If using dynamic URL
            // Response<Void> response = networkApiCallInterface.makeHttpPostCrashRequest("your/api/endpoint", entry).execute();

            if (!response.isSuccessful()) {
                Log.e("LogCollector", "Failed to send log: " + response.code());
            }
        } catch (IOException e) {
            Log.e("LogCollector", "Error sending log", e);
        }*/
    }

    public List<LogEntry> getRecentLogs() {
        synchronized (logBuffer) {
            return new ArrayList<>(logBuffer);
        }
    }

    // Static helper method for convenience
    public static void logStatic(String level, String tag, String message) {
        if (instance != null) {
            instance.log(level, tag, message);
        } else {
            Log.e("LogCollector", "Logger not initialized - falling back to system log");
            Log.println(getPriorityFromLevel(level), tag, message);
        }
    }

    private static int getPriorityFromLevel(String level) {
        switch (level) {
            case "VERBOSE": return Log.VERBOSE;
            case "DEBUG": return Log.DEBUG;
            case "INFO": return Log.INFO;
            case "WARN": return Log.WARN;
            case "ERROR": return Log.ERROR;
            case "ASSERT": return Log.ASSERT;
            default: return Log.DEBUG;
        }
    }
}