package com.example.tvapp.view.splash

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.content.pm.PackageManager
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
import com.android.caastv.R
import com.example.tvapp.extensions.getIptvDeviceInfo
import com.example.tvapp.extensions.logAllDrmInfo
import com.example.tvapp.extensions.provideMacAddrLiveData
import com.example.tvapp.extensions.provideMacAddress
import com.example.tvapp.extensions.showToastS
import com.example.tvapp.extensions.toJSONObject
import com.example.tvapp.view.navigationhelper.Destination

import com.example.tvapp.view.uicomponent.ErrorDialog
import com.example.tvapp.viewmodels.SharedViewModel
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.core.content.FileProvider
import com.example.tvapp.extensions.applyUserInfo
import com.example.tvapp.utils.uistate.PreferenceManager
import com.example.tvapp.view.uicomponent.error.CommonDialog
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun SplashScreen(sharedViewModel: SharedViewModel, navController: NavController) {
    val macAddress = sharedViewModel.provideApplicationContext()?.provideMacAddrLiveData()
    val context   = LocalContext.current
    val activity  = (context as? Activity)

    val errorLoadingData by sharedViewModel.errorLoadingData.collectAsState()
    val isInitializeData by sharedViewModel.isInitializeData.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }
// 2) Update dialog state
    val showDialog by sharedViewModel.showUpdateDialog.collectAsState()
    val updateData by sharedViewModel.appUpdateData.collectAsState()
    val timeValid by sharedViewModel.isTimeValid.collectAsState()
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
        sharedViewModel.checkDeviceDateTime()
        sharedViewModel.checkForAppUpdate()
    }
    // 2) If the check completes and is invalid → show blocking dialog & return
    if (timeValid == false) {
        CommonDialog(
            showDialog = true,
            title = "Date & Time Error",
            message = null,
            painter =  painterResource(id = R.drawable.media_error),
            errorCode = null,
            errorMessage = "The date or time on your device appears incorrect. Please correct your system clock before continuing.",
            confirmButtonText = "Exit",
            onConfirm ={
                (context as? Activity)?.finishAffinity()
                android.os.Process.killProcess(android.os.Process.myPid())
            },
            dismissButtonText = null,
            onDismiss = {}
        )
    }

    LaunchedEffect(timeValid) {
        if (timeValid == true) {
            sharedViewModel.initializeAppRequiredData()
            sharedViewModel.checkForAppUpdate()
        }
    }


    val writePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                // Either permission already granted, or API >= 29
                sharedViewModel.onUserAcceptedUpdate()
            } else {
                context.showToastS("Storage permission denied")
            }
        }
    )
    LaunchedEffect(timeValid,isInitializeData, showDialog, errorLoadingData, isUpdating) {
        if (timeValid == false) return@LaunchedEffect
        if (!isInitializeData) return@LaunchedEffect
        if (showDialog)         return@LaunchedEffect
        if (isUpdating)         return@LaunchedEffect
        if (errorLoadingData != null) {
            //context.showToastS(errorLoadingData)
            showExitDialog = true
        }
        if(isInitializeData){
            showExitDialog = false
            if(PreferenceManager.getLoginResponse()?.loginData != null){
                PreferenceManager.getLoginResponse()?.let {
                    context.applyUserInfo(it)
                }
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
        if (updateData?.forceUpdate == 1) {
            // ────────── Forced ──────────
            CommonDialog(
                showDialog        = true,
                title             = "Update Required",
                painter           = painterResource(id =R.drawable.updateicon),
                message           = "A mandatory update is available. You must update to continue.",
                borderColor       = Color.Transparent,
                confirmButtonText = "Yes",
                onConfirm         = {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                        // Need to request WRITE_EXTERNAL_STORAGE
                        val perm = Manifest.permission.WRITE_EXTERNAL_STORAGE
                        if (ContextCompat.checkSelfPermission(context, perm)
                            != PackageManager.PERMISSION_GRANTED) {
                            writePermissionLauncher.launch(perm)
                        }else{
                            // Either permission already granted, or API >= 29
                            sharedViewModel.onUserAcceptedUpdate()
                        }
                    }else{
                        // Either permission already granted, or API >= 29
                        sharedViewModel.onUserAcceptedUpdate()
                    }
                }

                ,
                dismissButtonText = "Exit",
                onDismiss         = {
                    activity?.finishAffinity()
                    android.os.Process.killProcess(android.os.Process.myPid())
                },
                initialFocusOnConfirm  = true

            )
        } else {
            CommonDialog(
                showDialog        = true,
                title             = "Update Available",
                painter           = painterResource(id =R.drawable.updateicon),
                message           = "There’s a new version. Would you like to update now?",
                borderColor       = Color.Transparent,
                confirmButtonText = "Yes",
                onConfirm         = {if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    // Need to request WRITE_EXTERNAL_STORAGE
                    val perm = Manifest.permission.WRITE_EXTERNAL_STORAGE
                    if (ContextCompat.checkSelfPermission(context, perm)
                        != PackageManager.PERMISSION_GRANTED) {
                        writePermissionLauncher.launch(perm)
                    }else{
                        // Either permission already granted, or API >= 29
                        sharedViewModel.onUserAcceptedUpdate()
                    }
                }else{
                    // Either permission already granted, or API >= 29
                    sharedViewModel.onUserAcceptedUpdate()
                } },
                dismissButtonText = "No",
                onDismiss         = { sharedViewModel.onUserDeclinedUpdate() }
            )
        }
    }

    Spacer(Modifier.height(16.dp))

    DisposableEffect(downloadId) {
        if (downloadId != null && updateData != null) {
            val fileName = "tvapp_${updateData?.appVersion}.apk"
            val appCtx   = context.applicationContext
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context, intent: Intent) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                    if (id != downloadId) return
                    val q = DownloadManager.Query().setFilterById(id)
                    dm.query(q)?.use { c ->
                        if (c.moveToFirst()) {
                            val status = c.getInt(c.getColumnIndexOrThrow(
                                DownloadManager.COLUMN_STATUS
                            ))
                            val apkFile = File(
                                appCtx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                                fileName
                            )
                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                val apkFile = File(
                                    appCtx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                                    fileName
                                )
                                val apkUri = FileProvider.getUriForFile(
                                    appCtx,
                                    "${appCtx.packageName}.provider",
                                    apkFile
                                )
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                                    !appCtx.packageManager.canRequestPackageInstalls()
                                ) {
                                    val settings = Intent(
                                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES
                                    ).apply {
                                        data = Uri.parse("package:${appCtx.packageName}")
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    appCtx.startActivity(settings)
                                    return
                                }
                                val install = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                                    setDataAndType(
                                        apkUri,
                                        "application/vnd.android.package-archive"
                                    )
                                    addFlags(
                                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    )
                                }
                                appCtx.startActivity(install)
                                activity?.finish()
                            } else {
                                if (apkFile.exists()) {
                                    val deleted = apkFile.delete()
                                    Log.d("Splash", "Download failed (status=$status). APK deleted? $deleted")
                                } else {
                                    Log.d("Splash", "Download failed (status=$status). No APK file found to delete.")
                                }
                                sharedViewModel.clearDownloadId()
                                context.showToastS("Download failed (status=$status)")
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
            .background(Color.Black)
    ) {
        val device = context.getIptvDeviceInfo().toJSONObject()
        // center your animated logo
        AnimatedSvgFromAssets(
            assetFileName = "splash_logo.svg",
            modifier = Modifier
                .align(Alignment.Center)
                // tweak size to taste
                .size(600.dp)
        )
//        if (isUpdating) {
//            CircularProgressIndicator(
//                modifier = Modifier
//                    .align(Alignment.BottomCenter)
//                    .padding(bottom = 32.dp)
//            )
//        }
    }
}


@Composable
fun AnimatedSvgFromAssets(
    assetFileName: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                // ensure the SVG background is transparent
                setBackgroundColor(0x00000000)

                settings.apply {
                    // you usually don’t need JS for SVG
                    javaScriptEnabled = false
                    useWideViewPort    = true
                    loadWithOverviewMode = true
                }
                webViewClient = WebViewClient()
                loadUrl("file:///android_asset/$assetFileName")
            }
        },
        update = { it.loadUrl("file:///android_asset/$assetFileName") },
        modifier = modifier
    )
}
