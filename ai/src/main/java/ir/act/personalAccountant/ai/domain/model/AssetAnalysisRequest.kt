package ir.act.personalAccountant.ai.domain.model

data class AssetAnalysisRequest(
    val imageBase64: String,
    val availableAssetTypes: List<String>
)