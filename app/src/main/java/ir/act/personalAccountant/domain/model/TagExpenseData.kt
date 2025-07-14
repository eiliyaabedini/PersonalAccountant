package ir.act.personalAccountant.domain.model

data class TagExpenseData(
    val tag: String,
    val totalAmount: Double,
    val percentage: Float,
    val averageDailyAmount: Double,
    val color: androidx.compose.ui.graphics.Color
)