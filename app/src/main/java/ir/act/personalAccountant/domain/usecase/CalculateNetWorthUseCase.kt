package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.domain.model.NetWorthSnapshot
import ir.act.personalAccountant.domain.repository.AssetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CalculateNetWorthUseCase @Inject constructor(
    private val repository: AssetRepository
) {
    suspend fun calculateAndSaveNetWorth(
        assetValues: Map<Long, Double>,
        targetCurrency: String
    ): Long {
        val assets = repository.getAllAssets().first()

        val totalAssets = assets.sumOf { asset ->
            val valueInTargetCurrency = assetValues[asset.id] ?: 0.0
            valueInTargetCurrency * asset.quantity
        }

        val snapshot = NetWorthSnapshot(
            totalAssets = totalAssets,
            netWorth = totalAssets, // For now, net worth equals total assets (no liabilities)
            currency = targetCurrency
        )

        return repository.saveNetWorthSnapshot(snapshot)
    }

    fun getAllNetWorthSnapshots(): Flow<List<NetWorthSnapshot>> {
        return repository.getAllNetWorthSnapshots()
    }

    suspend fun getLatestNetWorthSnapshot(): NetWorthSnapshot? {
        return repository.getLatestNetWorthSnapshot()
    }

    fun getNetWorthSnapshotsByDateRange(
        startTime: Long,
        endTime: Long
    ): Flow<List<NetWorthSnapshot>> {
        return repository.getNetWorthSnapshotsByDateRange(startTime, endTime)
    }
}