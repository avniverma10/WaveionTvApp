package com.example.tvapp

import com.example.tvapp.models.EPGChannel
import com.example.tvapp.models.EPGProgram
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

object XMLParser {

    fun parseEPG(inputStream: InputStream): Pair<EPGChannel, List<EPGProgram>> {
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(inputStream, "UTF-8")

        var channelId = ""
        var channelName = ""
        val programs = mutableListOf<EPGProgram>()

        var date = ""
        var startTime = ""
        var endTime = ""
        var eventId = UUID.randomUUID().toString()
        var eventName = ""
        var eventDescription = ""

        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "channel" -> channelId = parser.getAttributeValue(null, "id") ?: ""
                        "display-name" -> channelName = parser.nextText()
                        "programme" -> {
                            startTime = parser.getAttributeValue(null, "start")
                            endTime = parser.getAttributeValue(null, "stop")
                        }
                        "title" -> eventName = parser.nextText()
                        "desc" -> eventDescription = parser.nextText()
                        "date" -> date = parser.nextText()
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "programme") {
                        programs.add(
                            EPGProgram(
                                id = UUID.randomUUID().toString(),
                                channelId = channelId,
                                date = date,
                                startTime = startTime,
                                endTime = endTime,
                                eventName = eventName,
                                eventDescription = eventDescription
                            )
                        )
                    }
                }
            }
            parser.next()
        }
        return Pair(EPGChannel(id = channelId, name = channelName), programs)
    }
}
