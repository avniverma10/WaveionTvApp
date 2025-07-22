package com.caastv.tvapp.di

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.core.DataStore
import com.caastv.tvapp.model.data.DataStoreManager
import com.caastv.tvapp.model.data.FilterPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Create the datastore instance extension
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "filters")

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDataStoreManager(@ApplicationContext context: Context): DataStoreManager {
        return DataStoreManager(context)
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideFilterPreferences(dataStore: DataStore<Preferences>): FilterPreferences {
        return FilterPreferences(dataStore)
    }
}
