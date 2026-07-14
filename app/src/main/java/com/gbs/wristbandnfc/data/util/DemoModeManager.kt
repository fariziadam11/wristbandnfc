package com.gbs.wristbandnfc.data.util

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages demo mode auto-detection based on backend availability
 */
@Singleton
class DemoModeManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient
) {
    companion object {
        private const val PREFS_NAME = "demo_prefs"
        private val DEMO_MODE_KEY = booleanPreferencesKey("demo_mode")
        private const val DEFAULT_DEMO_MODE = true
        private const val BACKEND_URL = "http://10.0.2.2:8080"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Auto-detect: returns true if demo mode should be used.
     * Demo mode is used when:
     * 1. Saved preference says demo mode, OR
     * 2. Backend is unreachable
     */
    suspend fun shouldUseDemoMode(): Boolean {
        if (prefs.getBoolean("demo_mode", DEFAULT_DEMO_MODE)) return true
        return !isBackendReachable()
    }

    /**
     * Check if demo mode is enabled in preferences
     */
    fun isDemoModeEnabled(): Boolean = prefs.getBoolean("demo_mode", DEFAULT_DEMO_MODE)

    /**
     * Toggle demo mode
     */
    fun setDemoMode(enabled: Boolean) {
        prefs.edit().putBoolean("demo_mode", enabled).apply()
    }

    private fun isBackendReachable(): Boolean {
        return try {
            val req = Request.Builder().url("$BACKEND_URL/health").get().build()
            okHttpClient.newCall(req).execute().isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
