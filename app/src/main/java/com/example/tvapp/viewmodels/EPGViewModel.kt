package com.example.tvapp.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tvapp.XMLParser
import com.example.tvapp.models.EPGChannel
import com.example.tvapp.models.EPGProgram
import com.example.tvapp.repository.EPGRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class EPGViewModel @Inject constructor(
    private val repository: EPGRepository,
    application: Application
) : AndroidViewModel(application) {

    val epgChannels: StateFlow<List<EPGChannel>> = repository.getAllChannels().stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        emptyList()
    )

    private val _filteredPrograms = MutableStateFlow<List<EPGProgram>>(emptyList())
    val filteredPrograms: StateFlow<List<EPGProgram>> = _filteredPrograms.asStateFlow()

    // State flow for the logo URL.
    private val _logoUrl = MutableStateFlow<String?>(null)
    val logoUrl: StateFlow<String?> = _logoUrl.asStateFlow()

    private val _epgFileVideoUrl = MutableStateFlow<String?>(null)
    val epgFileVideoUrl: StateFlow<String?> = _epgFileVideoUrl

    private val _title = MutableStateFlow<String?>(null)
    val title: StateFlow<String?> = _title


    init {
        viewModelScope.launch {
            try {
                val channelProgramPairs = XMLParser.readEPGsFromAssetsFolder(application.applicationContext)
                channelProgramPairs.forEach { (channel, programs) ->
                    repository.insertAll(channel, programs)
                }
                filterProgramsByTime_test()
            } catch (e: Exception) {
                Log.e("RISHI", "Error loading data", e)
            }
        }
        //Launch a coroutine to fetch the logo URL from the API.
        viewModelScope.launch {
            val url = repository.fetchLogoUrl()
            _logoUrl.value = url
        }
        viewModelScope.launch {
            val videoUrl = repository.fetchVideoUrl()
            _epgFileVideoUrl.value = videoUrl
        }
        viewModelScope.launch {
            val title = repository.fetchTitle()
            _title.value = title
        }

    }


    private fun filterProgramsByTime_test() {
        // TODO: RISHI Fixed current time for testing
        val currentTime = 20250205010000L
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentTime
            add(Calendar.HOUR, 4)
        }
        val endTime = calendar.timeInMillis

        viewModelScope.launch {
            repository.getProgramsForNextHours(currentTime, endTime).collect { programs ->
                _filteredPrograms.value = programs
                Log.d("RISHI", "Programs fetched from fixed current time to next 4 hours: ${programs.size}")
                programs.forEach { program ->
                    Log.d("RISHI", "Channel: ${program.channelId} Program: ${program.eventName}, Start Time: ${program.startTime}, End Time__???: ${program.endTime}")
                }
            }
        }
    }






}
