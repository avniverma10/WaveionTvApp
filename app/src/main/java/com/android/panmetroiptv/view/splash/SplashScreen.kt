package com.android.panmetroiptv.view.splash

import android.Manifest
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.android.panmetroiptv.R
import com.android.panmetroiptv.extensions.loge
import com.android.panmetroiptv.extensions.showToastS
import com.android.panmetroiptv.utils.uistate.PreferenceManager
import com.android.panmetroiptv.view.navigationhelper.Destination
import com.android.panmetroiptv.view.uicomponent.ErrorDialog
import com.android.panmetroiptv.view.uicomponent.error.CommonDialog
import com.android.panmetroiptv.viewmodels.SharedViewModel
import java.io.File

@Composable
fun SplashScreen(
    sharedViewModel: SharedViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val activity = (context as? Activity)

    val errorLoadingData by sharedViewModel.errorLoadingData.collectAsStateWithLifecycle()
    val isInitializeData by sharedViewModel.isInitializeData.collectAsStateWithLifecycle()
    var showExitDialog by remember { mutableStateOf(false) }

    val showDialog by sharedViewModel.showUpdateDialog.collectAsStateWithLifecycle()
    val updateData by sharedViewModel.appUpdateData.collectAsStateWithLifecycle()
    val timeValid by sharedViewModel.isTimeValid.collectAsStateWithLifecycle()
    val isServerAvailable by sharedViewModel.isServerAvailable.collectAsState()

    val downloadId by sharedViewModel.downloadId.collectAsStateWithLifecycle()
    val isUpdating = downloadId != null

    val dm = remember {
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    var pendingInstallIntent by remember { mutableStateOf<Intent?>(null) }

    val writePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            sharedViewModel.onUserAcceptedUpdate()
        } else {
            context.showToastS("Storage permission denied")
        }
    }


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
        PreferenceManager.clearSaveGenre()
        PreferenceManager.clearSaveChannel()
        sharedViewModel.checkDeviceDateTime()
        sharedViewModel.checkForAppUpdate()
    }

    if (timeValid == false) {
        CommonDialog(
            isErrorAdded= if(!isServerAvailable) false else true,
            showDialog = true,
            title = if(!isServerAvailable) "\uD83D\uDEA7  Service Temporarily Unavailable" else "Date & Time Error",
            message = null,
            painter = painterResource(id = R.drawable.media_error),
            errorCode = null,
            errorMessage = if(!isServerAvailable) "We're working to restore the connection." else "The date or time on your device appears incorrect. Please correct your system clock before continuing.",
            confirmButtonText = "Exit",
            onConfirm = {
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

    LaunchedEffect(timeValid, isInitializeData, showDialog, errorLoadingData, isUpdating) {
        if (timeValid == false) return@LaunchedEffect
        if (!isInitializeData) return@LaunchedEffect
        if (showDialog) return@LaunchedEffect
        if (isUpdating) return@LaunchedEffect

        if (errorLoadingData != null) {
            showExitDialog = true
            return@LaunchedEffect
        }

        showExitDialog = false
        if (PreferenceManager.getLoginResponse() != null) {
            navController.navigate(Destination.genreScreen) {
                popUpTo(Destination.splashScreen) { inclusive = true }
            }
        } else {
            navController.navigate(Destination.loginScreen) {
                popUpTo(Destination.splashScreen) { inclusive = true }
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

    if (showExitDialog) {
        ErrorDialog(
            message = errorLoadingData ?: "Server Error",
            onConfirmExit = {
                sharedViewModel._errorLoadingData.value = null
                showExitDialog = false
                sharedViewModel.initializeAppRequiredData()
            },
            onDismiss = {
                showExitDialog = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // center your animated logo
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
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