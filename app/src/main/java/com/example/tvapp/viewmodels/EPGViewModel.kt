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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
//
//@HiltViewModel
//class EPGViewModel @Inject constructor(
//    private val repository: EPGRepository,
//    application: Application
//) : AndroidViewModel(application) {
//
//    val epgChannels: StateFlow<List<EPGChannel>> = repository.getAllChannels().stateIn(
//        viewModelScope,
//        SharingStarted.Lazily,
//        emptyList()
//    )
//
//
//
//    init {
//        viewModelScope.launch {
//            Log.d("AVNI","Inside vm scope")
//            repository.getAllChannels().collect { channelList ->
//
//                    val (channels, programs) = XMLParser.readEPGFromAssets(application.applicationContext)
//                    Log.d("AVNI", "Parsed ${channels.id} channels and ${programs.size} programs")
//                    repository.insertAll(channels, programs)
//
//            }
//        }
//    }
//
////    fun getProgramsForChannel(channelId: String): Flow<ChannelWithPrograms> =
////        repository.getChannelWithPrograms(channelId)
////fun getProgramsForChannel(channelId: String): Flow<ChannelWithPrograms> =
////    repository.getChannelWithPrograms(channelId)
////        .catch { emit(ChannelWithPrograms(EPGChannel("", ""), emptyList())) }  // Emit empty list in case of error
////        .onStart { emit(ChannelWithPrograms(EPGChannel("", ""), emptyList())) } // Emit default value when flow starts
//
//
//
//    fun getProgramsForChannel(channelId: String): Flow<ChannelWithPrograms> =
//        repository.getChannelWithPrograms(channelId)
//            .distinctUntilChanged()  // ✅ Prevents stale data from Room
//            .catch { emit(ChannelWithPrograms(EPGChannel(channelId, ""), emptyList())) }
//
//}
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
                Log.d("AVNI", "Parsed XML: ${channels.id}, Programs: ${programs.size}")
                repository.insertAll(channels, programs)
            } catch (e: Exception) {
                Log.e("AVNI", "Error loading data", e)
            }
        }
    }

//    fun filterProgramsByChannelAndDate(channelId: String, date: String) {
//        viewModelScope.launch {
//            val filteredPrograms = repository.getFilteredProgramsByChannelAndDate(channelId, date)
//            _filteredPrograms.value = filteredPrograms
//        }
//    }
//
//    fun getProgramsForChannel(channelId: String): Flow<ChannelWithPrograms> =
//        repository.getChannelWithPrograms(channelId)
//            .distinctUntilChanged()
//            .catch { emit(ChannelWithPrograms(EPGChannel(channelId, ""), emptyList())) }
}

