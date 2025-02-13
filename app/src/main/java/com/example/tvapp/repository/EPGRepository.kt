package com.example.tvapp.repository

import com.example.tvapp.database.EPGDao
import com.example.tvapp.database.EPGEntity
import com.example.tvapp.models.EPGProgram


import javax.inject.Inject

class EPGRepository @Inject constructor(
    private val dao: EPGDao
) {
    suspend fun insertAll(events: List<EPGProgram>) {
        val entities = events.map { program ->
            EPGEntity(
                id = program.id,
                serviceId = program.serviceId,
                serviceName = program.serviceName,
                date = program.date,
                startTime = program.startTime,
                endTime = program.endTime,
                eventName = program.eventName,
                eventDescription = program.eventDescription,
                rating = program.rating
            )
        }
        dao.insertAll(entities)
    }
}
