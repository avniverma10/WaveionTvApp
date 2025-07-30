package com.caastv.tvapp.model.wtvdatabase.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.caastv.tvapp.model.wtvdatabase.database.dao.AppDataDao
import com.caastv.tvapp.model.wtvdatabase.database.entity.AppDataEntity

@Database(entities = [AppDataEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDataDao(): AppDataDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "iptv_database"
                )
                    .fallbackToDestructiveMigration() // For simplicity in example
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}