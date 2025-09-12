package com.android.panmetroiptv.viewmodels


import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.panmetroiptv.PanmetroApplication
import com.android.panmetroiptv.extensions.appPkgChannelsLiveData
import com.android.panmetroiptv.extensions.convertIntoModel
import com.android.panmetroiptv.extensions.coreEPGLiveData
import com.android.panmetroiptv.extensions.logReport
import com.android.panmetroiptv.extensions.loge
import com.android.panmetroiptv.extensions.provideMacAddress
import com.android.panmetroiptv.model.data.DataStoreManager
import com.android.panmetroiptv.model.data.FilterPreferences
import com.android.panmetroiptv.model.data.FilterState
import com.android.panmetroiptv.model.data.banner.Banner
import com.android.panmetroiptv.model.data.epgdata.Channel
import com.android.panmetroiptv.model.data.epgdata.EPGDataItem
import com.android.panmetroiptv.model.data.epgdata.Programme
import com.android.panmetroiptv.model.data.favorite.Favorite
import com.android.panmetroiptv.model.data.filter.PanMetroGenreFilter
import com.android.panmetroiptv.model.data.sseresponse.GlobalSSEResponse
import com.android.panmetroiptv.model.data.sseresponse.PlayerSSEResponse
import com.android.panmetroiptv.model.repository.common.WTVNetworkRepositoryImpl
import com.android.panmetroiptv.model.repository.login.LoginPrefsRepository
import com.android.panmetroiptv.model.wtvdatabase.EPGContract
import com.android.panmetroiptv.utils.Constants
import com.android.panmetroiptv.utils.network.heper.ConnectivityObserver
import com.android.panmetroiptv.utils.network.heper.NetworkStatus
import com.android.panmetroiptv.utils.network.interceptors.ApiStatusInterceptor
import com.android.panmetroiptv.utils.uistate.PreferenceManager
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.log
import kotlin.text.filter
import kotlin.text.mapNotNull
import kotlin.text.orEmpty

