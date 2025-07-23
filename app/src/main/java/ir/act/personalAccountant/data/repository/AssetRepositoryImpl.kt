package ir.act.personalAccountant.data.repository

import ir.act.personalAccountant.data.local.dao.AssetDao
import ir.act.personalAccountant.data.local.dao.NetWorthSnapshotDao
import ir.act.personalAccountant.data.local.entity.AssetEntity
import ir.act.personalAccountant.data.local.entity.NetWorthSnapshotEntity
import ir.act.personalAccountant.domain.model.Asset
import ir.act.personalAccountant.domain.model.NetWorthSnapshot
import ir.act.personalAccountant.domain.repository.AssetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssetRepositoryImpl @Inject constructor(
    private val assetDao: AssetDao,
    private val netWorthSnapshotDao: NetWorthSnapshotDao
) : AssetRepository {

    override suspend fun addAsset(asset: Asset): Long {
        return assetDao.insertAsset(asset.toEntity())
    }

    override suspend fun updateAsset(asset: Asset) {
        assetDao.updateAsset(asset.toEntity())
    }

    override suspend fun deleteAsset(assetId: Long) {
        assetDao.getAssetById(assetId)?.let { entity ->
            assetDao.deleteAsset(entity)
        }
    }

    override suspend fun getAssetById(id: Long): Asset? {
        return assetDao.getAssetById(id)?.toDomain()
    }

    override fun getAllAssets(): Flow<List<Asset>> {
        return assetDao.getAllAssets().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTotalAssets(): Flow<Double> {
        return assetDao.getTotalAssets().map { total ->
            total ?: 0.0
        }
    }

    override fun getAllAssetTypes(): Flow<List<String>> {
        return assetDao.getAllAssetTypes()
    }

    override fun getAssetsByType(type: String): Flow<List<Asset>> {
        return assetDao.getAssetsByType(type).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveNetWorthSnapshot(snapshot: NetWorthSnapshot): Long {
        return netWorthSnapshotDao.insertSnapshot(snapshot.toEntity())
    }

    override fun getAllNetWorthSnapshots(): Flow<List<NetWorthSnapshot>> {
        return netWorthSnapshotDao.getAllSnapshots().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getNetWorthSnapshotsFromDate(fromDate: Long): Flow<List<NetWorthSnapshot>> {
        return netWorthSnapshotDao.getSnapshotsFromDate(fromDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getLatestNetWorthSnapshot(): NetWorthSnapshot? {
        return netWorthSnapshotDao.getLatestSnapshot()?.toDomain()
    }

    override fun getNetWorthSnapshotsByDateRange(
        startTime: Long,
        endTime: Long
    ): Flow<List<NetWorthSnapshot>> {
        return netWorthSnapshotDao.getSnapshotsByDateRange(startTime, endTime).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    private fun Asset.toEntity(): AssetEntity {
        return AssetEntity(
            id = id,
            name = name,
            type = type,
            amount = amount,
            currency = currency,
            quantity = quantity,
            notes = notes,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun AssetEntity.toDomain(): Asset {
        return Asset(
            id = id,
            name = name,
            type = type,
            amount = amount,
            currency = currency,
            quantity = quantity,
            notes = notes,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun NetWorthSnapshot.toEntity(): NetWorthSnapshotEntity {
        return NetWorthSnapshotEntity(
            id = id,
            totalAssets = totalAssets,
            netWorth = netWorth,
            currency = currency,
            calculatedAt = calculatedAt
        )
    }

    private fun NetWorthSnapshotEntity.toDomain(): NetWorthSnapshot {
        return NetWorthSnapshot(
            id = id,
            totalAssets = totalAssets,
            netWorth = netWorth,
            currency = currency,
            calculatedAt = calculatedAt
        )
    }
}