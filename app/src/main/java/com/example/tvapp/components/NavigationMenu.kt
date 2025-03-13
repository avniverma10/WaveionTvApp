package com.example.tvapp.components
import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.example.tvapp.R
import com.example.tvapp.screens.MenuItem
import com.example.tvapp.viewmodels.EPGViewModel
import androidx.compose.foundation.lazy.LazyRow


@Composable
fun NavigationMenu(viewModel: EPGViewModel = hiltViewModel()) {
    val menuItems = listOf(
        MenuItem(R.drawable.all, "All Channels"),
        MenuItem(R.drawable.recent, "Recent"),
        MenuItem(R.drawable.music, "Sports"),
        MenuItem(R.drawable.news, "News"),
        MenuItem(R.drawable.star, "Movies"),
        MenuItem(R.drawable.kid, "Kids")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color(0xFF161D25), shape = RoundedCornerShape(12.dp)) // Rounded Background
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(53.dp), // **Increased space between items**
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(menuItems) { _, item ->
                val focusRequester = remember { FocusRequester() }
                val isFocused = remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .background(
                            if (isFocused.value) Color(0xFF1F7A8C) else Color(0xFF161D25), // Highlight focused item
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            2.dp,
                            if (isFocused.value) Color.White else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            if (item.label == "Recent") {
                                viewModel.showRecentlyWatched()
                            } else {
                                viewModel.filterChannelsByGenre(item.label)
                            }
                        }
                        .focusRequester(focusRequester)
                        .focusable()
                        .onFocusChanged { isFocused.value = it.isFocused }
                        .padding(horizontal = 22.dp, vertical = 30.dp), // Padding inside the filter button
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.label,
                        color = Color.White,
                        style = TextStyle(
                            fontSize = 18.sp,
                            lineHeight = 28.01.sp,
                            fontFamily = FontFamily(Font(R.font.figtree_light)),
                            fontWeight = FontWeight(400),
                        ),
                    modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }

    // **Bottom Divider**
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color(0xFF353C44))
    )
}


