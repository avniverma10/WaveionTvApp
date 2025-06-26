package com.example.tvapp.view.navigationhelper

import android.util.Log
import android.view.KeyEvent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.android.caastv.R
import com.example.tvapp.extensions.appManifestLiveData
import com.example.tvapp.extensions.loge
import com.example.tvapp.ui.theme.filter_selected_color
import com.example.tvapp.ui.theme.base_color
import com.example.tvapp.ui.theme.focus_background
import com.example.tvapp.viewmodels.SharedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

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
    val menuItems = sharedViewModel.provideApplicationContext().appManifestLiveData().value?.genre?: arrayListOf()

    val languageItems = sharedViewModel.provideApplicationContext().appManifestLiveData().value?.language?: arrayListOf()

    if (languageItems.isEmpty()) {
        return
    }
    val categoryIcons = listOf(
       R.drawable.english,
        R.drawable.letter_hindi_a_svgrepo_com__3_,
        R.drawable.english,
        R.drawable.letter_hindi_a_svgrepo_com__3_,
        R.drawable.english,
        R.drawable.letter_hindi_a_svgrepo_com__3_,
        R.drawable.english,
        R.drawable.letter_hindi_a_svgrepo_com__3_,
    )
    LaunchedEffect(selectedIndex.value) {
        // let Compose lay out first
        delay(50)

        val visible = listState.layoutInfo.visibleItemsInfo
        if (visible.isEmpty()) return@LaunchedEffect

        val firstVisible  = visible.first().index
        val lastVisible   = visible.last().index
        val visibleCount  = visible.size

        when {
            // moved off the left edge?
            selectedIndex.value < firstVisible -> {
                // just snap that item to the front
                listState.animateScrollToItem(selectedIndex.value)
            }

            // moved past the right edge?
            selectedIndex.value > lastVisible -> {
                // scroll so that the newly-selected item sits at the end of the viewport
                val newFirst = (selectedIndex.value - visibleCount + 1).coerceAtLeast(0)
                listState.animateScrollToItem(newFirst)
            }

            else -> {
                // still fully in view, do nothing
            }
        }
    }

    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp),
        contentPadding = PaddingValues(start = 25.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(languageItems) { index, item ->
            val iconRes = categoryIcons.getOrElse(index) {
                R.drawable.english
            }
            val isSelected = selectedIndex.value == index
            val isFocused = remember { mutableStateOf(false) }
            val gap = if (isFocused.value || isSelected) 36.dp else 16.dp

            val modifier = Modifier
                .then(
                    if (isFocused.value) {
                        Modifier
                            .border(1.dp, color = base_color, shape = RoundedCornerShape(30.dp),).background(color = focus_background, shape = RoundedCornerShape(30.dp))
                    } else if (isSelected) {
                        Modifier.background(color = filter_selected_color, shape = RoundedCornerShape(30.dp))
                    } else Modifier
                )
                .onFocusChanged {
                    isFocused.value = it.isFocused
                    if (it.isFocused) {
                        selectedIndex.value = index
                        sharedViewModel.updateLastSelectedChannelIndex(-1)
                        sharedViewModel.updateLastFocusedChannel(-1)
                        val languageName = item.name ?: "Unknown"
                        sharedViewModel.updateLanguage(languageName)
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
                                        try {
                                            requester.requestFocus()
                                        } catch (e: IllegalStateException) {
                                            loge("FocusError", "FocusRequester not initialized ${e.message}")
                                        }
                                    }
                                }
                            true
                        }

                        keyEvent.type == KeyEventType.KeyDown &&
                                keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_DOWN -> {
                            if (sharedViewModel.filteredEPGList.value.isNotEmpty()) {
                                firstChannelFocusRequester?.let { requester ->
                                    try {
                                        requester.requestFocus()
                                    } catch (e: IllegalStateException) {
                                        loge("FocusError", "FocusRequester not initialized  ${e.message}")
                                    }
                                }
                            }
                            true
                        }
                        else -> false
                    }
                }
                .padding(horizontal = 6.dp)
                .padding(horizontal = 20.dp, vertical = 10.dp)

            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {  if (isFocused.value || isSelected) {
                // expanded: icon + text
                Row(
                    verticalAlignment    = Alignment.CenterVertically,
                    horizontalArrangement= Arrangement.spacedBy(8.dp),
                    modifier             = Modifier.padding(horizontal = 8.dp)
                ) {
                    Image(
                        painter            = painterResource(iconRes),
                        contentDescription = item.name,
                        modifier           = Modifier.size(32.dp)
                    )
                    Text(
                        text = item.name ?: "",
                        color = Color.White,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily =FontFamily(
                          Font(R.font.figtree_light)
                            ),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            } else {
                // collapsed: icon only
                Image(
                    painter            = painterResource(iconRes),
                    contentDescription = item.name,
                    modifier           = Modifier.size(20.dp)
                )
            }
            }
        }
    }
}
