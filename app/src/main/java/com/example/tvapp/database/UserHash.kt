package com.example.tvapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_hashes")
data class UserHash(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val phoneNumber: String?,
    val deviceId: String,
    val watermarkHash: String
)
