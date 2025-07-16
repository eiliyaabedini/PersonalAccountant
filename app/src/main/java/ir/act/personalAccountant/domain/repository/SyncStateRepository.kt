package ir.act.personalAccountant.domain.repository

import ir.act.personalAccountant.domain.model.SyncMetadata
import ir.act.personalAccountant.domain.model.SyncState

interface SyncStateRepository {
    suspend fun getSyncState(expenseId: Long): SyncState?
    suspend fun saveSyncState(syncState: SyncState)
    suspend fun getSyncMetadata(spreadsheetId: String): SyncMetadata?
    suspend fun saveSyncMetadata(syncMetadata: SyncMetadata)
    suspend fun clearSyncState(expenseId: Long)
    suspend fun clearAllSyncStates()
    suspend fun getLastSyncTimestamp(): Long?
    suspend fun saveLastSyncTimestamp(timestamp: Long)
}