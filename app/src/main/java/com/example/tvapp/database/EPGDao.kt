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

}

