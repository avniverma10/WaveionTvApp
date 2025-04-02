package com.example.tvapp.viewmodels


import android.app.Application
import android.content.Context
import android.database.ContentObserver
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.tvapp.extensions.coreEPGLiveData
import com.example.tvapp.extensions.logReport
import com.example.tvapp.utils.sealed.WTVListResponse
import com.example.tvapp.model.data.banner.Banner
import com.example.tvapp.model.data.DataStoreManager
import com.example.tvapp.model.data.FilterPreferences
import com.example.tvapp.model.data.FilterState
import com.example.tvapp.model.data.epgdata.Channel
import com.example.tvapp.model.wtvdatabase.EPGContract
import com.example.tvapp.model.data.epgdata.EPGDataItem
import com.example.tvapp.model.data.epgdata.Programme
import com.example.tvapp.model.data.genre.WTVGenre
import com.example.tvapp.model.data.home.HomeContent
import com.example.tvapp.model.repository.WTVNetworkRepositoryImpl
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
open class SharedViewModel @Inject constructor(
    private val wtvNetworkRepositoryImpl: WTVNetworkRepositoryImpl,
    private val application: Application,
    private val dataStoreManager: DataStoreManager,
    private val filterPreferences: FilterPreferences
) : WTVViewModel(application = application, networkApiCallInterfaceImpl = wtvNetworkRepositoryImpl) {

    private val _bannerList = MutableStateFlow<List<Banner>>(emptyList())
    val bannerList: StateFlow<List<Banner>> = _bannerList.asStateFlow()

    private val _epgDataList = MutableStateFlow<List<EPGDataItem>>(emptyList())
    val epgDataList: StateFlow<List<EPGDataItem>> = _epgDataList.asStateFlow()

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

    private val _filteredPanMetroChannels = MutableStateFlow<List<EPGDataItem>>(emptyList())
    val filteredPanMetroChannels: StateFlow<List<EPGDataItem>> = _filteredPanMetroChannels.asStateFlow()

    private val _availableProgram = MutableStateFlow<List<Programme>>(emptyList())
    val availableProgram: StateFlow<List<Programme>> = _availableProgram.asStateFlow()

    init {
        // only load once, no continuous observation to avoid overriding
        viewModelScope.launch {
            val saved = filterPreferences.filterFlow.first() // <-- one-time load only
            _filterState.value = saved
            applyFilters()
        }

        // observe EPG changes continuously
        viewModelScope.launch {
            observeEPGChanges(application).collect()
        }

        // load banners
        viewModelScope.launch {
            provideBanners()
        }
    }





    fun clearWishlistPopup() { _wishlistPopupProgram.value = null }

    fun addToWishlist(program: Programme) {
        program.watchedAt = System.currentTimeMillis()
        _wishlist.value += program
        clearWishlistPopup()
    }

    fun onShowWishlistPopup(program: Programme) { _wishlistPopupProgram.value = program }

    fun clearWishlistAlert() { _wishlistAlertProgram.value = null }

    private val _selectedVideoUrl = MutableStateFlow<String?>(null)
    val selectedVideoUrl: StateFlow<String?> = _selectedVideoUrl.asStateFlow()

    val authToken = dataStoreManager.authToken

    fun checkUserLoginStatus(onLoggedIn: () -> Unit, onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authToken.collectLatest { token ->
                if (token.isNullOrEmpty()) onLoggedOut() else onLoggedIn()
            }
        }
    }

    suspend fun provideBanners() {
        wtvNetworkRepositoryImpl.getBanners("https://nextwave.waveiontechnologies.com:5000/api/banners").collect { response ->
            when (response) {
                is WTVListResponse.Success -> _bannerList.value = response.data
                is WTVListResponse.Failure -> logReport("_bannerList:${response.error.message}")
            }
        }
    }

    fun onChannelVideoSelected(videoUrl: String?, program: Programme?) {
        _selectedVideoUrl.value = videoUrl
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
                    _epgDataList.value = epgList
                    _epgChannels.value = epgList.mapNotNull { it.tv?.channel }
                    applyFilters()
                    trySend(epgList)
                    filterPanMetroChannelsByGenre()
                }
            }
        }
        context.contentResolver.registerContentObserver(
            EPGContract.EPGEntry.CONTENT_URI, true, observer
        )
        val epgList = fetchEPGList(context)
        _epgDataList.value = epgList
        _epgChannels.value = epgList.mapNotNull { it.tv?.channel }
        applyFilters()
        trySend(epgList)
        filterPanMetroChannelsByGenre()

        awaitClose { context.contentResolver.unregisterContentObserver(observer) }
    }.flowOn(Dispatchers.IO)

    fun updateGenre(genre: String?) {
        val newGenre = if (genre == "All") null else genre
        _filterState.value = _filterState.value.copy(genre = newGenre)
        saveFilters()
        applyFilters()
    }

    fun updateLanguage(language: String?) {
        val newLanguage = if (language == "All") null else language
        _filterState.value = _filterState.value.copy(language = newLanguage)
        saveFilters()
        applyFilters()
    }


    private fun saveFilters() {
        filterPreferences.saveFilter(viewModelScope, _filterState.value)
    }




    private fun applyFilters() {
        val fullList = _epgDataList.value
        val filter = _filterState.value

        val filtered = fullList.filter { epgItem ->
            val genreList = epgItem.content?.genre.orEmpty()
            val language = epgItem.content?.language.orEmpty()

            val genreMatch = filter.genre == null || genreList.any { it.equals(filter.genre, true) }
            val languageMatch = filter.language == null || language.equals(filter.language, true)

            genreMatch && languageMatch
        }

        _filteredEPGList.value = filtered
    }

    fun searchChannels(context: Context, query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchResults.value = _epgChannels.value
                return@launch
            }
            val epgList = fetchEPGList(context)
            val filteredChannels = epgList.mapNotNull { epgItem ->
                epgItem.tv?.channel?.takeIf { channel ->
                    val name = channel.displayName ?: ""
                    val genre = epgItem.content?.genreId ?: ""
                    (name.contains(query, ignoreCase = true) || genre.contains(query, ignoreCase = true))
                }?.copy(
                    logoUrl = epgItem.content?.thumbnailUrl,
                    videoUrl = epgItem.content?.videoUrl,
                    genreId = epgItem.content?.genreId ?: "Unknown"
                )
            }
            _searchResults.value = filteredChannels
        }
    }


    fun providePlayableProgramData(programs: List<Programme>){
        val currentTime = System.currentTimeMillis()
        _availableProgram.value = programs.filter { program ->
            // Convert _start and _stop to epoch milliseconds
            val startMillis = program.startTime?:0L
            val endMillis = program.endTime?:0L
            // Keep the program if it hasn't ended yet
            endMillis > currentTime
        }
    }



    fun filterPanMetroChannelsByGenre(genre:String?=null) {
          genre?.let {
              _filteredPanMetroChannels.value =    _epgDataList.value.filter { epgItem ->
                  val genreMatch = genre.equals("All", true) ||  (epgItem.content?.genre?.orEmpty()?.any { it.equals(genre, true) } == true)
                  genreMatch
              }
          }?:kotlin.run {
              _filteredPanMetroChannels.value = _epgDataList.value
          }

    }

    override fun onCleared() {
        filterPreferences.clearFilter(viewModelScope)
        super.onCleared()
    }
}
