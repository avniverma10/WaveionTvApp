package com.example.tvapp.view.splash

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
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import com.android.panmetroiptv.R
import com.example.tvapp.extensions.getIptvDeviceInfo
import com.example.tvapp.extensions.hideKeyboard
import com.example.tvapp.extensions.isNotNullOrEmpty
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

    //hide keyboard forcefully
    //HideKeyboardOnEnter()
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
            onConfirm = {
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
            },
            dismissButtonText = "No",
            onDismiss = { sharedViewModel.onUserDeclinedUpdate() },
        )
    }

    Spacer(Modifier.height(16.dp))


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
