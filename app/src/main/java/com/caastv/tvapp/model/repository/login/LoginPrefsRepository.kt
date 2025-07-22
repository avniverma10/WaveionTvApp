package com.caastv.tvapp.model.repository.login

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginPrefsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        val KEY_USERNAME = stringPreferencesKey("username")
        val KEY_PASSWORD = stringPreferencesKey("password")
        val KEY_REMEMBER = booleanPreferencesKey("remember_me")
    }

    val loginInfoFlow: Flow<LoginInfo> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            LoginInfo(
                username = prefs[KEY_USERNAME] ?: "",
                password = prefs[KEY_PASSWORD] ?: "",
                rememberMe = prefs[KEY_REMEMBER] ?: false
            )
        }

    suspend fun saveLoginInfo(username: String, password: String, rememberMe: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_USERNAME] = username
            prefs[KEY_PASSWORD] = password
            prefs[KEY_REMEMBER] = rememberMe
        }
    }

    suspend fun clearLoginInfo() {
        dataStore.edit {
            it.remove(KEY_USERNAME)
            it.remove(KEY_PASSWORD)
            it.remove(KEY_REMEMBER)
        }
    }
}

data class LoginInfo(
    val username: String = "",
    val password: String = "",
    val rememberMe: Boolean = false
)
