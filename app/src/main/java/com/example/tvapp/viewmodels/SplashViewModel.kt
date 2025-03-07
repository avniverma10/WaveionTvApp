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
import com.example.tvapp.models.Tab
import com.example.tvapp.repository.EPGRepository
import com.example.tvapp.repository.TabsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@RequiresApi(Build.VERSION_CODES.M)
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val repository: SplashRepository,
    application: Application,
    private val dataStoreManager: DataStoreManager,
    private val epgRepository: EPGRepository,
    private val tabsRepository: TabsRepository
) : AndroidViewModel(application) {

    private val _logoUrl = MutableStateFlow<String?>(null)
    val logoUrl: StateFlow<String?> = _logoUrl

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isDataLoaded = MutableStateFlow(false)
    val isDataLoaded: StateFlow<Boolean> = _isDataLoaded

    private val _tabs = MutableStateFlow<List<Tab>>(emptyList())
    val tabs: StateFlow<List<Tab>> = _tabs.asStateFlow()

    val authToken = dataStoreManager.authToken

    init {
        loadAllData() // Start fetching data immediately when SplashViewModel is created
    }

    private fun loadAllData() {
        viewModelScope.launch {
            if (!isInternetAvailable()) {
                _errorMessage.value = "No internet connection"
                return@launch
            }

            try {
                val timeTaken = measureTimeMillis {
                    // Fetch all required data in parallel
                    val fetchTabs = async { fetchTabs() }
                    val fetchEPGData = async { preloadDatabase() }
                    val fetchSplashContent = async { fetchSplashContent() }

                    // Wait for all tasks to complete
                    fetchTabs.await()
                    fetchEPGData.await()
                    fetchSplashContent.await()
                }
                Log.d("AVNI", "All data loaded in $timeTaken ms")

                // Mark data as fully loaded
                _isDataLoaded.value = true
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load data"
                Log.e("AVNI", "Error loading data", e)
            }
        }
    }

    private suspend fun fetchTabs() {
        try {
            val fetchedTabs = tabsRepository.fetchTabs().filter { it.isVisible }
            _tabs.value = fetchedTabs
        } catch (e: Exception) {
            Log.e("SplashViewModel", "Failed to fetch tabs", e)
        }
    }

    private suspend fun preloadDatabase() {
        try {
            epgRepository.fetchAndStoreEPGsFromApi()  // Fetch API data and save in Room
        } catch (e: Exception) {
            Log.e("SplashViewModel", "Error loading EPG data", e)
            _errorMessage.value = "Failed to load EPG data"
        }
    }

    private suspend fun fetchSplashContent() {
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
    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(ConnectivityManager::class.java)
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}
