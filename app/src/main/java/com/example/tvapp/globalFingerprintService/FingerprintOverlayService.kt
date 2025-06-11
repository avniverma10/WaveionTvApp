// File: FingerprintOverlayService.kt
package com.example.tvapp.globalFingerprintService

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.tvapp.WTVApp
import com.example.tvapp.model.data.sseresponse.Fingerprint
import com.example.tvapp.model.data.sseresponse.GlobalSSEResponse
import com.example.tvapp.utils.Constants
import com.example.tvapp.utils.uistate.PreferenceManager
import com.example.tvapp.extensions.convertIntoModel
import com.example.tvapp.extensions.generateTextFingerprint
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit

class FingerprintOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var sseEventSource: EventSource? = null

    // Keep one TextView per visible watermark
    private val watermarkViews = mutableListOf<TextView>()

    // Keep track of scheduled Runnables so we can cancel them on update or teardown
    private val scheduledRunnables = mutableListOf<Runnable>()

    // Always post UI changes on the main thread:
    private val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        private const val TAG = "FingerprintOverlaySvc"
        private const val NOTIF_CHANNEL_ID = "fingerprint_overlay_channel"
        private const val NOTIF_ID = 5551
    }

    // Observe ProcessLifecycleOwner so we clear overlays when app goes to foreground
    private val appLifecycleObserver = object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onAppForegrounded() {
            // App is now in foreground → clear any overlays immediately
            clearAllOverlays()
        }
        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onAppBackgrounded() {
            // App backgrounded; next SSE update will re‐add overlays if enabled
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        PreferenceManager.init(applicationContext)

        // Register our lifecycle observer
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        setupOverlayIfNeeded()
        startFingerprintSSE()
        return START_REDELIVER_INTENT
    }

    private fun setupOverlayIfNeeded() {
        if (windowManager != null) return
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        startInForeground()
    }

    private fun startInForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIF_CHANNEL_ID,
                "Fingerprint Overlay Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply { setShowBadge(false) }
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
            .setContentTitle("Fingerprint Overlay Running")
            .setContentText("Showing global fingerprint watermark")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setOngoing(true)
            .build()

        startForeground(NOTIF_ID, notification)
    }

    private fun startFingerprintSSE() {
        val login = PreferenceManager.getLoginResponse()
        val userId = login?.loginData?.userId?.toString() ?: ""
        val userName = login?.loginData?.username ?: ""
        val pkgList = login?.loginData?.packages
            ?.joinToString(",") { it.packageName } ?: ""

        val baseUri = Constants.BASE_URL + "app/combined-sse?"
        val sseUrl = buildString {
            append(baseUri)
            if (userId.isNotBlank() && userName.isNotBlank()) {
                append("user=").append(userId).append(":").append(userName)
            }
            if (pkgList.isNotBlank()) {
                if (!endsWith("?")) append("&")
                append("package=").append(pkgList)
            }
        }

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val withHeaders = original.newBuilder()
                    .header("x-api-key", Constants.HEADER_TOKEN)
                    .build()
                chain.proceed(withHeaders)
            }
            .retryOnConnectionFailure(true)
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()

        val request = Request.Builder().url(sseUrl).build()

        sseEventSource?.cancel()
        sseEventSource = EventSources.createFactory(client).newEventSource(request,
            object : EventSourceListener() {
                override fun onOpen(eventSource: EventSource, response: Response) {
                    Log.d(TAG, "SSE opened: $sseUrl")
                }

                override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                    try {
                        val globalResp = data.convertIntoModel(GlobalSSEResponse::class.java)
                        val enabledList = globalResp?.fingerprints?.filter { it.enabled == true } ?: emptyList()
                        mainHandler.post {
                            updateAllFingerprints(enabledList)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing SSE JSON: ${e.message}")
                    }
                }

                override fun onClosed(eventSource: EventSource) {
                    Log.d(TAG, "SSE closed by server. Reconnecting in 3s…")
                    mainHandler.postDelayed({ startFingerprintSSE() }, 3_000)
                }

                override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                    Log.e(TAG, "SSE failure: ${t?.message}. Reconnecting in 3s…")
                    eventSource.cancel()
                    mainHandler.postDelayed({ startFingerprintSSE() }, 3_000)
                }
            }
        )
    }

    private fun clearAllOverlays() {
        watermarkViews.forEach { tv ->
            try { windowManager?.removeView(tv) } catch (_: Exception) { }
        }
        watermarkViews.clear()

        scheduledRunnables.forEach { mainHandler.removeCallbacks(it) }
        scheduledRunnables.clear()
    }

    private fun updateAllFingerprints(fps: List<Fingerprint>) {
        if (WTVApp.isInForeground) {
            clearAllOverlays()
            return
        }

        clearAllOverlays()
        if (fps.isEmpty()) return

        val metrics = resources.displayMetrics
        val sw = metrics.widthPixels
        val sh = metrics.heightPixels

        fps.forEach { fp ->
            val intervalMillis = (fp.intervalSec ?: 5) * 1_000L
            val durationMillis = (fp.durationMs ?: 60_000).toLong()
            val repeats = (fp.repeatCount ?: 1).coerceAtLeast(1)

            var runCount = 0
            var currentTv: TextView? = null

            val fingerprintRunnable = object : Runnable {
                override fun run() {
                    // If app enters foreground mid‐cycle, remove overlay and stop
                    if (WTVApp.isInForeground) {
                        currentTv?.let { tv ->
                            try { windowManager?.removeView(tv) } catch (_: Exception) { }
                            watermarkViews.remove(tv)
                        }
                        return
                    }

                    if (runCount >= repeats) {
                        currentTv?.let { tv ->
                            try { windowManager?.removeView(tv) } catch (_: Exception) { }
                            watermarkViews.remove(tv)
                        }
                        return
                    }
                    runCount++

                    // Remove old TextView if it still exists
                    currentTv?.let { tv ->
                        try { windowManager?.removeView(tv) } catch (_: Exception) { }
                        watermarkViews.remove(tv)
                    }

                    // Compute new position
                    val (posX, posY) = if (fp.positionMode == "RANDOM") {
                        val fracX = kotlin.random.Random.nextFloat().coerceIn(0f, 1f)
                        val fracY = kotlin.random.Random.nextFloat().coerceIn(0f, 1f)
                        Pair((sw * fracX).toInt(), (sh * fracY).toInt())
                    } else {
                        val rawX = fp.posXPercent ?: 50f
                        val rawY = fp.posYPercent ?: 50f
                        val fracX = rawX.normalizeFraction()
                        val fracY = rawY.normalizeFraction()
                        Pair((sw * fracX).toInt(), (sh * fracY).toInt())
                    }

                    // Create a new TextView overlay
                    val tv = TextView(this@FingerprintOverlayService).apply {
                        text = context.generateTextFingerprint(
                            method = fp.method,
                            obfuscationKey = fp.obfuscationKey ?: ""
                        )
                        val fontSizeSp = fp.fontSizeDp?.toFloat() ?: 12f
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeSp)

                        runCatching {
                            val color = Color.parseColor(fp.fontColorHex ?: "#FFFFFF")
                            val alpha = (
                                    (1f - (fp.fontTransparency?.toFloatOrNull() ?: 0.5f))
                                        .coerceIn(0f, 1f) * 255
                                    ).toInt() and 0xFF
                            setTextColor((color and 0x00FFFFFF) or (alpha shl 24))
                        }.onFailure { setTextColor(Color.WHITE) }

                        runCatching {
                            val bg = Color.parseColor(fp.backgroundColorHex ?: "#000000")
                            val alpha = (
                                    (1f - (fp.backgroundTransparency?.toFloatOrNull() ?: 0.5f))
                                        .coerceIn(0f, 1f) * 255
                                    ).toInt() and 0xFF
                            setBackgroundColor((bg and 0x00FFFFFF) or (alpha shl 24))
                        }.onFailure { setBackgroundColor(Color.parseColor("#80000000")) }

                        setPadding(8.dpToPx(), 4.dpToPx(), 8.dpToPx(), 4.dpToPx())
                        typeface = Typeface.MONOSPACE
                        ellipsize = TextUtils.TruncateAt.END
                        maxLines = 1
                        isFocusable = false
                        isClickable = false
                        visibility = TextView.VISIBLE
                    }

                    // Build LayoutParams
                    val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    } else {
                        @Suppress("DEPRECATION")
                        WindowManager.LayoutParams.TYPE_PHONE
                    }
                    val lp = WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        layoutFlag,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        PixelFormat.TRANSLUCENT
                    ).apply {
                        gravity = Gravity.TOP or Gravity.START
                        x = posX
                        y = posY
                    }

                    windowManager?.addView(tv, lp)
                    watermarkViews.add(tv)
                    currentTv = tv

                    // Schedule removal after durationMillis
                    mainHandler.postDelayed({
                        if (WTVApp.isInForeground) {
                            currentTv?.let { existingTv ->
                                try { windowManager?.removeView(existingTv) } catch (_: Exception) { }
                                watermarkViews.remove(existingTv)
                                if (currentTv === existingTv) {
                                    currentTv = null
                                }
                            }
                            return@postDelayed
                        }
                        currentTv?.let { existingTv ->
                            try { windowManager?.removeView(existingTv) } catch (_: Exception) { }
                            watermarkViews.remove(existingTv)
                            if (currentTv === existingTv) {
                                currentTv = null
                            }
                        }
                    }, durationMillis)

                    // If more repeats remain, schedule next run after intervalMillis
                    if (runCount < repeats) {
                        mainHandler.postDelayed(this, intervalMillis)
                    }
                }
            }

            mainHandler.post(fingerprintRunnable)
            scheduledRunnables.add(fingerprintRunnable)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        scheduledRunnables.forEach { mainHandler.removeCallbacks(it) }
        scheduledRunnables.clear()

        val restartIntent = Intent(applicationContext, FingerprintOverlayService::class.java)
        Handler(Looper.getMainLooper()).postDelayed({
            ContextCompat.startForegroundService(applicationContext, restartIntent)
        }, 1_000)
    }

    override fun onDestroy() {
        super.onDestroy()
        watermarkViews.forEach { tv ->
            try { windowManager?.removeView(tv) } catch (_: Exception) { }
        }
        watermarkViews.clear()

        scheduledRunnables.forEach { mainHandler.removeCallbacks(it) }
        scheduledRunnables.clear()

        sseEventSource?.cancel()
        sseEventSource = null
        stopForeground(true)

        ProcessLifecycleOwner.get().lifecycle.removeObserver(appLifecycleObserver)
    }

    private fun Int.dpToPx(): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            resources.displayMetrics
        ).toInt()

    private fun Float.normalizeFraction(): Float {
        return if (this > 1f) {
            (this / 100f).coerceIn(0f, 1f)
        } else {
            this.coerceIn(0f, 1f)
        }
    }
}
