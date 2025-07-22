package ir.act.personalAccountant.domain.model

data class NotificationData(
    val dayOfMonth: Int,
    val monthName: String,
    val todayExpenses: Double,
    val totalExpenses: Double,
    val dailyBudget: Double?,
    val formattedTodayExpenses: String,
    val formattedTotalExpenses: String,
    val formattedBudget: String?,
    val formattedRemaining: String?,
    val isBudgetConfigured: Boolean
)