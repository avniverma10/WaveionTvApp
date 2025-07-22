package com.caastv.tvapp.di

import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import com.caastv.tvapp.model.data.customapp.InventoryApp
import com.caastv.tvapp.model.data.epgdata.EPGDataItem
import com.caastv.tvapp.model.data.genre.WTVGenre
import com.caastv.tvapp.model.data.language.WTVLanguage
import com.caastv.tvapp.model.data.login.LoginResponseData
import com.caastv.tvapp.model.data.manifest.WTVManifest
import com.caastv.tvapp.model.home.WTVHomeCategory

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
    fun provideHomeLiveData(): LiveData<List<WTVHomeCategory>>
    fun initializeHome(data: List<WTVHomeCategory>)
    fun provideInventoryApps(data: List<InventoryApp>): LiveData<List<InventoryApp>>
    fun provideMacAddr(): LiveData<String>
    fun initializeMacAddr(data: String)
    fun provideUserInfo(): LoginResponseData?
    fun initializeUserInfo(data: LoginResponseData)
}