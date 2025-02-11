package com.example.tvapp.viewmodels


import android.content.Context
import androidx.annotation.OptIn
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.tvapp.repository.ExoPlayerRepository
import com.example.applicationscreens.models.Data
//import com.example.tvapp.utils.DeviceInfoHelper
import kotlinx.coroutines.launch

@HiltViewModel
class ExoPlayerViewModel @Inject constructor(private val repository: ExoPlayerRepository) : ViewModel() {

    var videoList: MutableState<List<Data>> = mutableStateOf(emptyList())
    var isLoading: MutableState<Boolean> = mutableStateOf(true)

    // Hold ExoPlayer instance
    var exoPlayer: ExoPlayer? = null
        private set

    init {
        fetchVideoContent()
    }

    fun initializePlayer(context: Context, videoList: List<Data>, startIndex: Int = 0) {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                val mediaItems = videoList.map { MediaItem.fromUri(it.videoUrl) }
                setMediaItems(mediaItems, startIndex, 0L)
                prepare()
                playWhenReady = true
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun fetchVideoContent() {
        viewModelScope.launch {
            try {
                val contentResponse = repository.getVideoContent()
                videoList.value = contentResponse.data
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading.value = false
            }
        }
    }

    fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer() // Release ExoPlayer when ViewModel is cleared
    }

    fun stopPlayback() {
        exoPlayer?.playWhenReady = false
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = null
    }

}
