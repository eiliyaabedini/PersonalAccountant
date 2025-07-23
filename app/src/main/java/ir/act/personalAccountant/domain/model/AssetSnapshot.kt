package ir.act.personalAccountant.domain.model

data class AssetSnapshot(
    val id: Long = 0,
    val assetId: Long,
    val assetName: String,
    val assetType: String,
    val amount: Double, // price per unit
    val quantity: Double,
    val totalValue: Double, // amount * quantity
    val currency: String,
    val timestamp: Long = System.currentTimeMillis()
)