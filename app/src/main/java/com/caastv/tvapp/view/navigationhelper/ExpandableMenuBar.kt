package com.caastv.tvapp.view.navigationhelper

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.android.caastv.R
import com.caastv.tvapp.extensions.loge
import com.caastv.tvapp.model.data.manifest.TabInfo
import com.caastv.tvapp.utils.theme.base_color
import com.caastv.tvapp.view.uicomponent.error.CommonDialog
import com.caastv.tvapp.viewmodels.SharedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ExpandableNavigationMenu(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    menuFocusRequester: FocusRequester,
    onBackPressed: () -> Unit,
    onNavMenuIntent: (tabInfo: TabInfo, selectedIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = sharedViewModel.manifestData.collectAsState().value?.tab?.filter { it.name in arrayOf("epg","settings","channels","profile","home","search","all","movies","app" )&& it.isVisible }
    var expanded by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    var selectedIndex by remember { mutableStateOf(-1) }
    var showExitDialog by remember { mutableStateOf(false) }
    var backPressCount by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    var lastClickTime by remember { mutableStateOf(0L) }
    val context = LocalContext.current
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val profileTab = tabs?.find { it.name == "profile" }
    val profileTabIndex = tabs?.indexOf(profileTab)?:0
    val otherTabs = tabs?.drop(1) ?: emptyList()
    val profileFocusRequester = remember { FocusRequester() }
    val focusRequesters = List(otherTabs.size) { FocusRequester() }

    LaunchedEffect(currentBackStackEntry) {
        expanded = false
        val currentRoute = currentBackStackEntry?.destination?.route
        selectedIndex = when (currentRoute) {
            Destination.profile -> tabs?.indexOfFirst { it.name == "profile" } ?: -1
            Destination.appsScreen -> tabs?.indexOfFirst { it.name == "app" } ?: -1
            Destination.channel -> tabs?.indexOfFirst { it.name == "all" } ?: -1
            Destination.homeScreen -> tabs?.indexOfFirst { it.name == "home" } ?: -1
            Destination.demoHome -> tabs?.indexOfFirst { it.name == "movies" } ?: -1
            Destination.searchScreen -> tabs?.indexOfFirst { it.name == "search" } ?: -1
            Destination.epgScreen -> tabs?.indexOfFirst { it.name == "epg" } ?: -1
            Destination.genreScreen -> tabs?.indexOfFirst { it.name == "channels" } ?: -1
            Destination.settings -> tabs?.indexOfFirst { it.name == "settings" } ?: -1
            else -> -1
        }
    }

    BackHandler {
        onBackPressed()
    }

    if (showExitDialog) {
        CommonDialog(
            showDialog = true,
            title = "Exit App",
            borderColor = Color.Transparent,
            painter = painterResource(id = R.drawable.exit_icon),
            message = "Are you sure you want to exit the app?",
            confirmButtonText = "Yes",
            onConfirm = {
                (context as? Activity)?.finishAffinity()
//                Process.killProcess(Process.myPid())
            },
            dismissButtonText = "No",
            onDismiss = {
                showExitDialog = false
                if (selectedIndex <= 0) {
                    profileFocusRequester.requestFocus()
                } else {
                    focusRequesters.getOrNull(selectedIndex - 1)?.let { requester ->
                        try {
                            requester.requestFocus()
                        } catch (e: IllegalStateException) {
//                            loge("FocusError", "FocusRequester not initialized ${e.message}")
                        }
                    }
                }
            }
        )
    }

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
                                loge("FocusError", "FocusRequester not initialized ${e.message}")
                            }
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .zIndex(4f)
    ) {
        Column(
            modifier = Modifier
                .width(if (expanded) 280.dp else 70.dp)
                .fillMaxHeight()
                .focusRequester(menuFocusRequester)
                .onFocusChanged { state ->
                    if (state.isFocused) {
                        expanded = true
                    }
                }
                .onPreviewKeyEvent { keyEvent ->
                    when (keyEvent.nativeKeyEvent.keyCode) {
                        android.view.KeyEvent.KEYCODE_BACK -> {
                            backPressCount++
                            if (backPressCount > 1) {
                                showExitDialog = true
                                backPressCount = 0
                            }
                            menuFocusRequester.requestFocus()
                            true
                        }
                        android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            expanded = false
                            false
                        }
                        else -> false
                    }
                }
                .animateContentSize()
                .focusable()
                .drawBehind {
                    val gradient = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black,
                            Color.Black,
                            Color.Black.copy(alpha = 0.6f),
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
                onClick = {
                    selectedTabIndex = -1
                    selectedIndex = 0
                    expanded = false
                    navController.navigate(Destination.profile)
                },
                focusRequester = profileFocusRequester,
                nextFocusRequester = focusRequesters.firstOrNull(),
                prevFocusRequester = null
            ) {
                AsyncImage(
                    model = profileTab?.iconUrl ?: "",
                    contentDescription = profileTab?.displayName ?: "Profile",
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp).size(36.dp),
                    colorFilter = if (selectedIndex == 0) {
                        androidx.compose.ui.graphics.ColorFilter.tint(base_color)
                    } else {
                        null
                    }
                )
                if (expanded) {
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = profileTab?.displayName ?: "Profile",
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp),
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

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
                            "app" -> navController.navigate(Destination.appsScreen)
                            "all" -> navController.navigate(Destination.channel)
                            "home" -> navController.navigate(Destination.homeScreen)
                            "search" -> navController.navigate(Destination.searchScreen)
                            "movies" -> navController.navigate(Destination.demoHome)
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
                        modifier = Modifier.size(36.dp).padding(vertical = 6.dp, horizontal = 4.dp).size(36.dp),
                        colorFilter = if (selectedIndex == index + 1) {
                            androidx.compose.ui.graphics.ColorFilter.tint(base_color)
                        } else {
                            null
                        }
                    )
                    if (expanded) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tab.displayName,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp),
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
            .width(150.dp)
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
                color = if (selected && expanded) base_color else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}
