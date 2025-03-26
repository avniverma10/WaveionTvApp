package com.example.tvapp

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.tvapp.di.CoreComponentProvider
import com.example.tvapp.model.data.epgdata.EPGDataItem
import com.example.tvapp.model.data.genre.WTVGenre
import com.example.tvapp.model.data.language.WTVLanguage
import com.example.tvapp.model.data.manifest.WTVManifest
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WTVApp : Application(), CoreComponentProvider{
    private var wtvEGPLiveData: MutableLiveData<List<EPGDataItem>> = MutableLiveData()
    private var wtvAppManifest: MutableLiveData<WTVManifest> = MutableLiveData()
    private var wtvGenre: MutableLiveData<List<WTVGenre>> = MutableLiveData()
    private var wtvLanguage: MutableLiveData<List<WTVLanguage>> = MutableLiveData()

    override fun onCreate() {
        super.onCreate()
    }




    override fun provideEPGLiveData(): LiveData<List<EPGDataItem>>  = wtvEGPLiveData
    override fun initializeEPGData(data: List<EPGDataItem>) {
        this.wtvEGPLiveData.postValue(data)
    }

    override fun provideAppManifestLiveData(): LiveData<WTVManifest> = wtvAppManifest
    override fun initializeAppManifest(data: WTVManifest) {
       this.wtvAppManifest.postValue(data)
    }

    override fun provideGenreLiveData(): LiveData<List<WTVGenre>> = wtvGenre

    override fun initializeGenre(data: List<WTVGenre>) {
        this.wtvGenre.postValue(data)
    }

    override fun provideLanguageLiveData(): LiveData<List<WTVLanguage>>  = wtvLanguage

    override fun initializeLanguage(data: List<WTVLanguage>) {
        this.wtvLanguage.postValue(data)
    }

}