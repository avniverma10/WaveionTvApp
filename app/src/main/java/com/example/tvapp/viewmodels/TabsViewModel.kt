package com.example.tvapp.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tvapp.models.Tab
import com.example.tvapp.repository.TabsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.State

@HiltViewModel
class TabsViewModel @Inject constructor(private val repository: TabsRepository) : ViewModel() {

    private val _tabs = mutableStateOf<List<Tab>>(emptyList())
    val tabs: State<List<Tab>> = _tabs

    init {
        viewModelScope.launch {
            try {
                _tabs.value = repository.fetchTabs().filter { it.isVisible }
            } catch (e: Exception) {
                // Handle exceptions appropriately
            }
        }
    }

}
