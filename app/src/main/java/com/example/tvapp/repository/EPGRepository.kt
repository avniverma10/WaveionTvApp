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
//
//class EPGRepository @Inject constructor(
//    private val dao: EPGDao
//) {
//
//
//    suspend fun insertAll(channels: EPGChannel, programs: List<EPGProgram>) {
//        Log.d("AVNIV", "insertAll: Inside repository - Inserting ${programs.size} programs for channel ${channels.id}")
//        dao.insertChannels(channels)
//        dao.insertPrograms(programs)
//    }
//
//    fun getAllChannels(): Flow<List<EPGChannel>> = dao.getAllChannels()
//
////    fun getChannelWithPrograms(channelId: String): Flow<ChannelWithPrograms> =
////        dao.getChannelWithPrograms(channelId)
////            .catch { emit(ChannelWithPrograms(EPGChannel("", ""), emptyList())) }  // Provide fallback if error occurs
//
//
//    fun getChannelWithPrograms(channelId: String): Flow<ChannelWithPrograms> =
//        dao.getChannelWithPrograms(channelId)
//            .onStart { Log.d("EPGRepository", "Fetching programs for channel $channelId") }
//            .catch { e ->
//                Log.e("EPGRepository", "Error fetching programs", e)
//                emit(ChannelWithPrograms(EPGChannel("", ""), emptyList()))
//            }
//
//}
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

//    fun getAllChannelsWithPrograms(): Flow<List<ChannelWithPrograms>> = dao.getAllChannelsWithPrograms()
//
//    fun getChannelWithPrograms(channelId: String): Flow<ChannelWithPrograms> =
//        dao.getChannelWithPrograms(channelId)
//
//    fun getAllPrograms(): Flow<List<EPGProgram>> = dao.getAllPrograms()  // ✅ New function to get all programs
//
//    suspend fun getFilteredProgramsByChannelAndDate(channelId: String, date: String): List<EPGProgram> {
//        val allPrograms = dao.getAllPrograms().first() // Fetch all programs synchronously
//        val filteredPrograms = allPrograms.filter { it.channelId == channelId && it.date == date }
//
//        Log.d("AVNI", "Filtered Programs for $channelId on $date: $filteredPrograms")
//        return filteredPrograms
    }




