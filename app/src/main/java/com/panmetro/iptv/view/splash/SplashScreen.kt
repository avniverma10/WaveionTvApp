package com.panmetro.iptv.view.splash

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
import androidx.annotation.RequiresApi
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.panmetro.iptv.R
import com.panmetro.iptv.extensions.loge
import com.panmetro.iptv.extensions.showToastS
import com.panmetro.iptv.utils.uistate.PreferenceManager
import com.panmetro.iptv.view.navigationhelper.Destination
import com.panmetro.iptv.view.uicomponent.ErrorDialog
import com.panmetro.iptv.view.uicomponent.error.CommonDialog
import com.panmetro.iptv.viewmodels.SharedViewModel
import kotlinx.coroutines.time.delay
import java.io.File
import java.time.Duration

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SplashScreen(
    sharedViewModel: SharedViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val activity = (context as? Activity)

    // Add progress state collection
    var splashLoader by remember { mutableStateOf(false) }
    // Add these state variables at the top of your composable function
    var splashProgress by remember { mutableFloatStateOf(0f) }
    val splashMaxProgress = 100f
    val totalDuration = 30000 // 20 seconds in milliseconds

// LaunchedEffect to handle the animation
    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        val endTime = startTime + totalDuration

        while (System.currentTimeMillis() < endTime) {
            val elapsed = System.currentTimeMillis() - startTime
            splashProgress = (elapsed.toFloat() / totalDuration.toFloat()) * splashMaxProgress
            delay(Duration.ofMillis(10)) // ~60 FPS update rate
        }
        splashProgress = splashMaxProgress // Ensure it reaches 100%
    }

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

    val downloadProgress = remember { mutableStateOf(0f) }

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
        // Update progress
        if(downloadId==null){
            splashLoader = true
        }
        sharedViewModel.checkDeviceDateTime()
        sharedViewModel.checkForAppUpdate()
       // MediaDrm(WIDEVINE_UUID).getPropertyByteArray("deviceUniqueId")

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
            sharedViewModel.checkForAppUpdate()
            sharedViewModel.initializeAppRequiredData()
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
        splashProgress = splashMaxProgress // Ensure it reaches 100%
        splashLoader = false

        if (PreferenceManager.getLoginResponse() != null) {
            navController.navigate(Destination.genreScreen) {
                popUpTo(Destination.splashScreen) { inclusive = true }
            }
            //refresh pkg and channel list
            PreferenceManager.getLoginResponse()?.customerNumber?.let {
                sharedViewModel.userPackageUpdate(customerNumber = it, isChannelUpdateRequired = true)
            }
            //set saved fav list
            PreferenceManager.getUserFav(PreferenceManager.getUsername().toString())?.let {
                sharedViewModel.setFavorites(it)
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

        if (updateData?.checkRegionForceUpdate(PreferenceManager.getLoginResponse()?.provideUserRegionCode()) == true) {
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
        }
        else {
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
    LaunchedEffect(downloadId) {
        downloadProgress.value = 0f
        downloadId?.let { id ->
            splashLoader = false
            var finished = false
            while (!finished) {
                val q = DownloadManager.Query().setFilterById(id)
                dm.query(q)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val soFar = cursor.getLong(cursor.getColumnIndexOrThrow(
                            DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                        val total  = cursor.getLong(cursor.getColumnIndexOrThrow(
                            DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        if (total > 0) {
                            downloadProgress.value = (soFar / total.toFloat()).coerceIn(0f, 1f)
                        }
                        val status = cursor.getInt(cursor.getColumnIndexOrThrow(
                            DownloadManager.COLUMN_STATUS))
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            finished = true
                            downloadProgress.value = 1f
                        }
                    }
                }
                if (!finished)   delay(Duration.ofMillis(300))        }
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
            error = painterResource(R.drawable.app_logo_splash),        // Error state
            placeholder = painterResource(R.drawable.app_logo_splash)   // Loading state
        )

        // Progress bar UI
        if (splashLoader) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 70.dp)
                    .width(300.dp)
                    .height(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { splashProgress / splashMaxProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = Color(0xFF00BFFF),
                    trackColor = Color.LightGray.copy(alpha = 0.5f)
                )
            }
        }

        if (downloadId != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
            ) {
                LinearProgressIndicator(
                    progress = downloadProgress.value,
                    modifier = Modifier
                        .width(300.dp)
                        .height(8.dp),
                    color = Color(0xFF00BFFF),
                    trackColor = Color.LightGray
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (downloadProgress.value < 1f)
                        "Downloading update… ${(downloadProgress.value * 100).toInt()}%"
                    else
                        "Download complete!",
                    color = Color.Black
                )
            }
        }
    }
}