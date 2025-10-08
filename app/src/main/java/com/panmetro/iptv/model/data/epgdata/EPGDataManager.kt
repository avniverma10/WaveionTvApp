package com.panmetro.iptv.model.data.epgdata

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.internal.notify
import okhttp3.internal.notifyAll
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Singleton
import kotlin.collections.plus

@Singleton
class EPGDataManager {
    private val _epgData = ArrayList<EPGDataItem>()
    private val epgDataLock = Any()

    // StateFlow for reactive updates in Compose
    private val _epgDataState = MutableStateFlow<ArrayList<EPGDataItem>>(ArrayList())
    val epgDataState: StateFlow<ArrayList<EPGDataItem>> = _epgDataState

    private val _loadingState = MutableStateFlow<DataState>(DataState.Idle)
    val loadingState: StateFlow<DataState> = _loadingState

    // Memory optimization: Use weak references for large data if needed
    private val channelMap = ConcurrentHashMap<String, EPGDataItem>()
    private val idMap = ConcurrentHashMap<String, EPGDataItem>()

    sealed class DataState {
        object Idle : DataState()
        object Loading : DataState()
        object Success : DataState()
        data class Error(val message: String) : DataState()
    }

    // Load initial data from API
   /* suspend fun loadInitialData() {
        _loadingState.value = DataState.Loading
        try {
            val response = apiCallToGetEPGData() // Your API call
            synchronized(epgDataLock) {
                _epgData.clear()
                _epgData.addAll(response)
                updateMaps()
                _epgDataState.value = ArrayList(_epgData) // Create new ArrayList instance
            }
            _loadingState.value = DataState.Success
        } catch (e: Exception) {
            _loadingState.value = DataState.Error(e.message ?: "Failed to load EPG data")
        }
    }

    // Clear and replace all EPG data
    fun clearAndAddAllEPGData(items: List<EPGDataItem>) {
        synchronized(epgDataLock) {
            _epgData.clear()
            _epgData.addAll(items)
            updateMaps()
            _epgDataState.value = ArrayList(_epgData) // Create new ArrayList instance
        }
    }

    // Clear all EPG data
    fun clearEPGData() {
        synchronized(epgDataLock) {
            _epgData.clear()
            channelMap.clear()
            idMap.clear()
            _epgDataState.value = ArrayList() // Empty ArrayList
        }
    }

    // Add new EPG item
    fun addEPGItem(item: EPGDataItem) {
        synchronized(epgDataLock) {
            // Check if item already exists
            val existingIndex = _epgData.indexOfFirst { it._id == item._id }
            if (existingIndex != -1) {
                // Update existing item
                _epgData[existingIndex] = item
            } else {
                // Add new item
                _epgData.add(item)
            }
            updateMaps()
            _epgDataState.value = ArrayList(_epgData) // Create new ArrayList instance
        }
    }*/

    // Add multiple items efficiently with bulk operation
    fun addEPGItems(items: List<EPGDataItem>,isAddAll: Boolean=false) {
        _epgDataState.value = (if (isAddAll) {
            _epgDataState.value + items   // append items
        } else {
            items                         // replace items
        }) as ArrayList<EPGDataItem>
     }

