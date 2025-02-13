package com.example.tvapp

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import android.content.Context
import com.example.tvapp.models.EPGProgram
import java.io.InputStream

object XMLParser {

    fun parseEPG(inputStream: InputStream): List<EPGProgram> {
        val programs = mutableListOf<EPGProgram>()
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(inputStream, "UTF-8")

        var serviceId = ""
        var serviceName = ""
        var date = ""
        var eventId = ""
        var startTime = ""
        var endTime = ""
        var eventName = ""
        var eventDescription = ""
        var rating = 0

        var insideEvent = false

        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "Service" -> {
                            serviceId = parser.getAttributeValue(null, "id") ?: ""
                            serviceName = parser.getAttributeValue(null, "name") ?: ""
                        }
                        "ScheduleDay" -> {
                            date = parser.getAttributeValue(null, "date") ?: ""
                        }
                        "Event" -> {
                            eventId = parser.getAttributeValue(null, "id") ?: ""
                            insideEvent = true
                        }
                        "StartDateTime" -> startTime = parser.nextText()
                        "EndDateTime" -> endTime = parser.nextText()
                        "EventName" -> eventName = parser.nextText()
                        "EventDescription" -> eventDescription = parser.nextText()
                        "Rating" -> rating = parser.nextText().toIntOrNull() ?: 0

                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "Event" && insideEvent) {
                        programs.add(
                            EPGProgram(
                                id = eventId,
                                serviceId = serviceId,
                                serviceName = serviceName,
                                date = date,
                                startTime = startTime,
                                endTime = endTime,
                                eventName = eventName,
                                eventDescription = eventDescription,
                                rating = rating
                            )
                        )
                        insideEvent = false
                    }
                }
            }
            parser.next()
        }
        return programs
    }

    fun readEPGFromAssets(context: Context): List<EPGProgram> {
        return try {
            val inputStream = context.assets.open("Asianet_News_EPG.xml")
            val programs = parseEPG(inputStream)
            inputStream.close()
            programs
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
