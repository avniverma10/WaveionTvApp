package com.tccl.tvapp.view.panmetro.settings

import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.android.tccl.R
import com.tccl.tvapp.extensions.getAndroidTvDrmInfo
import com.tccl.tvapp.extensions.hideKeyboard
import com.tccl.tvapp.extensions.provideMacAddress
import com.tccl.tvapp.utils.uistate.PreferenceManager
import com.tccl.tvapp.view.uicomponent.keyboard.HideKeyboardOnEnter
import com.tccl.tvapp.viewmodels.SharedViewModel

@Composable
fun PanMetroInfoScreen(
    username: String = "TEST 56",
    macId: String = "DTS-CB95-FQE",
    validity: String = "26/04/2025",
    appVersion: String = "1.0.15",
    androidVersion: String = "11",
    ram: String = "2 GB",
    storage: String = "32 GB",
    stbModel: String = "DTP1731",
    networkName: String = "TCCL",
    drmId: String = "102",
    navController: NavController,
    sharedViewModel: SharedViewModel,
    onOkClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val systemInfo = context.getAndroidTvDrmInfo()

    /* track whether to show the confirmation dialog */
    var showExitDialog by remember { mutableStateOf(false) }


    Log.d("PanMetroInfoScreen", "showExitDialog: $showExitDialog")
    // intercept back-press as “logout” as well
    BackHandler {
        onOkClick()
    }

    //track focus state
    val logoutInteractionSource = remember { MutableInteractionSource() }
    val isLogoutFocused by logoutInteractionSource.collectIsFocusedAsState()
    val scope = rememberCoroutineScope()


    //auto-focus the Logout button
    val logoutRequester = remember { FocusRequester() }

    //hide keyboard forcefully
    HideKeyboardOnEnter()
    LaunchedEffect(Unit) {
        context.hideKeyboard()
        logoutRequester.requestFocus()
    }


    val okButtonFocusRequester = remember { FocusRequester() }

    //hide keyboard forcefully
    //HideKeyboardOnEnter()
    LaunchedEffect(Unit) {
        context.hideKeyboard()
        okButtonFocusRequester.requestFocus()
    }

    Scaffold(
        containerColor = Color(0xFF1A1A1D),
        bottomBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { onOkClick() },
                    modifier = Modifier
                        .focusRequester(logoutRequester)
                        .focusable(interactionSource = logoutInteractionSource)
                        .then(
                            if (isLogoutFocused) Modifier.border(
                                BorderStroke(2.dp, Color(0xFF49FEDD)),
                                shape = RoundedCornerShape(8.dp)
                            ) else Modifier
                        )
                        .width(200.dp)
                        .height(40.dp),
                    interactionSource = logoutInteractionSource,
                    colors = ButtonDefaults.buttonColors(
                        // your custom background and content colors
                        containerColor = if (isLogoutFocused) Color(0x1A49FEDD) else Color.White,
                        contentColor   = if (isLogoutFocused) Color.White      else Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("OK", fontSize = 16.sp)
                }
            }
        }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(vertical = 24.dp, horizontal = 16.dp)
        ) {
            // … your logo, title, info panel exactly as before …
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(R.drawable.tccl_logo),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("TCCL", color = Color.White, fontSize = 18.sp)
            }

            Spacer(Modifier.height(32.dp))

            Text(
                "System Information",
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    InfoRow("Username", PreferenceManager.getUsername()?:"TCCL")
                    InfoRow("MAC ID", context.provideMacAddress() ?: macId)
                    InfoRow("Validity", validity)
                    InfoRow("App version", value = context.packageManager
                        .getPackageInfo(context.packageName, 0)
                        .versionName?:appVersion)
                    InfoRow("Android version", systemInfo?.androidVersion ?: androidVersion)
                    InfoRow("RAM", systemInfo?.totalMemory ?: ram)
                    InfoRow("Storage", systemInfo?.storageInfo ?: storage)
                    InfoRow("STB Model", Build.MODEL)
                    InfoRow("Brand name", Build.BRAND)
                    InfoRow("Network name", networkName)
                    InfoRow("DRM ID", systemInfo?.drmScheme ?: drmId)
                }
            }
        }
    }
}

