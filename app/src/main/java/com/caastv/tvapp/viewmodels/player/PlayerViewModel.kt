package com.caastv.tvapp.viewmodels.player


import android.app.Application
import androidx.lifecycle.ViewModel
import com.caastv.tvapp.extensions.coreEPGLiveData
import com.caastv.tvapp.model.data.epgdata.EPGDataItem
import com.caastv.tvapp.model.data.epgdata.Programme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
open class PlayerViewModel @Inject constructor(
    private val application: Application,
) : ViewModel(){


    fun provideAvailableEPG() = application.coreEPGLiveData().value?: arrayListOf()


    /**
     * Emits System.currentTimeMillis() immediately, then once every [intervalMillis].
     */

    fun timestampFlow(intervalMillis: Long = 60_000L): Flow<Long> = flow {
        // emit right away
        emit(System.currentTimeMillis())
        // then emit every interval
        while (true) {
            delay(intervalMillis)
            emit(System.currentTimeMillis())
        }
    }.distinctUntilChanged()


    private var _channel = MutableStateFlow<EPGDataItem>(EPGDataItem())
    val selectedPlayerChannel: StateFlow<EPGDataItem> = _channel.asStateFlow()



    private var _currentProgramMinutesLeft = MutableStateFlow<Int>(0)
    val currentProgramMinutesLeft: StateFlow<Int> = _currentProgramMinutesLeft.asStateFlow()


    fun updateSelectedPProgramInfo(selectedChannel:EPGDataItem){
        _channel.value =  selectedChannel
        selectedChannel.tv?.programme?.let { provideAvailablePrograms(it) }?.let {
            it.getOrNull(0)?.let { it1 -> updateCurrentRunningProgramTimings(it1) }
        }
    }




    fun provideAvailablePrograms(programs: List<Programme>):List<Programme>{
        val now = System.currentTimeMillis()
        val formatter = SimpleDateFormat("hh:mm a", Locale.US)
       return programs
            .filter { program ->
                val start = program.startTime
                val end   = program.endTime
                if (start == null || end == null) return@filter false
                (start <= now && now < end) || (now < start)
            }
            .distinctBy { it.startTime to it.endTime }
            .sortedBy { it.startTime }
//            .take(3)
            .map { program ->
                program.copy(
                    startFormatedTime = program.startTime
                        ?.let { formatter.format(it) }
                        ?: "--",
                    endFormatedTime = program.endTime
                        ?.let { formatter.format(it) }
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
