package ir.act.personalAccountant.domain.model

data class BudgetSettings(
    val netSalary: Double = 0.0,
    val totalRent: Double = 0.0,
    val isConfigured: Boolean = false,
    val monthlySavingGoal: Double = 0.0
)