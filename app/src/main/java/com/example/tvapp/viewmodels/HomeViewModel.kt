package com.example.tvapp.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tvapp.models.HomeContent
import com.example.tvapp.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: HomeRepository) : ViewModel() {

    private val _homeContent = MutableStateFlow<List<HomeContent>>(emptyList())
    val homeContent: StateFlow<List<HomeContent>> get() = _homeContent

    fun loadHomeContent() {
        viewModelScope.launch {
            repository.fetchHomeContent().collect { contentList ->
                _homeContent.value = contentList
            }
        }
    }
}
