package com.example.tvapp.repository


import com.example.tvapp.api.ApiServiceForData
import javax.inject.Inject

class TabsRepository  @Inject constructor(private val apiService: ApiServiceForData) {
    suspend fun fetchTabs(): List<com.example.tvapp.models.Tab> {
        return apiService.getTabs()
    }
}