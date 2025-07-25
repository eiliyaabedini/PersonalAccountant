package ir.act.personalAccountant.domain.sync

import com.google.firebase.firestore.Exclude

data class CloudExpense(
    val id: String = "",
    val amount: Double = 0.0,
    val timestamp: Long = 0,
    val tag: String = "",
    val imageUrls: List<String> = emptyList(),
    val destinationAmount: Double? = null,
    val destinationCurrency: String? = null,
    val lastModified: Long = 0,
    @get:Exclude
    val syncStatus: SyncStatus = SyncStatus.PENDING
) {
    // Helper property to get localId from document ID (since we use local ID as document ID)
    val localId: Long get() = id.toLongOrNull() ?: 0L
}