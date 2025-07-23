package ir.act.personalAccountant.ai.domain.model

data class AssetAnalysisResponse(
    val success: Boolean,
    val assetName: String? = null,
    val assetType: String? = null,
    val amountPerUnit: String? = null, // Changed to String for precision
    val quantity: String? = null, // Changed to String for precision
    val currency: String? = null,
    val confidence: Float = 0.0f,
    val errorMessage: String? = null
)