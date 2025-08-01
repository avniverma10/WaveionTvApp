package com.tccl.tvapp.di

import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import com.tccl.tvapp.model.data.customapp.InventoryApp
import com.tccl.tvapp.model.data.epgdata.EPGDataItem
import com.tccl.tvapp.model.data.genre.WTVGenre
import com.tccl.tvapp.model.data.language.WTVLanguage
import com.tccl.tvapp.model.data.login.LoginResponseData
import com.tccl.tvapp.model.data.manifest.WTVManifest
import com.tccl.tvapp.model.home.WTVHomeCategory

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
    fun initInventoryApps(data: List<InventoryApp>)
    fun provideInventoryApps(): LiveData<List<InventoryApp>>
    fun provideMacAddr(): LiveData<String>
    fun initializeMacAddr(data: String)
    fun provideUserInfo(): LoginResponseData?
    fun initializeUserInfo(data: LoginResponseData)
}