package com.example.tvapp.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tvapp.XMLParser
import com.example.tvapp.models.ChannelWithPrograms
import com.example.tvapp.models.EPGChannel
import com.example.tvapp.models.EPGProgram
import com.example.tvapp.repository.EPGRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.system.measureTimeMillis

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
    private val _searchResults = MutableStateFlow<List<EPGProgram>>(emptyList())
    val searchResults: StateFlow<List<EPGProgram>> = _searchResults
    val filteredPrograms: StateFlow<List<EPGProgram>> = _filteredPrograms.asStateFlow()
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

}
