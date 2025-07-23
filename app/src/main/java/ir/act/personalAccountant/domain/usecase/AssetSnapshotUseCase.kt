package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.domain.model.AssetSnapshot
import kotlinx.coroutines.flow.Flow

interface AssetSnapshotUseCase {
    suspend fun createAssetSnapshot(
        assetId: Long,
        amount: Double,
        quantity: Double,
        currency: String
    )

    suspend fun getLatestAssetSnapshots(): List<AssetSnapshot>
    suspend fun getAssetSnapshotsHistory(assetId: Long): Flow<List<AssetSnapshot>>
    suspend fun getDailyNetWorthHistory(fromDate: Long? = null): List<DailyNetWorth>
    suspend fun getCurrentNetWorth(): Double
    suspend fun createMissingAssetSnapshots()
}

data class DailyNetWorth(
    val date: String,
    val totalValue: Double,
    val timestamp: Long
)