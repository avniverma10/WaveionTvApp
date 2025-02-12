package com.example.tvapp.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(entities = [EPGEntity::class], version = 1)
abstract class EPGDatabase : RoomDatabase() {
    abstract fun epgDao(): EPGDao

    companion object {
        @Volatile private var INSTANCE: EPGDatabase? = null

        fun getDatabase(context: Context): EPGDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EPGDatabase::class.java,
                    "epg_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
