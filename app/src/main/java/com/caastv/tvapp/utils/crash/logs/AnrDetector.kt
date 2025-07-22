package com.caastv.tvapp.utils.crash.logs

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class AnrDetector : ActivityLifecycleCallbacks {

    private val handler = Handler(Looper.getMainLooper())
    private var anrRunnable: Runnable? = null
    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
       // TODO("Not yet implemented")
    }

    override fun onActivityStarted(p0: Activity) {
        // TODO("Not yet implemented")
    }

    override fun onActivityResumed(activity: Activity) {
        startAnrWatchdog(activity)
    }

    override fun onActivityPaused(p0: Activity) {
        // TODO("Not yet implemented")
    }

    override fun onActivityStopped(p0: Activity) {
        // TODO("Not yet implemented")
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
        // TODO("Not yet implemented")
    }

    override fun onActivityDestroyed(p0: Activity) {
        //TODO("Not yet implemented")
    }

    private fun startAnrWatchdog(activity: Activity) {
        anrRunnable?.let { handler.removeCallbacks(it) }
        
        anrRunnable = Runnable {
            // This will run if main thread is blocked
           // CrashLogger(activity).logCrash("ANR", "Main thread blocked")
        }.also {
            handler.postDelayed(it, 5000) // 5-second threshold
        }
    }
}