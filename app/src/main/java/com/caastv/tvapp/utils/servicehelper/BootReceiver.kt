package com.caastv.tvapp.utils.servicehelper


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("BootReceiver", "Received intent: ${intent?.action}")

        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Starting BootService...")

            val serviceIntent = Intent(context, BootService::class.java)
            context.startForegroundService(serviceIntent)
        }
    }
}


