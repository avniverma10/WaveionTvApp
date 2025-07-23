package com.android.panmetroiptv.utils.servicehelper

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import com.android.panmetroiptv.R
import com.android.panmetroiptv.extensions.loge

class BootService : Service() {
    override fun onCreate() {
        super.onCreate()
        startForeground(1, createNotification()) // Forces Android to run it immediately
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        loge("BootService", "BootService started, preparing to launch MainActivity...")

        Handler(Looper.getMainLooper()).postDelayed({
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

            if (Settings.canDrawOverlays(this)) {
                loge("BootService", "Launching MainActivity in foreground...")
                startActivity(launchIntent)
            } else {
                loge("BootService", "SYSTEM_ALERT_WINDOW permission missing! Cannot bring app to foreground.")
            }

            stopSelf() // Stop service after launch
        }, 6000) // Reduced delay from 5s to 1s

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val channelId = "boot_service_channel"
        val notificationManager = getSystemService(NotificationManager::class.java)

        val channel = NotificationChannel(channelId, "Boot Service", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        return Notification.Builder(this, channelId)
            .setContentTitle("Boot Service Running")
            .setContentText("Launching the app immediately after boot")
            .setSmallIcon(R.drawable.panlogin)
            .build()
    }
}
