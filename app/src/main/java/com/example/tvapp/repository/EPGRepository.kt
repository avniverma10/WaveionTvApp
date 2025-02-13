package com.example.tvapp.repository

import androidx.media3.common.util.Log
import com.example.tvapp.database.EPGDao
import com.example.tvapp.database.EPGEntity
import com.example.tvapp.models.EPGProgram
import kotlinx.coroutines.flow.Flow


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
        Log.d("AVNIDB","Inserting ${entities.size} records into database...")  // Log data insertion
        dao.insertAll(entities)
    }
    fun getEPGData(): Flow<List<EPGEntity>> = dao.getAllEPG()

    fun getEventsByDate(date: String): Flow<List<EPGEntity>> = dao.getEventsByDate(date)  // Fetch by date
}
