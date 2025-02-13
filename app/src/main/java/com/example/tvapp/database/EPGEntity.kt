package com.example.tvapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "epg_events")
data class EPGEntity(
    @PrimaryKey val id: String,
    val serviceId: String,
    val serviceName: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val eventName: String,
    val eventDescription: String,
    val rating: Int
)