package ir.act.personalAccountant.domain.model

data class AssetAnalysisResult(
    val assetName: String,
    val assetType: String,
    val amountPerUnit: String, // Changed to String for precision
    val quantity: String, // Changed to String for precision
    val currency: String,
    val confidence: Float = 0.0f, // AI confidence level 0.0 to 1.0
    val rawData: Map<String, String> = emptyMap() // Additional extracted data for debugging
)