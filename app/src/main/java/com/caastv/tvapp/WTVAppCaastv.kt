package com.caastv.tvapp

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.caastv.tvapp.di.CoreComponentProvider
import com.caastv.tvapp.model.data.customapp.InventoryApp
import com.caastv.tvapp.model.data.epgdata.EPGDataItem
import com.caastv.tvapp.model.data.genre.WTVGenre
import com.caastv.tvapp.model.data.language.WTVLanguage
import com.caastv.tvapp.model.data.login.LoginResponseData
import com.caastv.tvapp.model.data.manifest.WTVManifest
import com.caastv.tvapp.model.home.WTVHomeCategory
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WTVAppCaastv : Application(), CoreComponentProvider, LifecycleObserver {
    //@Inject lateinit var logUploader: LogUploader
    private val wtvEGPLiveData: MutableLiveData<List<EPGDataItem>> = MutableLiveData()
    private val wtvAppManifest: MutableLiveData<WTVManifest> = MutableLiveData()
    private val wtvGenre: MutableLiveData<List<WTVGenre>> = MutableLiveData()
    private val wtvLanguage: MutableLiveData<List<WTVLanguage>> = MutableLiveData()
    private val wtvInventoryApp: MutableLiveData<List<InventoryApp>> = MutableLiveData()
    private val wtvHome: MutableLiveData<List<WTVHomeCategory>> = MutableLiveData()
    private val macAddr: MutableLiveData<String> = MutableLiveData()
    private var userInfo: LoginResponseData? = null
    companion object {
        /** True if any part of our app is visible in the foreground. */
        @JvmStatic
        var isInForeground: Boolean = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        // Register this Application as an observer of the overall process lifecycle:
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)


       // logUploader.scheduleAutoUpload()
    }

    // Called when the app’s first Activity comes to START (= any Activity visible).
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        isInForeground = true
    }

    // Called when the app’s last visible Activity is STOPPED (i.e., no UI in front).
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onMoveToBackground() {
        isInForeground = false
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

    override fun provideInventoryApps(data: List<InventoryApp>): LiveData<List<InventoryApp>> = wtvInventoryApp

    override fun initializeLanguage(data: List<WTVLanguage>) {
        this.wtvLanguage.postValue(data)
    }

    override fun provideHomeLiveData(): LiveData<List<WTVHomeCategory>>  = wtvHome

    override fun initializeHome(data: List<WTVHomeCategory>) {
        this.wtvHome.postValue(data)
    }

    override fun provideMacAddr(): LiveData<String> = macAddr
    override fun initializeMacAddr(data: String) {
        this.macAddr.value = data
    }

    override fun provideUserInfo(): LoginResponseData? = userInfo

    override fun initializeUserInfo(data: LoginResponseData) {
        this.userInfo = data
    }
    override fun onTerminate() {
        super.onTerminate()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
    }
}