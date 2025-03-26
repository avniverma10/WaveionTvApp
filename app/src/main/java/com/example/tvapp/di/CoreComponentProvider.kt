package com.example.tvapp.di

import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import com.example.tvapp.model.data.epgdata.EPGDataItem
import com.example.tvapp.model.data.genre.WTVGenre
import com.example.tvapp.model.data.language.WTVLanguage
import com.example.tvapp.model.data.manifest.WTVManifest

@Keep
interface CoreComponentProvider {
    fun provideEPGLiveData(): LiveData<List<EPGDataItem>>
    fun initializeEPGData(data: List<EPGDataItem>)
    fun provideAppManifestLiveData(): LiveData<WTVManifest>
    fun initializeAppManifest(data: WTVManifest)
    fun provideGenreLiveData(): LiveData<List<WTVGenre>>
    fun initializeGenre(data: List<WTVGenre>)
    fun provideLanguageLiveData(): LiveData<List<WTVLanguage>>
    fun initializeLanguage(data: List<WTVLanguage>)
}