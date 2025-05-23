package com.example.tvapp.viewmodels


import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.database.ContentObserver
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.tvapp.extensions.coreEPGLiveData
import com.example.tvapp.extensions.logReport
import com.example.tvapp.model.data.DataStoreManager
import com.example.tvapp.model.data.FilterPreferences
import com.example.tvapp.model.data.FilterState
import com.example.tvapp.model.data.banner.Banner
import com.example.tvapp.model.data.epgdata.Channel
import com.example.tvapp.model.data.epgdata.EPGDataItem
import com.example.tvapp.model.data.epgdata.Programme
import com.example.tvapp.model.data.filter.PanMetroGenreFilter
import com.example.tvapp.model.repository.common.WTVNetworkRepositoryImpl
import com.example.tvapp.model.repository.login.LoginPrefsRepository
import com.example.tvapp.model.wtvdatabase.EPGContract
import com.example.tvapp.utils.sealed.WTVListResponse
import com.example.tvapp.viewmodels.player.PlayerViewModel
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import androidx.core.content.edit
import com.example.tvapp.utils.Constants
import com.example.tvapp.utils.uistate.PreferenceManager
import okhttp3.OkHttpClient

@HiltViewModel
open class SharedViewModel @Inject constructor(
    private val wtvNetworkRepositoryImpl: WTVNetworkRepositoryImpl,
    private val application: Application,
    private val dataStoreManager: DataStoreManager,
    private val filterPreferences: FilterPreferences,
    private val loginPrefsRepository: LoginPrefsRepository,
) : WTVViewModel(application = application, networkApiCallInterfaceImpl = wtvNetworkRepositoryImpl,loginPrefsRepository=loginPrefsRepository, okHttpClient = OkHttpClient()) {

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

    private val _panMetroGenreState = MutableStateFlow(PanMetroGenreFilter())
    val panMetroGenreState: StateFlow<PanMetroGenreFilter> = _panMetroGenreState.asStateFlow()


    private val _availableProgram = MutableStateFlow<List<Programme>>(emptyList())
    val availableProgram: StateFlow<List<Programme>> = _availableProgram.asStateFlow()
    var lastFocusedChannelIndex = mutableStateOf(0)
        private set

    init {
        // only load once, no continuous observation to avoid overriding
        viewModelScope.launch {
            isInitializeData
                .filter { it }        // only when it becomes true
                .first()
            val saved = filterPreferences.filterFlow.first() // <-- one-time load only
            _filterState.value = saved
            applyFilters()
        }

        // observe EPG changes continuously
        /*viewModelScope.launch {
           // observeEPGChanges(application).collect()
            application.applicationContext.coreEPGLiveData().value?.let {epgList->
                _epgChannels.value = wtvEPGList.value.mapNotNull { it.tv?.channel }
                applyFilters()
                filterPanMetroChannelsByGenre()
            }
        }*/

        // load banners
        /*viewModelScope.launch {
            provideBanners()
        }*/
    }





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

    suspend fun provideBanners() {
        wtvNetworkRepositoryImpl.getBanners("https://api-demo.caastv.com/api/banners").collect { response ->
            when (response) {
                is WTVListResponse.Success -> _bannerList.value = response.data
                is WTVListResponse.Failure -> logReport("_bannerList:${response.error.message}")
            }
        }
    }

    fun updateLastFocusedChannel(index: Int) {
        lastFocusedChannelIndex.value = index
    }

    suspend fun fetchEPGList(context: Context): List<EPGDataItem> {
        return withContext(Dispatchers.IO) {
            val cursor = context.contentResolver.query(
                EPGContract.EPGEntry.CONTENT_URI, null, null, null, null
            )
            val list = mutableListOf<EPGDataItem>()
            cursor?.use {
                while (it.moveToNext()) {
                    val dataJson = it.getString(it.getColumnIndexOrThrow(EPGContract.EPGEntry.COLUMN_DATA))
                    Gson().fromJson(dataJson, EPGDataItem::class.java)?.let { item ->
                        list.add(item)
                    }
                }
            }
            list
        }
    }

    fun observeEPGChanges(context: Context): Flow<List<EPGDataItem>> = callbackFlow {
        val observer = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                launch {
                    val epgList = fetchEPGList(context)
                    _epgChannels.value = epgList.mapNotNull { it.tv?.channel }
                    applyFilters()
                    trySend(epgList)
                }
            }
        }
        context.contentResolver.registerContentObserver(
            EPGContract.EPGEntry.CONTENT_URI, true, observer
        )
        val epgList = fetchEPGList(context)
        _epgChannels.value = epgList.mapNotNull { it.tv?.channel }
        applyFilters()
        trySend(epgList)
        //filterPanMetroChannelsByGenre()

        awaitClose { context.contentResolver.unregisterContentObserver(observer) }
    }.flowOn(Dispatchers.IO)

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
            val genreList = epgItem.content?.genre.orEmpty()
            val language = epgItem.content?.language.orEmpty()

            val genreMatch = filter.genre == null || genreList.any { it.equals(filter.genre, true) }
            val languageMatch = filter.language == null || language.equals(filter.language, true)

            genreMatch && languageMatch
        }

        _filteredEPGList.value = filtered?: arrayListOf()
    }

    fun searchChannels(query: String) {
        viewModelScope.launch {
            // 1) Grab the full EPGDataItem list
            val epgList: List<EPGDataItem> = wtvEPGList.value ?: emptyList()

            // 2) Shortcut: if blank, return everything (but map to program titles)
            if (query.isBlank()) {
                val all = epgList.mapNotNull { item ->
                    val title = item.content?.title ?: return@mapNotNull null
                    item.tv?.channel?.copy(
                        displayName = title,
                        logoUrl     = item.content.thumbnailUrl,
                        videoUrl    = item.content.videoUrl,
                        genreId     = item.content.genreId ?: "Unknown"
                    )
                }
                _searchResults.value = all
                return@launch
            }

            // 3) Filter by program title only
            val filtered = epgList.mapNotNull { item ->
                val progTitle = item.content?.title.orEmpty()
                val matches   = progTitle.contains(query, ignoreCase = true)
                if (!matches) return@mapNotNull null
                // 4) Build a Channel whose displayName is the program title
                item.tv?.channel?.copy(
                    displayName = progTitle,
                    logoUrl     = item.content?.thumbnailUrl,
                    videoUrl    = item.content?.videoUrl,
                    genreId     = item.content?.genreId ?: "Unknown"
                )
            }
            _searchResults.value = filtered
        }
    }





    fun provideAvailableProgram(programs: List<Programme>): List<Programme> {
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

    /** Persist into SharedPreferences on minimize */
    fun persistToGenrePrefs(prefs: PreferenceManager,selectedGenreIndex:Int,selectedChannelIndex:Int) {
        prefs.selectedGenreIndex = selectedGenreIndex
        prefs.selectedChannelIndex = selectedChannelIndex
    }
   // Persist into SharedPreferences on minimize
    fun persistToPlayerPrefs(prefs: PreferenceManager,selectedChannel:EPGDataItem) {
       prefs.lastEpgDataItem = selectedChannel
    }


    /** Persist into SharedPreferences on minimize */
    fun restorePlayerPrefs(prefs: PreferenceManager) {
        prefs.lastEpgDataItem?.let { updateSelectedChannel(it) }
    }

    override fun onCleared() {
        filterPreferences.clearFilter(viewModelScope)
        super.onCleared()
    }
}
