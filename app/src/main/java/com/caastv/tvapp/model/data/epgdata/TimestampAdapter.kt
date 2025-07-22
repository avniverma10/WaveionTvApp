import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TimestampAdapter : JsonDeserializer<Long>, JsonSerializer<Long> {

    private val dateFormat = SimpleDateFormat("yyyyMMddHHmmss Z", Locale.US)

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Long {
        val dateStr = json.asString
        return try {
            // Parse the date string into a Date object
            val oldDate: Date = dateFormat.parse(dateStr) ?: return 0L

            // Create a Calendar instance for the parsed (old) date
            val oldCalendar = Calendar.getInstance(Locale.getDefault())
            oldCalendar.time = oldDate

            // Extract the time-of-day from the old timestamp.
            val hour = oldCalendar.get(Calendar.HOUR_OF_DAY)
            val minute = oldCalendar.get(Calendar.MINUTE)
            val second = oldCalendar.get(Calendar.SECOND)
            val millisecond = oldCalendar.get(Calendar.MILLISECOND)

            // Create a Calendar instance for the current date using System.currentTimeMillis()
            val currentCalendar = Calendar.getInstance(Locale.getDefault())
            currentCalendar.timeInMillis = System.currentTimeMillis()

            // Set the current date's time-of-day to the extracted values
            currentCalendar.set(Calendar.HOUR_OF_DAY, hour)
            currentCalendar.set(Calendar.MINUTE, minute)
            currentCalendar.set(Calendar.SECOND, second)
            currentCalendar.set(Calendar.MILLISECOND, millisecond)

            // Return the new timestamp (current date with the original time)
            currentCalendar.timeInMillis
        } catch (e: ParseException) {
            0L
        }
    }

    override fun serialize(
        src: Long,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        // Format the given timestamp into a string using the specified dateFormat
        val dateStr = dateFormat.format(Date(src))
        return JsonPrimitive(dateStr)
    }
}
