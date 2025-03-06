package com.example.tvapp.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(entities = [UserHash::class], version = 1)
abstract class UserHashDatabase : RoomDatabase() {
    abstract fun userHashDao(): UserHashDao

    companion object {
        @Volatile
        private var INSTANCE: UserHashDatabase? = null

        fun getDatabase(context: Context): UserHashDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserHashDatabase::class.java,
                    "user_hash_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}