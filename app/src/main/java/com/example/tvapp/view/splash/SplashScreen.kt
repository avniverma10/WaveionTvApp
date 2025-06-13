package com.example.tvapp.view.splash

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.android.panmetroiptv.R
import com.example.tvapp.extensions.getIptvDeviceInfo
import com.example.tvapp.extensions.hideKeyboard
import com.example.tvapp.extensions.isNotNullOrEmpty
import com.example.tvapp.extensions.loge
import com.example.tvapp.extensions.provideMacAddrLiveData
import com.example.tvapp.extensions.showToastS
import com.example.tvapp.extensions.toJSONObject
import com.example.tvapp.utils.uistate.PreferenceManager
import com.example.tvapp.view.navigationhelper.Destination
import com.example.tvapp.view.uicomponent.ErrorDialog
import com.example.tvapp.view.uicomponent.error.CommonDialog
import com.example.tvapp.view.uicomponent.keyboard.HideKeyboardOnEnter
import com.example.tvapp.viewmodels.SharedViewModel
import kotlinx.coroutines.flow.StateFlow
import java.io.File


@SuppressLint("ContextCastToActivity")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SplashScreen(sharedViewModel: SharedViewModel, navController: NavController) {
    val context = LocalContext.current
    // Launcher for WRITE_EXTERNAL_STORAGE on API <= 28
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
    val macAddress = sharedViewModel.provideApplicationContext()?.provideMacAddrLiveData()
    val errorLoadingData by sharedViewModel.errorLoadingData.collectAsState()
    val isInitializeData by sharedViewModel.isInitializeData.collectAsState()
    val activity  = (context as? Activity)
    var showExitDialog by remember { mutableStateOf(false) }

    val showDialog by sharedViewModel.showUpdateDialog.collectAsState()
    val updateData by sharedViewModel.appUpdateData.collectAsState()
    //Download state
    val downloadId by sharedViewModel.downloadId.collectAsState()
    val timeValid by sharedViewModel.isTimeValid.collectAsState()
    val isUpdating = downloadId != null
    val dm = remember {
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    var pendingInstallIntent by remember { mutableStateOf<Intent?>(null) }


    val unknownSourcesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // Nothing needed here, because we also handle ON_RESUME below.
    }

    //  Observe ON_RESUME: if pendingInstallIntent != null and we now have install permission,
    //    fire the install Intent.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (pendingInstallIntent != null &&
                        context.packageManager.canRequestPackageInstalls()
                    ) {
                        // Launch the stored install Intent exactly once
                        context.startActivity(pendingInstallIntent!!.apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                        // Clear it so we don’t re-launch repeatedly
                        pendingInstallIntent = null
                        activity?.finish()
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        context.hideKeyboard()
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
            borderColor = Color.Transparent,
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
            if(PreferenceManager.isLoggedIn()){
                navController.navigate(Destination.genreScreen) {
                    popUpTo(Destination.splashScreen) { inclusive = true }
                }
            }else{
                navController.navigate(Destination.loginScreen) {
                    popUpTo(Destination.splashScreen) { inclusive = true }
                }
            }
        }
        sharedViewModel.isFromSplash.value = true
    }
    if (showDialog && updateData != null) {
        val apkVersionName = updateData?.appVersion ?: "latest"
        val fileName = "tvapp_$apkVersionName.apk"

        if (updateData?.forceUpdate == 1) {
            // Forced update
            CommonDialog(
                showDialog = true,
                title = "Update Required",
                painter = painterResource(id = R.drawable.updateicon),
                message = "A mandatory update is available. You must update to continue.",
                borderColor = Color.Transparent,
                confirmButtonText = "Yes",
                onConfirm = {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                        val perm = Manifest.permission.WRITE_EXTERNAL_STORAGE
                        if (ContextCompat.checkSelfPermission(context, perm)
                            != PackageManager.PERMISSION_GRANTED
                        ) {
                            writePermissionLauncher.launch(perm)
                        } else {
                            sharedViewModel.onUserAcceptedUpdate()
                        }
                    } else {
                        sharedViewModel.onUserAcceptedUpdate()
                    }
                },
                dismissButtonText = "Exit",
                onDismiss = {
                    activity?.finishAffinity()
                    android.os.Process.killProcess(android.os.Process.myPid())
                },
                initialFocusOnConfirm = true
            )
        } else {
            CommonDialog(
                showDialog = true,
                title = "Update Available",
                painter = painterResource(id = R.drawable.updateicon),
                message = "There’s a new version. Would you like to update now?",
                borderColor = Color.Transparent,
                confirmButtonText = "Yes",
                onConfirm = {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                        val perm = Manifest.permission.WRITE_EXTERNAL_STORAGE
                        if (ContextCompat.checkSelfPermission(context, perm)
                            != PackageManager.PERMISSION_GRANTED
                        ) {
                            writePermissionLauncher.launch(perm)
                        } else {
                            sharedViewModel.onUserAcceptedUpdate()
                        }
                    } else {
                        sharedViewModel.onUserAcceptedUpdate()
                    }
                },
                dismissButtonText = "No",
                onDismiss = {
                    sharedViewModel.onUserDeclinedUpdate()
                }
            )
        }
    }

    DisposableEffect(downloadId) {
        if (downloadId != null && updateData != null) {
            val apkVersionName = updateData?.appVersion ?: "latest"
            val fileName = "tvapp_$apkVersionName.apk"
            val appCtx = context.applicationContext

            val receiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context, intent: Intent) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                    if (id != downloadId) return

                    val query = DownloadManager.Query().setFilterById(id)
                    dm.query(query)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val status = cursor.getInt(
                                cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
                            )

                            val apkFile = File(
                                appCtx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                                fileName
                            )

                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                val apkUri: Uri = FileProvider.getUriForFile(
                                    appCtx,
                                    "${appCtx.packageName}.provider",
                                    apkFile
                                )
                                val installIntent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                                    setDataAndType(
                                        apkUri,
                                        "application/vnd.android.package-archive"
                                    )
                                    addFlags(
                                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    )
                                }
                                pendingInstallIntent = installIntent

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                                    !appCtx.packageManager.canRequestPackageInstalls()
                                ) {
                                    val settingsIntent = Intent(
                                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES
                                    ).apply {
                                        data = Uri.parse("package:${appCtx.packageName}")
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    unknownSourcesLauncher.launch(settingsIntent)
                                    return
                                }

                                // 5) Otherwise, we’re good—install immediately
                                appCtx.startActivity(installIntent)
                                activity?.finish()
                            } else {
                                // Download failed: clean up and notify
                                if (apkFile.exists()) {
                                    val deleted = apkFile.delete()
                                    loge(
                                        "Splash",
                                        "Download failed (status=$status). APK deleted? $deleted"
                                    )
                                } else {
                                    loge(
                                        "Splash",
                                        "Download failed (status=$status). No APK file found."
                                    )
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

            onDispose {
                context.unregisterReceiver(receiver)
            }
        } else {
            onDispose { /* no-op until downloadId is non-null */ }
        }
    }



    // Exit confirmation dialog
    if (showExitDialog) {
        ErrorDialog(message = errorLoadingData?:"Server Error", onConfirmExit = {
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
       // context.showToastL(device.toString())
      //  Log.e("macAddress::${macAddress?.value}","")
     //   Log.e("logAllDrmInfo()","${logAllDrmInfo()}")

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                //.data(appManifestData?.splashUrl?:"https://waveiontechnologies.com/wp-content/uploads/2021/01/logo-header2.png")
                .diskCachePolicy(CachePolicy.ENABLED)    // cache image on disk
                .memoryCachePolicy(CachePolicy.ENABLED)  // cache image in memory
                .build(),
            contentDescription = "Default Background",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.align(Alignment.Center),
            error = painterResource(R.drawable.panmetro_logo_t),        // Error state
            placeholder = painterResource(R.drawable.panmetro_logo_t)   // Loading state
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
