package com.example.tvapp.repository
import android.util.Log
import com.example.tvapp.XMLParser
import com.example.tvapp.api.ApiServiceForData
import com.example.tvapp.database.EPGDao
import com.example.tvapp.models.EPGChannel
import com.example.tvapp.models.EPGProgram
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject


class EPGRepository @Inject constructor(private val dao: EPGDao, private val apiService: ApiServiceForData) {

    suspend fun insertAll(channels: EPGChannel, programs: List<EPGProgram>) {
        if (!dao.isChannelExists(channels.id)) {
            Log.d("RISHI", "insertAll: Inserting ${programs.size} programs for channel ${channels.id}")
            dao.insertChannels(channels)
            // TODO: RISHI no need to check "isProgramExists" on every insert, it will slow the process.
            programs.forEach { program ->
                if (!dao.isProgramExists(program.channelId, program.startTime, program.endTime)) {
                    dao.insertPrograms(programs)
                    val insertedChannels = dao.getAllChannelsWithProgramsSync()
                    Log.d("RISHI", "After insertion, channels with programs: $insertedChannels")
                }
            }
        }else{
            Log.i("RISHI", "Skip this due to this channel is already present in DB. ${channels.id} ")
        }
    }

    fun getAllChannels(): Flow<List<EPGChannel>> = dao.getAllChannels()

    fun getProgramsForNextHours(startTime: Long, endTime: Long): Flow<List<EPGProgram>> {
        return dao.getProgramsForNextHours(startTime, endTime)
    }


    suspend fun getProgramsByName(query: String): List<EPGProgram> {
        return dao.searchProgramsByName("%$query%").first()
    }


    suspend fun fetchAndStoreEPGsFromApi() {
        withContext(Dispatchers.IO) {
            try {
                Log.d("DEBUG1", "Inside try")
                val epgFiles = apiService.getEpgFiles()
                Log.d("DEBUG1", "Received ${epgFiles.size} EPG files from API")
                epgFiles.forEach { epgFile ->
                    Log.d("DEBUG1", "File URL: ${epgFile.url}")
                    val fixedUrl = epgFile.url.replace(" ", "%20")
                    Log.d("DEBUG1", "Fixed URL: $fixedUrl")
                    val url = URL(fixedUrl)
                    (url.openConnection() as? HttpURLConnection)?.let { connection ->
                        connection.requestMethod = "GET"
                        connection.connectTimeout = 5000
                        connection.readTimeout = 5000
                        connection.setRequestProperty("User-Agent", "Mozilla/5.0")
                        try {
                            Log.d("DEBUG1", "Calling connection.connect()")
                            connection.connect()
                            Log.d("DEBUG1", "connection.connect() returned")
                        } catch (ex: Exception) {
                            Log.e("DEBUG1", "Error during connection.connect()", ex)
                            connection.disconnect()
                            return@forEach
                        }

                        val responseCode = connection.responseCode
                        Log.d("DEBUG1", "Response code: $responseCode")
                        if (responseCode != HttpURLConnection.HTTP_OK) {
                            Log.e("DEBUG1", "Failed to connect. Response code: $responseCode, message: ${connection.responseMessage}")
                            connection.disconnect()
                            return@forEach
                        }

                        Log.d("DEBUG1", "Connected to ${epgFile.url}")
                        connection.inputStream.use { inputStream ->
                            try {
                                // Parse the XML file to get channel and programs.
                                val (channel, programs) = XMLParser.parseEPG(inputStream)
                                Log.d("DEBUG1", "Parsed channel: ${channel.id}, name: ${channel.name}")
                                Log.d("DEBUG1", "Parsed ${programs.size} programs")

                                // Update the channel with the title and logo from epgFile.content.
                                // (Assumes you updated your EPGChannel data class to include logoUrl and title.)
                                val updatedChannel = channel.copy(
                                    // Replace the parsed channel name with the one coming from API.
                                    name = epgFile.content.title,
                                    // Set the channel logo from the API.
                                    logoUrl = epgFile.content.thumbnailUrl,
                                    videoUrl = epgFile.content.videoUrl

                                )
                                Log.d("AVNI1","video url is -->${epgFile.content.videoUrl}")

                                // Insert the channel and programs in your database.
                                insertAll(updatedChannel, programs)
                            } catch (ex: Exception) {
                                Log.e("DEBUG1", "Error parsing XML", ex)
                            }
                        }
                        connection.disconnect()
                    }
                }
            } catch (e: Exception) {
                Log.e("EPG", "Error fetching EPG from API", e)
            }
        }
    }

}




