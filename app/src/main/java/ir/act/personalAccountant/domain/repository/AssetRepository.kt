package ir.act.personalAccountant.domain.repository

import ir.act.personalAccountant.domain.model.Asset
import ir.act.personalAccountant.domain.model.NetWorthSnapshot
import kotlinx.coroutines.flow.Flow

interface AssetRepository {
    suspend fun addAsset(asset: Asset): Long
    suspend fun updateAsset(asset: Asset)
    suspend fun deleteAsset(assetId: Long)
    suspend fun getAssetById(id: Long): Asset?
    fun getAllAssets(): Flow<List<Asset>>
    fun getTotalAssets(): Flow<Double>
    fun getAllAssetTypes(): Flow<List<String>>
    fun getAssetsByType(type: String): Flow<List<Asset>>

    suspend fun saveNetWorthSnapshot(snapshot: NetWorthSnapshot): Long
    fun getAllNetWorthSnapshots(): Flow<List<NetWorthSnapshot>>
    fun getNetWorthSnapshotsFromDate(fromDate: Long): Flow<List<NetWorthSnapshot>>
    suspend fun getLatestNetWorthSnapshot(): NetWorthSnapshot?
    fun getNetWorthSnapshotsByDateRange(
        startTime: Long,
        endTime: Long
    ): Flow<List<NetWorthSnapshot>>
}