package com.example.tvapp.database
import androidx.room.Database

import androidx.room.RoomDatabase
import com.example.tvapp.models.EPGChannel
import com.example.tvapp.models.EPGProgram


@Database(entities = [EPGChannel::class, EPGProgram::class, EPGEntity::class], version = 7, exportSchema = false)
abstract class EPGDatabase : RoomDatabase() {
    abstract fun epgDao(): EPGDao
}
