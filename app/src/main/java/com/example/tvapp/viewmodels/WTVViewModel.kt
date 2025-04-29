package com.example.tvapp.viewmodels

import android.app.Application
import android.app.DownloadManager
import android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tvapp.R
import com.example.tvapp.extensions.applyAppGenre
import com.example.tvapp.extensions.applyAppHome
import com.example.tvapp.extensions.applyAppLanguage
import com.example.tvapp.extensions.applyAppManifest
import com.example.tvapp.extensions.applyEPGData
import com.example.tvapp.extensions.logReport
import com.example.tvapp.model.data.appupdate.AppUpdateData
import com.example.tvapp.model.data.epgdata.EPGDataItem
import com.example.tvapp.model.data.genre.WTVGenre
import com.example.tvapp.model.data.language.WTVLanguage
import com.example.tvapp.model.data.notification.NotificationItem
import com.example.tvapp.model.data.sse.TabItem
import com.example.tvapp.model.home.WTVHomeCategory
import com.example.tvapp.model.repository.common.WTVNetworkRepositoryImpl
import com.example.tvapp.model.repository.login.LoginInfo
import com.example.tvapp.model.repository.login.LoginPrefsRepository
import com.example.tvapp.model.wtvdatabase.EPGContract
import com.example.tvapp.utils.sealed.WTVListResponse
import com.example.tvapp.utils.sealed.WTVResponse
import com.example.tvapp.utils.sealed.firstOrNullSuccess
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import javax.inject.Inject
import android.Manifest
import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import java.util.concurrent.TimeUnit


@HiltViewModel
open class WTVViewModel @Inject constructor(private val application: Application,private val networkApiCallInterfaceImpl: WTVNetworkRepositoryImpl, private val loginPrefsRepository: LoginPrefsRepository?=null) : AndroidViewModel(application) {
    fun provideApplicationContext() = application.applicationContext
//    private val observer = ConnectivityObserver(application.applicationContext)
    private val _userIdeal = MutableStateFlow<Boolean>(false)
    val userIdeal: StateFlow<Boolean> = _userIdeal.asStateFlow()


    private var _isInitializeData = MutableStateFlow<Boolean>(false)
    val isInitializeData: StateFlow<Boolean> get() = _isInitializeData
    private var _isProgress = MutableStateFlow<Boolean>(false)
    val provideIsProgress: StateFlow<Boolean> get() = _isProgress
    private var _wtvEPGList = MutableStateFlow<List<EPGDataItem>>(emptyList())
    val wtvEPGList: StateFlow<List<EPGDataItem>> = _wtvEPGList.asStateFlow()
    private var _selectedChannel = MutableStateFlow<EPGDataItem>(EPGDataItem())
    val selectedChannel: StateFlow<EPGDataItem> = _selectedChannel.asStateFlow()

    // ─── App‑Update state ───
    private val _appUpdateData = MutableStateFlow<AppUpdateData?>(null)
    val appUpdateData: StateFlow<AppUpdateData?> = _appUpdateData.asStateFlow()

    private val _showUpdateDialog = MutableStateFlow(false)
    val showUpdateDialog: StateFlow<Boolean> = _showUpdateDialog.asStateFlow()

    // add at top of class
    private val _downloadId = MutableStateFlow<Long?>(null)
    val downloadId: StateFlow<Long?> = _downloadId.asStateFlow()

    private val _tabItemsFlow = MutableStateFlow<List<TabItem>>(emptyList())
    val tabItemsFlow: StateFlow<List<TabItem>> = _tabItemsFlow
    // Flag to ensure we start the SSE connection only once.
    private var startedSSE = false


    // prevent double‐connecting
    private var startedNotifSSE = false

    companion object {
        private const val SSE_NOTIF_URL     = "https://nextwave.waveiontechnologies.com:5000/api/app/getNotification-sse"
        private const val NOTIF_CHANNEL_ID  = "tv_app_notifications"
        private const val NOTIF_CHANNEL_NAME= "TV App Updates"
    }

    val loginInfo = loginPrefsRepository?.loginInfoFlow?.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        LoginInfo()
    )

    fun clearLogin() {
        viewModelScope.launch {
            loginPrefsRepository?.clearLoginInfo()
        }
    }

    private val _errorLoadingData = MutableStateFlow<String?>(null)
    val errorLoadingData: StateFlow<String?> = _errorLoadingData

