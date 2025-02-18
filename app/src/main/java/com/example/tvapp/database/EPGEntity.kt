package com.example.tvapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "epg_events")
data class EPGEntity(
    @PrimaryKey val id: String,
    val channelId: String,
    val channelName: String,
    val date: String,
    val startTime: Long,
    val endTime: Long,
    val eventName: String,
    val eventDescription: String
)
