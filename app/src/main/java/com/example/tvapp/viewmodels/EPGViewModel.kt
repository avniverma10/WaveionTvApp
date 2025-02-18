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
    val filteredPrograms: StateFlow<List<EPGProgram>> = _filteredPrograms

    init {
        viewModelScope.launch {
            try {
                val (channels, programs) = XMLParser.readEPGFromAssets(application.applicationContext)
                repository.insertAll(channels, programs)
                //filterProgramsByTime()
                //filterProgramsBySpecificTime(1738693800000, 1738764000000)
                filterProgramsByTime_test()
            } catch (e: Exception) {
                Log.e("RISHI", "Error loading data", e)
            }
        }
    }


    private fun filterProgramsBySpecificTime(startTime: Long, endTime: Long) {
        viewModelScope.launch {
            repository.getProgramsForNextHours(startTime, endTime).collect { programs ->
                _filteredPrograms.value = programs
                Log.d(
                    "RISHI",
                    "Fetched ${programs.size} programs for the specified time range."
                )
                programs.forEach { program ->
                    Log.d(
                        "RISHI",
                        "Program: ${program.eventName}, Starts at: ${program.startTime}, Ends at: ${program.endTime}"
                    )
                }
            }
        }
    }


    private fun filterProgramsByTime_test() {
        // Fixed current time for testing
        val currentTime = 1738693800000L
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentTime
            add(Calendar.HOUR, 4)  // Calculate time for 4 hours ahead from the fixed current time
        }
        val endTime = calendar.timeInMillis

        viewModelScope.launch {
            repository.getProgramsForNextHours(currentTime, endTime).collect { programs ->
                _filteredPrograms.value = programs
                Log.d("RISHI", "Programs fetched from fixed current time to next 4 hours: ${programs.size}")
                programs.forEach { program ->
                    Log.d("RISHI", "Program: ${program.eventName}, Start Time: ${program.startTime}, End Time__???: ${program.endTime}")
                }
            }
        }
    }


    private fun filterProgramsByTime() {
        val currentTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentTime
            add(Calendar.HOUR, 4)  // Add 4 hours to the current time
        }
        val endTime = calendar.timeInMillis

        viewModelScope.launch {
            repository.getProgramsForNextHours(currentTime, endTime).collect { programs ->
                _filteredPrograms.value = programs
                Log.d("AVNI", "Programs fetched for the next 4 hours: ${programs.size}")
            }
        }
    }
}
