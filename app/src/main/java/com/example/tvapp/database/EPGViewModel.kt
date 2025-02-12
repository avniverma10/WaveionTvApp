package com.example.tvapp.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tvapp.XMLParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EPGViewModel @Inject constructor(
    private val repository: EPGRepository,
    application: Application
) : AndroidViewModel(application) {

    init {
        viewModelScope.launch {
            val programs = XMLParser.readEPGFromAssets(application.applicationContext)
            repository.insertAll(programs)
        }
    }
}
