package com.example.tvapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EPGDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<EPGEntity>)

    @Query("SELECT * FROM epg_events WHERE date = :date ORDER BY startTime")
    fun getEventsByDate(date: String): Flow<List<EPGEntity>>

    @Query("SELECT * FROM epg_events ORDER BY startTime ASC")
    fun getAllEPG(): Flow<List<EPGEntity>>

}
