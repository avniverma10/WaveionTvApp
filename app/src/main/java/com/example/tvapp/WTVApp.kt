package com.example.tvapp

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.tvapp.di.CoreComponentProvider
import com.example.tvapp.model.data.epgdata.EPGDataItem
import com.example.tvapp.model.data.genre.WTVGenre
import com.example.tvapp.model.data.language.WTVLanguage
import com.example.tvapp.model.data.manifest.WTVManifest
import com.example.tvapp.model.home.WTVHomeCategory
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WTVApp : Application(), CoreComponentProvider, LifecycleObserver {
    companion object {
        /** True if any part of our app is visible in the foreground. */
        @JvmStatic
        var isInForeground: Boolean = false
            private set
    }
    private val wtvEGPLiveData: MutableLiveData<List<EPGDataItem>> = MutableLiveData()
    private val wtvAppManifest: MutableLiveData<WTVManifest> = MutableLiveData()
    private val wtvGenre: MutableLiveData<List<WTVGenre>> = MutableLiveData()
    private val wtvLanguage: MutableLiveData<List<WTVLanguage>> = MutableLiveData()
    private val wtvHome: MutableLiveData<List<WTVHomeCategory>> = MutableLiveData()
    private val macAddr: MutableLiveData<String> = MutableLiveData()

    override fun onCreate() {
        super.onCreate()
        // Register this Application as an observer of the overall process lifecycle:
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        // Setup crash handler
       /* if (!BuildConfig.DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler( CustomCrashHandler(this))
        }*/

        // Initialize logging
        //LogCollector.initialize(this);
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

    override fun onTerminate() {
        super.onTerminate()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
    }
}