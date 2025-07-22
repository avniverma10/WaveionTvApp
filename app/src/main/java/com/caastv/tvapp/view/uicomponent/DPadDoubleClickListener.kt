package com.caastv.tvapp.view.uicomponent

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*

@Composable
fun DPadDoubleClickListener(
    onDoubleClickIntent: (String)->Unit,
    modifier: Modifier = Modifier,
    doublePressThresholdMillis: Long = 300L
) {
    // Store the time and key of the last press
    var lastPressTime by remember { mutableStateOf(0L) }
    var lastKey by remember { mutableStateOf<Key?>(null) }

    Box(
        modifier = modifier
            .onPreviewKeyEvent { keyEvent ->
                // Only handle KeyDown events
                if (keyEvent.type == KeyEventType.KeyDown) {
                    val currentTime = System.currentTimeMillis()
                    val currentKey = keyEvent.key

                    // Check if this is the same key as last time and within the threshold
                    if (currentKey == lastKey && (currentTime - lastPressTime) < doublePressThresholdMillis) {
                        // Double press detected! Perform your action or navigate.
                        onDoubleClickIntent("doubleClicked")

                        // Consume the event
                        true
                    } else {
                        // Update the last press info and do not consume
                        lastPressTime = currentTime
                        lastKey = currentKey
                        false
                    }
                } else {
                    false
                }
            }
    ) {
        // Your UI content here (e.g. lists, images, etc.)
    }
}
