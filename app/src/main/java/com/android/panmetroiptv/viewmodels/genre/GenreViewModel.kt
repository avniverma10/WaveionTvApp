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







    override fun onCleared() {
        super.onCleared()
    }
}
