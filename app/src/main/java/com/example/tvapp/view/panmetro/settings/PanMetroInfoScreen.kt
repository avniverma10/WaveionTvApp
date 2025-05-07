package com.example.tvapp.view.panmetro.settings

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tvapp.extensions.dataStore
import com.example.tvapp.extensions.getAndroidTvDrmInfo
import com.example.tvapp.extensions.hideKeyboard
import com.example.tvapp.extensions.provideMacAddress
import com.example.tvapp.utils.uistate.PreferenceManager
import com.example.tvapp.viewmodels.WTVViewModel.DataStoreKeys
import kotlinx.coroutines.flow.first

@Composable
fun PanMetroInfoScreen(
    macId: String = "DTS-CB95-FQE",
    validity: String = "25/05/2025",
    appVersion: String = "1.3",
    androidVersion: String = "11",
    ram: String = "2 GB",
    storage: String = "32.0 GB",
    ota: String = "Lasted",
    stbModel: String = "DTP1731",
    networkId: String = "1",
    networkName: String = "Panmetro Convergence \n Pvt Ltd",
    drmId: String = "102",
    drmVersion: String = "2024.01",
    onOkClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val systemInfo = context.getAndroidTvDrmInfo()
    var uName by remember { mutableStateOf("PanMetro") }

    BackHandler {
        onOkClick()
    }
    val okButtonFocusRequester = remember { FocusRequester() }

    //hide keyboard forcefully
    //HideKeyboardOnEnter()
    LaunchedEffect(Unit) {
        context.hideKeyboard()
        uName = context.dataStore.data.first().get(DataStoreKeys.USERNAME) ?: ""
        okButtonFocusRequester.requestFocus()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x80BBB7B7)), // semi-transparent black
        contentAlignment = Alignment.Center
    ) {
        // Centered Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .widthIn(min = 350.dp, max = 450.dp)
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
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Info rows
                InfoRow(label = "Username", value = PreferenceManager.getUsername()?:"Panmetro")
                InfoRow(label = "MAC ID", value = context.provideMacAddress()?:"")
                InfoRow(label = "Validity", value = validity)
                InfoRow(label = "App version", value = context.packageManager
                    .getPackageInfo(context.packageName, 0)
                    .versionName?:appVersion)
                InfoRow(label = "Android Version", value = Build.VERSION.SDK_INT.toString()?:systemInfo?.androidVersion?:androidVersion)
                InfoRow(label = "RAM", value = systemInfo?.totalMemory?:ram)
                InfoRow(label = "Storage", value = systemInfo?.storageInfo?:storage)
                InfoRow(label = "OTA", value = ota)
                InfoRow(label = "STB Model", value = Build.MODEL)
                InfoRow(label = "Network ID", value = networkId)
                InfoRow(label = "Network Name", value = networkName)
              //  InfoRow(label = "DRM ID", value = systemInfo?.drmScheme?:drmId)
              //  InfoRow(label = "DRM VERSION", value = drmVersion)

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
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label remains black
        Text(
            text = label.uppercase(),
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value.uppercase(),
            fontSize = 16.sp,
            color = Color.Black,
            textAlign = TextAlign.Start,
            modifier = Modifier.weight(1f)
        )
    }
}


