package ir.act.personalAccountant.ai.domain.model

data class ReceiptAnalysisRequest(
    val imageBase64: String,
    val availableCategories: List<String>,
    val currencySymbol: String = "$"
)