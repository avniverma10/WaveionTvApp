package com.example.tvapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "epg_programs")
data class EPGProgram(
    @PrimaryKey val id: String,
    val channelId: String,  // Foreign Key to EPGChannel
    val date: String,
    val startTime: String,
    val endTime: String,
    val eventName: String,
    val eventDescription: String,
    var watchedAt: Long? = null // Add timestamp to track when watched
)
