package com.example.tvapp.models

data class EPGProgram(
    val id: String,
    val serviceId: String,
    val serviceName: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val eventName: String,
    val eventDescription: String,
    val rating: Int
)