    // Add multiple items efficiently with bulk operation
    fun addAddonsEPGItems(items: List<EPGDataItem>) {
        _epgData.clear()
        _epgData.addAll(items)
     }
     /*  *//* synchronized(epgDataLock) {
            // Create a map of existing items for quick lookup
            val existingItemsMap = _epgData.associateBy { it._id }

            items.forEach { newItem ->
                val existingItem = existingItemsMap[newItem._id]
                if (existingItem != null) {
                    // Update existing item - find index and replace
                    val index = _epgData.indexOfFirst { it._id == newItem._id }
                    if (index != -1) {
                        _epgData[index] = newItem
                    }
                } else {
                    // Add new item
                    _epgData.add(newItem)
                }
            }
            updateMaps()
            _epgDataState.value = ArrayList(_epgData) // Create new ArrayList instance
        }*//*
    }

    // Add multiple items without checking for duplicates (faster for bulk inserts)
    fun addEPGItemsBulk(items: List<EPGDataItem>) {
        synchronized(epgDataLock) {
            _epgData.addAll(items)
            updateMaps()
            _epgDataState.value = ArrayList(_epgData) // Create new ArrayList instance
        }
    }

    // Delete EPG item by ID
    fun deleteEPGItemById(itemId: String): Boolean {
        synchronized(epgDataLock) {
            val iterator = _epgData.iterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                if (item._id == itemId) {
                    iterator.remove()
                    channelMap.remove(item.channelId)
                    idMap.remove(item._id)
                    _epgDataState.value = ArrayList(_epgData) // Create new ArrayList instance
                    return true
                }
            }
            return false
        }
    }

    // Delete EPG item by channel ID
    fun deleteEPGItemByChannelId(channelId: String): Boolean {
        synchronized(epgDataLock) {
            val iterator = _epgData.iterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                if (item.channelId == channelId) {
                    iterator.remove()
                    channelMap.remove(item.channelId)
                    idMap.remove(item._id)
                    _epgDataState.value = ArrayList(_epgData) // Create new ArrayList instance
                    return true
                }
            }
            return false
        }
    }

    // Delete multiple items by IDs
    fun deleteEPGItemsByIds(itemIds: List<String>): Int {
        synchronized(epgDataLock) {
            var deletedCount = 0
            val iterator = _epgData.iterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                if (item._id in itemIds) {
                    iterator.remove()
                    channelMap.remove(item.channelId)
                    idMap.remove(item._id)
                    deletedCount++
                }
            }
            if (deletedCount > 0) {
                _epgDataState.value = ArrayList(_epgData) // Create new ArrayList instance
            }
            return deletedCount
        }
    }

    // Get EPG item by ID (optimized)
    fun getEPGItemById(itemId: String): EPGDataItem? {
        return idMap[itemId]
    }

    // Get EPG item by channel ID (optimized)
    fun getEPGItemByChannelId(channelId: String): EPGDataItem? {
        return channelMap[channelId]
    }

    // Get all EPG items
    fun getAllEPGItems(): List<EPGDataItem> {
        return synchronized(epgDataLock) {
            _epgData.toList()
        }
    }

    // Get all EPG items as ArrayList
    fun getAllEPGItemsAsArrayList(): ArrayList<EPGDataItem> {
        return synchronized(epgDataLock) {
            ArrayList(_epgData)
        }
    }

    // Search EPG items with performance optimization
    fun searchEPGItems(query: String): List<EPGDataItem> {
        if (query.length < 2) return emptyList()

        return synchronized(epgDataLock) {
            _epgData.filter { item ->
                item.title?.contains(query, ignoreCase = true) == true ||
                        item.description?.contains(query, ignoreCase = true) == true ||
                        item.displayName?.contains(query, ignoreCase = true) == true
            }.take(50) // Limit results for performance
        }
    }

    // Search EPG items and return as ArrayList
    fun searchEPGItemsAsArrayList(query: String): ArrayList<EPGDataItem> {
        return ArrayList(searchEPGItems(query))
    }

    // Update existing EPG item
    fun updateEPGItem(updatedItem: EPGDataItem): Boolean {
        synchronized(epgDataLock) {
            val index = _epgData.indexOfFirst { it._id == updatedItem._id }
            if (index != -1) {
                _epgData[index] = updatedItem
                updateMaps()
                _epgDataState.value = ArrayList(_epgData) // Create new ArrayList instance
                return true
            }
            return false
        }
    }

    // Update multiple EPG items
    fun updateEPGItems(updatedItems: List<EPGDataItem>): Int {
        synchronized(epgDataLock) {
            var updatedCount = 0
            updatedItems.forEach { updatedItem ->
                val index = _epgData.indexOfFirst { it._id == updatedItem._id }
                if (index != -1) {
                    _epgData[index] = updatedItem
                    updatedCount++
                }
            }
            if (updatedCount > 0) {
                updateMaps()
                _epgDataState.value = ArrayList(_epgData) // Create new ArrayList instance
            }
            return updatedCount
        }
    }

    // Clear all data (called when app closes)
    fun clearAppEPGData() {
        synchronized(epgDataLock) {
            _epgDataState.value = ArrayList() // Empty ArrayList
            _loadingState.value = DataState.Idle
        }
    }
    // Clear all data (called when app closes)
    fun clearAllData() {
        synchronized(epgDataLock) {
            _epgData.clear()
            channelMap.clear()
            idMap.clear()
            _epgDataState.value = ArrayList() // Empty ArrayList
            _loadingState.value = DataState.Idle
        }
    }

    // Get total count
    fun getItemCount(): Int {
        return synchronized(epgDataLock) {
            _epgData.size
        }
    }

    // Check if data is empty
    fun isEmpty(): Boolean {
        return synchronized(epgDataLock) {
            _epgData.isEmpty()
        }
    }

    // Get sublist of EPG data (for pagination)
    fun getEPGDataSublist(fromIndex: Int, toIndex: Int): List<EPGDataItem> {
        return synchronized(epgDataLock) {
            if (fromIndex >= _epgData.size) {
                emptyList()
            } else {
                val endIndex = toIndex.coerceAtMost(_epgData.size)
                _epgData.subList(fromIndex, endIndex)
            }
        }
    }

    // Get sublist as ArrayList
    fun getEPGDataSublistAsArrayList(fromIndex: Int, toIndex: Int): ArrayList<EPGDataItem> {
        return ArrayList(getEPGDataSublist(fromIndex, toIndex))
    }

    // Memory optimization methods
    fun optimizeMemory() {
        // Call this periodically if needed
        _epgData.trimToSize() // Trim the ArrayList capacity to current size
        System.gc()
    }

    // Private method to update lookup maps
    private fun updateMaps() {
        channelMap.clear()
        idMap.clear()
        _epgData.forEach { item ->
            item.channelId?.let { channelMap[it] = item }
            item._id?.let { idMap[it] = item }
        }
    }

    // Simulated API call - replace with your actual API
    private suspend fun apiCallToGetEPGData(): List<EPGDataItem> {
        // Your API implementation here
        return emptyList()
    }*/
}