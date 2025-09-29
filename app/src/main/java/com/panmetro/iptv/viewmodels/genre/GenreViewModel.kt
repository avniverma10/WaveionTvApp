package com.panmetro.iptv.viewmodels.genre


import android.app.Application
import androidx.lifecycle.ViewModel
import com.panmetro.iptv.extensions.appManifestLiveData
import com.panmetro.iptv.extensions.coreEPGLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
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
