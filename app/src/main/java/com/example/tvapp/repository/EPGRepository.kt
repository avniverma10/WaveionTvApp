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
        if (!dao.isChannelExists(channels.id)) {
            Log.d("RISHI", "insertAll: Inserting ${programs.size} programs for channel ${channels.id}")
            dao.insertChannels(channels)
            // TODO: RISHI no need to check "isProgramExists" on every insert, it will slow the process.
            programs.forEach { program ->
                if (!dao.isProgramExists(program.channelId, program.startTime, program.endTime)) {
                    dao.insertPrograms(programs)
                    val insertedChannels = dao.getAllChannelsWithProgramsSync()
                    Log.d("RISHI", "After insertion, channels with programs: $insertedChannels")
                }
            }
        }else{
            Log.i("RISHI", "Skip this due to this channel is already present in DB. ${channels.id} ")
        }
    }

    fun getAllChannels(): Flow<List<EPGChannel>> = dao.getAllChannels()

    fun getProgramsForNextHours(startTime: Long, endTime: Long): Flow<List<EPGProgram>> {
        return dao.getProgramsForNextHours(startTime, endTime)
    }

}




