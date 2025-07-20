package ir.act.personalAccountant.domain.model

data class BudgetData(
    val currentDay: Int,
    val totalDaysInMonth: Int,
    val dailyIncome: Double,
    val dailyRent: Double,
    val totalIncomeToDate: Double,
    val totalRentToDate: Double,
    val totalExpensesToDate: Double,
    val averageDailyExpenses: Double,
    val estimatedEndOfMonthBalance: Double,
    val budgetStatus: BudgetStatus,
    val savingGoal: Double = 0.0,
    val currentBalance: Double = 0.0,
    val dailySavingsNeeded: Double = 0.0,
    val recommendedDailyExpenseBudget: Double = 0.0,
    val savingGoalStatus: SavingGoalStatus = SavingGoalStatus.NOT_SET,
    val todayExpenses: Double = 0.0
)

enum class BudgetStatus {
    GOOD,    // Expenses are well within budget
    MIDDLE,  // Expenses are moderate
    RED      // Expenses exceed income
}

enum class SavingGoalStatus {
    NOT_SET,        // No saving goal set
    ON_TRACK,       // On track to meet saving goal
    NEED_ADJUSTMENT, // Need to reduce daily expenses to meet goal
    GOAL_EXCEEDED   // Already exceeded the saving goal
}