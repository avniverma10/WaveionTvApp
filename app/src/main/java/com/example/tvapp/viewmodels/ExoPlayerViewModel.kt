package com.example.tvapp.viewmodels


import android.content.Context
import androidx.annotation.OptIn
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.example.tvapp.repository.ExoPlayerRepository
import com.example.applicationscreens.models.Data
import com.example.tvapp.api.ApiService2
//import com.example.tvapp.utils.DeviceInfoHelper
import kotlinx.coroutines.launch


@HiltViewModel
class ExoPlayerViewModel @Inject constructor(private val repository: ExoPlayerRepository,private val apiService: ApiService2) : ViewModel() {

    // State to hold the list of videos
    var videoList: MutableState<List<Data>> = mutableStateOf(emptyList())
    var isLoading: MutableState<Boolean> = mutableStateOf(true)

    // Fetch the video content from the API
    @OptIn(UnstableApi::class)
    fun fetchVideoContent() {
        viewModelScope.launch {
            try {
//                isLoading.value = true
                val contentResponse = apiService.getVideoContent()
                videoList.value = contentResponse.data
            } catch (e: Exception) {
                // Handle error, for example show a toast or log
            } finally {
                isLoading.value = false
            }
        }
    }

    init {
        fetchVideoContent() // Fetch data when ViewModel is initialized
    }

//    fun logDeviceInfo(context: Context) {
//        val deviceInfo = DeviceInfoHelper.getDeviceInfo(context)
//        Log.d("DeviceInfoViewModel", deviceInfo.toString())
//    }
}
