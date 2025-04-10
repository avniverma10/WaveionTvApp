package com.example.tvapp.view.navigationhelper

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.tvapp.extensions.appManifestLiveData
import com.example.tvapp.model.data.manifest.TabInfo
import com.example.tvapp.viewmodels.SharedViewModel
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

@Composable
fun ExpandableNavigationMenu(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    onNavMenuIntent: (tabInfo: TabInfo, selectedIndex: Int) -> Unit
) {
    val tabs = sharedViewModel.provideApplicationContext().appManifestLiveData().value?.tab?.filter { it.name in arrayOf("epg","settings","channels","profile") }
    var expanded by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    var selectedIndex by remember { mutableStateOf(-1) }
    val menuFocusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    var lastClickTime by remember { mutableStateOf(0L) }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(currentBackStackEntry) {
        val currentRoute = currentBackStackEntry?.destination?.route
        selectedIndex = when (currentRoute) {
            // Destination.homeScreen -> tabs?.indexOfFirst { it.name == "home" } ?: -1
            //  Destination.searchScreen -> tabs?.indexOfFirst { it.name == "search" } ?: -1
            Destination.epgScreen -> tabs?.indexOfFirst { it.name == "epg" } ?: -1
            Destination.genreScreen -> tabs?.indexOfFirst { it.name == "channels" } ?: -1
            Destination.settings -> tabs?.indexOfFirst { it.name == "settings" } ?: -1
            else -> -1
        }
    }

    if (expanded) {
        BackHandler { expanded = false }
    }

    val profileTab = tabs?.find { it.name == "profile" }
    val profileTabIndex = tabs?.indexOf(profileTab)?:0
    val otherTabs = tabs?.drop(1) ?: emptyList()

    val profileFocusRequester = remember { FocusRequester() }
    val focusRequesters = List(otherTabs.size) { FocusRequester() }

    LaunchedEffect(expanded) {
        if (expanded) {
            coroutineScope.launch {
                delay(200)
                if (expanded) {
                    if(selectedIndex<=0){
                        profileFocusRequester.requestFocus()
                    }else {
                        focusRequesters.getOrNull(selectedIndex - 1)?.let { requester ->
                            try {
                                requester.requestFocus()
                            } catch (e: IllegalStateException) {
                                Log.e("FocusError", "FocusRequester not initialized", e)
                            }
                        }
                    }
                }

                /*when {
                    selectedIndex == -1 -> {
                        selectedIndex = 0
                        profileFocusRequester.requestFocus()
                    }
                    selectedIndex == 0 -> {
                        profileFocusRequester.requestFocus()
                    }
                    else -> {
                        focusRequesters.getOrNull(selectedIndex - 1)?.requestFocus()
                    }
                }*/
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .zIndex(4f)
            .focusRequester(menuFocusRequester)
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    expanded = true
                }
            }
            .focusable()
            .onPreviewKeyEvent { keyEvent ->
                when (keyEvent.nativeKeyEvent.keyCode) {
                    android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        expanded = false
                        false
                    }
                    else -> false
                }
            }
    ) {
        Column(
            modifier = Modifier
                .width(if (expanded) 199.dp else 70.dp)
                .fillMaxHeight()
                .animateContentSize()
                .focusable()
                .drawBehind {
                    val gradient = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black,
                            Color.Black.copy(alpha = 0.9f),
                            Color.Black.copy(alpha = 0.6f),
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                    drawRect(brush = gradient, size = size)
                }
        ) {
            FocusableRow(
                selected = selectedIndex == 0,
                expanded = expanded,
                onFocus = { selectedIndex = 0 },
                onClick = { expanded = true },
                focusRequester = profileFocusRequester,
                nextFocusRequester = focusRequesters.firstOrNull(),
                prevFocusRequester = null
            ) {
                AsyncImage(
                    model = profileTab?.iconUrl ?: "",
                    contentDescription = profileTab?.displayName ?: "Profile",
                    modifier = Modifier.size(32.dp),
                    colorFilter = if (selectedIndex == 0) {
                        androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFF49FEDD))
                    } else {
                        null
                    }
                )
                if (expanded) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = profileTab?.displayName ?: "Profile",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))

            otherTabs.forEachIndexed { index, tab ->
                val isFirstItem = index == 0
                val isLastItem = index == otherTabs.lastIndex

                FocusableRow(
                    modifier = Modifier.focusRequester(focusRequesters[index]),
                    selected = selectedIndex == index + 1,
                    expanded = expanded,
                    onFocus = { selectedIndex = index + 1 },
                    focusRequester = focusRequesters[index],
                    nextFocusRequester = if (isLastItem) null else focusRequesters[index + 1],
                    prevFocusRequester = if (isFirstItem) profileFocusRequester else focusRequesters[index - 1],
                    onClick = {
                        selectedTabIndex = index
                        selectedIndex = index + 1
                        expanded = false
                        when (tab.name) {
                            //"home" -> navController.navigate(Destination.homeScreen)
                            "channels" -> navController.navigate(Destination.genreScreen)
                            "settings" -> navController.navigate(Destination.settings)
                            "epg" -> navController.navigate(Destination.epgScreen) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                ) {
                    AsyncImage(
                        model = tab.iconUrl,
                        contentDescription = tab.displayName,
                        modifier = Modifier.size(32.dp),
                        colorFilter = if (selectedIndex == index + 1) {
                            androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFF49FEDD))
                        } else {
                            null
                        }
                    )
                    if (expanded) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tab.displayName,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
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
    focusRequester: FocusRequester,
    nextFocusRequester: FocusRequester?,
    prevFocusRequester: FocusRequester?,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var keyPressCooldown by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .width(160.dp)
            .height(50.dp)
            .padding(8.dp)
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    onFocus()
                }
            }
            .focusable()
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown && !keyPressCooldown) {
                    keyPressCooldown = true
                    coroutineScope.launch {
                        delay(200)
                        keyPressCooldown = false
                    }
                    when (keyEvent.nativeKeyEvent.keyCode) {
                        android.view.KeyEvent.KEYCODE_DPAD_DOWN -> {
                            nextFocusRequester?.requestFocus()
                            true
                        }
                        android.view.KeyEvent.KEYCODE_DPAD_UP -> {
                            prevFocusRequester?.requestFocus()
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
            .background(if (selected && expanded) Color.White.copy(alpha = 0.2f) else Color.Transparent)
            .border(
                width = if (selected && expanded) 2.dp else 0.dp,
                color = if (selected && expanded) Color(0xFF49FEDD) else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}
