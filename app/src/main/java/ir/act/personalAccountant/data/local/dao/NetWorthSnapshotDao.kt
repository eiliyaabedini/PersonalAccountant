package ir.act.personalAccountant.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ir.act.personalAccountant.data.local.entity.NetWorthSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NetWorthSnapshotDao {

    @Insert
    suspend fun insertSnapshot(snapshot: NetWorthSnapshotEntity): Long

    @Query("SELECT * FROM net_worth_snapshots ORDER BY calculatedAt DESC")
    fun getAllSnapshots(): Flow<List<NetWorthSnapshotEntity>>

    @Query("SELECT * FROM net_worth_snapshots WHERE calculatedAt >= :fromDate ORDER BY calculatedAt DESC")
    fun getSnapshotsFromDate(fromDate: Long): Flow<List<NetWorthSnapshotEntity>>

    @Query("SELECT * FROM net_worth_snapshots ORDER BY calculatedAt DESC LIMIT 1")
    suspend fun getLatestSnapshot(): NetWorthSnapshotEntity?

    @Query("SELECT * FROM net_worth_snapshots WHERE calculatedAt >= :startTime AND calculatedAt <= :endTime ORDER BY calculatedAt DESC")
    fun getSnapshotsByDateRange(startTime: Long, endTime: Long): Flow<List<NetWorthSnapshotEntity>>

    @Query("DELETE FROM net_worth_snapshots WHERE calculatedAt < :olderThan")
    suspend fun deleteOldSnapshots(olderThan: Long)
}