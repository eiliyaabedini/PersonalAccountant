package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.domain.model.Asset
import ir.act.personalAccountant.domain.repository.AssetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ManageAssetsUseCase @Inject constructor(
    private val repository: AssetRepository
) {
    suspend fun addAsset(
        name: String,
        type: String,
        amount: Double,
        currency: String,
        quantity: Double = 1.0,
        notes: String? = null
    ): Long {
        if (amount < 0) {
            throw IllegalArgumentException("Amount cannot be negative")
        }
        if (quantity <= 0) {
            throw IllegalArgumentException("Quantity must be greater than 0")
        }
        if (name.isBlank()) {
            throw IllegalArgumentException("Asset name cannot be empty")
        }
        if (type.isBlank()) {
            throw IllegalArgumentException("Asset type cannot be empty")
        }

        val asset = Asset(
            name = name.trim(),
            type = type.trim(),
            amount = amount,
            currency = currency,
            quantity = quantity,
            notes = notes?.trim()
        )

        return repository.addAsset(asset)
    }

    suspend fun updateAsset(
        id: Long,
        name: String,
        type: String,
        amount: Double,
        currency: String,
        quantity: Double = 1.0,
        notes: String? = null
    ) {
        if (amount < 0) {
            throw IllegalArgumentException("Amount cannot be negative")
        }
        if (quantity <= 0) {
            throw IllegalArgumentException("Quantity must be greater than 0")
        }
        if (name.isBlank()) {
            throw IllegalArgumentException("Asset name cannot be empty")
        }
        if (type.isBlank()) {
            throw IllegalArgumentException("Asset type cannot be empty")
        }

        val asset = Asset(
            id = id,
            name = name.trim(),
            type = type.trim(),
            amount = amount,
            currency = currency,
            quantity = quantity,
            notes = notes?.trim(),
            updatedAt = System.currentTimeMillis()
        )

        repository.updateAsset(asset)
    }

    suspend fun deleteAsset(assetId: Long) {
        repository.deleteAsset(assetId)
    }

    suspend fun getAssetById(id: Long): Asset? {
        return repository.getAssetById(id)
    }

    fun getAllAssets(): Flow<List<Asset>> {
        return repository.getAllAssets()
    }

    fun getTotalAssets(): Flow<Double> {
        return repository.getTotalAssets()
    }

    fun getAllAssetTypes(): Flow<List<String>> {
        return repository.getAllAssetTypes()
    }

    fun getAssetsByType(type: String): Flow<List<Asset>> {
        return repository.getAssetsByType(type)
    }
}