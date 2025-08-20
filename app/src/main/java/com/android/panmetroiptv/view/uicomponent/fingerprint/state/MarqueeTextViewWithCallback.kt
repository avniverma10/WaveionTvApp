package com.android.panmetroiptv.view.uicomponent.fingerprint.state

import android.content.Context
import android.graphics.Canvas
import androidx.appcompat.widget.AppCompatTextView

// MarqueeTextViewWithCallback1.kt
class MarqueeTextViewWithCallback(
    context: Context,
    private var onMarqueeComplete: () -> Unit = {}
) : AppCompatTextView(context) {

    private var startTime: Long = 0
    private var durationMs: Long = -1 // -1 means infinite
    private var isDurationBased = false

    fun setDuration(durationMs: Long) {
        this.durationMs = durationMs
        this.isDurationBased = durationMs > 0
        this.startTime = System.currentTimeMillis()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startTime = System.currentTimeMillis()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Check duration if it's duration-based
        if (isDurationBased && durationMs > 0) {
            val elapsedTime = System.currentTimeMillis() - startTime
            if (elapsedTime >= durationMs) {
                isSelected = false // Stop marquee
                post { onMarqueeComplete() }
                return
            }
        }
        
        // Continue drawing if not expired
        if (isSelected) {
            postInvalidateDelayed(16) // Continue animation
        }
    }

    fun setRepeatCount(count: Int) {
        marqueeRepeatLimit = if (count <= 0) -1 else count - 1
    }
}