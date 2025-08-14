package com.android.panmetroiptv.viewmodels

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.app.DownloadManager
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
import com.android.panmetroiptv.R
import com.android.panmetroiptv.extensions.applyAppManifest
import com.android.panmetroiptv.extensions.applyEPGData
import com.android.panmetroiptv.extensions.isNotNullOrEmpty
import com.android.panmetroiptv.extensions.loge
import com.android.panmetroiptv.extensions.provideMacAddress
import com.android.panmetroiptv.extensions.showToastS
import com.android.panmetroiptv.extensions.toJSONObject
import com.android.panmetroiptv.model.data.appupdate.AppUpdateData
import com.android.panmetroiptv.model.data.epgdata.EPGDataItem
import com.android.panmetroiptv.model.data.epgdata.Programme
import com.android.panmetroiptv.model.data.genre.WTVGenre
import com.android.panmetroiptv.model.data.language.WTVLanguage
import com.android.panmetroiptv.model.data.login.LoginInfo
import com.android.panmetroiptv.model.data.sse.TabItem
import com.android.panmetroiptv.model.notification.NotificationItem
import com.android.panmetroiptv.model.repository.common.WTVNetworkRepositoryImpl
import com.android.panmetroiptv.model.repository.login.LoginPrefsRepository
import com.android.panmetroiptv.utils.Constants
import com.android.panmetroiptv.utils.sealed.WTVResponse
import com.android.panmetroiptv.utils.sealed.firstOrNullSuccess
import com.android.panmetroiptv.utils.uistate.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
import kotlin.text.equals


