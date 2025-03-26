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
import com.example.tvapp.model.data.epgdata.Channel
import com.example.tvapp.model.wtvdatabase.EPGContract
import com.example.tvapp.model.data.epgdata.EPGDataItem
import com.example.tvapp.model.data.epgdata.Programme
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
    private val dataStoreManager: DataStoreManager) : WTVViewModel(application= application,networkApiCallInterfaceImpl= wtvNetworkRepositoryImpl) {

    // New state for banner list
    private val _bannerList = MutableStateFlow<List<Banner>>(emptyList())
    val bannerList: StateFlow<List<Banner>> = _bannerList.asStateFlow()

    private val _epgChannels = MutableStateFlow<List<Channel>>(emptyList())
    val epgChannels: StateFlow<List<Channel>> = _epgChannels.asStateFlow()

    private val _filteredChannels = MutableStateFlow<List<Channel>>(emptyList())
    val filteredChannels: StateFlow<List<Channel>> = _filteredChannels.asStateFlow()

    private val _filteredEPGList = MutableStateFlow<List<EPGDataItem>>(emptyList())
    val filteredEPGList: StateFlow<List<EPGDataItem>> = _filteredEPGList.asStateFlow()


    private val _filteredPrograms = MutableStateFlow<List<Programme>>(emptyList())
    val filteredPrograms: StateFlow<List<Programme>> = _filteredPrograms.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Channel>>(emptyList())
    val searchResults: StateFlow<List<Channel>> = _searchResults.asStateFlow()


    // ================= Wishlist Integration =================

    // Wishlist state flows for popup and alert.
    private val _wishlistPopupProgram = MutableStateFlow<Programme?>(null)
    val wishlistPopupProgram: StateFlow<Programme?> = _wishlistPopupProgram.asStateFlow()

    private val _wishlistAlertProgram = MutableStateFlow<Programme?>(null)
    val wishlistAlertProgram: StateFlow<Programme?> = _wishlistAlertProgram.asStateFlow()

    // Internal wishlist list.
    private val _wishlist = MutableStateFlow<List<Programme>>(emptyList())
    val wishlist: StateFlow<List<Programme>> = _wishlist.asStateFlow()

    val epgDataFlow = observeEPGChanges(application).stateIn(
        viewModelScope, SharingStarted.Lazily, emptyList()
    )

    init {
        // Fetch banners from API
        viewModelScope.launch {
            provideBanners()
        }

        // Set full EPG list as the initial filtered list
        viewModelScope.launch {
            epgDataFlow.collect { data ->
                _filteredEPGList.value = data
            }
        }
    }

    // Clear the wishlist popup.
    fun clearWishlistPopup() {
        _wishlistPopupProgram.value = null
    }

    // Add an event to the wishlist and clear the popup.
    fun addToWishlist(program: Programme) {
        program.watchedAt = System.currentTimeMillis()
        _wishlist.value += program
        clearWishlistPopup()
    }
    // Called when a future event is clicked to show the popup.
    fun onShowWishlistPopup(program: Programme) {
        _wishlistPopupProgram.value = program
    }
    // Clear the wishlist alert popup.
    fun clearWishlistAlert() {
        _wishlistAlertProgram.value = null
    }


    // State to trigger video playback for a particular channel.
    private val _selectedVideoUrl = MutableStateFlow<String?>(null)
    val selectedVideoUrl: StateFlow<String?> = _selectedVideoUrl.asStateFlow()

    val authToken = dataStoreManager.authToken

    fun checkUserLoginStatus(onLoggedIn: () -> Unit, onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authToken.collect { token ->
                if (token.isNullOrEmpty()) {
                    onLoggedOut()
                } else {
                    onLoggedIn()
                }
            }
        }
    }

    suspend fun provideBanners() {
        wtvNetworkRepositoryImpl.getBanners("https://nextwave.waveiontechnologies.com:5000/api/banners").collect{response ->
            when (response) {
                is WTVListResponse.Success -> {
                    // Handle successful response
                    _bannerList.value = response.data
                    logReport("_bannerList:${ response.data}")

                }
                is WTVListResponse.Failure -> {
                    logReport("_bannerList:${ response.error.message}")

                }
            }
        }
    }


    fun onChannelVideoSelected(videoUrl: String?, program: Programme?) {
        _selectedVideoUrl.value = videoUrl
        //program?.let { addToRecentlyWatched(it) }
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
                    val epgList = fetchEPGList((context))
                    provideChannelFilterData(epgList=epgList)
                    trySend(epgList)
                }
            }
        }
        context.contentResolver.registerContentObserver(
            EPGContract.EPGEntry.CONTENT_URI,
            true,
            observer
        )
        // Initial load
        val epgList = fetchEPGList((context))
        provideChannelFilterData(epgList=epgList)
        trySend(epgList)

        awaitClose {
            context.contentResolver.unregisterContentObserver(observer)
        }
    }.flowOn(Dispatchers.IO)



    fun provideChannelFilterData(epgList:List<EPGDataItem>){
        _epgChannels.value = epgList.mapNotNull { epg ->
            epg.tv?.channel?.let { existingChannel ->
                existingChannel.copy(
                    videoUrl = epg.content?.videoUrl,
                    logoUrl = epg.content?.thumbnailUrl,
                    genreId = epg.content?.genreId?:""
                )
            }
        }
        _filteredChannels.value =  _epgChannels.value
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

    fun filterChannelsByGenre(genre: String) {
        if (genre.equals("All", ignoreCase = true)) {
            _filteredEPGList.value = epgDataFlow.value
        } else {
            _filteredEPGList.value = epgDataFlow.value.filter { epgItem ->
                Log.d("SharedViewModel", "Genres: ${epgItem.content?.genre}")
                (epgItem.content?.genre?.any { (it as? String)?.equals(genre, ignoreCase = true) == true } ?: false)

            }
        }
    }
    fun filterChannelsByLanguage(language: String) {
        if (language.equals("All", ignoreCase = true)) {
            _filteredEPGList.value = epgDataFlow.value
        } else {
            _filteredEPGList.value = epgDataFlow.value.filter { epgItem ->
                Log.d("SharedViewModel", "Languages: ${epgItem.content?.language}")
                epgItem.content?.language?.equals(language, ignoreCase = true) ?: false
            }
        }
    }





}
