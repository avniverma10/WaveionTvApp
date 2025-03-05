package com.example.tvapp.database
import androidx.room.Dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.tvapp.models.ChannelWithPrograms
import com.example.tvapp.models.EPGChannel
import com.example.tvapp.models.EPGProgram
import kotlinx.coroutines.flow.Flow

@Dao
interface EPGDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: EPGChannel)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPrograms(programs: List<EPGProgram>)

    @Query("SELECT * FROM epg_channels")
     fun getAllChannels(): Flow<List<EPGChannel>>


    // Synchronous version for filtering
    @Transaction
    @Query("SELECT * FROM epg_channels")
    suspend fun getAllChannelsWithProgramsSync(): List<ChannelWithPrograms>

    @Query("SELECT * FROM epg_programs WHERE startTime >= :startTime AND startTime <= :endTime")
    fun getProgramsForNextHours(startTime: Long, endTime: Long): Flow<List<EPGProgram>>

    // Check if a channel exists
    @Query("SELECT EXISTS(SELECT 1 FROM epg_channels WHERE id = :channelId)")
    suspend fun isChannelExists(channelId: String): Boolean


    @Query("SELECT * FROM epg_programs")
    fun getAllPrograms(): Flow<List<EPGProgram>>

    // Check if a specific program exists
    @Query("SELECT EXISTS(SELECT 1 FROM epg_programs WHERE channelId = :channelId AND startTime = :startTime AND endTime = :endTime)")
    suspend fun isProgramExists(channelId: String, startTime: String, endTime: String): Boolean

    @Query("SELECT * FROM epg_programs WHERE eventName LIKE :query")
    fun searchProgramsByName(query: String): Flow<List<EPGProgram>>

    @Query("""
    SELECT * FROM epg_programs 
    WHERE channelId = :channelId 
    AND startTime > :currentTime 
    ORDER BY startTime ASC 
    LIMIT 1
""")
    suspend fun getNextProgramForChannel(channelId: String, currentTime: String): EPGProgram?

//    @Query("UPDATE epg_programs SET watchedAt = :timestamp WHERE id = :programId")
//    suspend fun updateWatchedProgram(programId: String, timestamp: Long) // Mark as watched
//
//    @Query("SELECT * FROM epg_programs WHERE watchedAt IS NOT NULL ORDER BY watchedAt DESC LIMIT 10")
//    fun getRecentlyWatchedPrograms(): Flow<List<EPGProgram>> // Get last 10 watched

}

