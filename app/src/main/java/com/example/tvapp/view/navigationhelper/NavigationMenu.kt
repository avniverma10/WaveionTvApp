package com.example.tvapp.view.navigationhelper
import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.example.tvapp.extensions.provideCategoryResource
import com.example.tvapp.model.data.manifest.EPGCategory


@Composable
fun NavigationMenu(menuItems:List<EPGCategory>) {
    Column(
        modifier = Modifier
            .width(200.dp)
            .fillMaxHeight()
            .background(Color.DarkGray)
            .padding(top = 16.dp)
    ) {
        LazyColumn {
            itemsIndexed(menuItems) { index, item ->
                val focusRequester = remember { FocusRequester() }
                val isFocused = remember { mutableStateOf(false) }

                val isFirstItem = index == 0
                val isLastItem = index == menuItems.lastIndex

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                        .then(if (isFocused.value) Modifier.border(2.dp, Color.White) else Modifier)
                        .onFocusChanged { isFocused.value = it.isFocused }
                        .focusRequester(focusRequester)
                        .focusable()
                        .onPreviewKeyEvent { keyEvent ->
                            if (keyEvent.type == KeyEventType.KeyDown) {
                                when (keyEvent.nativeKeyEvent.keyCode) {
                                    KeyEvent.KEYCODE_DPAD_CENTER -> {
                                        if (item.name == "Recently Watched") {
                                           // viewModel.showRecentlyWatched()
                                        } else {
                                           // viewModel.filterChannelsByGenre(item.label)
                                        }
                                        true
                                    }
                                    KeyEvent.KEYCODE_DPAD_UP -> {
                                        if (isFirstItem) true else false
                                    }
                                    KeyEvent.KEYCODE_DPAD_DOWN -> {
                                        if (isLastItem) true else false
                                    }
                                    else -> false
                                }
                            } else false
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = item.name?.provideCategoryResource()!!),
                        contentDescription = item.name,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = item.name?:"",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

            }
        }
    }
}
