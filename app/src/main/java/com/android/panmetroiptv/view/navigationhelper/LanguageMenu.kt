package com.android.panmetroiptv.view.navigationhelper

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.android.panmetroiptv.R
import com.android.panmetroiptv.extensions.appManifestLiveData
import com.android.panmetroiptv.extensions.loge
import com.android.panmetroiptv.utils.theme.base_color
import com.android.panmetroiptv.utils.theme.filter_selected_color
import com.android.panmetroiptv.utils.theme.focus_background
import com.android.panmetroiptv.viewmodels.SharedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LanguageMenu(
    sharedViewModel: SharedViewModel,
    selectedIndex: MutableState<Int>,
    firstChannelFocusRequester: FocusRequester,
    categoryFocusRequesters: List<FocusRequester>,
    languageFocusRequesters: List<FocusRequester>,
    categorySelectedIndex: MutableState<Int>
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val languageItems = sharedViewModel
        .provideApplicationContext()
        .appManifestLiveData()
        .value?.language ?: arrayListOf()
    if (languageItems.isEmpty()) return

    var selectionArmed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(180)
        selectionArmed = true
    }

    LaunchedEffect(selectedIndex.value, languageItems.size) {
        val idx = selectedIndex.value.coerceIn(0, (languageItems.size - 1).coerceAtLeast(0))
        runCatching { listState.animateScrollToItem(idx) }
    }

    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp),
        contentPadding = PaddingValues(start = 25.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(languageItems) { index, item ->
            val isSelected = selectedIndex.value == index
            val isFocused = remember { mutableStateOf(false) }

            val modifier = Modifier
                .then(
                    when {
                        isFocused.value -> Modifier
                            .border(1.dp, color = base_color, shape = RoundedCornerShape(30.dp))
                            .background(focus_background, RoundedCornerShape(30.dp))
                        isSelected -> Modifier
                            .background(filter_selected_color, RoundedCornerShape(30.dp))
                        else -> Modifier
                    }
                )
                .onFocusChanged { st ->
                    isFocused.value = st.isFocused
                    if (st.isFocused && selectionArmed) {
                        if (selectedIndex.value != index) {
                            selectedIndex.value = index
                            sharedViewModel.updateLastSelectedChannelIndex(-1)
                            sharedViewModel.updateLastFocusedChannel(-1)
                            sharedViewModel.updateLanguage(item.name ?: "Unknown")
                        }
                    }
                }
                .focusRequester(languageFocusRequesters[index])
                .focusable()
                .onPreviewKeyEvent { keyEvent ->
                    when {
                        keyEvent.type == KeyEventType.KeyDown &&
                                keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_UP -> {
                            categoryFocusRequesters
                                .getOrNull(categorySelectedIndex.value)
                                ?.let { requester ->
                                    coroutineScope.launch {
                                        delay(50)
                                        runCatching {
                                            try {
                                                requester.requestFocus()
                                            } catch (e: IllegalStateException) { }
                                        }
                                            .onFailure {
                                                loge("FocusError", "Category focus restore failed: ${it.message}")
                                            }
                                    }
                                }
                            true
                        }

                        keyEvent.type == KeyEventType.KeyDown &&
                                keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_DOWN -> {
                            coroutineScope.launch {
                                delay(100)
                                if (sharedViewModel.filteredEPGList.value.isNotEmpty()) {
                                    runCatching {
                                        try {
                                            firstChannelFocusRequester.requestFocus()
                                        } catch (e: IllegalStateException) { }

                                    }
                                        .onFailure {
                                            loge("FocusError", "First channel focus failed: ${it.message}")
                                        }
                                }
                            }
                            true
                        }
                        keyEvent.type == KeyEventType.KeyDown &&
                                (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                                        keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) -> true

                        else -> false
                    }
                }
                .padding(horizontal = 6.dp)
                .padding(horizontal = 20.dp, vertical = 10.dp)

            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                Text(
                    text = item.name ?: "",
                    color = Color.White,
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 14.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily(
                            androidx.compose.ui.text.font.Font(R.font.figtree_light)
                        ),
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    )
                )
            }
        }
    }
}
