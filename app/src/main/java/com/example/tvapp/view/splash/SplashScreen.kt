package com.example.tvapp.view.splash

import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.example.tvapp.R
import com.example.tvapp.extensions.getIptvDeviceInfo
import com.example.tvapp.extensions.logAllDrmInfo
import com.example.tvapp.extensions.provideMacAddrLiveData
import com.example.tvapp.extensions.provideMacAddress
import com.example.tvapp.extensions.showToastS
import com.example.tvapp.extensions.toJSONObject
import com.example.tvapp.view.navigationhelper.Destination
import com.example.tvapp.view.player.CommonDialog
import com.example.tvapp.view.uicomponent.ErrorDialog
import com.example.tvapp.viewmodels.SharedViewModel


@Composable
fun SplashScreen(sharedViewModel: SharedViewModel, navController: NavController) {
    val macAddress = sharedViewModel.provideApplicationContext()?.provideMacAddrLiveData()
    val context   = LocalContext.current
    val activity  = (context as? Activity)

    val loginInfo = sharedViewModel.loginInfo?.collectAsState()?.value
    val errorLoadingData by sharedViewModel.errorLoadingData.collectAsState()
    val isInitializeData by sharedViewModel.isInitializeData.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }
// 2) Update dialog state
    val showDialog by sharedViewModel.showUpdateDialog.collectAsState()
    val updateData by sharedViewModel.appUpdateData.collectAsState()

    // 3) Download state
    val downloadId by sharedViewModel.downloadId.collectAsState()
    val isUpdating = downloadId != null
    Log.d("Splash", "downloadId = $downloadId")
    Log.d("Splash","show dialog in splash = $showDialog")
    Log.d("Splash","App update data ----> $updateData")
    val dm = remember {
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    LaunchedEffect(Unit) {
        sharedViewModel.checkForAppUpdate()
    }



    LaunchedEffect(errorLoadingData,isInitializeData) {
        if (errorLoadingData != null) {
            //context.showToastS(errorLoadingData)
            showExitDialog = true
        }
        if(isInitializeData){
            showExitDialog = false
            if(loginInfo?.username?.isNotEmpty() == true){
                navController.navigate(Destination.genreScreen) {
                    popUpTo(Destination.splashScreen) { inclusive = true }
                }
            }else{
                navController.navigate(Destination.loginScreen) {
                    popUpTo(Destination.splashScreen) { inclusive = true }
                }
            }
        }
    }
    // — show the update dialog —
    if (showDialog && updateData != null) {
        CommonDialog(
            showDialog = true,
            title = "Update available",
            message = "Do you want to update the app?",
            errorCode = null,
            errorMessage = null,
            borderColor = Color.Transparent,
            confirmButtonText = "Yes",
            onConfirm = { sharedViewModel.onUserAcceptedUpdate() },
            dismissButtonText = "No",
            onDismiss = { sharedViewModel.onUserDeclinedUpdate() },
        )
    }

    // — register for download‑complete only once downloadId is set —
    DisposableEffect(downloadId) {
        if (downloadId != null) {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context, intent: Intent) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                    if (id == downloadId) {
                        // 1) Query status...
                        val q = DownloadManager.Query().setFilterById(id)
                        dm.query(q).use { cursor ->
                            if (cursor != null && cursor.moveToFirst()) {
                                val status = cursor.getInt(cursor.getColumnIndexOrThrow(
                                    DownloadManager.COLUMN_STATUS))

                                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                    val apkUri: Uri = dm.getUriForDownloadedFile(id)

                                    // **Clear the downloadId first** so spinner hides immediately
                                    sharedViewModel.clearDownloadId()

                                    // 2) Launch the installer using ACTION_VIEW
                                    val installIntent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(apkUri, "application/vnd.android.package-archive")
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                                                Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }

                                    // Only start if there's an activity to handle it
                                    if (installIntent.resolveActivity(context.packageManager) != null) {
                                        context.startActivity(installIntent)
                                    } else {
                                        context.showToastS("No installer found on device")
                                    }

                                    // 3) Finish splash so old process ends
                                    activity?.finish()

                                } else {
                                    // failure case: also clear so spinner goes away
                                    sharedViewModel.clearDownloadId()
                                    context.showToastS("Download failed (status=$status)")
                                }
                            }
                        }
                    }
                }
            }

            ContextCompat.registerReceiver(
                context,
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                ContextCompat.RECEIVER_EXPORTED
            )

            onDispose { context.unregisterReceiver(receiver) }
        } else {
            onDispose { /* nothing to clean up */ }
        }
    }


    // — only navigate away when not in “update?” dialog and not mid‑download —
    LaunchedEffect(isInitializeData, showDialog, errorLoadingData, loginInfo, isUpdating) {
        if (!isInitializeData) return@LaunchedEffect
        if (showDialog)         return@LaunchedEffect
        if (isUpdating)         return@LaunchedEffect

        errorLoadingData?.let {
            context.showToastS(it)
            return@LaunchedEffect
        }

        if (loginInfo?.username?.isNotEmpty() == true) {
            navController.navigate(Destination.genreScreen) {
                popUpTo(Destination.splashScreen) { inclusive = true }
            }
        } else {
            navController.navigate(Destination.loginScreen) {
                popUpTo(Destination.splashScreen) { inclusive = true }
            }
        }
    }

    // Exit confirmation dialog
    if (showExitDialog) {
        ErrorDialog(errorLoadingData?:"Server Error", onConfirmExit = {
            sharedViewModel._errorLoadingData.value = null
            showExitDialog = false
            sharedViewModel.initializeAppRequiredData()
        }, onDismiss = {
            showExitDialog = false
        })
    }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        val device = context.getIptvDeviceInfo().toJSONObject()
        AsyncImage(
            model = ImageRequest.Builder(context)
                // .data(yourSplashUrlHere)
                .diskCachePolicy(CachePolicy.ENABLED)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .build(),
            contentDescription = "Splash background",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.align(Alignment.Center),
            error       = painterResource(R.drawable.gtpl_logo),
            placeholder = painterResource(R.drawable.gtpl_logo)
        )
        if (isUpdating) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            )
        }
    }
}
