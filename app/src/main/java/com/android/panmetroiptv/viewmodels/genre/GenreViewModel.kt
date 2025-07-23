package com.android.panmetroiptv.viewmodels.genre


import android.app.Application
import androidx.lifecycle.ViewModel
import com.android.panmetroiptv.extensions.appManifestLiveData
import com.android.panmetroiptv.extensions.coreEPGLiveData
import com.android.panmetroiptv.model.data.epgdata.EPGDataItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
open class GenreViewModel @Inject constructor(
    private val application: Application,
) : ViewModel(){

    fun provideAvailableEPG() = application.coreEPGLiveData().value

    fun provideAvailableGenre() = application.appManifestLiveData().value?.genre?: arrayListOf()

    private val _filteredPanMetroChannels = MutableStateFlow<List<EPGDataItem>>(emptyList())
    val filteredPanMetroChannels: StateFlow<List<EPGDataItem>> = _filteredPanMetroChannels.asStateFlow()


    fun filterPanMetroChannelsByGenre(genre:String?=null) {
        genre?.let {
            _filteredPanMetroChannels.value = provideAvailableEPG()?.filter { epgItem ->
                val genreMatch = genre.equals("All", true) ||  (epgItem.content?.genre?.map { it.name }.orEmpty()?.any { it.equals(genre, true) } == true)
                genreMatch
            }?: arrayListOf()
        }?:kotlin.run {
            _filteredPanMetroChannels.value = provideAvailableEPG()?: arrayListOf()
        }

    }



    override fun onCleared() {
        super.onCleared()
    }
}
