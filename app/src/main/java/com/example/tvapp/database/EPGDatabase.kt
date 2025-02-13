package com.example.tvapp.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(entities = [EPGEntity::class], version = 1,exportSchema = false)
abstract class EPGDatabase : RoomDatabase() {
    abstract fun epgDao(): EPGDao
}
