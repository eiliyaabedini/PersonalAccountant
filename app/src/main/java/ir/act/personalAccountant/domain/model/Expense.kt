package ir.act.personalAccountant.domain.model

data class Expense(
    val id: Long = 0,
    val amount: Double,
    val timestamp: Long,
    val tag: String = "General"
)