//    init {
//        // Check for update as soon as ViewModel is created
//        checkForAppUpdate()
//        // Start SSE if needed
//        // startSSE()
//    }

    init {
        startNotificationSSE()
    }


    /** 1) Create the Android O+ channel */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun createNotificationChannel() {
        val mgr = application.getSystemService(NotificationManager::class.java)
        if (mgr.getNotificationChannel(NOTIF_CHANNEL_ID) == null) {
            mgr.createNotificationChannel(
                NotificationChannel(
                    NOTIF_CHANNEL_ID,
                    NOTIF_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Messages from server"
                }
            )
        }
    }

    /** 2) Kick off the SSE connection to your notifications endpoint */

    fun startNotificationSSE() {
        if (startedNotifSSE) return
        startedNotifSSE = true

        createNotificationChannel()

        // 1) disable read timeout so the SSE stays open
        val client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true)
            .build()

        // 2) explicit SSE header (optional but recommended)
        val request = Request.Builder()
            .url(SSE_NOTIF_URL)
            .addHeader("Accept", "text/event-stream")
            .build()

        EventSources.createFactory(client).newEventSource(request, object : EventSourceListener() {
            override fun onOpen(es: EventSource, response: Response) {
                Log.d("WTVViewModel", "Notification SSE opened (HTTP ${response.code})")
            }

            override fun onEvent(es: EventSource, id: String?, type: String?, data: String) {
                Log.d("WTVViewModel", "SSE event: $data")
                // parse + notify
                val listType = object : TypeToken<List<NotificationItem>>() {}.type
                val items: List<NotificationItem> = Gson().fromJson(data, listType)
                items.forEach { showPushNotification(it) }
            }

            override fun onFailure(es: EventSource, t: Throwable?, response: Response?) {
                Log.e("WTVViewModel", "Notification SSE failed: ${t?.message}")
                // you could retry here if you like
            }
        })
    }
    /** 3) Build and issue a local notification */
    @SuppressLint("MissingPermission")
    private fun showPushNotification(item: NotificationItem) {
        // 1) Post the Toast on the main thread
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(application, "SSE message: ${item.message}", Toast.LENGTH_LONG).show()
        }
        Log.d("WTVViewModel", " showPushNotification: ${item.message}")

        // 2) Check POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(application, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w("WTVViewModel", "Missing POST_NOTIFICATIONS permission!")
            return
        }
        val builder = NotificationCompat.Builder(application, NOTIF_CHANNEL_ID)
            .setSmallIcon(R.drawable.gtpl_logo)
            .setContentTitle("New message")
            .setContentText(item.message)
            .setAutoCancel(true)

        NotificationManagerCompat.from(application)
            .notify(item.id.hashCode(), builder.build())
    }

    fun initializeAppRequiredData() {
        viewModelScope.launch {
            // This scope will suspend until ALL async children complete
            val manifestDeferred = async {
                networkApiCallInterfaceImpl
                    .provideWTVManifest("https://nextwave.waveiontechnologies.com:5000/api/manifest")
                    .firstOrNullSuccess()
            }.await()
            val epgDeferred = async {
                networkApiCallInterfaceImpl
                    .provideWTVEPGData("https://nextwave.waveiontechnologies.com:5000/api/epg-files/join-epg-content")
                    .firstOrNullSuccess()
            }.await()
            // Wait for all to complete (success or failure)
            if(manifestDeferred != null && epgDeferred != null){
                // **This line runs only after all of the above finish.**
                application.applyAppManifest(manifestDeferred)
                val epgData = dedupeKeepFirst(epgDeferred)
                _wtvEPGList.value = epgData
                application.applyEPGData(epgData)
                epgData.find { it.channelId == manifestDeferred.landingChannel?.ChannelID }
                    ?.let(::updateSelectedChannel)?:kotlin.run {
                    _selectedChannel.value =  epgData.getOrNull(0)!!
                }
                _isInitializeData.value = true
            }else{
                // **This line runs only after all of the above finish.**
                _isInitializeData.value = false
                _errorLoadingData.value = "Server not responding yet"
            }

            launch {
                networkApiCallInterfaceImpl
                    .provideWTVHomeData("https://nextwave.waveiontechnologies.com:5000/api/homescreenCategory")
                    .collect { response ->
                        if (response is WTVListResponse.Success) {
                            application.applyAppHome(response.data)
                            logReport("applyAppHome:${response.data}")
                        } else if (response is WTVListResponse.Failure) {
                            logReport("applyAppHome error:${response.error.message}")
                        }
                    }
            }
        }
    }


    fun clearDownloadId() {
        _downloadId.value = null
    }
    fun checkForAppUpdate() = viewModelScope.launch {
        _isProgress.value = true
        val resp = networkApiCallInterfaceImpl
            .provideAppUpdateInfo("https://api-demo.caastv.com/api/app/appupdate")
            .firstOrNullSuccess()
        _isProgress.value = false

        resp?.data?.let { update ->
            // 1. grab the currently installed version
            val current = application.packageManager
                .getPackageInfo(application.packageName, 0)
                .versionName
                .orEmpty()

            // 2. only if the server’s version is higher do we prompt or download
            if (isVersionHigher(update.appVersion, current)) {
                _appUpdateData.value = update
                handleAppUpdate(update)
            } else {
                // 3. otherwise clear any stale state so we never re‐show
                _appUpdateData.value    = null
                _showUpdateDialog.value = false
            }
        }
    }


    private fun handleAppUpdate(update: AppUpdateData) {
        val current = application.packageManager
            .getPackageInfo(application.packageName, 0)
            .versionName
            .orEmpty()
        Log.d("AVNI","current version: $current, new version: ${update.appVersion}")
        if (isVersionHigher(update.appVersion, current)) {
            if (update.forceUpdate == 1) {
                Log.d("AVNI","Force update")
                downloadApk(update.apkUrl)
            }
            else {
                Log.d("AVNI","SHow Dialog")
                Log.d("AVNI","APK url ---> ${update.apkUrl}")
                _showUpdateDialog.value = true
            }

        }
    }

    private fun isVersionHigher(newVer: String, oldVer: String): Boolean {
        Log.d("AVNI","Inside isVersionHigher")
        val n = newVer.split(".").map { it.toIntOrNull() ?: 0 }
        val o = oldVer.split(".").map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(n.size, o.size)) {
            val ni = n.getOrNull(i) ?: 0
            val oi = o.getOrNull(i) ?: 0
            if (ni > oi) return true
            if (ni < oi) return false
        }
        return false
    }


    private fun downloadApk(apkUrl: String): Long {
        val dm = application.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val req = DownloadManager.Request(Uri.parse(apkUrl)).apply {
            setTitle("Downloading v${_appUpdateData.value?.appVersion}")
            setDestinationInExternalPublicDir(
                DIRECTORY_DOWNLOADS,
                "tvapp_${_appUpdateData.value?.appVersion}.apk"
            )
            setNotificationVisibility(VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        }
        val id = dm.enqueue(req)
        _downloadId.value = id
        return id
    }

    /** Called from “Yes” button on dialog */
    fun onUserAcceptedUpdate() {
        _showUpdateDialog.value = false
        _appUpdateData.value?.apkUrl?.let(::downloadApk)
    }

    /** Called from “No” button on dialog */
    fun onUserDeclinedUpdate() {
        _showUpdateDialog.value = false
    }



    suspend fun saveEPGList(context: Context, epgList: List<EPGDataItem>) {
        withContext(Dispatchers.IO) {
            epgList.forEach { item ->
                val values = ContentValues().apply {
                    put(EPGContract.EPGEntry.COLUMN_ID, item._id)
                    put(EPGContract.EPGEntry.COLUMN_CHANNEL_ID, item.channelId)
                    put(EPGContract.EPGEntry.COLUMN_CHANNEL_HASH, item.channelHash)
                    put(EPGContract.EPGEntry.COLUMN_LAST_UPDATED, item.lastUpdated)
                    put(EPGContract.EPGEntry.COLUMN_DATA, Gson().toJson(item))
                }
                val rowsUpdated = context.contentResolver.update(
                    EPGContract.EPGEntry.CONTENT_URI,
                    values,
                    "${EPGContract.EPGEntry.COLUMN_CHANNEL_ID} = ?",
                    arrayOf(item.channelId)
                )

                // If no row was updated, then insert a new record.
                if (rowsUpdated == 0) {
                    context.contentResolver.insert(EPGContract.EPGEntry.CONTENT_URI, values)
                }
            }
        }
    }


    fun startSSE() {
        val client = OkHttpClient()
        val gson = Gson()

        if (startedSSE) return
        startedSSE = true

        val request = Request.Builder()
            .url("https://nextwave.waveiontechnologies.com:5000/api/tabs/sse-tabs") // replace with your endpoint URL
            .build()

        val listener = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                // Log or perform actions on open
            }

            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                // Update the global state with new event data.
                logReport("SSE", "Event received: $data")
                try {
                    // Parse the JSON array into a List<TabItem>
                    val itemType = object : TypeToken<List<TabItem>>() {}.type
                    val items: List<TabItem> = gson.fromJson(data, itemType)
                    _tabItemsFlow.value = items
                } catch (e: Exception) {
                    logReport("SSE", "Error parsing JSON: ${e.message}")
                }
            }

            override fun onClosed(eventSource: EventSource) {
                // Optionally handle close events.
                logReport("SSE", "Connection closed")
            }

            override fun onFailure(
                eventSource: EventSource,
                t: Throwable?,
                response: Response?
            ) {
                // Handle failures (and consider restarting the connection).
                logReport("SSE", "Connection failed: ${t?.message}")
            }
        }

        // Start the SSE connection.
        EventSources.createFactory(client).newEventSource(request, listener)
    }


    fun updateSelectedChannel(selectedChannel:EPGDataItem){
        val programs = selectedChannel.tv?.programme
        val currentTime = System.currentTimeMillis()
        _selectedChannel.value =  selectedChannel
        /*_selectedChannel.value =  selectedChannel.copy(
            currentPrograms = programs?.filter { program ->
                    // Convert _start and _stop to epoch milliseconds
                    val startMillis = program.startTime?:0L
                    val endMillis = program.endTime?:0L
                    // Keep the program if it hasn't ended yet
                    endMillis > currentTime
                }
        )*/
    }

    //is user ideal since 10 sec
    fun updateUserIdeal(isUserIdeal:Boolean) {
        _userIdeal.value = isUserIdeal
    }


}


fun dedupeKeepFirst(items: List<EPGDataItem>): List<EPGDataItem> {
    return items
        .filter { it.channelId != null }       // optional: drop null IDs
        .distinctBy { it.channelId }            // keep first of each channelId
}