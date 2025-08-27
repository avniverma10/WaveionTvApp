package com.android.panmetroiptv.di

import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import com.android.panmetroiptv.model.data.epgdata.EPGDataItem
import com.android.panmetroiptv.model.data.genre.WTVGenre
import com.android.panmetroiptv.model.data.language.WTVLanguage
import com.android.panmetroiptv.model.data.manifest.WTVManifest
import com.android.panmetroiptv.model.home.WTVHomeCategory

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