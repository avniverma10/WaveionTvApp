package com.caastv.tvapp.view.appscreen

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.caastv.tvapp.extensions.hideKeyboard
import com.caastv.tvapp.extensions.launchPackageIfInstalled
import com.caastv.tvapp.extensions.showToastS
import com.caastv.tvapp.utils.theme.base_color
import com.caastv.tvapp.view.navigationhelper.ExpandableNavigationMenu
import com.caastv.tvapp.viewmodels.SharedViewModel

@Composable
fun AppsScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val menuFocusRequester = remember { FocusRequester() }
    val lifecycleOwner = LocalLifecycleOwner.current

    val apps by sharedViewModel.inventoryApps.collectAsState()
    val lastSelectedApp = rememberSaveable { mutableStateOf<String?>(null) }
    val focusRequesters = remember(apps) {
        apps.associate { it.packageName to FocusRequester() }
    }

    LaunchedEffect(apps) {
        context.hideKeyboard()
        val fallback = apps.firstOrNull()?.packageName
        focusRequesters[lastSelectedApp.value ?: fallback]?.requestFocus()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 70.dp, top = 50.dp, end = 16.dp, bottom = 16.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(22.dp),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(apps, key = { it.packageName }) { app ->
                    val interactionSource = remember { MutableInteractionSource() }
                    val isFocused by interactionSource.collectIsFocusedAsState()

                    if (isFocused) {
                        lastSelectedApp.value = app.packageName
                    }

                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(100.dp)
                            .focusRequester(focusRequesters[app.packageName] ?: FocusRequester())
                            .focusable(interactionSource = interactionSource)
                            .border(
                                width = if (isFocused) 2.dp else 0.dp,
                                color = if (isFocused) base_color else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                val launched = context.launchPackageIfInstalled(app.packageName)
                                if (!launched) {
                                    context.showToastS("App not installed")
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = android.net.Uri.parse("market://details?id=${app.packageName}")
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    if (intent.resolveActivity(context.packageManager) != null) {
                                        context.startActivity(intent)
                                    } else {
                                        val webIntent = Intent(Intent.ACTION_VIEW).apply {
                                            data = android.net.Uri.parse("https://play.google.com/store/apps/details?id=${app.packageName}")
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(webIntent)
                                    }
                                }
                            },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2E)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    ) {
                        AsyncImage(
                            model = app.iconUrl,
                            contentDescription = app.appName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        ExpandableNavigationMenu(
            navController = navController,
            sharedViewModel = sharedViewModel,
            onNavMenuIntent = { _, _ -> },
            modifier = Modifier.align(Alignment.CenterStart),
            menuFocusRequester = menuFocusRequester,
            onBackPressed = {
                menuFocusRequester.requestFocus()
            }
        )
    }

    DisposableEffect(lifecycleOwner, apps) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val fallback = apps.firstOrNull()?.packageName
                focusRequesters[lastSelectedApp.value ?: fallback]?.requestFocus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
