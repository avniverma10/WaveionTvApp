package com.caastv.tvapp.model.data.epgdata

import com.caastv.tvapp.extensions.loge
import com.google.gson.*
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class CurrentDateTimeAdapter : JsonDeserializer<String> {

    private val serverFormat = SimpleDateFormat("yyyyMMddHHmmss Z", Locale.US).apply {
        timeZone = TimeZone.getDefault()
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyyMMddHHmmss Z", Locale.US)
            val outputFormat = SimpleDateFormat("HH:mm", Locale.US)
            val date: Date? = inputFormat.parse(json.asString)
            return date?.let { outputFormat.format(it) } ?: ""
        } catch (e: Exception) {
            loge("CurrentDateTimeAdapter", "Parse Error: ${json.asString} ${e.message}")
        }.toString()
    }
}
