package com.example.tvapp.viewmodels


import android.app.Application
import android.content.ContentValues
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tvapp.extensions.appManifestLiveData
import com.example.tvapp.extensions.applyAppGenre
import com.example.tvapp.extensions.applyAppHome
import com.example.tvapp.extensions.applyAppLanguage
import com.example.tvapp.extensions.applyAppManifest
import com.example.tvapp.extensions.logReport
import com.example.tvapp.utils.sealed.WTVListResponse
import com.example.tvapp.utils.sealed.WTVResponse
import com.example.tvapp.model.wtvdatabase.EPGContract
import com.example.tvapp.model.data.epgdata.EPGDataItem
import com.example.tvapp.model.data.genre.WTVGenre
import com.example.tvapp.model.data.language.WTVLanguage
import com.example.tvapp.model.data.sse.TabItem
import com.example.tvapp.model.home.WTVHomeCategory
import com.example.tvapp.model.repository.WTVNetworkRepositoryImpl
import com.example.tvapp.utils.Constants
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
open class WTVViewModel @Inject constructor(private val application: Application,private val networkApiCallInterfaceImpl: WTVNetworkRepositoryImpl) : AndroidViewModel(application) {
    fun provideApplicationContext() = application.applicationContext
    private var _isInitializeData = MutableStateFlow<Boolean>(false)
    val isInitializeData: StateFlow<Boolean> get() = _isInitializeData
    private var _isProgress = MutableStateFlow<Boolean>(false)
    val provideIsProgress: StateFlow<Boolean> get() = _isProgress
    private var _selectedChannel = MutableStateFlow<EPGDataItem>(EPGDataItem())
    val selectedChannel: StateFlow<EPGDataItem> = _selectedChannel.asStateFlow()
    private val _availableGenre = MutableStateFlow<List<WTVGenre>>(emptyList())
    val availableGenre: StateFlow<List<WTVGenre>> = _availableGenre.asStateFlow()



    // Expose the latest list of TabItems
    private val _tabItemsFlow = MutableStateFlow<List<TabItem>>(emptyList())
    val tabItemsFlow: StateFlow<List<TabItem>> = _tabItemsFlow
    // Flag to ensure we start the SSE connection only once.
    private var startedSSE = false


    fun updateEPGData(epgList: List<EPGDataItem>) {
        viewModelScope.launch {
            saveEPGList(application, epgList)
        }
    }

    private val _errorLoadingData = MutableStateFlow<String?>(null)
    val errorLoadingData: StateFlow<String?> = _errorLoadingData

    init {
        initAppRequiredData()
        // Start the SSE connection globally.
     //   startSSE()
    }

    fun initAppRequiredData(){
        viewModelScope.launch {
           // tvManifest.value = WTVManifest()
            // Launch API calls in parallel
            networkApiCallInterfaceImpl.provideWTVManifest(manifestUrl = "https://nextwave.waveiontechnologies.com:5000/api/manifest").collect{ response ->
                when (response) {
                    is WTVResponse.Success -> {
                        application.applicationContext.applyAppManifest(response.data)
                        Constants.manifest = response.data
                        logReport("ManifestResponse:${ response.data}")
                    }
                    is WTVResponse.Failure -> {
                        // Handle error state
                       // _errorLoadingData.value = response.error.message
                        logReport("ManifestResponse:${ response.error.message}")

                    }
                }
            }
        }
        viewModelScope.launch {
            networkApiCallInterfaceImpl.provideWTVEPGData(epgContentUrl = "https://nextwave.waveiontechnologies.com:5000/api/epg-files/join-epg-content").collect{response ->
                when (response) {
                    is WTVListResponse.Success -> {
                        // Handle successful response
                        updateEPGData(response.data)
                        Constants.epgItemList = response.data
                        response.data.find { it.channelId == provideApplicationContext().appManifestLiveData().value?.landingChannel?.ChannelID }?.let {
                            updateSelectedChannel(it)
                        }
                        logReport("EPGResponse:${ response.data}")

                    }
                    is WTVListResponse.Failure -> {
                        // Handle error state
                        // _errorLoadingData.value = response.error.message
                        logReport("EPGResponse:${ response.error.message}")

                    }
                }
            }
            _isInitializeData.value = true
        }
        viewModelScope.launch {
            networkApiCallInterfaceImpl.provideWTVGenreData(genreUrl = "https://nextwave.waveiontechnologies.com:5000/api/genres/").collect{response ->
                when (response) {
                    is WTVListResponse.Success -> {
                        var genreList = arrayListOf<WTVGenre>()
                        genreList.add(WTVGenre(name = "All"))
                        genreList.addAll(response.data)
                        _availableGenre.value = genreList
                        Constants.genreList = genreList
                        // Handle successful response
                        application.applicationContext.applyAppGenre(genreList)
                        logReport("applyAppGenre:${ response.data}")

                    }
                    is WTVListResponse.Failure -> {
                        // Handle error state
                        // _errorLoadingData.value = response.error.message
                        logReport("applyAppGenre:${ response.error.message}")

                    }
                }
            }
            _isInitializeData.value = true
        }
        viewModelScope.launch {
            networkApiCallInterfaceImpl.provideWTVLanguageData(languageUrl = "https://nextwave.waveiontechnologies.com:5000/api/languages/").collect{response ->
                when (response) {
                    is WTVListResponse.Success -> {
                        var languageList = arrayListOf<WTVLanguage>()
                        languageList.add(WTVLanguage(name = "All"))
                        languageList.addAll(response.data)
                        // Handle successful response
                        Constants.languageList = languageList
                        application.applicationContext.applyAppLanguage(response.data)
                        logReport("applyAppLanguage:${ response.data}")

                    }
                    is WTVListResponse.Failure -> {
                        // Handle error state
                        // _errorLoadingData.value = response.error.message
                        logReport("applyAppLanguage:${ response.error.message}")

                    }
                }
            }
            _isInitializeData.value = true
        }
        viewModelScope.launch {
            networkApiCallInterfaceImpl.provideWTVHomeData(homeUrl = "https://nextwave.waveiontechnologies.com:5000/api/homescreenCategory").collect{response ->
                when (response) {
                    is WTVListResponse.Success -> {
                        // Handle successful response
                        application.applicationContext.applyAppHome(response.data)
                        logReport("applyAppLanguage:${ response.data}")

                    }
                    is WTVListResponse.Failure -> {
                        // Handle error state
                        // _errorLoadingData.value = response.error.message
                        logReport("applyAppLanguage:${ response.error.message}")

                    }
                }
            }
            _isInitializeData.value = true
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
                context.contentResolver.insert(EPGContract.EPGEntry.CONTENT_URI, values)
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

}
