package com.example.tvapp.repository
import android.util.Log
import com.example.tvapp.database.EPGDao
import com.example.tvapp.models.ChannelWithPrograms
import com.example.tvapp.models.EPGChannel
import com.example.tvapp.models.EPGProgram
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
class EPGRepository @Inject constructor(
    private val dao: EPGDao
) {

    suspend fun insertAll(channels: EPGChannel, programs: List<EPGProgram>) {
        Log.d("AVNI", "insertAll: Inserting ${programs.size} programs for channel ${channels.id}")
        dao.insertChannels(channels)
        dao.insertPrograms(programs)
        val insertedChannels = dao.getAllChannelsWithProgramsSync()
        Log.d("AVNI", "After insertion, channels with programs: $insertedChannels")
    }

    fun getAllChannels(): Flow<List<EPGChannel>> = dao.getAllChannels()

    fun getProgramsForNextHours(startTime: Long, endTime: Long): Flow<List<EPGProgram>> {
        return dao.getProgramsForNextHours(startTime, endTime)
    }

}




