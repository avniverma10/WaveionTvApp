package com.caastv.tvapp.model.data


import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.core.DataStore
import com.caastv.tvapp.model.data.filter.PanMetroGenreFilter
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class FilterPreferences(private val dataStore: DataStore<Preferences>) {

    private val genreFilterDataStore = stringPreferencesKey("genre_filter_prefs")
    private val genreKey = stringPreferencesKey("filter_genre")
    private val languageKey = stringPreferencesKey("filter_language")

//    private val countryKey = stringPreferencesKey("filter_country")
//    private val sortOrderKey = stringPreferencesKey("filter_sort_order")

    var filterFlow: Flow<FilterState> = dataStore.data.map { prefs ->
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


    fun clearFilter(scope: CoroutineScope) {
        scope.launch {
            dataStore.edit { prefs ->
                prefs[genreKey]="All"
                prefs[languageKey]="All"
            }
        }
    }


    fun saveGenreSelection(genreFilter: PanMetroGenreFilter,scope: CoroutineScope){
        scope.launch {
            val jsonString = Gson().toJson(genreFilter)
            dataStore.edit { prefs ->
                prefs[genreFilterDataStore] = jsonString
            }
        }

    }

    /** Read the filter object, or null if not set or parsing fails */
    val genreSelectionFlow: Flow<PanMetroGenreFilter?> = dataStore.data
        .map { prefs ->
            prefs[genreFilterDataStore]?.let { jsonString ->
                try {
                    Gson().fromJson(
                        jsonString,
                        PanMetroGenreFilter::class.java
                    )
                } catch (e: Exception) {
                    null
                }
            }
        }
}