@HiltViewModel
open class SharedViewModel @Inject constructor(
    private val wtvNetworkRepositoryImpl: WTVNetworkRepositoryImpl,
    private val application: Application,
    private val dataStoreManager: DataStoreManager,
    private val filterPreferences: FilterPreferences,
    private val loginPrefsRepository: LoginPrefsRepository,
) : WTVViewModel(application = application, networkApiCallInterfaceImpl = wtvNetworkRepositoryImpl,loginPrefsRepository=loginPrefsRepository, okHttpClient = OkHttpClient()) {
    fun provideApplicationInstance() = application.applicationContext as? PanmetroApplication
    private val observer = ConnectivityObserver(application)
    var deviceMacAddr = MutableStateFlow<String>("")
    var isFromSplash = MutableStateFlow<Boolean>(false)

    var isGlobalSSEClosed = MutableStateFlow<Boolean>(false)

    private var globalEventSource: EventSource? = null
    private val _globalSSERules = MutableStateFlow<GlobalSSEResponse?>(null)
    val globalSSERules: StateFlow<GlobalSSEResponse?> = _globalSSERules
    val goingToFullPlayer = mutableStateOf(false)

    private var playerEventSource: EventSource? = null
    private val _playerSSERules = MutableStateFlow<PlayerSSEResponse?>(null)
    val playerSSERules: StateFlow<PlayerSSEResponse?> = _playerSSERules


    private var prePlayerEventSource: EventSource? = null
    private val _prePlayerSSERules = MutableStateFlow<PlayerSSEResponse?>(null)
    val prePlayerSSERules: StateFlow<PlayerSSEResponse?> = _prePlayerSSERules


    private val _bannerList = MutableStateFlow<List<Banner>>(emptyList())
    val bannerList: StateFlow<List<Banner>> = _bannerList.asStateFlow()

    private val _filteredEPGList = MutableStateFlow<List<EPGDataItem>>(emptyList())
    val filteredEPGList: StateFlow<List<EPGDataItem>> = _filteredEPGList.asStateFlow()

    private val _epgChannels = MutableStateFlow<List<Channel>>(emptyList())
    val epgChannels: StateFlow<List<Channel>> = _epgChannels.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Channel>>(emptyList())
    val searchResults: StateFlow<List<Channel>> = _searchResults.asStateFlow()

    private val _wishlistPopupProgram = MutableStateFlow<Programme?>(null)
    val wishlistPopupProgram: StateFlow<Programme?> = _wishlistPopupProgram.asStateFlow()

    private val _wishlistAlertProgram = MutableStateFlow<Programme?>(null)
    val wishlistAlertProgram: StateFlow<Programme?> = _wishlistAlertProgram.asStateFlow()

    private val _wishlist = MutableStateFlow<List<Programme>>(emptyList())
    val wishlist: StateFlow<List<Programme>> = _wishlist.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()



    // StateFlow for raw IDs
    private val _favoriteChannelIds = MutableStateFlow<List<String>>(emptyList())
    val favoriteChannelIds: StateFlow<List<String>> = _favoriteChannelIds.asStateFlow()

    private val _favoriteChannels = MutableStateFlow<List<Channel>>(emptyList())
    val favoriteChannels: StateFlow<List<Channel>> = _favoriteChannels.asStateFlow()


    private val _panMetroGenreState = MutableStateFlow(PanMetroGenreFilter())
    val panMetroGenreState: StateFlow<PanMetroGenreFilter> = _panMetroGenreState.asStateFlow()

    private val _currentPlaylist = MutableStateFlow<List<EPGDataItem>>(emptyList())
    val currentPlaylist: StateFlow<List<EPGDataItem>> = _currentPlaylist


    private val _lastSelectedChannelIndex = MutableStateFlow<Int>(-1)

    val lastSelectedChannelIndex: StateFlow<Int> = _lastSelectedChannelIndex

    private val _genreScreenLastGenreIndex = MutableStateFlow(0)
    val genreScreenLastGenreIndex: StateFlow<Int> = _genreScreenLastGenreIndex

    private val _genreScreenLastChannelIndex = MutableStateFlow(0)
    val genreScreenLastChannelIndex: StateFlow<Int> = _genreScreenLastChannelIndex

    var lastFocusedChannelIndex = mutableStateOf(0)
        private set


    fun updateGenreScreenLastGenreIndex(index: Int) {
        _genreScreenLastGenreIndex.value = index
    }

    fun updateGenreScreenLastChannelIndex(index: Int) {
        _genreScreenLastChannelIndex.value = index
    }
    // updater
    fun updateLastSelectedChannelIndex(index: Int) {
        _lastSelectedChannelIndex.value = index
    }
    init {
        //provideGlobalFingerprintInfo()
        //provideScrollMessageInfo()
        // only load once, no continuous observation to avoid overriding
        viewModelScope.launch {
            isInitializeData
                .filter { it }        // only when it becomes true
                .first()
            val saved = filterPreferences.filterFlow.first() // <-- one-time load only
            _filterState.value = saved
            applyFilters()
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    val networkStatus: StateFlow<NetworkStatus> =
        observer.observe()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = NetworkStatus.Unavailable
            )



    fun clearWishlistPopup() { _wishlistPopupProgram.value = null }

    fun addToWishlist(program: Programme) {
        program.watchedAt = System.currentTimeMillis()
        _wishlist.value += program
        clearWishlistPopup()
    }

    fun onShowWishlistPopup(program: Programme) { _wishlistPopupProgram.value = program }

    fun clearWishlistAlert() { _wishlistAlertProgram.value = null }


    val authToken = dataStoreManager.authToken

    fun checkUserLoginStatus(onLoggedIn: () -> Unit, onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authToken.collectLatest { token ->
                if (token.isNullOrEmpty()) onLoggedOut() else onLoggedIn()
            }
        }
    }


    fun updateLastFocusedChannel(index: Int) {
        lastFocusedChannelIndex.value = index
    }


    fun updateGenre(genre: String?) {
        val newGenre = if (genre.equals("All",true) ) null else genre
        _filterState.value = _filterState.value.copy(genre = newGenre)
        saveFilters()
        applyFilters()
    }
    fun updatePanMetroGenre(genre: String?,videoUrl:String?) {
        _panMetroGenreState.value = _panMetroGenreState.value.copy(genre = genre,videoUrl=videoUrl)
    }

    fun updateLanguage(language: String?) {
        val newLanguage = if (language.equals("All",true) ) null else language
        _filterState.value = _filterState.value.copy(language = newLanguage)
        saveFilters()
        applyFilters()
    }


    private fun saveFilters() {
        filterPreferences.saveFilter(viewModelScope, _filterState.value)
    }

    private fun savePanMetroGenre() {
        filterPreferences.saveGenreSelection(scope=viewModelScope, genreFilter =  _panMetroGenreState.value)
    }


    private fun applyFilters() {
        val fullList = provideApplicationContext().coreEPGLiveData().value?:wtvEPGList.value
        val filter = _filterState.value

        val filtered = fullList?.filter { epgItem ->
            val genreList = epgItem.genre?.map { it.name }.orEmpty()
            val language = epgItem.language?.name.orEmpty()

            val genreMatch = filter.genre == null || genreList.any { it.equals(filter.genre, true) }
            val languageMatch = filter.language == null || language.equals(filter.language, true)

            genreMatch && languageMatch
        }

        _filteredEPGList.value = filtered?: arrayListOf()
    }


    fun provideAvailableProgram(programs: List<Programme>): List<Programme?> {
        val now = System.currentTimeMillis()
        return programs
            .filter { program ->
                val start = program.startTime
                val end   = program.endTime
                // Only include if both times are non-null and end is strictly in the future:
                if (start == null || end == null) return@filter false
                // 1) Currently running: start <= now < end
                // 2) Upcoming: now < start
                (start <= now && now < end) || (now < start)
            }
            .distinctBy { it.startTime to it.endTime }
            .sortedBy { it.startTime }
    }


    fun provideGlobalSSERequest() {
        val queryBuilder = (Constants.BASE_URL + "app/combined-sse?")
            .toUri()
            .buildUpon()

        // Only append if values are not null or blank
        PreferenceManager.getUserPackageInfo()?.results?.joinToString(",") { it.serviceName }?.let {
            queryBuilder.appendQueryParameter("package", it)
        }

        PreferenceManager.getUsername()?.takeIf { it.isNotBlank() }?.let {
            queryBuilder.appendQueryParameter("user", "${PreferenceManager.getLoginResponse()?.userId}:${it}")
        }

        PreferenceManager.getLoginResponse()?.provideUserRegionCode()?.takeIf { it.isNotBlank() }?.let {
            queryBuilder.appendQueryParameter("region", it)
        }?: run {
            queryBuilder.appendQueryParameter("region", "01")
        }

        queryBuilder.appendQueryParameter("appVersion","panmetro_${application.packageManager
            .getPackageInfo(application.packageName, 0)
            .versionName}")

        deviceMacAddr?.value?.let {
            queryBuilder.appendQueryParameter("macId", it)
        }

        val sseUrl = queryBuilder.build().toString()
        loge("finalUrl>",sseUrl)

        val client = OkHttpClient.Builder()
             .addInterceptor { chain ->
                val original = chain.request()
                val withHeaders = original.newBuilder()
                    .header("x-api-key", Constants.HEADER_TOKEN)
                    .build()
                chain.proceed(withHeaders)
             }
            .addInterceptor(ApiStatusInterceptor.getInstance())
            .retryOnConnectionFailure(true)
            .readTimeout(0, TimeUnit.MILLISECONDS) // Required for SSE!
            .build()

        val request = Request.Builder()
            .url(sseUrl) // replace with your endpoint URL
            .build()

        val listener = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                loge("SSE onOpen>",sseUrl)
                // Log or perform actions on open
                isGlobalSSEClosed.value = false
            }

            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                // Update the global state with new event data.
                loge("SSE>",sseUrl+data.toString())
                try {
                    data.toString()
                        .convertIntoModel(GlobalSSEResponse::class.java)?.let {
                            _globalSSERules.value = it
                        }
                } catch (e: Exception) {
                    loge("SSE>", "Error parsing JSON: ${e.message}")
                }
            }

            override fun onClosed(eventSource: EventSource) {
                isGlobalSSEClosed.value = true
                // Optionally handle close events.
                loge("SSE>", "Connection closed")
            }

            override fun onFailure(
                eventSource: EventSource,
                t: Throwable?,
                response: Response?
            ) {
                // Handle failures (and consider restarting the connection).
                loge("SSE SSE", "Connection failed: ${t?.message}")
                isGlobalSSEClosed.value = true
            }
        }

        // Start the SSE connection.
        globalEventSource = EventSources.createFactory(client).newEventSource(request, listener)
    }

    fun providePlayerSSERequest(
        channel: String?=null,//"1003:RAAPCHIK"
    ) {
        playerEventSource?.let {
            playerEventSource?.cancel()
            playerEventSource = null
        }
        val queryBuilder = (Constants.BASE_URL +"app/combined-sse?")
            .toUri()
            .buildUpon()
        channel?.takeIf { it.isNotBlank() }?.let {
            queryBuilder.appendQueryParameter("liveChannel", it)
        }

        PreferenceManager.getLoginResponse()?.provideUserRegionCode()?.takeIf { it.isNotBlank() }?.let {
            queryBuilder.appendQueryParameter("region", it)
        }?: run {
            queryBuilder.appendQueryParameter("region", "01")
        }

        queryBuilder.appendQueryParameter("appVersion","panmetro_${application.packageManager
            .getPackageInfo(application.packageName, 0)
            .versionName}")

        application.provideMacAddress()?.let {
            queryBuilder.appendQueryParameter("macId", it)
        }
        val sseUrl = queryBuilder.build().toString()
        loge("PlayerFingerprint url>",sseUrl)
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val withHeaders = original.newBuilder()
                    .header("x-api-key", Constants.HEADER_TOKEN)
                    .build()
                chain.proceed(withHeaders)
            }
            .retryOnConnectionFailure(true)
            .readTimeout(0, TimeUnit.MILLISECONDS) // Required for SSE!
            .build()

        val request = Request.Builder()
            .url(sseUrl) // replace with your endpoint URL
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
                loge("SSE >",sseUrl+data.toString())

                try {
                    data.toString()
                        .convertIntoModel(PlayerSSEResponse::class.java)?.let {
                            _playerSSERules.value = it
                        }
                    loge("SSE >", data.toString())

                } catch (e: Exception) {
                    loge("SSE ", "Error parsing JSON: ${e.message}")
                }
            }

            override fun onClosed(eventSource: EventSource) {
                // Optionally handle close events.
                loge("SSE", "Connection closed")
            }

            override fun onFailure(
                eventSource: EventSource,
                t: Throwable?,
                response: Response?
            ) {
                // Handle failures (and consider restarting the connection).
                loge("PlayerFingerprint SSE", "Connection failed: ${t?.message}")
            }
        }

        // Start the SSE connection.
        playerEventSource = EventSources.createFactory(client).newEventSource(request, listener)
    }

    fun providePrePlayerSSERequest(
        channel: String?=null,//"1003:RAAPCHIK"
    ) {
        prePlayerEventSource?.let {
            prePlayerEventSource?.cancel()
            prePlayerEventSource = null
        }
        val queryBuilder = (Constants.BASE_URL +"app/combined-sse?")
            .toUri()
            .buildUpon()
        channel?.takeIf { it.isNotBlank() }?.let {
            queryBuilder.appendQueryParameter("liveChannel", it)
        }

        PreferenceManager.getLoginResponse()?.provideUserRegionCode()?.takeIf { it.isNotBlank() }?.let {
            queryBuilder.appendQueryParameter("region", it)
        }?: run {
            queryBuilder.appendQueryParameter("region", "01")
        }

        queryBuilder.appendQueryParameter("appVersion","panmetro_${application.packageManager
            .getPackageInfo(application.packageName, 0)
            .versionName}")

        application.provideMacAddress()?.let {
            queryBuilder.appendQueryParameter("macId", it)
        }
        val sseUrl = queryBuilder.build().toString()
        loge("PlayerFingerprint url>",sseUrl)
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val withHeaders = original.newBuilder()
                    .header("x-api-key", Constants.HEADER_TOKEN)
                    .build()
                chain.proceed(withHeaders)
            }
            .retryOnConnectionFailure(true)
            .readTimeout(0, TimeUnit.MILLISECONDS) // Required for SSE!
            .build()

        val request = Request.Builder()
            .url(sseUrl) // replace with your endpoint URL
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
                loge("SSE >",sseUrl+data.toString())

                try {
                    data.toString()
                        .convertIntoModel(PlayerSSEResponse::class.java)?.let {
                            _prePlayerSSERules.value = it
                        }
                    loge("SSE >", data.toString())

                } catch (e: Exception) {
                    loge("SSE ", "Error parsing JSON: ${e.message}")
                }
            }

            override fun onClosed(eventSource: EventSource) {
                // Optionally handle close events.
                loge("SSE", "Connection closed")
            }

            override fun onFailure(
                eventSource: EventSource,
                t: Throwable?,
                response: Response?
            ) {
                // Handle failures (and consider restarting the connection).
                loge("PlayerFingerprint SSE", "Connection failed: ${t?.message}")
            }
        }

        // Start the SSE connection.
        prePlayerEventSource = EventSources.createFactory(client).newEventSource(request, listener)
    }

    fun filterPanMetroChannelsByGenre(genre:String?=null) {
        val fullEPGList = provideApplicationContext().coreEPGLiveData().value?:wtvEPGList.value
        if(genre.equals("Favorites",true)){
            _filteredPanMetroChannels.value = fullEPGList
                .filter { it.channelId in favoriteChannelIds.value }
                .mapNotNull { it } ?: arrayListOf()
        }else {
            genre?.let {
                _filteredPanMetroChannels.value = fullEPGList?.filter { epgItem ->
                    val genreMatch =
                        genre.equals("All", true) || (epgItem.genre?.map { it.name }.orEmpty()
                            ?.any { it.equals(genre, true) } == true)
                    genreMatch
                } ?: arrayListOf()
            } ?: kotlin.run {
                _filteredPanMetroChannels.value = fullEPGList ?: arrayListOf()
            }
        }

    }

    fun getUserEnableToPlayChannel(assetId:String): Boolean{
        val userPackages = PreferenceManager.getUserPackageInfo()?.provideAvailablePkgData()//availablePkg?.value ?: return false
        val appPkgChannels = appPkgChannels?.value ?: return false
        // Check if any user package contains the assetId
        val canPlay = userPackages?.any { pkg ->
            // If package is not expired, then check if it contains the assetId
            appPkgChannels[pkg]?.contains(assetId) == true
        }
        // Create a set of all allowed channels for faster lookup
        /*val allowedChannels = mutableSetOf<String>()

        userPackages.forEach { pkg ->
            appPkgChannels[pkg]?.let { channels ->
                allowedChannels.addAll(channels)
            }
        }

        val canPlay = assetId in allowedChannels
       */
        loge("getUserEnableToPlayChannel", "Asset: $assetId, Can play: $canPlay")
        return canPlay?:false
    }

    // To update the favorites
    fun addFavorite(channelId: String): Boolean {
        _favoriteChannelIds.update { currentList ->
            if (currentList.contains(channelId)) {
                currentList // already exists
            } else {
                if(currentList.size == Constants.maxLimit){
                    return false
                }
                currentList + channelId
            }
        }
        // Save favorites after state update
        val username = PreferenceManager.getUsername()
        val favorites = favoriteChannelIds.value
        if (username != null && favorites.isNotEmpty()) {
            val favorite = Favorite(channelIds = favorites)
            PreferenceManager.saveUserFav(username, favorite)
        }
        return true
    }

    fun removeFavorite(channelId: String) {
        _favoriteChannelIds.update { currentList ->
            currentList.filter { it != channelId }
        }
        // Save favorites after state update
        val username = PreferenceManager.getUsername()
        val favorites = favoriteChannelIds.value
        if (username != null && favorites.isNotEmpty()) {
            val favorite = Favorite(channelIds = favorites)
            PreferenceManager.saveUserFav(username, favorite)
        }
    }

    fun setFavorites(newFavorites: List<String>) {
        _favoriteChannelIds.value = newFavorites
    }


    fun stopGlobalSSE() {
        globalEventSource?.cancel()
        globalEventSource = null
    }
    fun stopPlayerSSE() {
        playerEventSource?.cancel()
        playerEventSource = null
    }

    fun stopPrePlayerSSE() {
        prePlayerEventSource?.cancel()
        prePlayerEventSource = null
    }

    override fun onCleared() {
        filterPreferences.clearFilter(viewModelScope)
        stopGlobalSSE()
        stopPlayerSSE()
        stopPrePlayerSSE()
        super.onCleared()
    }
}
