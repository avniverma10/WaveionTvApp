package com.example.tvapp.view.navigationhelper

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.example.tvapp.extensions.appGenreLiveData
import com.example.tvapp.model.data.genre.WTVGenre
import com.example.tvapp.viewmodels.SharedViewModel
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

@Composable
fun CategoryMenu(
    sharedViewModel: SharedViewModel,
    selectedIndex: MutableState<Int>,
    languageSelectedIndex: MutableState<Int>,
    categoryFocusRequesters: MutableMap<Int, FocusRequester>,
    languageFocusRequesters: MutableMap<Int, FocusRequester>
) {
    val appGenreData by sharedViewModel
        .provideApplicationContext()
        .appGenreLiveData()
        .observeAsState(initial = emptyList())
    val menuItems: List<WTVGenre> = appGenreData?: emptyList()

    if (menuItems.isEmpty()) {
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color(0xFF161D25), shape = RoundedCornerShape(12.dp))
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(53.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(menuItems) { index, item ->
                val focusRequester = remember { FocusRequester() }
                categoryFocusRequesters[index] = focusRequester
                val isFocused = remember { mutableStateOf(false) }
                val isSelected = selectedIndex.value == index

                val modifier = Modifier
                    .then(
                        when {
                            isFocused.value ->
                                Modifier.border(2.dp, Color(0xFF49FEDD), shape = RoundedCornerShape(4.dp))
                            isSelected ->
                                Modifier.background(Color(0x1A49FEDD), shape = RoundedCornerShape(4.dp))
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
                    .focusRequester(focusRequester)
                    .focusable()
                    .onPreviewKeyEvent { keyEvent ->
                        if (keyEvent.type == KeyEventType.KeyDown &&
                            keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                        ) {
//                            languageFocusRequesters[0]?.requestFocus()
                            languageFocusRequesters[ languageSelectedIndex.value ]?.requestFocus()

                            true
                        } else false
                    }
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(60.dp)
                        .then(modifier),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.name ?: "",
                        color = Color.White,
                        style = TextStyle(
                            fontSize = 17.sp,
                            fontFamily = FontFamily(Font(R.font.figtree_light)),
                            fontWeight = FontWeight(400)
                        ),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}
