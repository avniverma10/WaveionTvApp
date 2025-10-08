package com.panmetro.iptv.extensions

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.focus.FocusRequester
import kotlin.collections.first
import kotlin.collections.last


/** Returns Triple(firstVisibleIndex, lastVisibleIndex, visibleCount). */
fun LazyListState.visiblePage(): Triple<Int, Int, Int> {
    val items = layoutInfo.visibleItemsInfo
    if (items.isEmpty()) return Triple(0, 0, 0)
    val first = items.first().index
    val last = items.last().index
    return Triple(first, last, items.size)
}




// Extension function for safe focus request
fun List<FocusRequester>.requestFocusSafely(targetIndex: Int, isFocusEnabled: Boolean) {
    this.getOrNull(targetIndex)?.let { requester ->
        try {
            if (isFocusEnabled) {
                requester.requestFocus()
            }
        } catch (e: IllegalStateException) {
            // loge("FocusError", "FocusRequester not initialized: ${e.message}")
        } catch (e: Exception) {
            // loge("FocusError", "Unexpected focus error: ${e.message}")
        }
    }
}