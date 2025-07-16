package ir.act.personalAccountant.data.repository

import ir.act.personalAccountant.domain.model.SyncMetadata
import ir.act.personalAccountant.domain.model.SyncState
import ir.act.personalAccountant.domain.repository.SettingsRepository
import ir.act.personalAccountant.domain.repository.SyncStateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncStateRepositoryImpl @Inject constructor(
    private val settingsRepository: SettingsRepository
) : SyncStateRepository {

    companion object {
        private const val SYNC_STATE_PREFIX = "sync_state_"
        private const val SYNC_METADATA_PREFIX = "sync_metadata_"
        private const val LAST_SYNC_TIMESTAMP_KEY = "last_sync_timestamp"
    }

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getSyncState(expenseId: Long): SyncState? = withContext(Dispatchers.IO) {
        try {
            val key = "$SYNC_STATE_PREFIX$expenseId"
            val jsonString = settingsRepository.getStringSetting(key)
            jsonString?.let { json.decodeFromString<SyncState>(it) }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun saveSyncState(syncState: SyncState) = withContext(Dispatchers.IO) {
        try {
            val key = "$SYNC_STATE_PREFIX${syncState.expenseId}"
            val jsonString = json.encodeToString(syncState)
            settingsRepository.saveStringSetting(key, jsonString)
        } catch (e: Exception) {
            // Ignore serialization errors
        }
    }

    override suspend fun getSyncMetadata(spreadsheetId: String): SyncMetadata? =
        withContext(Dispatchers.IO) {
            try {
                val key = "$SYNC_METADATA_PREFIX$spreadsheetId"
                val jsonString = settingsRepository.getStringSetting(key)
                jsonString?.let { json.decodeFromString<SyncMetadata>(it) }
            } catch (e: Exception) {
                null
            }
        }

    override suspend fun saveSyncMetadata(syncMetadata: SyncMetadata) =
        withContext(Dispatchers.IO) {
            try {
                val key = "$SYNC_METADATA_PREFIX${syncMetadata.spreadsheetId}"
                val jsonString = json.encodeToString(syncMetadata)
                settingsRepository.saveStringSetting(key, jsonString)
            } catch (e: Exception) {
                // Ignore serialization errors
            }
        }

    override suspend fun clearSyncState(expenseId: Long) = withContext(Dispatchers.IO) {
        try {
            val key = "$SYNC_STATE_PREFIX$expenseId"
            settingsRepository.saveStringSetting(key, "")
        } catch (e: Exception) {
            // Ignore errors
        }
    }

    override suspend fun clearAllSyncStates() = withContext(Dispatchers.IO) {
        // This would require a way to get all keys from settings
        // For now, we'll implement it when needed
    }

    override suspend fun getLastSyncTimestamp(): Long? = withContext(Dispatchers.IO) {
        try {
            val timestampString = settingsRepository.getStringSetting(LAST_SYNC_TIMESTAMP_KEY)
            timestampString?.toLongOrNull()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun saveLastSyncTimestamp(timestamp: Long) = withContext(Dispatchers.IO) {
        try {
            settingsRepository.saveStringSetting(LAST_SYNC_TIMESTAMP_KEY, timestamp.toString())
        } catch (e: Exception) {
            // Ignore errors
        }
    }
}