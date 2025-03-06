package com.example.tvapp.repository


import android.util.Log
import com.example.tvapp.api.ApiServiceForData
import com.example.tvapp.models.HomeContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class HomeRepository @Inject constructor(private val apiService: ApiServiceForData) {

    suspend fun fetchHomeContent(): Flow<List<HomeContent>> = flow {
        try {
            val epgFiles = apiService.getEpgFiles()
            Log.d("HOME_REPO", "Fetched ${epgFiles.size} items from API")

            val homeContentList = epgFiles.map { epgFile ->
                Log.d("HOME_REPO", "Processing item: $epgFile")
                HomeContent(
                    title = epgFile.content.title,
                    thumbnailUrl = epgFile.content.thumbnailUrl,
                    videoUrl = epgFile.content.videoUrl,
                    genreId = epgFile.content.genreId
                )
            }
            emit(homeContentList) // Emit result from API
        } catch (e: Exception) {
            Log.e("HOME_REPO", "Error fetching home content", e)
        }
    }.flowOn(Dispatchers.IO) // <-- This moves the emission to the IO thread
}
