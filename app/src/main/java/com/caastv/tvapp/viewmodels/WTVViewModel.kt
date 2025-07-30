package com.caastv.tvapp.viewmodels

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.caastv.R
import com.caastv.tvapp.extensions.isNotNullOrEmpty
import com.caastv.tvapp.extensions.logReport
import com.caastv.tvapp.extensions.loge
import com.caastv.tvapp.extensions.provideMacAddress
import com.caastv.tvapp.extensions.showToastS
import com.caastv.tvapp.model.data.appupdate.AppUpdateData
import com.caastv.tvapp.model.data.banner.Banner
import com.caastv.tvapp.model.data.customapp.InventoryApp
import com.caastv.tvapp.model.data.epgdata.EPGDataItem
import com.caastv.tvapp.model.data.epgdata.Programme
import com.caastv.tvapp.model.data.genre.WTVGenre
import com.caastv.tvapp.model.data.language.WTVLanguage
import com.caastv.tvapp.model.data.login.LoginResponseData
import com.caastv.tvapp.model.data.manifest.WTVManifest
import com.caastv.tvapp.model.data.notification.NotificationItem
import com.caastv.tvapp.model.data.sse.TabItem
import com.caastv.tvapp.model.home.WTVHomeCategory
import com.caastv.tvapp.model.repository.common.WTVNetworkRepositoryImpl
import com.caastv.tvapp.model.repository.database.AppDataRepository
import com.caastv.tvapp.model.repository.login.LoginPrefsRepository
import com.caastv.tvapp.model.wtvdatabase.EPGContract
import com.caastv.tvapp.utils.network.UrlManager
import com.caastv.tvapp.utils.network.interceptors.ApiStatusInterceptor
import com.caastv.tvapp.utils.sealed.WTVListResponse
import com.caastv.tvapp.utils.sealed.WTVResponse
import com.caastv.tvapp.utils.sealed.firstOrNullSuccess
import com.caastv.tvapp.utils.uistate.PreferenceManager
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs


