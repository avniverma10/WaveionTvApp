package com.example.tvapp.model.data.epgdata

import android.util.Log
import androidx.annotation.Keep
import com.example.tvapp.extensions.loge
import com.google.gson.*
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

@Keep
class ProgramTimestampAdapter :  JsonDeserializer<Long> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Long {
        return try {
            val inputFormat = SimpleDateFormat("yyyyMMddHHmmss Z", Locale.US)
            val parsedDate = inputFormat.parse(json.asString)
                ?: return 0L

            // 2) Build two Calendars: one for 'now', one for our event (time of day)
            val now = Calendar.getInstance()
            val eventCal = Calendar.getInstance().apply {
                time = parsedDate
                // overwrite year/month/day with today's values
                set(Calendar.YEAR,        now.get(Calendar.YEAR))
                set(Calendar.MONTH,       now.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH,now.get(Calendar.DAY_OF_MONTH))
            }

            // 3) If that event time is already past today, move it to tomorrow
            if (eventCal.timeInMillis < now.timeInMillis) {
                eventCal.add(Calendar.DAY_OF_MONTH, 1)
            }
            return eventCal.timeInMillis
        } catch (e: Exception) {
            loge("CurrentDateTimeAdapter", "Parse Error: ${json.asString} ${e.message}")
            0L
        }
    }
}

