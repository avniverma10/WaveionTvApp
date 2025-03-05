package com.example.tvapp.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tvapp.XMLParser
import com.example.tvapp.models.Banner
import com.example.tvapp.models.EPGChannel
import com.example.tvapp.models.EPGProgram
import com.example.tvapp.repository.EPGRepository
import com.example.tvapp.api.ApiServiceForData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltViewModel
class EPGViewModel @Inject constructor(
    private val repository: EPGRepository,
    private val apiService: ApiServiceForData,
    application: Application
) : AndroidViewModel(application) {

//    val epgChannels: StateFlow<List<EPGChannel>> = repository.getAllChannels().stateIn(
//        viewModelScope,
//        SharingStarted.Lazily,
//        emptyList()
//    )

    private val _epgChannels = MutableStateFlow<List<EPGChannel>>(emptyList())
    val epgChannels: StateFlow<List<EPGChannel>> = _epgChannels.asStateFlow()

    private val _filteredChannels = MutableStateFlow<List<EPGChannel>>(emptyList())
    val filteredChannels: StateFlow<List<EPGChannel>> = _filteredChannels.asStateFlow()

    private val _filteredPrograms = MutableStateFlow<List<EPGProgram>>(emptyList())
    val filteredPrograms: StateFlow<List<EPGProgram>> = _filteredPrograms.asStateFlow()

    private val _selectedGenre = MutableStateFlow<String?>("ALL")
    val selectedGenre: StateFlow<String?> = _selectedGenre.asStateFlow()

    private val _searchResults = MutableStateFlow<List<EPGProgram>>(emptyList())
    val searchResults: StateFlow<List<EPGProgram>> = _searchResults


    // New state for banner list
    private val _bannerList = MutableStateFlow<List<Banner>>(emptyList())
    val bannerList: StateFlow<List<Banner>> = _bannerList.asStateFlow()

    // State to trigger video playback for a particular channel.
    private val _selectedVideoUrl = MutableStateFlow<String?>(null)
    val selectedVideoUrl: StateFlow<String?> = _selectedVideoUrl.asStateFlow()

    //state for next program in the overlay of the player
    private val _nextProgram = MutableStateFlow<EPGProgram?>(null)
    val nextProgram: StateFlow<EPGProgram?> = _nextProgram

//    private val _recentlyWatched = MutableStateFlow<List<EPGProgram>>(emptyList())
//    val recentlyWatched: StateFlow<List<EPGProgram>> = _recentlyWatched.asStateFlow()

//
//
//    private val _isRecentlyWatchedSelected = MutableStateFlow(false)
//    val isRecentlyWatchedSelected: StateFlow<Boolean> = _isRecentlyWatchedSelected.asStateFlow()



    init {
        viewModelScope.launch {
            try {
                repository.fetchAndStoreEPGsFromApi()
                loadEPGData()
                filterProgramsByTime_test()
            } catch (e: Exception) {
                Log.e("RISHI", "Error loading data", e)
            }
        }

        // Fetch banners from API
        viewModelScope.launch {
            fetchBanners()
        }
//        viewModelScope.launch {
//            repository.getRecentlyWatched().collect { programs ->
//                _recentlyWatched.value = programs
//            }
//        }
    }

//    fun markProgramAsWatched(programId: String) {
//        viewModelScope.launch {
//            repository.markProgramAsWatched(programId)
//            _recentlyWatched.value = repository.getRecentlyWatched().first() // ✅ Store watched program
//        }
//    }

//    fun toggleRecentlyWatched(selected: Boolean) {
//        _isRecentlyWatchedSelected.value = selected
//        if (selected) {
//            _filteredPrograms.value = _recentlyWatched.value // Show recently watched programs
//        }
//    }

    private fun loadEPGData() {
        viewModelScope.launch {
//            _isRecentlyWatchedSelected.value = false // Reset Recently Watched mode when filtering channels
            val channels = repository.getAllChannels().first()
            val programs = repository.getAllEPGPrograms().first()

            _epgChannels.value = channels
            _filteredChannels.value = channels
            _filteredPrograms.value = programs
        }
    }

    fun filterChannelsByGenre(genre: String) {
        viewModelScope.launch {
            _selectedGenre.value = genre

            if (genre == "ALL") {
                _filteredChannels.value = _epgChannels.value
                _filteredPrograms.value = repository.getAllEPGPrograms().first()
            } else {
                val filteredList = _epgChannels.value.filter {
                    it.genreId.equals(
                        genre,
                        ignoreCase = true
                    )
                } // Filter by genreId
                _filteredChannels.value = filteredList

                val allPrograms = repository.getAllEPGPrograms().first()
                val filteredProgramsList = allPrograms.filter { program ->
                    filteredList.any { it.id == program.channelId }
                }
                _filteredPrograms.value = filteredProgramsList
            }
        }
    }

    private fun filterProgramsByTime_test() {
        // TODO: RISHI Fixed current time for testing
        val currentTime = 20250212013600L
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentTime
            add(Calendar.HOUR, 4)
        }
        val endTime = calendar.timeInMillis

        viewModelScope.launch {
            val duration = measureTimeMillis {
                repository.getProgramsForNextHours(currentTime, endTime).collect { programs ->
                    _filteredPrograms.value = programs
                    Log.d("RISHI", "Programs fetched from fixed current time to next 4 hours: ${programs.size}")
                    programs.forEach { program ->
                        Log.d("RISHI", "Channel: ${program.channelId} Program: ${program.eventName}, Start Time: ${program.startTime}, End Time: ${program.endTime}")
                    }
                }
            }
            Log.d("RISHI", "Fetching and filtering programs took $duration ms")
        }
    }

    fun searchPrograms(query: String) {
        viewModelScope.launch {
            _searchResults.value = repository.getProgramsByName(query)
        }
    }

    private suspend fun fetchBanners() {
        try {
            val banners = apiService.getBanners()
            _bannerList.value = banners
        } catch (e: Exception) {
            Log.e("RISHI - Banners", "Error fetching banners", e)
        }
    }

    fun fetchNextProgram(channelId: String, currentTime: String) {
        viewModelScope.launch {
            _nextProgram.value = repository.getNextProgram(channelId, currentTime)
        }
    }
    fun onChannelVideoSelected(videoUrl: String?) {
        _selectedVideoUrl.value = videoUrl
    }
}
