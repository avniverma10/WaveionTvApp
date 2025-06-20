package com.example.tvapp

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.tvapp.ui.theme.base_color
import com.example.tvapp.view.navigationhelper.ExpandableNavigationMenu
import com.example.tvapp.viewmodels.SharedViewModel
import com.example.tvapp.CustomApps.AppItem


private val staticApps = listOf(
    AppItem("YouTube",    logoRes = com.android.caastv.R.drawable.youtube,     url = "https://youtube.com",       packageName = "com.google.android.youtube"),
    AppItem("SonyLiv",    logoRes = com.android.caastv.R.drawable.sony_liv,    url = "https://sonyliv.com",       packageName = "com.sonyliv"),
    AppItem("JioHotstar", logoRes = com.android.caastv.R.drawable.jiohotstar, url = "https://hotstar.com",       packageName = "in.startv.hotstar"),
    AppItem("Zee5",       logoRes = com.android.caastv.R.drawable.zee_5,       url = "https://zee5.com",          packageName = "com.graymatrix.did"),
    AppItem("PlayStore",  logoRes = com.android.caastv.R.drawable.playstore,   url = "https://play.google.com",   packageName = "com.android.vending")
)

@Composable
fun AppsScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
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
                contentPadding = PaddingValues(15.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(staticApps) { app ->
                    val interactionSource = remember { MutableInteractionSource() }
                    val isFocused by interactionSource.collectIsFocusedAsState()

                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(width = 300.dp, height = 100.dp)
                            .focusable(interactionSource = interactionSource)
                            .border(
                                width = if (isFocused) 2.dp else 0.dp,
                                color = if (isFocused) base_color else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                context.packageManager
                                    .getLaunchIntentForPackage(app.packageName)
                                    ?.apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(this)
                                    }
                            },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2E)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = app.logoRes),
                            contentDescription = app.displayName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        ExpandableNavigationMenu(
            navController = navController,
            sharedViewModel = sharedViewModel,
            onNavMenuIntent = { _, _ -> },
            modifier = Modifier.align(Alignment.CenterStart)
        )
    }
}