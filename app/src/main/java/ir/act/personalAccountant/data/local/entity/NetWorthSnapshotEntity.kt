package ir.act.personalAccountant.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "net_worth_snapshots")
data class NetWorthSnapshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val totalAssets: Double,
    val netWorth: Double,
    val currency: String,
    val calculatedAt: Long = System.currentTimeMillis()
)