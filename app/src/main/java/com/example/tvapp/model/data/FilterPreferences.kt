package com.example.tvapp.model.data

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class FilterPreferences(private val dataStore: DataStore<Preferences>) {

    private val genreKey = stringPreferencesKey("filter_genre")
    private val languageKey = stringPreferencesKey("filter_language")
//    private val countryKey = stringPreferencesKey("filter_country")
//    private val sortOrderKey = stringPreferencesKey("filter_sort_order")

    val filterFlow: Flow<FilterState> = dataStore.data.map { prefs ->
        FilterState(
            genre = prefs[genreKey],
            language = prefs[languageKey],
//            country = prefs[countryKey],
//            sortOrder = prefs[sortOrderKey]
        )
    }

    fun saveFilter(scope: CoroutineScope, state: FilterState) {
        scope.launch {
            dataStore.edit { prefs ->
                state.genre?.let { prefs[genreKey] = it } ?: prefs.remove(genreKey)
                state.language?.let { prefs[languageKey] = it } ?: prefs.remove(languageKey)
//                state.country?.let { prefs[countryKey] = it } ?: prefs.remove(countryKey)
//                state.sortOrder?.let { prefs[sortOrderKey] = it } ?: prefs.remove(sortOrderKey)
            }
        }
    }
}
