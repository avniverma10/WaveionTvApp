package com.example.tvapp.viewmodels

import com.example.tvapp.repository.SplashRepository
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(private val repository: SplashRepository) : ViewModel() {

    private val _logoUrl = MutableStateFlow<String?>(null)
    val logoUrl: StateFlow<String?> = _logoUrl

    init {
        fetchSplashContent()
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
}
