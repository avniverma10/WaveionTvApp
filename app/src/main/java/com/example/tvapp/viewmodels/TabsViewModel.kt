package com.example.tvapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tvapp.models.Tab
import com.example.tvapp.repository.TabsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TabsViewModel @Inject constructor(private val repository: TabsRepository) : ViewModel() {
    private val _tabs = MutableStateFlow<List<Tab>>(emptyList())
    val tabs: StateFlow<List<Tab>> = _tabs

    init {
        viewModelScope.launch {
            repository.streamTabs().collect { tabsList ->
                _tabs.value = tabsList
            }
        }
    }
}