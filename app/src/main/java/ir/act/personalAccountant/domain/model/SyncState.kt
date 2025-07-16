package ir.act.personalAccountant.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SyncState(
    val expenseId: Long,
    val lastSyncTimestamp: Long,
    val syncedImageUrl: String? = null,
    val syncedToSheetId: String? = null,
    val expenseHash: String // Hash of expense data to detect changes
)

@Serializable
data class SyncMetadata(
    val spreadsheetId: String,
    val lastFullSync: Long,
    val syncStates: List<SyncState>
)