package com.caastv.tvapp.view.panmetro.settings



import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.android.caastv.R
import com.caastv.tvapp.extensions.hideKeyboard
import com.caastv.tvapp.utils.theme.base_color
import com.caastv.tvapp.utils.uistate.PreferenceManager
import com.caastv.tvapp.view.navigationhelper.ExpandableNavigationMenu
import com.caastv.tvapp.view.uicomponent.error.CommonDialog
import com.caastv.tvapp.viewmodels.SharedViewModel


@Composable
fun NewPanMetroSettingsScreen(navController: NavController,sharedViewModel: SharedViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val firstMenuItemFocusRequester = remember { FocusRequester() }

    var showInfo by remember { mutableStateOf(false) }
    var appSettingsDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    val menuFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        context.hideKeyboard()
        firstMenuItemFocusRequester.requestFocus()
    }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF14161A))
    ) {
        NewMainSettingsContent(
            onInfoClick = { showInfo = true },
            onPreferencesClick = { appSettingsDialog = true },
            onLogoutClick = { showExitDialog = true },
            firstMenuItemFocusRequester = firstMenuItemFocusRequester,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF2A2D32))
                .padding(start = 70.dp)         // <<< inset so it never shifts
        )

        ExpandableNavigationMenu(
            navController   = navController,
            sharedViewModel = sharedViewModel,
            onNavMenuIntent = { _, _ -> },
            modifier        = Modifier.align(Alignment.CenterStart),
            menuFocusRequester = menuFocusRequester,
            onBackPressed = {
                menuFocusRequester.requestFocus()
            }
        )
        if (showExitDialog) {
            CommonDialog(
                showDialog = true,
                title = "Logout App",
                message = "Are you sure you want to logout and exit the app?",
                painter = painterResource(id = R.drawable.logout_icon),
                errorCode = null,
                errorMessage = null,
                borderColor = Color.Transparent,
                confirmButtonText ="Yes" ,
                onConfirm =  {
                    PreferenceManager.clearLogin()
                    sharedViewModel.clearRecentlyWatched()
                    showExitDialog = false
                    context.hideKeyboard()
                    (context as? Activity)?.finishAffinity()

                },
                dismissButtonText = "No",
                onDismiss = { showExitDialog = false }
            )
        }
    }
    if (showInfo) {
        SystemInfoDialog(
           onBack = { showInfo = false }
        )
    }

    if (appSettingsDialog) {
        AppSettingsDialog(
            onToggle = { appSettings->
                PreferenceManager.saveAppSettings(appSettings)
            },
            onBack = { appSettingsDialog = false }
        )
    }

}
@Composable
fun NewMainSettingsContent(
    onInfoClick: () -> Unit,
    onPreferencesClick: () -> Unit,
    onLogoutClick: () -> Unit,
    firstMenuItemFocusRequester: FocusRequester,
    modifier: Modifier = Modifier  // our injected requester
) {
    val menuItems = listOf("Info","Preferences", "Logout")
    val menuIcons = listOf(R.drawable.info,R.drawable.settings, R.drawable.logout)
    val menuData  = menuItems.zip(menuIcons)

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2A2D32)),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment   = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(1f))

        val padding   = 25.dp
        val itemSpacing = 16.dp

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
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                verticalArrangement   = Arrangement.spacedBy(itemSpacing),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 90.dp)
            ) {
                itemsIndexed(menuData) { index, (title, iconRes) ->
                    NewMenuItemCard(
                        title     = title,
                        iconResId = iconRes,
                        // only the very first card picks up focus on screen-open:
                        modifier  = if (index == 0)
                            Modifier.focusRequester(firstMenuItemFocusRequester)
                        else
                            Modifier,
                        onClick   = {
                            when (title) {
                                "Info"   -> onInfoClick()
                                "Preferences"   -> onPreferencesClick()
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
    modifier: Modifier = Modifier,              // ← new
    onClick: () -> Unit = {}
) {
    var isFocused by remember { mutableStateOf(false) }

    Card(
        modifier = modifier                         // ← apply it here
            .size(width = 100.dp, height = 150.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable(interactionSource = remember { MutableInteractionSource() })
            .clickable { onClick() }
            .then(
                if (isFocused)
                    Modifier
                        .background(color = base_color, shape = RoundedCornerShape(8.dp))
                        .border(
                            width = 3.dp,
                            color = base_color,
                            shape = RoundedCornerShape(8.dp)
                        )
                else Modifier
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2D32)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement   = Arrangement.SpaceEvenly,
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = "$title Icon",
                modifier = Modifier
                    .size(55.dp)
                    .clip(CircleShape)
            )
            Text(
                text      = title,
                fontSize  = 18.sp,
                textAlign = TextAlign.Center,
                color     = Color.White,
                fontFamily = FontFamily(Font(R.font.figtree_medium))
            )
        }
    }
}
