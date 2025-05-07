package com.example.tvapp.model.repository.login

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.tvapp.extensions.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginPrefsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        val KEY_USERNAME = stringPreferencesKey("username")
        val KEY_PASSWORD = stringPreferencesKey("password")
    }

    suspend fun saveLoginInfo(username: String, password: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USERNAME] = username
            prefs[KEY_PASSWORD] = password
        }
    }

    suspend fun clearLoginInfo() {
        context.dataStore.edit { prefs ->
            prefs.clear()  // removes *every* key: username, password, is_login, etc.
        }
    }
}
