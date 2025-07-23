package ir.act.personalAccountant.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "asset_snapshots",
    foreignKeys = [
        ForeignKey(
            entity = AssetEntity::class,
            parentColumns = ["id"],
            childColumns = ["assetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["assetId"]), Index(value = ["timestamp"])]
)
data class AssetSnapshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val assetId: Long,
    val amount: Double, // price per unit
    val quantity: Double,
    val totalValue: Double, // amount * quantity
    val currency: String,
    val timestamp: Long = System.currentTimeMillis()
)