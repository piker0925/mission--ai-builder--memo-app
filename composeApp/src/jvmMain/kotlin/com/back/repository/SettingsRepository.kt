package com.back.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

class SettingsRepository {
    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { File(System.getProperty("user.home"), ".notepad_settings.preferences_pb") }
    )

    private object PreferencesKeys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val FONT_SIZE = intPreferencesKey("font_size")
    }

    val darkThemeFlow: Flow<Boolean> = dataStore.data.map { it[PreferencesKeys.DARK_THEME] ?: false }
    val fontSizeFlow: Flow<Int> = dataStore.data.map { it[PreferencesKeys.FONT_SIZE] ?: 14 }

    suspend fun updateDarkTheme(isDark: Boolean) {
        dataStore.edit { it[PreferencesKeys.DARK_THEME] = isDark }
    }

    suspend fun updateFontSize(size: Int) {
        dataStore.edit { it[PreferencesKeys.FONT_SIZE] = size }
    }
}