@HiltViewModel
open class WTVViewModel @Inject constructor(
    private val application: Application,
    private val networkApiCallInterfaceImpl: WTVNetworkRepositoryImpl,
    private val loginPrefsRepository: LoginPrefsRepository?=null,
    private val okHttpClient: OkHttpClient
) : AndroidViewModel(application) {
    fun provideApplicationContext() = application.applicationContext
    private val _userIdeal = MutableStateFlow<Boolean>(false)

    private var _isInitializeData = MutableStateFlow<Boolean>(false)
    val isInitializeData: StateFlow<Boolean> get() = _isInitializeData
    private var _isProgress = MutableStateFlow<Boolean>(false)
    val provideIsProgress: StateFlow<Boolean> get() = _isProgress
    private var _wtvEPGList = MutableStateFlow<List<EPGDataItem>>(emptyList())
    val wtvEPGList: StateFlow<List<EPGDataItem>> = _wtvEPGList.asStateFlow()
    private var _selectedChannel = MutableStateFlow<EPGDataItem>(EPGDataItem())
    val selectedChannel: StateFlow<EPGDataItem> = _selectedChannel.asStateFlow()

    private var _filterAvailablePrograms = MutableStateFlow<List<Programme>>(arrayListOf())
    val filterAvailablePrograms: StateFlow<List<Programme>> = _filterAvailablePrograms.asStateFlow()

    // ─── Date and time state ───
    private val _isTimeValid = MutableStateFlow<Boolean?>(null)
    val isTimeValid: StateFlow<Boolean?> = _isTimeValid.asStateFlow()
    private val _isServerAvailable = MutableStateFlow<Boolean>(false)
    val isServerAvailable: StateFlow<Boolean> = _isServerAvailable.asStateFlow()

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


    /*fun updateEPGData(epgList: List<EPGDataItem>) {
        viewModelScope.launch {
            saveEPGList(application, epgList)
        }
    }*/

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
                .provideNotificationSSE(Constants.BASE_URL+"app/getNotification-sse")
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
        loge("WTVViewModel", " showPushNotification: ${item.message}")

        // 2) Check POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(application, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w("WTVViewModel", "Missing POST_NOTIFICATIONS permission!")
            return
        }
        val builder = NotificationCompat.Builder(application, NOTIF_CHANNEL_ID)
            .setSmallIcon(R.drawable.panlogin)
            .setContentTitle("New message")
            .setContentText(item.message)
            .setAutoCancel(true)

        NotificationManagerCompat.from(application)
            .notify(item.id.hashCode(), builder.build())
    }

    fun initializeAppRequiredData() {
        viewModelScope.launch {
            //First call health check API
            /*val healthCheckSuccess = async {
                networkApiCallInterfaceImpl
                    .provideServerTimeStamp("https://api-panmetro.caastv.com/api/app/health")
                    .firstOrNullSuccess()
            }.await()

            if (healthCheckSuccess == null) {
                Constants.applyBaseUrl(false)
                provideApplicationContext().showToastS("Switching the Server now..")
            }else{
                provideApplicationContext().showToastS("Server is working now..")
            }*/
            // This scope will suspend until ALL async children complete
            val manifestDeferred = async {
                networkApiCallInterfaceImpl
                    .provideWTVManifest(Constants.BASE_URL+"manifest")
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
                        manifest.copy(genre = genre, language = language)
                    }
            }.await()
            val epgDeferred = async {
                networkApiCallInterfaceImpl
                    .provideWTVEPGData(Constants.BASE_URL+"epg-files/join-epg-content")
                    .firstOrNullSuccess()
                    ?.let { epgData ->
                        epgData
                    }
            }.await()


            // Wait for all to complete (success or failure)
            if (manifestDeferred != null && epgDeferred != null) {
                // **This line runs only after all of the above finish.**
                loge("manifestDeferred",manifestDeferred.toJSONObject().toString())
                application.applyAppManifest(manifestDeferred)
                val epgData = removeDuplicateEPG(epgDeferred)
                _wtvEPGList.value = epgData
                application.applyEPGData(epgData)
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
            /*launch {
                networkApiCallInterfaceImpl
                    .provideWTVHomeData(Constants.BASE_URL+"homescreenCategory")
                    .collect { response ->
                        if (response is WTVListResponse.Success) {
                            application.applyAppHome(response.data)
                            logReport("applyAppHome:${response.data}")
                        } else if (response is WTVListResponse.Failure) {
                            logReport("applyAppHome error:${response.error.message}")
                        }
                    }
            }*/
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun fetchServerTimeMillis(): Long? {
        return try {
            val healthAPI = Constants.BASE_URL + "app/health"
            val req = Request.Builder()
                .url(healthAPI)
                .get()
                .build()

            loge(TAG, healthAPI)

            val resp = okHttpClient.newCall(req).execute()

            if (!resp.isSuccessful) {
                loge(TAG, "Health endpoint error: HTTP ${resp.code}")
                // Instead of throwing, return null and let the caller handle it
                return null
            }

            loge("API:","Url:${healthAPI}>${resp.body}")
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

    fun updateSelectedChannel(selectedChannel: EPGDataItem) {
        _selectedChannel.value = selectedChannel
        selectedChannel.tv?.programme?.let { providePlayableProgramData(it) }
    }

    //is user ideal since 10 sec
    fun updateUserIdeal(isUserIdeal: Boolean) {
        _userIdeal.value = isUserIdeal
    }


    //check for updates

    fun clearDownloadId() {
        _downloadId.value = null
    }

    fun checkForAppUpdate() = viewModelScope.launch {
        _isProgress.value = true
        val resp = networkApiCallInterfaceImpl
            .provideAppUpdateInfo(Constants.BASE_URL+"app/appupdate")
            .firstOrNullSuccess()
        _isProgress.value = false

        resp?.data?.let { update ->
            // grab the currently installed version
            val current = application.packageManager
                .getPackageInfo(application.packageName, 0)
                .versionName
                .orEmpty()

            // only if the server’s version is higher do we prompt or download
                if (shouldUpdateRequired(update.appVersion, current) && update.checkRegionUpdate(PreferenceManager.getLoginResponse()?.regionCode) == true) {
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
        loge(
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

        val destDir  = ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!
        // Clean up any previous APK files
        cleanUpOldApks(destDir)

        val fileName = "tvapp_${_appUpdateData.value?.appVersion}.apk"
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

    /*fun downloadApk(apkUrl: String): Long {
        val ctx = getApplication<Application>()
        val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val version = _appUpdateData.value?.appVersion ?: "unknown"

        // Create downloads directory if it doesn't exist
        val destDir = ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.apply {
            if (!exists()) mkdirs()
        } ?: run {
            // Fallback to cache directory if downloads directory isn't available
            ctx.cacheDir.apply {
                if (!exists()) mkdirs()
            }.also {
                Log.w("ApkDownload", "Using cache directory as fallback for APK download")
            }
        }
        // Clean up any previous APK files
        cleanUpOldApks(destDir)

        // Generate version-specific filename
        val fileName = "tvapp_v${version}.apk"
        val file = File(destDir, fileName)
        val destUri = Uri.fromFile(file)

        // Create download request
        val req = DownloadManager.Request(Uri.parse(apkUrl)).apply {
            setTitle("TVApp v$version")
            setDescription("Downloading update")
            setDestinationUri(destUri)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            // Optional: set network requirements
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE )
            setAllowedOverRoaming(false)
        }

        // Enqueue download and store ID
        val downloadId = dm.enqueue(req)
        _downloadId.value = downloadId
        return downloadId
    }*/

    private fun cleanUpOldApks(directory: File) {
        try {
            directory.listFiles()?.forEach { file ->
                if (file.isFile && file.name.startsWith("tvapp_") && file.name.endsWith(".apk")) {
                    file.delete()
                    Log.d("ApkDownload", "Deleted old APK: ${file.name}")
                }
            }
        } catch (e: Exception) {
            Log.e("ApkDownload", "Error cleaning up old APKs", e)
        }
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
                if (start == null || end == null) return@filter false
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
            networkApiCallInterfaceImpl.provideUserHash(Constants.BASE_URL+"userData?username="+ PreferenceManager.getUsername()).collect { response ->
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
                                hashUrl = Constants.BASE_URL+"userData",
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
                            hashUrl = Constants.BASE_URL+"userData",
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


    fun validateUserLogin(uName:String,paswrd:String,macId:String,onLoginResponse:(LoginInfo?,String?)->Unit){
        viewModelScope.launch {
            //Prepare headers and body
            val headers = mapOf(
                "Authorization" to "56fdsr237df325fv454v3v4532drferh",
                "Content-Type"  to "application/json"
            )
            val requestBody = hashMapOf(
                "uname" to (uName),
                "paswrd" to (paswrd ),
                "macaddr" to (macId)
            )
            networkApiCallInterfaceImpl.provideUserLogin(
                loginUrl = Constants.LOGIN_SMS_BASE+"src/api/v1/logincheck",//"osmsapi/cryptodrm/logincheck",
                requestBody = requestBody).collect { response ->
                when (response) {
                    is WTVResponse.Success -> {
                        //update userId
                        //userProfileAPI(uName=uName)
                        onLoginResponse(response.data,null)
                        if(response.data.customerNumber?.isNotEmpty() == true){
                            userPackageUpdate(customerNumber = response.data.customerNumber)
                        }
                    }//_bannerList.value = response.data
                    is WTVResponse.Failure -> onLoginResponse(null,response.error.toString()) //logReport("_bannerList:${response.error.message}")
                }
            }
        }
    }


    /*fun userProfileAPI(uName:String){
        viewModelScope.launch {
            networkApiCallInterfaceImpl.getUserInfo(
                requestUrl = Constants.DRM_LICENSE_BASE+"/src/api/v1/user-logins/$uName",
                headers = Constants.staticHeaders).collect { response ->
                when (response) {
                    is WTVResponse.Success -> {
                        if(response.data.customerNumber.isNotNullOrEmpty()){
                            provideApplicationContext().showToastS("Customer${response.data.customerNumber}")
                            val userUpdates = PreferenceManager.getLoginResponse()
                            userUpdates?.drmInfo = response.data
                            userUpdates?.let { PreferenceManager.saveUserInfo(it) }
                            userPackageUpdate(customerNumber = response.data.customerNumber,onPKGResponse={response ->
                               if((response?.results?.size ?: 0) > 0){
                                    response?.results?.forEach {
                                        packageChannelUpdate(pkgName = it.id.toString(), onPKGChannelResponse = {

                                        })
                                    }
                                }
                            })
                        }
                    }
                    is WTVResponse.Failure -> {
                        provideApplicationContext().showToastS("User customer number not found.")
                    }
                }
            }
        }
    }*/
    fun userPackageUpdate(customerNumber:String){
        viewModelScope.launch {
            networkApiCallInterfaceImpl.getCustomerPackageInfo(
                requestUrl = Constants.LOGIN_SMS_BASE+"src/api/v1/customer-services/${customerNumber}?page=1&limit=10").collect { response ->
                when (response) {
                    is WTVResponse.Success -> {
                        PreferenceManager.saveUserPackageInfo(response.data)
                        /*response.data?.results?.map { it.serviceId }?.let {
                            customerChannelUpdates(it)
                        }*/
                    }
                    is WTVResponse.Failure -> {
                        loge("customer-services>","User customer number not found.")
                    }
                }
            }
        }
    }

    fun customerChannelUpdates(pkgName: List<Int>){
        viewModelScope.launch {
            networkApiCallInterfaceImpl.getCustomerChannelInfo(pkgName)
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

}
fun removeDuplicateEPG(items: List<EPGDataItem>): List<EPGDataItem> {
    return items
        .filter { it.channelId != null }       // optional: drop null IDs
        .distinctBy { it.channelId }            // keep first of each channelId
}