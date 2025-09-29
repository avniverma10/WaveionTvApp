package com.panmetro.iptv.di

import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import com.panmetro.iptv.model.data.epgdata.EPGDataItem
import com.panmetro.iptv.model.data.genre.WTVGenre
import com.panmetro.iptv.model.data.language.WTVLanguage
import com.panmetro.iptv.model.data.manifest.WTVManifest
import com.panmetro.iptv.model.home.WTVHomeCategory

@Keep
interface CoreComponentProvider {
    fun provideEPGLiveData(): LiveData<List<EPGDataItem>>
    fun initializeEPGData(data: List<EPGDataItem>)
    fun provideAppManifestLiveData(): LiveData<WTVManifest>
    fun initializeAppManifest(data: WTVManifest)
    fun updatePkgChannel(pkg:String,channelData: MutableSet<String>)
    fun providePkgChannel():LiveData<HashMap<String, MutableSet<String>>>
    fun updateUserBlocked(username:String,isBlocked: Boolean)
    fun provideIsUserBlocked():LiveData<Boolean>
    fun provideGenreLiveData(): LiveData<List<WTVGenre>>
    fun initializeGenre(data: List<WTVGenre>)
    fun provideLanguageLiveData(): LiveData<List<WTVLanguage>>
    fun initializeLanguage(data: List<WTVLanguage>)
    fun provideHomeLiveData(): LiveData<List<WTVHomeCategory>>
    fun initializeHome(data: List<WTVHomeCategory>)
    fun provideMacAddr(): LiveData<String>
    fun initializeMacAddr(data: String)
}