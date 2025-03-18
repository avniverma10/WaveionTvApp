package com.example.tvapp.view.navigationhelper

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.example.tvapp.extensions.appManifestLiveData
import com.example.tvapp.model.data.manifest.TabInfo
import com.example.tvapp.viewmodels.SharedViewModel

@Composable
fun ExpandableNavigationMenu(navController: NavController,sharedViewModel: SharedViewModel,onNavMenuIntent:(tabInfo:TabInfo,selectedIndex:Int)->Unit) {
    val tabs = sharedViewModel.provideApplicationContext().appManifestLiveData().value?.tab
    var expanded by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableStateOf(0) }

    // Handle back button press to collapse the menu
    if (expanded) {
        BackHandler { expanded = false }
    }

    // Split tabs into profile and others if available
    val profileTab = tabs?.firstOrNull()
    val otherTabs = if (tabs?.isNotEmpty() == true) tabs.drop(1) else emptyList()

    Column(
        modifier = Modifier
            .width(if (expanded) 180.dp else 80.dp)
            .fillMaxHeight()
            .background(Color.Black)
            .animateContentSize()
            .padding(8.dp)
    ) {
        // Profile row (Show a placeholder if tabs are empty)
        FocusableRow(
            onClick = {
                expanded = !expanded
                tabs?.getOrNull(selectedTabIndex)?.let { onNavMenuIntent(it,selectedTabIndex) }
            }
        ) {
            if (profileTab?.iconUrl != null) {
                AsyncImage(
                    model = profileTab.iconUrl,
                    contentDescription = profileTab.displayName,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.Gray) // Placeholder for profile icon
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = profileTab?.displayName ?: "Profile",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }

        Divider(
            color = Color.Gray,
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(60.dp))

        // If tabs are not available yet, show a small loading indicator instead of empty space
        if (tabs?.isEmpty() == true) {
            Text(
                text = "Loading...",
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp)
            )
        } else {
            // Render available tabs
            otherTabs.forEachIndexed { index, tab ->
                FocusableRow(
                    onClick = {
                        selectedTabIndex = index
                        if (!expanded) {
                            expanded = true
                        } else {
                            if (tab.displayName == "Home") {
                                navController.navigate(Destination.homeScreen)
                            }
                            if (tab.displayName == "Search") {
                                navController.navigate(Destination.searchScreen)
                            }
                            if (tab.displayName == "Live Tv") {
                                navController.navigate(Destination.epgScreen)
                            }
                            expanded = false
                        }
                    }
                ) {
                    if (tab.iconUrl != null) {
                        AsyncImage(
                            model = tab.iconUrl,
                            contentDescription = tab.displayName,
                            modifier = Modifier.size(32.dp)
                        )
                    }
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
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            // First, mark this as a focus target and capture focus changes.
            .focusTarget()
            .onFocusChanged { isFocused = it.isFocused }
            // Optionally add a background tint when focused.
            .background(if (isFocused) Color.White.copy(alpha = 0.1f) else Color.Transparent)
            // Draw the white border if focused.
            .border(
                border = if (isFocused) BorderStroke(2.dp, Color.White)
                else BorderStroke(0.dp, Color.Transparent)
            )
            // Make the row clickable and focusable.
            .clickable(onClick = onClick)
            .focusable(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}



