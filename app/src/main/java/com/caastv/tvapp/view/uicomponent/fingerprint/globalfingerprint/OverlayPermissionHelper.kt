package com.caastv.tvapp.view.uicomponent.fingerprint.globalfingerprint


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import com.caastv.tvapp.utils.servicehelper.FingerprintOverlayService

class OverlayPermissionHelper(
    private val activity: ComponentActivity
) {
    // Exposed state for Compose
    val hasOverlayPermissionState = mutableStateOf(Settings.canDrawOverlays(activity))

    // Launcher for ACTION_MANAGE_OVERLAY_PERMISSION
    lateinit var overlayPermissionLauncher: ActivityResultLauncher<Intent>

    /** Call this from onCreate() before super.onStart()/onResume() */
    fun registerLauncher() {
        overlayPermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            // user returned from Settings
            val granted = Settings.canDrawOverlays(activity)
            hasOverlayPermissionState.value = granted
            if (granted) startOverlayServiceIfNeeded()
            else
            // you can surface a Toast or callback here if you like
                Unit
        }
    }

    /** Launch the system settings screen */
    fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${activity.packageName}")
        )
        overlayPermissionLauncher.launch(intent)
    }

    /** If we have permission, start the foreground service */
    fun startOverlayServiceIfNeeded() {
        if (!hasOverlayPermissionState.value) return
        val svcIntent = Intent(activity, FingerprintOverlayService::class.java)
            .apply { putExtra("MODE", "GLOBAL") }
        ContextCompat.startForegroundService(activity, svcIntent)
    }

    /** Ask the user to ignore battery optimizations (optional) */
    fun requestIgnoreBatteryOptimizationsIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return

        val pm = activity.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        if (!pm.isIgnoringBatteryOptimizations(activity.packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${activity.packageName}")
            }
            activity.startActivity(intent)
        }
    }
}

