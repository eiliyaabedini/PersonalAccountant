package ir.act.personalAccountant.domain.model

data class BudgetData(
    val currentDay: Int,
    val totalDaysInMonth: Int,
    val dailyIncome: Double,
    val dailyRent: Double,
    val totalIncomeToDate: Double,
    val totalRentToDate: Double,
    val totalExpensesToDate: Double,
    val budgetStatus: BudgetStatus
)

enum class BudgetStatus {
    GOOD,    // Expenses are well within budget
    MIDDLE,  // Expenses are moderate
    RED      // Expenses exceed income
}