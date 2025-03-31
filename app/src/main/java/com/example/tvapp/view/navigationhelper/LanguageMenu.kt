package com.example.tvapp.view.navigationhelper

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.example.tvapp.R
import com.example.tvapp.extensions.appLanguageLiveData
import com.example.tvapp.model.data.language.WTVLanguage
import com.example.tvapp.viewmodels.SharedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LanguageMenu(
    sharedViewModel: SharedViewModel,
    selectedIndex: MutableState<Int>,
    firstChannelFocusRequester: FocusRequester,
    languageFocusRequesters: MutableMap<Int, FocusRequester>,
    categoryFocusRequesters: MutableMap<Int, FocusRequester>,
    categorySelectedIndex: MutableState<Int>
) {
    val appLanguageData by sharedViewModel
        .provideApplicationContext()
        .appLanguageLiveData()
        .observeAsState(initial = emptyList())
    val languageItems: List<WTVLanguage> = appLanguageData ?: emptyList()
    val allLanguageItems = listOf(WTVLanguage(name = "All")) + languageItems

    if (allLanguageItems.isEmpty()) {
        return
    }

    val coroutineScope = rememberCoroutineScope()

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp),
        contentPadding = PaddingValues(start = 25.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(allLanguageItems) { index, item ->
            val focusRequester = remember { FocusRequester() }
            languageFocusRequesters[index] = focusRequester
            val isSelected = selectedIndex.value == index
            val isFocused = remember { mutableStateOf(false) }

            val modifier = Modifier
                .then(
                    if (isFocused.value) {
                        Modifier
                            .border(1.dp, Color(0xFF49FEDD), shape = RoundedCornerShape(30.dp),)
                    } else if (isSelected) {
                        Modifier.background(Color(0x1A49FEDD), shape = RoundedCornerShape(30.dp))
                    } else Modifier
                )
                .onFocusChanged {
                    isFocused.value = it.isFocused
                    if (it.isFocused) {
                        selectedIndex.value = index
                        val languageName = item.name ?: "Unknown"
                        sharedViewModel.updateLanguage(languageName)
                    }
                }
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent { keyEvent ->
                    when {
                        keyEvent.type == KeyEventType.KeyDown &&
                                keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_UP -> {
                            categoryFocusRequesters[categorySelectedIndex.value]?.let { requester ->
                                coroutineScope.launch {
                                    delay(50)
                                    requester.requestFocus()
                                }
                            }
                            true
                        }

                        keyEvent.type == KeyEventType.KeyDown &&
                                keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_DOWN -> {
                            if (sharedViewModel.filteredEPGList.value.isNotEmpty()) {
                                firstChannelFocusRequester.requestFocus()
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
            ) {
                Text(
                    text = item.name ?: "",
                    color = Color.White,
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize = 12.sp,
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
