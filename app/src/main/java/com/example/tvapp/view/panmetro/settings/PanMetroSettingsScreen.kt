package com.example.tvapp.view.panmetro.settings

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.tvapp.view.uicomponent.GradientBackground
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tvapp.R
import com.example.tvapp.view.navigationhelper.Destination
import com.example.tvapp.view.panmetro.common.PermettoTopBar
import com.example.tvapp.view.uicomponent.keyboard.HideKeyboardOnEnter
import com.example.tvapp.viewmodels.SharedViewModel


@Composable
fun PanMetroSettingsScreen(navController: NavController,sharedViewModel: SharedViewModel) {

    HideKeyboardOnEnter()
    var showInfo by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    val loginInfo = sharedViewModel.loginInfo?.collectAsState()?.value
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1F3A6B))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PermettoTopBar()
            GradientBackground {
                MainSettingsContent(
                    onInfoClick = { showInfo = true },
                    onLogoutClick = { showExitDialog = true }

                )
            }
        }
    }
    if (showInfo) {
        PanMetroInfoScreen(
            username = loginInfo?.username?:"WTV",
            onOkClick = { showInfo = false }
        )
    }
    if (showExitDialog) {
        PanMetroLogoutDialog (
            onConfirmExit = {
                sharedViewModel.clearLogin()
                showExitDialog = false
                navController.navigate(Destination.loginScreen) {
                    popUpTo(0)
                }
            },
            onDismiss = {
                showExitDialog = false
            }
        )
    }
}

@Composable
fun MainSettingsContent(  onInfoClick: () -> Unit ,  onLogoutClick: () -> Unit) {
    val menuItems = listOf(
        "Info", "Logout"
    )

    val menuIcons = listOf(
        R.drawable.info, R.drawable.logout
    )

    val menuData = menuItems.zip(menuIcons)

    val itemSpacing = 16.dp
    val padding = 25.dp

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .width(500.dp)
                .height(250.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF7F00FF),
                            Color(0xFFE100FF)
                        )
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(padding)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                verticalArrangement = Arrangement.spacedBy(itemSpacing),
                modifier = Modifier.fillMaxSize().fillMaxWidth().padding(vertical = 20.dp)
            ) {
                items(menuData) { item ->
                    MenuItemCard(
                        title = item.first,
                        iconResId = item.second,
                        onClick = {
                            when (item.first) {
                                "Info" -> onInfoClick()
                                "Logout" -> onLogoutClick()
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }


}

@Composable
fun MenuItemCard(
    title: String,
    iconResId: Int,
    onClick: () -> Unit = {}
) {
    var isFocused by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .size(width = 100.dp, height = 150.dp)
            .onFocusChanged { isFocused = it.isFocused } // detect focus
            .focusable(interactionSource = remember { MutableInteractionSource() })
            .clickable { onClick() }
            .then(
                if (isFocused) Modifier.background(Color.Yellow)
                    .border(
                        width = 3.dp,
                        color = Color.Yellow,
                        shape = RoundedCornerShape(8.dp)
                    ) else Modifier
            ),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circular Image
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = "$title Icon",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
            )
            Text(
                text = title,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = Color.Black
            )
        }
    }
}

