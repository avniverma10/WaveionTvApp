package com.caastv.tvapp.view.panmetro.settings

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.caastv.tvapp.extensions.getAndroidTvDrmInfo
import com.caastv.tvapp.extensions.provideMacAddress
import com.caastv.tvapp.utils.uistate.PreferenceManager

@Composable
fun SystemInfoDialog(
    username: String = "TEST 56",
    macId: String = "DTS-CB95-FQE",
    validity: String = "26/04/2025",
    appVersion: String = "1.0.15",
    androidVersion: String = "11",
    ram: String = "2 GB",
    storage: String = "32 GB",
    stbModel: String = "DTP1731",
    networkName: String = "CAASTV",
    drmId: String = "102",
    onBack: () -> Unit
) {

    val context = LocalContext.current
    val systemInfo = context.getAndroidTvDrmInfo()

    Dialog(onDismissRequest = onBack) {         // full-screen overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = .3f))        // scrim
        ) {
            // ─── Main card ────────────────────────────────────────────────
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2B2B)),
                shape  = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .widthIn(max = 640.dp)
                    .padding(horizontal = 24.dp)
            ) {

                Row(
                    modifier = Modifier
                        .padding(start = 24.dp, top = 12.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        "CAASTV",
                        color = Color(0xFF00E5FF),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "System Information",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(20.dp))

                Column(
                    modifier = Modifier
                        .padding(horizontal = 32.dp, vertical = 24.dp)
                        .verticalScroll(rememberScrollState())
                ) {

                    InfoItem("Username", PreferenceManager.getUsername()?:"CaasTV")
                    InfoItem("MAC ID", context.provideMacAddress() ?: macId)
                    InfoItem("Validity", validity)
                    InfoItem("App version", value = context.packageManager
                        .getPackageInfo(context.packageName, 0)
                        .versionName?:appVersion)
                    InfoItem("Android version", systemInfo?.androidVersion ?: androidVersion)
                    InfoItem("RAM", systemInfo?.totalMemory ?: ram)
                    InfoItem("Storage", systemInfo?.storageInfo ?: storage)
                    InfoItem("STB Model", Build.MODEL)
                    InfoItem("Brand name", Build.BRAND)
                    InfoItem("Network name", networkName)
                    InfoItem("DRM ID", systemInfo?.drmScheme ?: drmId)

                    Spacer(Modifier.height(24.dp))

                    // ─── Logout button ────────────────────────────────
                    val btnFocus = remember { FocusRequester() }
                    LaunchedEffect(Unit) { btnFocus.requestFocus() }

                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .focusRequester(btnFocus)
                            .width(220.dp)
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(2.dp, Color(0xFF00E5FF)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor   = Color(0xFF00E5FF)
                        ),
                    ) {
                        Text("Back", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}


@Composable
private fun InfoItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text  = label.uppercase(),
            color = Color(0xFFBDBDBD),
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text  = value.uppercase(),
            color = Color(0xFFF5F5F5),
            fontSize = 14.sp,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}