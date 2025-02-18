package com.example.tvapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "epg_channels")
data class EPGChannel(
    @PrimaryKey val id: String,
    val name: String,
)
