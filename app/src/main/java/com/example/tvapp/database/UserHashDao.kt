package com.example.tvapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserHashDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserHash(userHash: UserHash)

    @Query("SELECT * FROM user_hashes WHERE watermarkHash = :hash LIMIT 1")
    suspend fun getUserByHash(hash: String): UserHash?
}
