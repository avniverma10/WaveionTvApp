package com.caastv.tvapp.view.navigationhelper

import android.view.KeyEvent
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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.android.caastv.R
import com.caastv.tvapp.extensions.loge
import com.caastv.tvapp.utils.Constants
import com.caastv.tvapp.utils.theme.base_color
import com.caastv.tvapp.utils.theme.filter_selected_color
import com.caastv.tvapp.utils.theme.focus_background
import com.caastv.tvapp.viewmodels.SharedViewModel
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
    val languageItems = sharedViewModel.manifestData.collectAsState().value?.language?: emptyList()//provideApplicationContext().appManifestLiveData().value?.language ?: emptyList()

    if (languageItems.isEmpty()) return

    LaunchedEffect(selectedIndex.value) {
        delay(50)
        val visible = listState.layoutInfo.visibleItemsInfo
        if (visible.isEmpty()) return@LaunchedEffect
        val firstVisible = visible.first().index
        val lastVisible = visible.last().index
        val visibleCount = visible.size
        when {
            selectedIndex.value < firstVisible -> {
                listState.animateScrollToItem(selectedIndex.value)
            }
            selectedIndex.value > lastVisible -> {
                val newFirst = (selectedIndex.value - visibleCount + 1).coerceAtLeast(0)
                listState.animateScrollToItem(newFirst)
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
            val isFocused = remember { mutableStateOf(false) }
            val isSelected = selectedIndex.value == index
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
                .padding(horizontal = 10.dp, vertical = 6.dp)

            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) { if (isFocused.value || isSelected) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        LanguageIcon(
                            customIconUrl = item.customIconUrl,
                            defaultIconName = item.defaultIcon,
                            contentDescription = item.name,
                            modifier = Modifier.size(40.dp)
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
                    LanguageIcon(
                        customIconUrl = item.customIconUrl,
                        defaultIconName = item.defaultIcon,
                        contentDescription = item.name,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LanguageIcon(
    customIconUrl: String?,
    defaultIconName: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val baseUrlForDefault = Constants.BASE_ICON_URL+"uploads/languageIcon/DefaultIcons"
    val (imageUrl, source) = when {
        !customIconUrl.isNullOrBlank() ->
            customIconUrl to "customIconUrl"
        !defaultIconName.isNullOrBlank() ->
            "$baseUrlForDefault/${defaultIconName}.svg" to "defaultIcon"
        else ->
            null to "none"
    }
    if (!imageUrl.isNullOrBlank()) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = modifier

        )
    }
}