package com.example.tvapp.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tvapp.models.Banner
import com.example.tvapp.models.EPGChannel
import com.example.tvapp.models.EPGProgram
import com.example.tvapp.repository.EPGRepository
import com.example.tvapp.api.ApiServiceForData
import com.example.tvapp.models.Tab
import com.example.tvapp.repository.TabsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltViewModel
class EPGViewModel @Inject constructor(
    private val repository: EPGRepository,
    private val apiService: ApiServiceForData,
    tab : TabsRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _epgChannels = MutableStateFlow<List<EPGChannel>>(emptyList())
    val epgChannels: StateFlow<List<EPGChannel>> = _epgChannels.asStateFlow()

    private val _filteredChannels = MutableStateFlow<List<EPGChannel>>(emptyList())
    val filteredChannels: StateFlow<List<EPGChannel>> = _filteredChannels.asStateFlow()

    val tabs: StateFlow<List<Tab>> = tab.streamTabs().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

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

    private val _recentlyWatched = MutableStateFlow<List<EPGProgram>>(emptyList())
    val recentlyWatched: StateFlow<List<EPGProgram>> = _recentlyWatched.asStateFlow()


    fun addToRecentlyWatched(program: EPGProgram) {
        viewModelScope.launch {
            val currentList = _recentlyWatched.value.toMutableList()

            // Remove duplicates (only keep the latest entry for the same program)
            currentList.removeAll { it.channelId == program.channelId && it.eventName == program.eventName }

            // Add the new program to the front
            currentList.add(0, program)

            // Limit list to 10 most recent
            _recentlyWatched.value = currentList.take(10)
        }
    }
    // ================= Wishlist Integration =================

    // Wishlist state flows for popup and alert.
    private val _wishlistPopupProgram = MutableStateFlow<EPGProgram?>(null)
    val wishlistPopupProgram: StateFlow<EPGProgram?> = _wishlistPopupProgram.asStateFlow()

    private val _wishlistAlertProgram = MutableStateFlow<EPGProgram?>(null)
    val wishlistAlertProgram: StateFlow<EPGProgram?> = _wishlistAlertProgram.asStateFlow()

    // Internal wishlist list.
    private val _wishlist = MutableStateFlow<List<EPGProgram>>(emptyList())
    val wishlist: StateFlow<List<EPGProgram>> = _wishlist.asStateFlow()

    // Called when a future event is clicked to show the popup.
    fun onShowWishlistPopup(program: EPGProgram) {
        _wishlistPopupProgram.value = program
    }

    // Clear the wishlist popup.
    fun clearWishlistPopup() {
        _wishlistPopupProgram.value = null
    }

    // Add an event to the wishlist and clear the popup.
    fun addToWishlist(program: EPGProgram) {
        _wishlist.value = _wishlist.value + program
        clearWishlistPopup()
    }

    // Clear the wishlist alert popup.
    fun clearWishlistAlert() {
        _wishlistAlertProgram.value = null
    }

    // Helper function to convert program time string to milliseconds.
    private fun getTimeInMillis(timeStr: String): Long {
        return try {
            val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss Z")
            val zdt = java.time.ZonedDateTime.parse(timeStr, formatter)
            zdt.toInstant().toEpochMilli()
        } catch (e: Exception) {
            try {
                val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                val zdt = java.time.ZonedDateTime.parse("$timeStr +0000", java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss Z"))
                zdt.toInstant().toEpochMilli()
            } catch (ex: Exception) {
                System.currentTimeMillis()
            }
        }
    }
    // ========================================================

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

    }

    private fun loadEPGData() {
        viewModelScope.launch {

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
                // Wishlist check: Periodically check if any wishlist event is due.
                viewModelScope.launch {
                    while (true) {
                        kotlinx.coroutines.delay(1000)
                        val currentTime = System.currentTimeMillis()
                        _wishlist.value.forEach { program ->
                            val programStartMillis = getTimeInMillis(program.startTime)
                            // If current time is within 1 minute of the program start, trigger the alert.
                            if (currentTime in programStartMillis until (programStartMillis + 60 * 1000)) {
                                _wishlistAlertProgram.value = program
                                // Remove the program from wishlist once alerted.
                                _wishlist.value = _wishlist.value.filter { it != program }
                            }
                        }
                    }
                }
            }
        }
    }

        fun filterProgramsByTime_test() {
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
                        Log.d(
                            "RISHI",
                            "Programs fetched from fixed current time to next 4 hours: ${programs.size}"
                        )
                        programs.forEach { program ->
                            Log.d(
                                "RISHI",
                                "Channel: ${program.channelId} Program: ${program.eventName}, Start Time: ${program.startTime}, End Time: ${program.endTime}"
                            )
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

        suspend fun fetchBanners() {
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

        fun onChannelVideoSelected(videoUrl: String?, program: EPGProgram?) {
            _selectedVideoUrl.value = videoUrl
            program?.let { addToRecentlyWatched(it) }
        }

        fun showRecentlyWatched() {
            viewModelScope.launch {
                _filteredPrograms.value = _recentlyWatched.value // Show only recently watched
                val recentChannelIds = _recentlyWatched.value.map { it.channelId }.distinct()
                _filteredChannels.value =
                    _epgChannels.value.filter { recentChannelIds.contains(it.id) }
            }
        }
    }
