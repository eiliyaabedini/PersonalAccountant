package ir.act.personalAccountant.ai.domain.model

data class AssetAIAnalysisResult(
    val assetName: String,
    val assetType: String,
    val amountPerUnit: String, // Changed to String for precision
    val quantity: String, // Changed to String for precision
    val currency: String,
    val confidence: Float
)