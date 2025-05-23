package ir.act.personalAccountant.presentation.expense_list

import ir.act.personalAccountant.domain.model.Expense
import ir.act.personalAccountant.domain.model.TagExpenseData

data class ExpenseListUiState(
    val expenses: List<Expense> = emptyList(),
    val totalExpenses: Double = 0.0,
    val tagExpenseData: List<TagExpenseData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class ExpenseListEvent {
    object AddClicked : ExpenseListEvent()
    object ClearError : ExpenseListEvent()
}

sealed class ExpenseListUiInteraction {
    object NavigateToExpenseEntry : ExpenseListUiInteraction()
}