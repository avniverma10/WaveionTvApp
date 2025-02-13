package com.example.tvapp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.Log
import com.example.tvapp.XMLParser
import com.example.tvapp.database.EPGEntity
import com.example.tvapp.repository.EPGRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EPGViewModel @Inject constructor(
    private val repository: EPGRepository,
    application: Application
) : AndroidViewModel(application) {

    val epgList: StateFlow<List<EPGEntity>> = repository.getEPGData().stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        emptyList()
    )

    init {
            viewModelScope.launch {
                repository.getEPGData().collect { epgList ->
                    if (epgList.isEmpty()) {   // Step 1: Check if DB is empty
//                        Log.i("AVNI", "Database is empty. Parsing XML and inserting data...")
                        val programs = XMLParser.readEPGFromAssets(application.applicationContext)  // Step 2: Parse XML
                        repository.insertAll(programs)  // Step 3: Save to DB
                        checkAndLogDatabase()  // Step 4: Log Database Events
                    } else {
//                        Log.i("AVNI", "Database already contains ${epgList.size} records. Skipping XML parsing.")
                        checkAndLogDatabase()  // Step 4: Log Existing Data
                    }
                }
            }
        }

        private suspend fun checkAndLogDatabase() {
            repository.getEventsByDate("2024/12/18").collect { events ->
                if (events.isNullOrEmpty()) {
//                    Log.i("AVNI", "No events found in the database for the specified date.")
                } else {
                    events.forEach { event ->
//                        Log.i("AVNI", "Database event: ${event.eventName}")
                    }
                }
            }
        }
    }





