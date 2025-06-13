package ir.act.personalAccountant.presentation.expense_list

import ir.act.personalAccountant.domain.model.Expense
import ir.act.personalAccountant.domain.model.TagExpenseData
import ir.act.personalAccountant.domain.model.CurrencySettings

data class ExpenseListUiState(
    val expenses: List<Expense> = emptyList(),
    val totalExpenses: Double = 0.0,
    val tagExpenseData: List<TagExpenseData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDeleteConfirmation: Boolean = false,
    val expenseToDelete: Expense? = null,
    val currencySettings: CurrencySettings = CurrencySettings(),
    val currentYear: Int = 0,
    val currentMonth: Int = 0
)

sealed class ExpenseListEvent {
    object AddClicked : ExpenseListEvent()
    object ClearError : ExpenseListEvent()
    data class EditClicked(val expense: Expense) : ExpenseListEvent()
    data class DeleteClicked(val expense: Expense) : ExpenseListEvent()
    object ConfirmDelete : ExpenseListEvent()
    object CancelDelete : ExpenseListEvent()
    object NextMonthClicked : ExpenseListEvent()
    object PreviousMonthClicked : ExpenseListEvent()
}

sealed class ExpenseListUiInteraction {
    object NavigateToExpenseEntry : ExpenseListUiInteraction()
    data class NavigateToExpenseEdit(val expenseId: Long) : ExpenseListUiInteraction()
}