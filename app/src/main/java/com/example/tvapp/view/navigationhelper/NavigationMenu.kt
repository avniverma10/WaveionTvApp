package com.example.tvapp.view.navigationhelper

import android.util.Log
import android.view.KeyEvent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.android.caastv.R
import com.example.tvapp.extensions.appManifestLiveData
import com.example.tvapp.ui.theme.filter_selected_color
import com.example.tvapp.ui.theme.base_color
import com.example.tvapp.ui.theme.focus_background
import com.example.tvapp.viewmodels.SharedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun CategoryMenu(
    sharedViewModel: SharedViewModel,
    selectedIndex: MutableState<Int>,
    languageSelectedIndex: MutableState<Int>,
    categoryFocusRequesters: List<FocusRequester>,
    languageFocusRequesters: List<FocusRequester>
) {

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val menuItems = sharedViewModel.provideApplicationContext().appManifestLiveData().value?.genre?: arrayListOf()

    val languageItems = sharedViewModel.provideApplicationContext().appManifestLiveData().value?.language?: arrayListOf()


    if (menuItems.isEmpty()) {
        return
    }
    val categoryIcons = listOf(
        R.drawable.all_1,
        R.drawable.ball,
        R.drawable.news_feed_svgrepo_com,
        R.drawable.religoius,
        R.drawable.ball,
        R.drawable.news_feed_svgrepo_com,
        R.drawable.religoius,
        R.drawable.all_1,
        R.drawable.ball,
        R.drawable.news_feed_svgrepo_com,
        R.drawable.religoius,
        R.drawable.ball,
        R.drawable.news_feed_svgrepo_com,
        R.drawable.religoius,
    )
    // only scroll when the selected index moves out of view
    LaunchedEffect(selectedIndex.value) {
        delay(50)
        val visible = listState.layoutInfo.visibleItemsInfo
        if (visible.isEmpty()) return@LaunchedEffect
        val firstVisible  = visible.first().index
        val lastVisible   = visible.last().index
        val visibleCount  = visible.size
        when {
            selectedIndex.value < firstVisible -> {
                listState.animateScrollToItem(selectedIndex.value)
            }
            selectedIndex.value > lastVisible -> {
                val newFirst = (selectedIndex.value - visibleCount + 1).coerceAtLeast(0)
                listState.animateScrollToItem(newFirst)
            }
            else -> {
                // still fully in view, do nothing
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color(0xFF161D25), shape = RoundedCornerShape(12.dp))
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(53.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(menuItems) { index, item ->
                val iconRes = categoryIcons.getOrElse(index) {
                    R.drawable.news
                }
                val isFocused = remember { mutableStateOf(false) }
                val isSelected = selectedIndex.value == index
                val gap = if (isFocused.value || isSelected) 10.dp else 12.dp
                val modifier = Modifier
                    .then(
                        when {
                            isFocused.value ->
                                Modifier.border(2.dp, color = base_color, shape = RoundedCornerShape(4.dp)).background(color = focus_background)
                            isSelected ->
                                Modifier.background(color = filter_selected_color, shape = RoundedCornerShape(4.dp))
                            else -> Modifier
                        }
                    )
                    .onFocusChanged {
                        isFocused.value = it.isFocused
                        if (it.isFocused) {
                            selectedIndex.value = index
                            val genreName = item.name ?: "Unknown"
                            sharedViewModel.updateGenre(genreName)
                        }
                    }
                    .focusRequester(categoryFocusRequesters[index])
                    .focusable()
                    .onFocusChanged { state ->
                        if (state.isFocused) {
                            sharedViewModel.updateLastSelectedChannelIndex(-1)
                            sharedViewModel.updateLastFocusedChannel(-1)
                        }
                    }
                    .onPreviewKeyEvent { keyEvent ->
                        if (keyEvent.type == KeyEventType.KeyDown &&
                            keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        ) {
                            languageFocusRequesters
                                .getOrNull(languageSelectedIndex.value)
                                ?.let { requester ->
                                    coroutineScope.launch {
                                        delay(50)
                                        requester.requestFocus()
                                    }
                                }
                           // languageFocusRequesters[ languageSelectedIndex.value ].requestFocus()
                            true
                        } else false
                    }
                    .padding(end = gap)
                Box(
                    modifier = Modifier
                        .wrapContentWidth()
                        .defaultMinSize(100.dp)
                        .height(60.dp)
                        .then(modifier),
                    contentAlignment = Alignment.Center
                ) {
                    if (isFocused.value || isSelected) {
                        // expanded: icon + text
                        Row(
                            verticalAlignment    = Alignment.CenterVertically,
                            horizontalArrangement= Arrangement.spacedBy(8.dp),
                            modifier             = Modifier.padding(start = 8.dp)
                        ) {
                            Image(
                                painter            = painterResource(iconRes),
                                contentDescription = item.name,
                                modifier           = Modifier.size(25.dp)
                            )
                            Text(
                                text    = item.name.orEmpty().uppercase(Locale.ROOT),
                                maxLines= 1,
                                color   = Color.White,
                                style   = TextStyle(
                                    fontSize     = 17.sp,
                                    fontFamily   = FontFamily(Font(R.font.figtree_light)),
                                    fontWeight   = FontWeight(400)
                                ),
//                                modifier= Modifier
//                                    .padding(start = 10.dp, end = 10.dp)
                            )
                        }
                    } else {
                        // collapsed: icon only
                        Image(
                            painter            = painterResource(iconRes),
                            contentDescription = item.name,
                            modifier           = Modifier.size(25.dp)
                        )
                    }

                }
            }
        }
    }
}
