package ir.act.personalAccountant.ai.domain.model

data class ReceiptAnalysisResponse(
    val success: Boolean,
    val totalAmount: Double? = null,
    val category: String? = null,
    val confidence: Float = 0f,
    val errorMessage: String? = null
)

data class AIAnalysisResult(
    val total_amount: Double,
    val category: String,
    val confidence: Float,
    val currency_detected: String? = null
)