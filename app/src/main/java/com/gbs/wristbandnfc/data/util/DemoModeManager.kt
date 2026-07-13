package com.gbs.wristbandnfc.data.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.demoDataStore: DataStore<Preferences> by preferencesDataStore(name = "demo_settings")

@Singleton
class DemoModeManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private val DEMO_MODE_KEY = booleanPreferencesKey("demo_mode")
        private const val DEFAULT_DEMO_MODE = true // Demo mode ON by default
    }

    val isDemoModeEnabled: Flow<Boolean> = context.demoDataStore.data.map { preferences ->
        preferences[DEMO_MODE_KEY] ?: DEFAULT_DEMO_MODE
    }

    suspend fun setDemoMode(enabled: Boolean) {
        context.demoDataStore.edit { preferences ->
            preferences[DEMO_MODE_KEY] = enabled
        }
    }
}
