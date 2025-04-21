package com.example.tvapp.view.panmetro

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tvapp.extensions.findMyDeviceId
//import com.example.tvapp.extensions.getActiveMacAddress
import com.example.tvapp.extensions.getAndroidTvDrmInfo
import com.example.tvapp.extensions.macAddress
import com.example.tvapp.extensions.readMacFromSysfs

@RequiresApi(Build.VERSION_CODES.M)
@Composable
fun NewPanMetroInfoScreen(
    username: String = "TEST 56",
    macId: String = "DTS-CB95-FQE",
    validity: String = "03/04/2025",
    appVersion: String = "1.2",
    androidVersion: String = "11",
    ram: String = "2 GB",
    storage: String = "32.0 GB",
    ota: String = "N/A",
    stbModel: String = "DTP1731",
    networkId: String = "N/A",
    networkName: String = "GTPLKCBPL Digital Cable TV & Broadband",
    drmId: String = "102",
    drmVersion: String = "2024.01",
    onOkClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val systemInfo = context.getAndroidTvDrmInfo()

    BackHandler {
        onOkClick()
    }
    val okButtonFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        okButtonFocusRequester.requestFocus()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x80000000)), // semi-transparent black
        contentAlignment = Alignment.Center
    ) {
        // Centered Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF364154)),
            modifier = Modifier
                .widthIn(min = 300.dp, max = 400.dp)
                .heightIn(max = 600.dp)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "System information",
                    fontSize = 20.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Info rows
                NewInfoRow(label = "Username", value = username)
                NewInfoRow(label = "MAC ID", value = "/sys/class/net/wlan0/address".macAddress()?:"")
                NewInfoRow(label = "Validity", value = validity)
                NewInfoRow(label = "App version", value = appVersion)
                NewInfoRow(label = "Android Version", value = systemInfo?.androidVersion?:androidVersion)
                NewInfoRow(label = "RAM", value = systemInfo?.totalMemory?:ram)
                NewInfoRow(label = "Storage", value = systemInfo?.storageInfo?:storage)
                NewInfoRow(label = "OTA", value = ota)
                NewInfoRow(label = "STB Model", value = systemInfo?.model?:stbModel)
                NewInfoRow(label = "Network ID", value = networkId)
                NewInfoRow(label = "Network Name", value = networkName)
                NewInfoRow(label = "DRM ID", value = systemInfo?.drmScheme?:drmId)
                NewInfoRow(label = "DRM VERSION", value = drmVersion)

                Spacer(modifier = Modifier.height(16.dp))

                // OK button
                Button(
                    onClick = { onOkClick() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5CAD5C)),
                    modifier = Modifier
                        .focusRequester(okButtonFocusRequester) // this will get focus automatically
                        .align(Alignment.CenterHorizontally)
                        .width(200.dp)
                        .height(38.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "OK", fontSize = 18.sp)
                }


            }
        }
    }
}

@Composable
fun NewInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label remains black
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            color = if (label == "Username" || label == "MAC ID") Color.White else Color(0xFF5CAD5C),
            textAlign = TextAlign.Start,
            modifier = Modifier.weight(1f)
        )
    }
}