@HiltViewModel
open class WTVViewModel @Inject constructor(
    private val application: Application,
    private val networkApiCallInterfaceImpl: WTVNetworkRepositoryImpl,
    private val loginPrefsRepository: LoginPrefsRepository?=null,
    private val dataRepository: AppDataRepository?=null
) : AndroidViewModel(application) {
    fun provideApplicationContext() = application.applicationContext
    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(ApiStatusInterceptor.getInstance())
        .build()
    private val _userIdeal = MutableStateFlow<Boolean>(false)

    private var _isInitializeData = MutableStateFlow<Boolean>(false)
    val isInitializeData: StateFlow<Boolean> get() = _isInitializeData
    private var _isProgress = MutableStateFlow<Boolean>(false)
    val provideIsProgress: StateFlow<Boolean> get() = _isProgress
    private var _wtvEPGList = MutableStateFlow<List<EPGDataItem>>(emptyList())
    val wtvEPGList: StateFlow<List<EPGDataItem>> = _wtvEPGList.asStateFlow()
    private var _manifestData = MutableStateFlow<WTVManifest?>(null)
    val manifestData: StateFlow<WTVManifest?> = _manifestData.asStateFlow()
    private var _homeData = MutableStateFlow<List<WTVHomeCategory>>(emptyList())
    val homeData: StateFlow<List<WTVHomeCategory>> = _homeData.asStateFlow()

    private val _inventoryApps = MutableStateFlow<List<InventoryApp>>(emptyList())
    val inventoryApps: StateFlow<List<InventoryApp>> = _inventoryApps.asStateFlow()

    private val _bannerList = MutableStateFlow<List<Banner>>(emptyList())
    val bannerList: StateFlow<List<Banner>> = _bannerList.asStateFlow()


    private var _selectedChannel = MutableStateFlow<EPGDataItem>(EPGDataItem())
    val selectedChannel: StateFlow<EPGDataItem> = _selectedChannel.asStateFlow()

    private var _filterAvailablePrograms = MutableStateFlow<List<Programme>>(arrayListOf())
    val filterAvailablePrograms: StateFlow<List<Programme>> = _filterAvailablePrograms.asStateFlow()

    // ─── Date and time state ───
    private val _isTimeValid = MutableStateFlow<Boolean?>(true)
    val isTimeValid: StateFlow<Boolean?> = _isTimeValid.asStateFlow()

    private val _isServerAvailable = MutableStateFlow<Boolean>(false)
    val isServerAvailable: StateFlow<Boolean> = _isServerAvailable.asStateFlow()

    val _isOfflineEnable = MutableStateFlow<Boolean>(false)
    val isOfflineEnable: StateFlow<Boolean> = _isOfflineEnable.asStateFlow()
    private val _filteredPanMetroChannels = MutableStateFlow<List<EPGDataItem>>(emptyList())
    val filteredPanMetroChannels: StateFlow<List<EPGDataItem>> = _filteredPanMetroChannels.asStateFlow()


    // ─── App‑Update state ───
    private val _appUpdateData = MutableStateFlow<AppUpdateData?>(null)
    val appUpdateData: StateFlow<AppUpdateData?> = _appUpdateData.asStateFlow()

    private val _showUpdateDialog = MutableStateFlow(false)
    val showUpdateDialog: StateFlow<Boolean> = _showUpdateDialog.asStateFlow()

    private val _downloadId = MutableStateFlow<Long?>(null)
    val downloadId: StateFlow<Long?> = _downloadId.asStateFlow()

    private val _tabItemsFlow = MutableStateFlow<List<TabItem>>(emptyList())
    val tabItemsFlow: StateFlow<List<TabItem>> = _tabItemsFlow
    private val _bannerMessage = MutableStateFlow<String?>(null)
    val bannerMessage: StateFlow<String?> = _bannerMessage.asStateFlow()

    // Flag to ensure we start the SSE connection only once.
    private var startedSSE = false

    // prevent double‐connecting
    private var startedNotifSSE = false
    private var skipFirst = true

    private val TAG = "TimeCheck"

    companion object {
        private const val NOTIF_CHANNEL_ID = "tv_app_notifications"
        private const val NOTIF_CHANNEL_NAME = "TV App Updates"
    }

    fun clearLogin() {
        viewModelScope.launch {
            loginPrefsRepository?.clearLoginInfo()
        }
    }

    fun updateEPGData(epgList: List<EPGDataItem>) {
        viewModelScope.launch {
            saveEPGList(application, epgList)
        }
    }

    var _errorLoadingData = MutableStateFlow<String?>(null)
    val errorLoadingData: StateFlow<String?> = _errorLoadingData

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startNotificationSSE()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startNotificationSSE() {
        if (startedNotifSSE) return
        startedNotifSSE = true

        viewModelScope.launch {
            networkApiCallInterfaceImpl
                .provideNotificationSSE(UrlManager.getCurrentBaseUrl() +"app/getNotification-sse")
                .catch { loge("WTVViewModel", "SSE failed $it") }
                .collect { item ->
                    if (skipFirst) {
                        skipFirst = false
                    } else {
                        showPushNotification(item)
                    }
                }
        }
    }

    /** 3) Build and issue a local notification */
    @SuppressLint("MissingPermission")
    private fun showPushNotification(item: NotificationItem) {
        _bannerMessage.value = item.message
        // 1) Post the Toast on the main thread
//        Handler(Looper.getMainLooper()).post {
//            Toast.makeText(application, " ${item.message}", Toast.LENGTH_LONG).show()
//        }
        // (optional) clear after a delay so banner goes away
        viewModelScope.launch {
            delay(TimeUnit.MINUTES.toMillis(1))
            _bannerMessage.value = null
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
            .setSmallIcon(R.drawable.app_logo)
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
                    .provideWTVManifest(UrlManager.getCurrentBaseUrl() +"manifest")
                    .firstOrNullSuccess()
                    ?.let {
                        val manifest = it
                        val genre = arrayListOf<WTVGenre>()
                        it.genre?.let { c ->
                            val apiAllGenre = c.find { g ->
                                g.name.equals("All", ignoreCase = true)
                            }
                            genre.add(
                                WTVGenre(
                                    _id = "all",
                                    name = "All",
                                    published = true,
                                    version = 0,
                                    customIconUrl = apiAllGenre?.customIconUrl,
                                    defaultIcon = apiAllGenre?.defaultIcon ?: "All"
                                )
                            )
                            genre.addAll(c)
                        }
                        val language = arrayListOf<WTVLanguage>()
                        it.language?.let { c ->
                            val apiAllLanguage = c.find { lang ->
                                lang.name.equals("All", ignoreCase = true)
                            }
                            language.add(
                                WTVLanguage(
                                    _id = "all",
                                    name = "All",
                                    published = true,
                                    version = 0,
                                    customIconUrl = apiAllLanguage?.customIconUrl,
                                    defaultIcon = apiAllLanguage?.defaultIcon ?: "All"
                                )
                            )
                            language.addAll(c)
                        }
                        manifest.copy(genre = genre, language = language)
                    }
            }.await()
            val epgDeferred = async {
                networkApiCallInterfaceImpl
                    .provideWTVEPGData(UrlManager.getCurrentBaseUrl() +"epg-files/join-epg-content",dataRepository)
                    .firstOrNullSuccess()
                    ?.let { epgData ->
                        epgData
                    }
            }.await()

            // Wait for all to complete (success or failure)
            if (manifestDeferred != null && epgDeferred != null) {
                // **This line runs only after all of the above finish.**
                dataRepository?.saveWtvManifest(manifestDeferred)
                updateManifest(manifestDeferred)
                val epgData = removeDuplicateEPG(epgDeferred)
                //updateEPGData(epgData)
                updateEpgData(epgData)
                epgData.find { it.channelId == manifestDeferred.landingChannel?.channelId }
                    ?.let(::updateSelectedChannel) ?: kotlin.run {
                    epgData?.getOrNull(0)?.let {
                        _selectedChannel.value = it
                    } ?: run {
                        _selectedChannel.value = EPGDataItem()
                    }
                }
                _isInitializeData.value = true
            } else {
                var errorMsg = ""
                // **This line runs only after all of the above finish.**
                if (manifestDeferred == null) {
                    errorMsg = "manifest api"
                } else if (epgDeferred == null) {
                    errorMsg = "epg api"
                }
                _errorLoadingData.value = "Server api ${errorMsg} not responding yet!"
                _isInitializeData.value = false
                loge("_errorLoadingData", "${_errorLoadingData}")
            }
            launch {
                networkApiCallInterfaceImpl
                    .provideWTVHomeData(UrlManager.getCurrentBaseUrl() +"homescreenCategory")
                    .collect { response ->
                        if (response is WTVListResponse.Success) {
                            dataRepository?.saveAllHomeCategories(response.data)
                            updateHome(response.data)
                            logReport("applyAppHome:${response.data}")
                        } else if (response is WTVListResponse.Failure) {
                            logReport("applyAppHome error:${response.error.message}")
                        }
                    }
            }
            launch {
                fetchInventoryApps()
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun fetchServerTimeMillis(): Long? {
        return try {
            val req = Request.Builder()
                .url(UrlManager.getCurrentBaseUrl() + "app/health")
                .get()
                .build()

            val resp = okHttpClient.newCall(req).execute()

            if (!resp.isSuccessful) {
                loge(TAG, "Health endpoint error: HTTP ${resp.code}")
                // Instead of throwing, return null and let the caller handle it
                return null
            }

            val bodyStr = resp.body?.string() ?: run {
                loge(TAG, "Empty response body")
                return null
            }

            try {
                val timestampStr = JSONObject(bodyStr).getString("timestamp")
                val serverInst = Instant.parse(timestampStr)
                val serverMs = serverInst.toEpochMilli()
                loge(TAG, "Server epoch ms: $serverMs")
                serverMs
            } catch (e: Exception) {
                loge(TAG, "JSON parsing failed: ${e.message}")
                null
            }
        } catch (e: Exception) {
            loge(TAG, "Network error: ${e.message}")
            null
        }
    }

    /**
     * Checks that:
     *  • server date == device date, AND
     *  • |deviceTime – serverTime| ≤ thresholdMs
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun checkDeviceDateTime(thresholdMs: Long = TimeUnit.HOURS.toMillis(24)) {
        viewModelScope.launch {
            try {
                val valid = withContext(Dispatchers.IO) {
                   // If we can't get server time, assume invalid (or adjust logic as needed)
                    val serverMs = fetchServerTimeMillis() ?: run {
                        _isServerAvailable.value = false
                        // If we can't get server time, assume invalid (or adjust logic as needed)
                        return@withContext false
                    }
                    _isServerAvailable.value = true

                    val deviceMs = System.currentTimeMillis()
                    val drift = abs(deviceMs - serverMs)

                    // calendar-date check
                    val zone = ZoneId.systemDefault()
                    val serverDate = Instant.ofEpochMilli(serverMs).atZone(zone).toLocalDate()
                    val deviceDate = Instant.ofEpochMilli(deviceMs).atZone(zone).toLocalDate()

                    (serverDate == deviceDate) && (drift <= thresholdMs)
                }
                _isTimeValid.value = valid
            } catch (e: Exception) {
                loge(TAG, "Error in time validation: ${e.message}")
                _isTimeValid.value = false
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

    fun updateSelectedChannel(selectedChannel: EPGDataItem) {
        _selectedChannel.value = selectedChannel
        selectedChannel.tv?.programme?.let { providePlayableProgramData(it) }
    }

    //update epg data
    fun updateEpgData(epgData: List<EPGDataItem>) {
        _wtvEPGList.value = epgData
    }

    //update manifest data
    fun updateManifest(manifest: WTVManifest) {
        _manifestData.value = manifest
    }
    //update home data
    fun updateHome(homeData: List<WTVHomeCategory>) {
        _homeData.value = homeData
    }

    //update AppsInventory data
    fun updateAppsInventory(appsInventory: List<InventoryApp>) {
        _inventoryApps.value = appsInventory
    }
    //update banner data
    fun updateBanners(banners: List<Banner>) {
        _bannerList.value = banners
    }


    //check for updates

    fun clearDownloadId() {
        _downloadId.value = null
    }

    fun checkForAppUpdate() = viewModelScope.launch {
        _isProgress.value = true
        val resp = networkApiCallInterfaceImpl
            .provideAppUpdateInfo(UrlManager.getCurrentBaseUrl() +"app/appupdate")
            .firstOrNullSuccess()
        _isProgress.value = false

        resp?.data?.let { update ->
            // 1. grab the currently installed version
            val current = application.packageManager
                .getPackageInfo(application.packageName, 0)
                .versionName
                .orEmpty()

            // 2. only if the server’s version is higher do we prompt or download
            if (shouldUpdateRequired(update.appVersion, current)) {
                _appUpdateData.value = update
                handleAppUpdate(update)
            } else {
                // 3. otherwise clear any stale state so we never re‐show
                _appUpdateData.value = null
                _showUpdateDialog.value = false
            }
        }
    }


    private fun handleAppUpdate(update: AppUpdateData) {
        val current = application.packageManager
            .getPackageInfo(application.packageName, 0)
            .versionName
            .orEmpty()
        Log.d(
            "App version",
            "current version: $current, new version: ${update.appVersion} and isVersionHigher:>${
                shouldUpdateRequired(
                    update.appVersion,
                    current
                )
            }"
        )

        val needsUpdate = shouldUpdateRequired(update.appVersion, current)
        _showUpdateDialog.value = needsUpdate
    }

    private fun isVersionHigher(newVer: String, oldVer: String): Boolean {
        Log.d("AVNI", "Inside isVersionHigher")
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
            val new = newVer.replace(".", "").trim().toInt()
            val old = oldVer.replace(".", "").trim().toInt()
            return new > old
        } catch (ex: Exception) {
            return false
        }
    }
    @SuppressLint("MissingPermission")
    fun downloadApk(apkUrl: String): Long {
        // getApplication<T>() gives you your Application instance in an AndroidViewModel
        val ctx = getApplication<Application>()
        val dm  = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        // construct a file in YOUR app’s external-files/Download directory
        val fileName = "tvapp_${_appUpdateData.value?.appVersion}.apk"
        val destDir  = ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!
        val file     = File(destDir, fileName)
        val destUri  = Uri.fromFile(file)

        val req = DownloadManager.Request(Uri.parse(apkUrl)).apply {
            setTitle("Downloading v${_appUpdateData.value?.appVersion}")
            // write into your app’s own folder (no storage permission needed)
            setDestinationUri(destUri)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
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


    fun providePlayableProgramData(programs: List<Programme>) {
        val now = System.currentTimeMillis()
        val formatter = SimpleDateFormat("hh:mm a", Locale.US)
        _filterAvailablePrograms.value = programs
            .asSequence()
            .filter { program ->
                val start = program.startTime
                val end = program.endTime
                //loge("", "start:${start} and end:${end}")
                // Only include if both times are non-null and end is strictly in the future:
                if (start == null || end == null) return@filter false
                // 1) Currently running: start <= now < end
                // 2) Upcoming: now < start
                (start <= now && now < end) || (now < start)
            }
            .distinctBy { it.startTime to it.endTime }
            .sortedBy { it.startTime }
            .take(3)
            .map { program ->
                program.copy(
                    startFormatedTime = program.startTime
                        ?.let { formatter.format(it) }
                        ?: "--",
                    endFormatedTime = program.endTime
                        ?.let { formatter.format(it) }
                        ?: "--"
                )
            }.toList()
    }


    fun provideUserHash(){
        viewModelScope.launch {
            networkApiCallInterfaceImpl.provideUserHash(UrlManager.getCurrentBaseUrl()+"userData?username="+ PreferenceManager.getUsername()).collect { response ->
                val macId = application.provideMacAddress()?:""
                when (response) {
                    is WTVResponse.Success -> {
                        if(response.data.hash.isNotNullOrEmpty()) {
                            PreferenceManager.saveHash(response.data.hash)
                        }else{
                            val requestBody = hashMapOf<String, String>().apply {
                                PreferenceManager.getUsername()?.let { put("username", it) }
                                macId?.let { put("macId", it) }
                            }

                            networkApiCallInterfaceImpl.registerUserHash(
                                hashUrl = UrlManager.getCurrentBaseUrl()+"userData",
                                requestBody = requestBody).collect { response ->
                                when (response) {
                                    is WTVResponse.Success -> {
                                        provideApplicationContext().showToastS(response.data.toString())
                                        if(response.data.hash.isNotNullOrEmpty()) {
                                            PreferenceManager.saveHash(response.data.hash)
                                        }
                                    }

                                    is WTVResponse.Failure -> {

                                    }
                                }
                            }
                        }
                    }
                    is WTVResponse.Failure -> {


                        val requestBody = hashMapOf<String, String>().apply {
                            PreferenceManager.getUsername()?.let { put("username", it) }
                            macId?.let { put("macId", it) }
                        }

                        networkApiCallInterfaceImpl.registerUserHash(
                            hashUrl = UrlManager.getCurrentBaseUrl()+"userData",
                            requestBody = requestBody).collect { response ->
                            when (response) {
                                is WTVResponse.Success -> {
                                    provideApplicationContext().showToastS(response.data.toString())
                                    if(response.data.hash.isNotNullOrEmpty()) {
                                        PreferenceManager.saveHash(response.data.hash)
                                    }
                                }

                                is WTVResponse.Failure -> {

                                }
                            }
                        }
                    }
                }
            }
        }
    }


    fun fetchInventoryApps() {
        viewModelScope.launch {
            networkApiCallInterfaceImpl
                .provideWTVInventoryApps(UrlManager.getCurrentBaseUrl() + "app/inventory-apps")
                .catch { }
                .collect { resp ->
                    if (resp is WTVListResponse.Success) {
                        updateAppsInventory(resp.data)
                        dataRepository?.saveAllAppsInventory(resp.data)
                        // application.applyAppInventoryApp(resp.data)
                    }
                }
        }
    }
    suspend fun provideBanners() {
        networkApiCallInterfaceImpl.getBanners(UrlManager.getCurrentBaseUrl() +"banners").collect { response ->
            when (response) {
                is WTVListResponse.Success -> {
                    updateBanners(response.data)
                    dataRepository?.saveAllBanner(response.data)
                }
                is WTVListResponse.Failure -> logReport("_bannerList:${response.error.message} ")
            }
        }
    }


    fun validateUserLogin(userName: String,userPassword: String,onLoginResponse:(LoginResponseData?,String?)->Unit){
        viewModelScope.launch {
            val requestBody = hashMapOf(
                "username" to (userName),
                "password" to (userPassword)
            )
            networkApiCallInterfaceImpl.provideUserLogin(
                loginUrl = UrlManager.getCurrentBaseUrl()+"app/tv-users/login",
                requestBody = requestBody).collect { response ->
                when (response) {
                    is WTVResponse.Success -> onLoginResponse(response.data,null)//_bannerList.value = response.data
                    is WTVResponse.Failure -> onLoginResponse(null,response.error.message) //logReport("_bannerList:${response.error.message}")
                }
            }
        }
    }

    fun packageUpdate(){
        viewModelScope.launch {
            val requestBody = hashMapOf<String, Any>(
                "username" to (PreferenceManager.getUsername() ?: ""),
                "packageUpdate" to 0
            )
            networkApiCallInterfaceImpl.provideUserProfileCMS(
                requestBody = requestBody).collect { response ->
            }
        }
    }

    fun filterPanMetroChannelsByGenre(genre:String?=null) {
        genre?.let {
            _filteredPanMetroChannels.value = _wtvEPGList.value?.filter { epgItem ->
                val genreMatch = genre.equals("All", true) ||  (epgItem.content?.genre?.map { it.name }.orEmpty()?.any { it.equals(genre, true) } == true)
                genreMatch
            }?: arrayListOf()
        }?:kotlin.run {
            _filteredPanMetroChannels.value = _wtvEPGList.value
        }

    }

    fun syncOfflineData(): Job {
      return viewModelScope.launch {
            // Execute all data operations first
            dataRepository?.getWtvManifest()?.let {
                Log.e("data:", it.toString())
                updateManifest(it)
            }
            dataRepository?.getAllHomeCategories()?.let {
                Log.e("data:", it.toString())
                updateHome(it)
            }
            dataRepository?.getAllEpgData()?.let {
                Log.e("data:", it.toString())
                updateEpgData(it)
            }
            dataRepository?.getAllAppsInventory()?.let {
                Log.e("data:", it.toString())
                updateAppsInventory(it)
            }
            dataRepository?.getAllBanner()?.let {
                Log.e("data:", it.toString())
                updateBanners(it)
            }
        }
    }

    fun enableOffline(isOfflineEnable: Boolean? = false) {
        // Update the offline enable state after all operations complete
        _isOfflineEnable.value = isOfflineEnable ?: false
    }

}
fun removeDuplicateEPG(items: List<EPGDataItem>): List<EPGDataItem> {
    return items
        .filter { it.channelId != null }       // optional: drop null IDs
        .distinctBy { it.channelId }
// keep first of each channelId
}