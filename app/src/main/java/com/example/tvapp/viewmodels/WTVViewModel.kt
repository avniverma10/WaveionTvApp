package com.example.tvapp.viewmodels


import android.app.Application
import android.content.ContentValues
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tvapp.extensions.applyAppManifest
import com.example.tvapp.extensions.logReport
import com.example.tvapp.utils.sealed.WTVListResponse
import com.example.tvapp.utils.sealed.WTVResponse
import com.example.tvapp.model.wtvdatabase.EPGContract
import com.example.tvapp.model.data.epgdata.EPGDataItem
import com.example.tvapp.model.repository.WTVNetworkRepositoryImpl
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
open class WTVViewModel @Inject constructor(private val application: Application,private val networkApiCallInterfaceImpl: WTVNetworkRepositoryImpl) : AndroidViewModel(application) {
    fun provideApplicationContext() = application.applicationContext
    private var _isInitializeData = MutableStateFlow<Boolean>(false)
    val isInitializeData: StateFlow<Boolean> get() = _isInitializeData
    private var _isProgress = MutableStateFlow<Boolean>(false)
    val provideIsProgress: StateFlow<Boolean> get() = _isProgress


    fun updateEPGData(epgList: List<EPGDataItem>) {
        viewModelScope.launch {
            saveEPGList(application, epgList)
        }
    }

    private val _errorLoadingData = MutableStateFlow<String?>(null)
    val errorLoadingData: StateFlow<String?> = _errorLoadingData

    init {
        initAppRequiredData()
    }

    fun initAppRequiredData(){
        viewModelScope.launch {
           // tvManifest.value = WTVManifest()
            // Launch API calls in parallel
            networkApiCallInterfaceImpl.provideWTVManifest(manifestUrl = "https://nextwave.waveiontechnologies.com:5000/api/manifest").collect{ response ->
                when (response) {
                    is WTVResponse.Success -> {
                        // Handle successful response
                        application.applicationContext.applyAppManifest(response.data)
                        logReport("ManifestResponse:${ response.data}")
                    }
                    is WTVResponse.Failure -> {
                        // Handle error state
                       // _errorLoadingData.value = response.error.message
                        logReport("ManifestResponse:${ response.error.message}")

                    }
                }
            }
        }

        viewModelScope.launch {
            networkApiCallInterfaceImpl.provideWTVEPGData(epgContentUrl = "https://nextwave.waveiontechnologies.com:5000/api/epg-files/join-epg-content").collect{response ->
                when (response) {
                    is WTVListResponse.Success -> {
                        // Handle successful response
                        updateEPGData(response.data)
                        logReport("EPGResponse:${ response.data}")

                    }
                    is WTVListResponse.Failure -> {
                        // Handle error state
                        // _errorLoadingData.value = response.error.message
                        logReport("EPGResponse:${ response.error.message}")

                    }
                }
            }
            _isInitializeData.value = true
        }
    }

    suspend fun saveEPGList(context: Context, epgList: List<EPGDataItem>) {
        withContext(Dispatchers.IO) {
            epgList.forEach { item ->
                val values = ContentValues().apply {
                    put(EPGContract.EPGEntry.COLUMN_ID, item._id)
                    put(EPGContract.EPGEntry.COLUMN_CHANNEL_ID, item.channelId)
                    put(EPGContract.EPGEntry.COLUMN_CHANNEL_HASH, item.channelHash)
                    put(EPGContract.EPGEntry.COLUMN_LAST_UPDATED, item.lastUpdated)
                    put(EPGContract.EPGEntry.COLUMN_DATA, Gson().toJson(item))
                }
                context.contentResolver.insert(EPGContract.EPGEntry.CONTENT_URI, values)
            }
        }
    }

}
