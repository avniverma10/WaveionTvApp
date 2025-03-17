@file:Suppress("IMPLICIT_CAST_TO_ANY")

package com.example.tvapp.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.example.tvapp.viewmodels.TabsViewModel
@Composable
fun ExpandableNavigationMenu(
    navController: NavController,
    viewModel: TabsViewModel = hiltViewModel()
) {
    val tabs = viewModel.tabs.collectAsState(initial = emptyList()).value
    var expanded by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(-1) } // No initial focus
    val menuFocusRequester = remember { FocusRequester() }

    // Handle back button press to collapse the menu
    if (expanded) {
        BackHandler { expanded = false }
    }

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .zIndex(4f) // Ensure it appears on top when expanded
    ) {
        Column(
            modifier = Modifier
                .width(if (expanded) 199.dp else 70.dp)
                .fillMaxHeight()
                .drawBehind {
                    if (expanded) {
                        drawIntoCanvas { canvas ->
                            val gradient = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Black,
                                    Color.Black.copy(alpha = 0.9f),
                                    Color.Black.copy(alpha = 0.8f),
                                    Color.Black.copy(alpha = 0.6f),
                                    Color.Transparent
                                )
                            )
                            drawRect(brush = gradient, size = size)
                        }
                    } else {
                        drawRect(color = Color.Black) // Solid black when collapsed
                    }
                }
                .animateContentSize()
                .padding(8.dp)
                .focusRequester(menuFocusRequester)
                .focusable()
        ) {
            // **Profile Row (No Initial Focus)**
            FocusableRow(
                selected = selectedIndex == 0,
                expanded = expanded,
                onFocus = { selectedIndex = 0 },
                onClick = { expanded = !expanded }
            ) {
                if (tabs.isNotEmpty()) {
                    val profileTab = tabs.first()
                    AsyncImage(
                        model = profileTab.iconUrl,
                        contentDescription = profileTab.displayName,
                        modifier = Modifier
                            .width(25.dp)
                            .height(25.dp)
                    )
                    if (expanded) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = profileTab.displayName,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(50.dp))

            // **Available Tabs**
            tabs.drop(1).forEachIndexed { index, tab ->
                val itemFocusRequester = remember { FocusRequester() }

                FocusableRow(
                    modifier = Modifier.focusRequester(itemFocusRequester),
                    selected = selectedIndex == index + 1,
                    expanded = expanded,
                    onFocus = { selectedIndex = index + 1 },
                    onClick = {
                        selectedIndex = index + 1
                        if (!expanded) {
                            expanded = true
                        } else {
                            when (tab.displayName) {
                                "Home" -> navController.navigate("home_screen")
                                "Search" -> navController.navigate("search_screen")
                                "Live Tv" -> navController.navigate("epg")
                            }
                            expanded = false
                        }
                    }
                ) {
                    AsyncImage(
                        model = tab.iconUrl,
                        contentDescription = tab.displayName,
                        modifier = Modifier
                            .width(25.dp)
                            .height(25.dp)
                    )
                    if (expanded) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = tab.displayName,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}







@Composable
fun FocusableRow(
    modifier: Modifier = Modifier,
    selected: Boolean,
    expanded: Boolean,
    onFocus: () -> Unit,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .width(160.dp)
            .height(50.dp)
            .padding(8.dp)
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    onFocus()
                } else if (!focusState.hasFocus) {
                    onFocus() // Ensures focus moves out properly
                }
            }
            .focusable()
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_RIGHT) {
                    // 🔹 If moving right, remove focus from menu
                    false
                } else {
                    false
                }
            }
            .background(if (selected && expanded) Color.White.copy(alpha = 0.2f) else Color.Transparent)
            .then(
                if (selected) {
                    Modifier
                        .border(1.dp, if (expanded) Color(0xFF49FEDD) else Color.Transparent, shape = RoundedCornerShape(4.dp))
                        .background(
                            if (expanded) Color(0x1A49FEDD) else Color.Transparent,
                            shape = RoundedCornerShape(4.dp)
                        )
                } else Modifier
            )
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}





