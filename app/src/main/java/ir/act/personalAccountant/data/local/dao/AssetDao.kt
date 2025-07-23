package ir.act.personalAccountant.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ir.act.personalAccountant.data.local.entity.AssetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {

    @Insert
    suspend fun insertAsset(asset: AssetEntity): Long

    @Update
    suspend fun updateAsset(asset: AssetEntity)

    @Delete
    suspend fun deleteAsset(asset: AssetEntity)

    @Query("SELECT * FROM assets WHERE id = :id")
    suspend fun getAssetById(id: Long): AssetEntity?

    @Query("SELECT * FROM assets ORDER BY updatedAt DESC")
    fun getAllAssets(): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets ORDER BY updatedAt DESC")
    suspend fun getAllAssetsOnce(): List<AssetEntity>

    @Query("SELECT SUM(amount * quantity) FROM assets")
    fun getTotalAssets(): Flow<Double?>

    @Query("SELECT DISTINCT type FROM assets ORDER BY type")
    fun getAllAssetTypes(): Flow<List<String>>

    @Query("SELECT * FROM assets WHERE type = :type ORDER BY updatedAt DESC")
    fun getAssetsByType(type: String): Flow<List<AssetEntity>>
}