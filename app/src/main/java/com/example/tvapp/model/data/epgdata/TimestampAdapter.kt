package com.example.tvapp.model.data.epgdata

import com.google.gson.*
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class TimestampAdapter : JsonDeserializer<Long>, JsonSerializer<Long> {

    private val dateFormat = SimpleDateFormat("yyyyMMddHHmmss Z", Locale.US)

    override fun deserialize(
        json: JsonElement, 
        typeOfT: Type, 
        context: JsonDeserializationContext
    ): Long {
        val dateStr = json.asString
        return try {
            dateFormat.parse(dateStr)?.time ?: 0L
        } catch (e: ParseException) {
            0L
        }
    }

    override fun serialize(
        src: Long, 
        typeOfSrc: Type, 
        context: JsonSerializationContext
    ): JsonElement {
        val dateStr = dateFormat.format(Date(src))
        return JsonPrimitive(dateStr)
    }
}
