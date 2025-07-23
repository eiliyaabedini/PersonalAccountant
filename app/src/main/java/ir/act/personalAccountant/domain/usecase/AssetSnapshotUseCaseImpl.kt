package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.data.local.dao.AssetDao
import ir.act.personalAccountant.data.local.dao.AssetSnapshotDao
import ir.act.personalAccountant.data.local.entity.AssetSnapshotEntity
import ir.act.personalAccountant.domain.model.AssetSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssetSnapshotUseCaseImpl @Inject constructor(
    private val assetSnapshotDao: AssetSnapshotDao,
    private val assetDao: AssetDao
) : AssetSnapshotUseCase {

    override suspend fun createAssetSnapshot(
        assetId: Long,
        amount: Double,
        quantity: Double,
        currency: String
    ) {
        val totalValue = amount * quantity
        val snapshot = AssetSnapshotEntity(
            assetId = assetId,
            amount = amount,
            quantity = quantity,
            totalValue = totalValue,
            currency = currency,
            timestamp = System.currentTimeMillis()
        )
        assetSnapshotDao.insertSnapshot(snapshot)
    }

    override suspend fun getLatestAssetSnapshots(): List<AssetSnapshot> {
        val snapshots = assetSnapshotDao.getLatestSnapshotsForAllAssets()
        val assets = assetDao.getAllAssetsOnce()

        return snapshots.mapNotNull { snapshotEntity ->
            val asset = assets.find { it.id == snapshotEntity.assetId }
            asset?.let {
                AssetSnapshot(
                    id = snapshotEntity.id,
                    assetId = snapshotEntity.assetId,
                    assetName = asset.name,
                    assetType = asset.type,
                    amount = snapshotEntity.amount,
                    quantity = snapshotEntity.quantity,
                    totalValue = snapshotEntity.totalValue,
                    currency = snapshotEntity.currency,
                    timestamp = snapshotEntity.timestamp
                )
            }
        }
    }

    override suspend fun getAssetSnapshotsHistory(assetId: Long): Flow<List<AssetSnapshot>> {
        return assetSnapshotDao.getSnapshotsForAsset(assetId).map { snapshots ->
            val asset = assetDao.getAssetById(assetId)
            snapshots.map { snapshotEntity ->
                AssetSnapshot(
                    id = snapshotEntity.id,
                    assetId = snapshotEntity.assetId,
                    assetName = asset?.name ?: "Unknown",
                    assetType = asset?.type ?: "Unknown",
                    amount = snapshotEntity.amount,
                    quantity = snapshotEntity.quantity,
                    totalValue = snapshotEntity.totalValue,
                    currency = snapshotEntity.currency,
                    timestamp = snapshotEntity.timestamp
                )
            }
        }
    }

    override suspend fun getDailyNetWorthHistory(fromDate: Long?): List<DailyNetWorth> {
        val dailySnapshots = assetSnapshotDao.getDailyNetWorthSnapshots(fromDate)
        return dailySnapshots.map { snapshot ->
            DailyNetWorth(
                date = snapshot.date,
                totalValue = snapshot.totalValue,
                timestamp = snapshot.timestamp
            )
        }
    }

    override suspend fun getCurrentNetWorth(): Double {
        val latestSnapshots = assetSnapshotDao.getLatestSnapshotsForAllAssets()
        return latestSnapshots.sumOf { it.totalValue }
    }

    override suspend fun createMissingAssetSnapshots() {
        // Get all assets
        val allAssets = assetDao.getAllAssetsOnce()

        // Get all asset IDs that have snapshots
        val assetsWithSnapshots =
            assetSnapshotDao.getLatestSnapshotsForAllAssets().map { it.assetId }.toSet()

        // Find assets without snapshots
        val assetsWithoutSnapshots = allAssets.filter { asset ->
            asset.id !in assetsWithSnapshots
        }

        // Create snapshots for assets without them
        assetsWithoutSnapshots.forEach { asset ->
            createAssetSnapshot(
                assetId = asset.id,
                amount = asset.amount,
                quantity = asset.quantity,
                currency = asset.currency
            )
        }
    }
}