package com.panmetro.iptv.viewmodels.player


import android.app.Application
import androidx.lifecycle.ViewModel
import com.panmetro.iptv.extensions.coreEPGLiveData
import com.panmetro.iptv.extensions.loge
import com.panmetro.iptv.model.data.epgdata.EPGDataItem
import com.panmetro.iptv.model.data.epgdata.Programme
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


    // Should be StateFlow or SharedFlow for Compose
    val selectedEPG = MutableStateFlow<EPGDataItem?>(null)
    val selectedProgram = MutableStateFlow<Programme?>(null)
    val selectedTimeLeft = MutableStateFlow(0)

    // Should be StateFlow or SharedFlow for Compose
    val currentEPG = MutableStateFlow<EPGDataItem?>(null)
    val currentProgram = MutableStateFlow<Programme?>(null)
    val timeLeft = MutableStateFlow(0)

    // Example update function
    fun updateProgramInfo(selectedChannel: EPGDataItem?, program: Programme?, remainingTime: Int) {
        loge("PlayerViewModel", "Updating program: $program, time: $remainingTime")
        currentEPG.value = selectedChannel
        currentProgram.value = program
        timeLeft.value = remainingTime
    }
    // Example update function
    fun updateSelectedProgramInfo(isSelected: Boolean) {
        if(isSelected){
            loge("PlayerViewModel", "Updating program: ${currentProgram.value}, time: ${timeLeft.value}")
            selectedEPG.value = currentEPG.value
            selectedProgram.value = currentProgram.value
            selectedTimeLeft.value = timeLeft.value
        }
    }


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


    private var _channel = MutableStateFlow<EPGDataItem?>(null)
    val channel: StateFlow<EPGDataItem?> = _channel.asStateFlow()

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
            .take(3)
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


    override fun onCleared() {
        super.onCleared()
    }
}
