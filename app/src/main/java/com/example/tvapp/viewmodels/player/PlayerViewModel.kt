package com.example.tvapp.viewmodels.player


import android.app.Application
import androidx.lifecycle.ViewModel
import com.example.tvapp.extensions.coreEPGLiveData
import com.example.tvapp.model.data.epgdata.EPGDataItem
import com.example.tvapp.model.data.epgdata.Programme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
open class PlayerViewModel @Inject constructor(
    private val application: Application,
) : ViewModel(){
    fun provideAvailableEPG() = application.coreEPGLiveData().value?: arrayListOf()


    private var _channel = MutableStateFlow<EPGDataItem>(EPGDataItem())
    val selectedPlayerChannel: StateFlow<EPGDataItem> = _channel.asStateFlow()


    private var _filterAvailablePrograms = MutableStateFlow<List<Programme>>(arrayListOf())
    val filterAvailablePrograms: StateFlow<List<Programme>> = _filterAvailablePrograms.asStateFlow()


    private var _currentProgramMinutesLeft = MutableStateFlow<Int>(0)
    val currentProgramMinutesLeft: StateFlow<Int> = _currentProgramMinutesLeft.asStateFlow()


    fun updateSelectedPProgramInfo(selectedChannel:EPGDataItem){
        _channel.value =  selectedChannel
        selectedChannel.tv?.programme?.let { provideAvailablePrograms(it) }?.let {
            it.getOrNull(0)?.let { it1 -> updateCurrentRunningProgramTimings(it1) }
        }
    }



    fun providePlayableProgramData(programs: List<Programme>){
        val now = System.currentTimeMillis()
        _filterAvailablePrograms.value =  programs
            .filter { program ->
                val start = program.startTime
                val end   = program.endTime
                // Only include if both times are non-null and end is strictly in the future:
                if (start == null || end == null) return@filter false
                // 1) Currently running: start <= now < end
                // 2) Upcoming: now < start
                (start <= now && now < end) || (now < start)
            }
            .sortedBy { it.startTime }
            .take(3)
            .map { program ->
                program.copy(
                    startFormatedTime = program.startTime
                        ?.let { SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it) }
                        ?: "--",
                    endFormatedTime = program.endTime
                        ?.let { SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it) }
                        ?: "--"
                )
            }
    }

    fun provideAvailablePrograms(programs: List<Programme>):List<Programme>{
        val now = System.currentTimeMillis()
       return programs
            .filter { program ->
                val start = program.startTime
                val end   = program.endTime
                // Only include if both times are non-null and end is strictly in the future:
                if (start == null || end == null) return@filter false
                // 1) Currently running: start <= now < end
                // 2) Upcoming: now < start
                (start <= now && now < end) || (now < start)
            }
            .sortedBy { it.startTime }
            .take(3)
            .map { program ->
                program.copy(
                    startFormatedTime = program.startTime
                        ?.let { SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it) }
                        ?: "--",
                    endFormatedTime = program.endTime
                        ?.let { SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it) }
                        ?: "--"
                )
            }
    }

    fun updateCurrentRunningProgramTimings(currentProgram:Programme){
        val now = System.currentTimeMillis()
        val diff = currentProgram.endTime?.minus(now)
        (diff?.div(60000))?.toInt()?.let {
            _currentProgramMinutesLeft.value = it
        }
    }

    fun provideCurrentRunningProgramTimings(currentProgram:Programme?):Int{
        val now = System.currentTimeMillis()
        val diff = currentProgram?.endTime?.minus(now)
        return (diff?.div(60000))?.toInt()?:0
    }

    override fun onCleared() {
        super.onCleared()
    }
}
