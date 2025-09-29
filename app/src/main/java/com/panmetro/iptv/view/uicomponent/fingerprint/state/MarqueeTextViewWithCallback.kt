package com.panmetro.iptv.view.uicomponent.fingerprint.state

import android.content.Context
import android.graphics.Canvas
import androidx.appcompat.widget.AppCompatTextView
import android.util.AttributeSet
import java.lang.reflect.Field

// MarqueeTextViewWithCallback.kt
class MarqueeTextViewWithCallback(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private var onMarqueeComplete: () -> Unit = {}
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var startTime: Long = 0
    private var durationMs: Long = -1 // -1 means infinite
    private var isDurationBased = false

    // Marquee speed multiplier (increase this to make it faster)
    private var speedMultiplier: Float = 2.0f // Default 2x speed

    init {
        // Ensure marquee is enabled
        isSelected = true
        isSingleLine = true
        ellipsize = android.text.TextUtils.TruncateAt.MARQUEE
        marqueeRepeatLimit = -1 // Infinite by default

        // Apply initial speed
        setMarqueeSpeed(speedMultiplier)
    }

    fun setSpeedMultiplier(multiplier: Float) {
        speedMultiplier = multiplier
        setMarqueeSpeed(multiplier)
    }

    private fun setMarqueeSpeed(multiplier: Float) {
        try {
            // Use reflection to access the private marquee speed field
            val field: Field = this::class.java.superclass.getDeclaredField("mMarquee")
            field.isAccessible = true
            val marquee = field.get(this)

            if (marquee != null) {
                val speedField = marquee.javaClass.getDeclaredField("mScrollUnit")
                speedField.isAccessible = true

                // Default scroll unit is typically around 0.003f, we multiply to make it faster
                val defaultSpeed = 0.003f
                val newSpeed = defaultSpeed * multiplier
                speedField.setFloat(marquee, newSpeed)
            }
        } catch (e: Exception) {
            // Fallback: Use postInvalidateDelayed with shorter interval for faster refresh
            e.printStackTrace()
        }
    }

    fun setDuration(durationMs: Long) {
        this.durationMs = durationMs
        this.isDurationBased = durationMs > 0
        this.startTime = System.currentTimeMillis()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startTime = System.currentTimeMillis()
        // Restart marquee when attached
        isSelected = true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Stop marquee when detached to prevent memory leaks
        isSelected = false
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
            // Faster refresh rate for smoother animation (8ms ≈ 120fps)
            postInvalidateDelayed(8)
        }
    }

    fun setRepeatCount(count: Int) {
        marqueeRepeatLimit = if (count <= 0) -1 else count - 1
    }

    // Alternative method using custom marquee implementation
    fun setCustomMarqueeSpeed(speedPixelsPerSecond: Float) {
        try {
            val field: Field = this::class.java.superclass.getDeclaredField("mMarquee")
            field.isAccessible = true
            val marquee = field.get(this)

            if (marquee != null) {
                // Some Android versions use different field names
                val fieldNames = arrayOf("mPixelsPerSecond", "mScrollUnit", "mStep")

                for (fieldName in fieldNames) {
                    try {
                        val speedField = marquee.javaClass.getDeclaredField(fieldName)
                        speedField.isAccessible = true

                        when (speedField.type) {
                            Float::class.java -> {
                                speedField.setFloat(marquee, speedPixelsPerSecond)
                                break
                            }
                            Int::class.java -> {
                                speedField.setInt(marquee, speedPixelsPerSecond.toInt())
                                break
                            }
                        }
                    } catch (e: NoSuchFieldException) {
                        continue
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}