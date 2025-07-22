package com.caastv.tvapp.model.data.epgdata

import com.caastv.tvapp.extensions.loge
import com.google.gson.*
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class ProgramTimestampAdapter :  JsonDeserializer<Long> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Long {
        return try {
            val inputFormat = SimpleDateFormat("yyyyMMddHHmmss Z", Locale.US)
            val outputFormat = SimpleDateFormat("HH:mm", Locale.US)
            val now = Calendar.getInstance()
            val timestampInfo = Calendar.getInstance().apply {
                time =  inputFormat.parse(json.asString)
                // Apply current date to the parsed time
                set(Calendar.YEAR, now.get(Calendar.YEAR))
                set(Calendar.MONTH, now.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))
            }
            return timestampInfo.timeInMillis
        } catch (e: Exception) {
            loge("CurrentDateTimeAdapter", "Parse Error: ${json.asString} ${e.message}")
            0L
        }
    }
}
