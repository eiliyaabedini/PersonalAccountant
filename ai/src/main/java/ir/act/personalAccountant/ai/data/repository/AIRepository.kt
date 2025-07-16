package ir.act.personalAccountant.ai.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val OPENAI_API_KEY = stringPreferencesKey("openai_api_key")

    val apiKey: Flow<String> = dataStore.data.map { preferences ->
        preferences[OPENAI_API_KEY] ?: ""
    }

    suspend fun saveApiKey(apiKey: String) {
        dataStore.edit { preferences ->
            preferences[OPENAI_API_KEY] = apiKey
        }
    }

    suspend fun clearApiKey() {
        dataStore.edit { preferences ->
            preferences.remove(OPENAI_API_KEY)
        }
    }
}