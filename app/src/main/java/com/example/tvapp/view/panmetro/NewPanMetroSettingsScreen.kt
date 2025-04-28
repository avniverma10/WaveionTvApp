package com.example.tvapp.view.panmetro



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tvapp.R
import com.example.tvapp.ui.theme.base_color
import com.example.tvapp.ui.theme.screen_bg_color
import com.example.tvapp.view.navigationhelper.Destination
import com.example.tvapp.view.player.CommonDialog
import com.example.tvapp.viewmodels.SharedViewModel


@Composable
fun NewPanMetroSettingsScreen(navController: NavController,sharedViewModel: SharedViewModel) {

    var showInfo by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = screen_bg_color)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PermettoTopBar()

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = screen_bg_color)
            ) {
//                ExpandableNavigationMenu(navController, sharedViewModel, onNavMenuIntent = { _, _ -> })

                NewMainSettingsContent(
                    onInfoClick = { showInfo = true },
                    onLogoutClick = { showExitDialog = true }

                )
                }

        }
    }
    if (showInfo) {
        NewPanMetroInfoScreen(
            onOkClick = { showInfo = false }
        )
    }
    if (showExitDialog) {
        CommonDialog(
            showDialog = true,
            title = "Logout App",
            message = "Are you sure you want to logout?",
            errorCode = null,
            errorMessage = null,
            borderColor = Color.Transparent,
            confirmButtonText ="Yes" ,
            onConfirm =  {
                sharedViewModel.clearLogin()
                showExitDialog = false
                navController.navigate(Destination.loginScreen) {
                    popUpTo(0)
                }
            },
            dismissButtonText = "No",
            onDismiss = { showExitDialog = false }
        )
    }
}

@Composable
fun NewMainSettingsContent(  onInfoClick: () -> Unit ,  onLogoutClick: () -> Unit) {
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
            .fillMaxSize().background(Color(0xFF2A2D32)),
//            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .width(700.dp)
                .height(400.dp)
                .background(
                  Color(0xFF364154),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(padding)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                verticalArrangement = Arrangement.spacedBy(itemSpacing),
                modifier = Modifier.fillMaxSize().fillMaxWidth().padding(vertical = 90.dp)
            ) {
                items(menuData) { item ->
                    NewMenuItemCard(
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
fun NewMenuItemCard(
    title: String,
    iconResId: Int,
    onClick: () -> Unit = {}
) {
    var isFocused by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .size(width = 100.dp, height = 160.dp)
            .onFocusChanged { isFocused = it.isFocused } // detect focus
            .focusable(interactionSource = remember { MutableInteractionSource() })
            .clickable { onClick() }
            .then(
                if (isFocused) Modifier.background(color = base_color,shape = RoundedCornerShape(8.dp))
                    .border(
                        width = 3.dp,
                        color = base_color,
                        shape = RoundedCornerShape(8.dp)
                    ) else Modifier
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2D32)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
//                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circular Image
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = "$title Icon",
                modifier = Modifier
                    .size(55.dp)
                    .clip(CircleShape)
            )
            Text(
                text = title,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                color = Color.White,
                fontFamily = androidx.compose.ui.text.font.FontFamily(
                    androidx.compose.ui.text.font.Font(R.font.figtree_medium)
                ),


            )
        }
    }
}

