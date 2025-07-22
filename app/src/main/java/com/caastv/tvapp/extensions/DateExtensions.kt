package com.caastv.tvapp.extensions

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


fun findProgramTimeStamp(start:Long?,end:Long?):String{
    val startTime = SimpleDateFormat("hh:mm", Locale.getDefault()).format(Date(start?:0L))?: "00:00"
    val endTime = SimpleDateFormat("hh:mm", Locale.getDefault()).format(Date(end?:0L))?: "00:00"
    return "${startTime}-${endTime}"
}


fun findProgramDateTimeStamp(timeStamp:Long?):Long{
    val startTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timeStamp?:0L))
    return startTime.toTimestamp()
}

fun findProgramDateTimeStamp(start:Long?,end:Long?):String{
    val startTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(start?:0L))?: "00:00"
    val endTime = SimpleDateFormat("hh:mm", Locale.getDefault()).format(Date(end?:0L))?: "00:00"
    return "${startTime}-${endTime}"
}

fun convertProgramDateTimeStamp(start:Long?,end:Long?):String{
    val startTime = SimpleDateFormat("dd/mm/yyyy hh:mm", Locale.getDefault()).format(Date(start?:0L))?: "00:00"
    val endTime = SimpleDateFormat("hh:mm", Locale.getDefault()).format(Date(end?:0L))?: "00:00"
    return "${startTime}-${endTime}"
}

fun convertOldTimestampWithCurrentDate(input: String): Long {
    // Define the input format (make sure day/month/year separator is correct)
    val inputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    // Parse the input string into a Date object
    val parsedDate = inputFormat.parse(input)

    // Create a calendar from the parsed input to extract hour and minute
    val inputCalendar = Calendar.getInstance().apply {
        time = parsedDate
    }
    val hour = inputCalendar.get(Calendar.HOUR_OF_DAY)
    val minute = inputCalendar.get(Calendar.MINUTE)

    // Create a calendar with the current date
    val currentCalendar = Calendar.getInstance()
    // Set the current calendar's time to the extracted hour and minute
    currentCalendar.set(Calendar.HOUR_OF_DAY, hour)
    currentCalendar.set(Calendar.MINUTE, minute)
    currentCalendar.set(Calendar.SECOND, 0)
    currentCalendar.set(Calendar.MILLISECOND, 0)

    // Define an output format that includes the current date with the new time
    val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return outputFormat.format(currentCalendar.time).toTimestamp()
}