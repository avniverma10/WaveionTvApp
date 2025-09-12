package com.android.panmetroiptv.viewmodels


import android.app.Application
import androidx.lifecycle.viewModelScope
import com.android.panmetroiptv.extensions.coreEPGLiveData
import com.android.panmetroiptv.extensions.loge
import com.android.panmetroiptv.model.data.home.HomeContent
import com.android.panmetroiptv.model.repository.common.WTVNetworkRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val application: Application, private val networkApiCallInterfaceImpl: WTVNetworkRepositoryImpl) : WTVViewModel(application= application,networkApiCallInterfaceImpl, okHttpClient = OkHttpClient()) {
    private val _homeContent = MutableStateFlow<List<HomeContent>>(emptyList())
    val homeContent: StateFlow<List<HomeContent>> get() = _homeContent

    init {
        initHomeContent()
    }


    fun initHomeContent(){
        viewModelScope.launch {
            provideHomeContent(application).collect { contentList ->
                _homeContent.value = contentList.map { it }
            }
        }
    }



    suspend fun provideHomeContent(application: Application): Flow<List<HomeContent>> = flow {
        try {
            val epgHomeItems = application.coreEPGLiveData().value?.map { epgDataItem ->
                HomeContent(
                    title = epgDataItem.title ?: "",
                    thumbnailUrl = epgDataItem.thumbnailUrl ?: "",
                    videoUrl = epgDataItem.videoUrl ?: "",
                    genreId = epgDataItem.genreId ?: "",
                    _id = epgDataItem._id?:"",
                    description = TODO(),
                    contentType = TODO(),
                    categoryId = TODO(),
                    duration = TODO(),
                    releaseDate = TODO(),
                    languageId = TODO(),
                    published = TODO(),
                    __v = TODO(),
                    channelNo = TODO(),
                    ChannelID = TODO(),
                )
            }

            emit(epgHomeItems!!) // Emit result from API
        } catch (e: Exception) {
            loge("HOME_REPO", "Error fetching home content  ${e.message}")
        }
    }.flowOn(Dispatchers.IO)
}
