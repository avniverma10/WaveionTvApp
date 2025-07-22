package com.caastv.tvapp.viewmodels

/*

@HiltViewModel
class EPGViewModel @Inject constructor(
    private val wtvDatabase: WTVDatabase,
    private val wtvNetworkRepositoryImpl: WTVNetworkRepositoryImpl,
    private val wtvCommonRepositoryImpl: WTVCommonRepositoryImpl,
    private val application: Application
) : WTVViewModel(wtvDatabase=wtvDatabase,networkApiCallInterfaceImpl=wtvNetworkRepositoryImpl) {

    private val _epgChannels = MutableStateFlow<List<EPGData>>(emptyList())
    val epgChannels: StateFlow<List<EPGData>> = _epgChannels.asStateFlow()

    private val _filteredChannels = MutableStateFlow<List<Channel>>(emptyList())
    val filteredChannels: StateFlow<List<Channel>> = _filteredChannels.asStateFlow()

    */
/*val tabs: StateFlow<List<Tab>> = tab.streamTabs().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )*//*


    private val _filteredPrograms = MutableStateFlow<List<Programme>>(emptyList())
    val filteredPrograms: StateFlow<List<Programme>> = _filteredPrograms.asStateFlow()

    private val _selectedGenre = MutableStateFlow<String?>("ALL")
    val selectedGenre: StateFlow<String?> = _selectedGenre.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Programme>>(emptyList())
    val searchResults: StateFlow<List<Programme>> = _searchResults

    // New state for banner list
    private val _bannerList = MutableStateFlow<List<Banner>>(emptyList())
    val bannerList: StateFlow<List<Banner>> = _bannerList.asStateFlow()

    // State to trigger video playback for a particular channel.
    private val _selectedVideoUrl = MutableStateFlow<String?>(null)
    val selectedVideoUrl: StateFlow<String?> = _selectedVideoUrl.asStateFlow()

    //state for next program in the overlay of the player
    private val _nextProgram = MutableStateFlow<Programme?>(null)
    val nextProgram: StateFlow<Programme?> = _nextProgram

    private val _recentlyWatched = MutableStateFlow<List<Programme>>(emptyList())
    val recentlyWatched: StateFlow<List<Programme>> = _recentlyWatched.asStateFlow()


    fun addToRecentlyWatched(program: Programme) {
        viewModelScope.launch {
            val currentList = _recentlyWatched.value.toMutableList()

            // Remove duplicates (only keep the latest entry for the same program)
            currentList.removeAll { it._channel == program._channel && it.title == program.title }

            // Add the new program to the front
            currentList.add(0, program)

            // Limit list to 10 most recent
            _recentlyWatched.value = currentList.take(10)
        }
    }
    // ================= Wishlist Integration =================

    // Wishlist state flows for popup and alert.
    private val _wishlistPopupProgram = MutableStateFlow<Programme?>(null)
    val wishlistPopupProgram: StateFlow<Programme?> = _wishlistPopupProgram.asStateFlow()

    private val _wishlistAlertProgram = MutableStateFlow<Programme?>(null)
    val wishlistAlertProgram: StateFlow<Programme?> = _wishlistAlertProgram.asStateFlow()

    // Internal wishlist list.
    private val _wishlist = MutableStateFlow<List<Programme>>(emptyList())
    val wishlist: StateFlow<List<Programme>> = _wishlist.asStateFlow()

    // Called when a future event is clicked to show the popup.
    fun onShowWishlistPopup(program: Programme) {
        _wishlistPopupProgram.value = program
    }

    // Clear the wishlist popup.
    fun clearWishlistPopup() {
        _wishlistPopupProgram.value = null
    }

    // Add an event to the wishlist and clear the popup.
    fun addToWishlist(program: Programme) {
        _wishlist.value += program
        clearWishlistPopup()
    }

    // Clear the wishlist alert popup.
    fun clearWishlistAlert() {
        _wishlistAlertProgram.value = null
    }



    init {
        loadEPGData()

        // Fetch banners from API
        viewModelScope.launch {
            wtvNetworkRepositoryImpl.getBanners("http://nextwave.waveiontechnologies.com:5000/api/banner").collect{bannerData->
                try {
                    _bannerList.value = bannerData
                } catch (e: Exception) {
                    loge("Error", "Error fetching banners")
                }
            }
        }

    }

    private fun loadEPGData() {
        viewModelScope.launch {
            wtvDatabase.provideEPGDataDao().getEPGData()?.let {
                val epgData = it.map { it.toEPGData()}
                val channels = epgData.map {it.tv?.channel?:Channel() }
                val programs = epgData.map { it.tv?.programme?:Programme() }
                _epgChannels.value = epgData
                _filteredChannels.value = channels
                _filteredPrograms.value = programs as  List<Programme>
            }
        }
    }

    fun filterChannelsByGenre(genre: String) {
        viewModelScope.launch {
            _selectedGenre.value = genre

            if (genre == "ALL") {
                _filteredChannels.value = _epgChannels.value
                _filteredPrograms.value = wtvCommonRepositoryImpl.getAllEPGPrograms().first()
            } else {
                val filteredList = _epgChannels.value.filter {
                    it.genreId.equals(
                        genre,
                        ignoreCase = true
                    )
                } // Filter by genreId
                _filteredChannels.value = filteredList

                val allPrograms = wtvCommonRepositoryImpl.getAllEPGPrograms().first()
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
                            val programStartMillis = program.startTime.provideTimeInMillis()
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
                    wtvCommonRepositoryImpl.getProgramsForNextHours(currentTime, endTime).collect { programs ->
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
                _searchResults.value = wtvCommonRepositoryImpl.getProgramsByName(query)
            }
        }

        fun fetchNextProgram(channelId: String, currentTime: String) {
            viewModelScope.launch {
                _nextProgram.value = wtvCommonRepositoryImpl.getNextProgram(channelId, currentTime)
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
*/
