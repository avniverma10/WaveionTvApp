package com.example.tvapp.viewmodels

import android.app.Application
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.example.tvapp.repository.SplashRepository
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tvapp.models.DataStoreManager
import com.example.tvapp.repository.EPGRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.M)
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val repository: SplashRepository,
    application: Application,
    private val dataStoreManager: DataStoreManager,
    private val epgRepository: EPGRepository
) :  AndroidViewModel(application){

    private val _logoUrl = MutableStateFlow<String?>(null)
    val logoUrl: StateFlow<String?> = _logoUrl

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isDataLoaded = MutableStateFlow(false)
    val isDataLoaded: StateFlow<Boolean> = _isDataLoaded


    val authToken = dataStoreManager.authToken

    init {
        checkAppConditions()
        fetchSplashContent()
        preloadDatabase()
    }

    private fun preloadDatabase() {
        viewModelScope.launch {
            try {
                epgRepository.fetchAndStoreEPGsFromApi()  // Fetch API data and save it in Room
                _isDataLoaded.value = true // Mark data as loaded
            } catch (e: Exception) {
                Log.e("AVNI", "Error loading EPG data", e)
                _errorMessage.value = "Failed to load data"
            }
        }
    }

    fun checkUserLoginStatus(onLoggedIn: () -> Unit, onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authToken.collect { token ->
                if (token.isNullOrEmpty()) {
                    onLoggedOut()
                } else {
                    onLoggedIn()
                }
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkAppConditions() {
        Log.d("AVNI", "is internet available: ${isInternetAvailable()}")
        viewModelScope.launch {
            if (!isInternetAvailable()) {
                Log.d("AVNI","Inside network not available")
                _errorMessage.value = "Connect to the internet"
                return@launch
            }

//            try {
//                // Check if the server is accessible
//                val splashApiResponse = repository.getLogo()
//                if (splashApiResponse.success) {
//                    _logoUrl.value = splashApiResponse.logo
//                } else {
//                    _errorMessage.value = "Server Error: 404 not found"
//                    return@launch
//                }
//
////                // Check for updates
////                val updateResponse = repository.checkForUpdates()
////                if (updateResponse.isUpdateAvailable) {
////                    _updateUrl.value = updateResponse.updateLink
////                }
//            } catch (e: Exception) {
//                _errorMessage.value = "Failed to reach server"
//                Log.e("SplashViewModel", "Error: 404 ", e)
//            }
        }
    }

    private fun fetchSplashContent() {
        viewModelScope.launch {
            try {

                val splashApiResponse = repository.getLogo()
                if (splashApiResponse.success) {
                    _logoUrl.value = splashApiResponse.logo
                } else {
                    Log.e("AVNI", "Failed to fetch logo: Response not successful")
                }
            } catch (e: Exception) {
                Log.e("AVNI", "Failed to fetch splash content", e)
            }
        }
    }



    @RequiresApi(Build.VERSION_CODES.M)
    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(ConnectivityManager::class.java)
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}
