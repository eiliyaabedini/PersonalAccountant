package ir.act.personalAccountant.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ir.act.personalAccountant.data.local.entity.AssetSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetSnapshotDao {

    @Insert
    suspend fun insertSnapshot(snapshot: AssetSnapshotEntity)

    @Query("SELECT * FROM asset_snapshots WHERE assetId = :assetId ORDER BY timestamp DESC")
    fun getSnapshotsForAsset(assetId: Long): Flow<List<AssetSnapshotEntity>>

    @Query("SELECT * FROM asset_snapshots WHERE assetId = :assetId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestSnapshotForAsset(assetId: Long): AssetSnapshotEntity?

    @Query(
        """
        SELECT * FROM asset_snapshots a1 
        WHERE timestamp = (
            SELECT MAX(timestamp) 
            FROM asset_snapshots a2 
            WHERE a2.assetId = a1.assetId 
            AND DATE(a2.timestamp/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch')
        )
        AND DATE(timestamp/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch')
    """
    )
    suspend fun getLatestSnapshotsForDate(date: Long): List<AssetSnapshotEntity>

    @Query(
        """
        SELECT * FROM asset_snapshots a1 
        WHERE timestamp = (
            SELECT MAX(timestamp) 
            FROM asset_snapshots a2 
            WHERE a2.assetId = a1.assetId
        )
    """
    )
    suspend fun getLatestSnapshotsForAllAssets(): List<AssetSnapshotEntity>

    @Query(
        """
        SELECT * FROM asset_snapshots a1 
        WHERE timestamp = (
            SELECT MAX(timestamp) 
            FROM asset_snapshots a2 
            WHERE a2.assetId = a1.assetId 
            AND a2.timestamp >= :fromDate
        )
        AND timestamp >= :fromDate
    """
    )
    suspend fun getLatestSnapshotsFromDate(fromDate: Long): List<AssetSnapshotEntity>

    @Query(
        """
        SELECT DISTINCT DATE(timestamp/1000, 'unixepoch') as date, 
        SUM(totalValue) as totalValue,
        timestamp
        FROM asset_snapshots a1 
        WHERE timestamp = (
            SELECT MAX(timestamp) 
            FROM asset_snapshots a2 
            WHERE a2.assetId = a1.assetId 
            AND DATE(a2.timestamp/1000, 'unixepoch') = DATE(a1.timestamp/1000, 'unixepoch')
        )
        AND (:fromDate IS NULL OR timestamp >= :fromDate)
        GROUP BY DATE(timestamp/1000, 'unixepoch')
        ORDER BY timestamp ASC
    """
    )
    suspend fun getDailyNetWorthSnapshots(fromDate: Long?): List<DailyNetWorthSnapshot>

    @Query("DELETE FROM asset_snapshots WHERE timestamp < :cutoffTime")
    suspend fun deleteOldSnapshots(cutoffTime: Long)
}

data class DailyNetWorthSnapshot(
    val date: String,
    val totalValue: Double,
    val timestamp: Long
)