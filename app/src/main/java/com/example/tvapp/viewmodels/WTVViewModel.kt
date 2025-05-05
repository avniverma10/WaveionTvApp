package com.example.tvapp.viewmodels


import android.app.Application
import android.app.DownloadManager
import android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
import android.content.ContentValues
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.os.Build
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tvapp.extensions.applyAppManifest
import com.example.tvapp.extensions.applyEPGData
import com.example.tvapp.extensions.logReport
import com.example.tvapp.model.data.appupdate.AppUpdateData
import com.example.tvapp.model.data.epgdata.EPGDataItem
import com.example.tvapp.model.data.genre.WTVGenre
import com.example.tvapp.model.data.language.WTVLanguage
import com.example.tvapp.model.data.sse.TabItem
import com.example.tvapp.model.repository.common.WTVNetworkRepositoryImpl
import com.example.tvapp.model.repository.login.LoginInfo
import com.example.tvapp.model.repository.login.LoginPrefsRepository
import com.example.tvapp.model.wtvdatabase.EPGContract
import com.example.tvapp.utils.network.heper.ConnectivityObserver
import com.example.tvapp.utils.network.heper.NetworkStatus
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

@HiltViewModel
open class WTVViewModel @Inject constructor(private val application: Application,private val networkApiCallInterfaceImpl: WTVNetworkRepositoryImpl, private val loginPrefsRepository: LoginPrefsRepository?=null) : AndroidViewModel(application) {
    fun provideApplicationContext() = application.applicationContext
    private val observer = ConnectivityObserver(application.applicationContext)
    private val _userIdeal = MutableStateFlow<Boolean>(false)

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

    // Expose the latest list of TabItems
    // add at top of class
    private val _downloadId = MutableStateFlow<Long?>(null)
    val downloadId: StateFlow<Long?> = _downloadId.asStateFlow()


    // Expose the latest list of TabItems
    private val _tabItemsFlow = MutableStateFlow<List<TabItem>>(emptyList())
    val tabItemsFlow: StateFlow<List<TabItem>> = _tabItemsFlow
    // Flag to ensure we start the SSE connection only once.
    private var startedSSE = false

    @RequiresApi(Build.VERSION_CODES.M)
    val networkStatus: StateFlow<NetworkStatus> =
        observer.observe()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = NetworkStatus.Unavailable
            )

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

    /*fun updateEPGData(epgList: List<EPGDataItem>) {
        viewModelScope.launch {
            saveEPGList(application, epgList)
        }
    }*/

    var _errorLoadingData = MutableStateFlow<String?>(null)
    val errorLoadingData: StateFlow<String?> = _errorLoadingData

    init {

        // Start the SSE connection globally.
     //   startSSE()
    }

    fun initializeAppRequiredData(){
        viewModelScope.launch {
            // This scope will suspend until ALL async children complete
            val manifestDeferred = async {
                networkApiCallInterfaceImpl
                    .provideWTVManifest("https://api-panmetro.caastv.com/api/manifest")
                    .firstOrNullSuccess()
                    ?.let {
                        val manifest = it
                        val genre = arrayListOf<WTVGenre>()
                        it.genre?.let { c ->
                            genre.add(WTVGenre(name = "All"))
                            genre.addAll(c)

                        }
                        val language = arrayListOf<WTVLanguage>()
                        it.language?.let { c ->
                            language.add(WTVLanguage(name = "All"))
                            language.addAll(c)

                        }
                        manifest.copy(genre= genre, language = language)
                    }
            }.await()
            val epgDeferred = async {
                networkApiCallInterfaceImpl
                    .provideWTVEPGData("https://api-panmetro.caastv.com/api/epg-files/join-epg-content")
                    .firstOrNullSuccess()
                    ?.let { epgData ->
                        val epgList = epgData.filter { it.content?.published == true }
                        epgList
                    }
            }.await()

            // Wait for all to complete (success or failure)
            //if(manifestDeferred != null && epgDeferred != null){
            if(manifestDeferred != null && epgDeferred != null){
                // **This line runs only after all of the above finish.**
                manifestDeferred.let {
                    application.applyAppManifest(it)
                }
                val epgData = removeDuplicateEPG(epgDeferred)
                _wtvEPGList.value = epgData
                application.applyEPGData(epgData)
                epgData.find { it.channelId == manifestDeferred.landingChannel?.ChannelID }
                    ?.let(::updateSelectedChannel)?:kotlin.run {
                    _selectedChannel.value =  epgData.getOrNull(0)!!
                }
                _isInitializeData.value = true
            }else{
                var errorMsg = ""
                // **This line runs only after all of the above finish.**
                if(manifestDeferred==null){
                    errorMsg = "manifest api"
                }else if(epgDeferred==null){
                    errorMsg = "epg api"
                }
                _errorLoadingData.value = "Server api ${errorMsg} not responding yet!"
                _isInitializeData.value = false
                Log.e("_errorLoadingData","${_errorLoadingData}")
            }
        }
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

                // Try to update the row with the given channelId.
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
        _selectedChannel.value =  selectedChannel
    }

    //is user ideal since 10 sec
    fun updateUserIdeal(isUserIdeal:Boolean) {
        _userIdeal.value = isUserIdeal
    }


    //check for updates

    fun clearDownloadId() {
        _downloadId.value = null
    }
    fun checkForAppUpdate() = viewModelScope.launch {
        _isProgress.value = true
        val resp = networkApiCallInterfaceImpl
            .provideAppUpdateInfo("https://api-panmetro.caastv.com/api/app/appupdate")
            .firstOrNullSuccess()
        _isProgress.value = false

        resp?.data?.let { update ->
            // 1. grab the currently installed version
            val current = application.packageManager
                .getPackageInfo(application.packageName, 0)
                .versionName
                .orEmpty()
            Log.d("App version","current version: $current, new version: ${update.appVersion} and isVersionHigher:>${shouldUpdateRequired(update.appVersion, current)}")
            // 2. only if the server’s version is higher do we prompt or download
            if (shouldUpdateRequired(update.appVersion, current)) {
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
        Log.d("App version","current version: $current, new version: ${update.appVersion} and isVersionHigher:>${shouldUpdateRequired(update.appVersion, current)}")

        if (shouldUpdateRequired(update.appVersion, current)) {
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
    private fun shouldUpdateRequired(newVer: String, oldVer: String): Boolean {
        try {
            val new = newVer.replace(".","").trim().toInt()
            val old = oldVer.replace(".","").trim().toInt()
            return new>old
        }catch (ex: Exception){
            return  false
        }
    }


    private fun downloadApk(apkUrl: String): Long {
        val dm = application.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val req = DownloadManager.Request(apkUrl.toUri()).apply {
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



}


fun removeDuplicateEPG(items: List<EPGDataItem>): List<EPGDataItem> {
    return items
        .filter { it.channelId != null }       // optional: drop null IDs
        .distinctBy { it.channelId }            // keep first of each channelId
}