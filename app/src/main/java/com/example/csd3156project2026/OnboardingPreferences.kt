package com.example.csd3156project2026

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("onboarding")

class OnboardingPreferences(private val context: Context) {

    companion object {
        private val HAS_SEEN_TUTORIAL = booleanPreferencesKey("has_seen_tutorial")
    }

    val hasSeenTutorial: Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[HAS_SEEN_TUTORIAL] ?: false
        }

    suspend fun setHasSeenTutorial(seen: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAS_SEEN_TUTORIAL] = seen
        }
    }
}