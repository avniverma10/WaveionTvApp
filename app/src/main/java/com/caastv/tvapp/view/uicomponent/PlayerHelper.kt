package com.caastv.tvapp.view.uicomponent

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
import android.widget.TextView
import androidx.media3.ui.PlayerView
import java.security.MessageDigest
import kotlin.random.Random


fun addWatermarkToPlayer(playerView: PlayerView, watermarkText: String) {
    val context = playerView.context
    val textView = TextView(context).apply {
        text = watermarkText
        setTextColor(Color.WHITE)
        textSize = 14f
        alpha = 0.3f // Visible but subtle
        setPadding(16, 16, 16, 16)
    }

    val overlayLayout = FrameLayout(context).apply {
        addView(textView)
    }

    playerView.addView(overlayLayout)

    val handler = Handler(Looper.getMainLooper())

    val updatePositionRunnable = object : Runnable {
        override fun run() {
            playerView.post {
                val parentWidth = playerView.width
                val parentHeight = playerView.height

                textView.measure(0, 0) // Ensure TextView is measured
                val textWidth = textView.measuredWidth
                val textHeight = textView.measuredHeight

                if (parentWidth > textWidth && parentHeight > textHeight) {
                    val maxX = parentWidth - textWidth
                    val maxY = parentHeight - textHeight

                    if (maxX > 0 && maxY > 0) {
                        val randomX = Random.nextInt(0, maxX)
                        val randomY = Random.nextInt(0, maxY)

                        //  Correctly update layout parameters
                        val layoutParams = overlayLayout.layoutParams as FrameLayout.LayoutParams
                        layoutParams.leftMargin = randomX
                        layoutParams.topMargin = randomY
                        overlayLayout.layoutParams = layoutParams

                        // Force layout refresh
                        overlayLayout.requestLayout()
                        overlayLayout.invalidate()
                    }
                }
            }
            handler.postDelayed(this, 50_00) //  Move every 10 seconds
        }
    }

    //  Only start moving the watermark once the layout is measured
    playerView.post {
        textView.measure(0, 0) // Ensure TextView gets measured before first position update
        handler.post(updatePositionRunnable) // Only start once
    }

    // Ensure the handler stops when the player is destroyed
    playerView.addOnAttachStateChangeListener(object : android.view.View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: android.view.View) {}
        override fun onViewDetachedFromWindow(v: android.view.View) {
            handler.removeCallbacks(updatePositionRunnable) // Stop movement when view is removed
        }
    })
}

// Generates watermark hash
fun generateWatermark(userPhone: String?, deviceId: String): String {
    val input = "$userPhone-$deviceId"
    val hashBytes = MessageDigest.getInstance("SHA-1").digest(input.toByteArray())
    return hashBytes.joinToString("") { "%02x".format(it) }.take(8)
}
