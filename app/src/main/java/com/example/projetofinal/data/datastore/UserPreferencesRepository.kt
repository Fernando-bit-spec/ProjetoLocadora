package com.example.projetofinal.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val isDarkMode: Boolean = false,
    val userName: String = "Usuário",
    val notificationsEnabled: Boolean = true,
    val sortPreference: String = "Nome (A-Z)",
    val isLoggedIn: Boolean = false,
    val loggedInUser: String = ""
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val USER_NAME = stringPreferencesKey("user_name")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val SORT_PREFERENCE = stringPreferencesKey("sort_preference")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val LOGGED_IN_USER = stringPreferencesKey("logged_in_user")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                isDarkMode = preferences[PreferencesKeys.IS_DARK_MODE] ?: false,
                userName = preferences[PreferencesKeys.USER_NAME] ?: "Usuário",
                notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
                sortPreference = preferences[PreferencesKeys.SORT_PREFERENCE] ?: "Nome (A-Z)",
                isLoggedIn = preferences[PreferencesKeys.IS_LOGGED_IN] ?: false,
                loggedInUser = preferences[PreferencesKeys.LOGGED_IN_USER] ?: ""
            )
        }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] = enabled
        }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_NAME] = name
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setSortPreference(preference: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_PREFERENCE] = preference
        }
    }

    suspend fun setLoggedIn(isLoggedIn: Boolean, username: String = "") {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] = isLoggedIn
            preferences[PreferencesKeys.LOGGED_IN_USER] = username
            if (isLoggedIn && username.isNotBlank()) {
                preferences[PreferencesKeys.USER_NAME] = username
            }
        }
    }
}
