package com.example.tvapp

import android.content.Context
import com.example.tvapp.models.EPGChannel
import com.example.tvapp.models.EPGProgram
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

object XMLParser {

    private fun parseEPG(inputStream: InputStream): Pair<EPGChannel, List<EPGProgram>> {
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

    private fun parseTime(timeString: String?): Long {
        val format = SimpleDateFormat("yyyyMMddHHmmss Z", Locale.getDefault())
        return format.parse(timeString)?.time ?: 0L
    }

    suspend fun readEPGsFromAssetsFolder(context: Context): List<Pair<EPGChannel, List<EPGProgram>>> = coroutineScope {
        val filenames = context.assets.list("epgXml")?.filter { it.lowercase().endsWith(".xml") } ?: listOf()
        filenames.map { filename ->
            async(Dispatchers.IO) {
                context.assets.open("epgXml/$filename").use { inputStream ->
                    parseEPG(inputStream)
                }
            }
        }.awaitAll()
    }
}
