package com.example.tvapp.repository
import android.util.Log
import com.example.tvapp.api.ApiServiceForData
import com.example.tvapp.database.EPGDao
import com.example.tvapp.models.EPGChannel
import com.example.tvapp.models.EPGProgram
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
class EPGRepository @Inject constructor(private val dao: EPGDao, private val apiService: ApiServiceForData) {

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

    //Function to fetch the logo URL from the API.
    suspend fun fetchLogoUrl(): String? {
        return try {
            val epgFiles = apiService.getEpgFiles()
            epgFiles.firstOrNull()?.thumbnailUrl
        } catch (e: Exception) {
            Log.e("logo", "Error fetching logo URL", e)
            null
        }
    }
    suspend fun fetchVideoUrl(): String? {
        return try {
            val epgFiles = apiService.getEpgFiles()
            epgFiles.firstOrNull()?.videoUrl
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    suspend fun fetchTitle(): String? {
        return try {
            val epgFiles = apiService.getEpgFiles()
            epgFiles.firstOrNull()?.title
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }




}




