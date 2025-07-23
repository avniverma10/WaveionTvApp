package com.android.panmetroiptv.utils.servicehelper


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.android.panmetroiptv.extensions.loge

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        loge("BootReceiver", "Received intent: ${intent?.action}")

        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            loge("BootReceiver", "Starting BootService...")

            val serviceIntent = Intent(context, BootService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }
}


