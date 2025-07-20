package ir.act.personalAccountant.presentation.view_all_expenses

import ir.act.personalAccountant.domain.model.CurrencySettings
import ir.act.personalAccountant.domain.model.Expense

enum class GroupingMode {
    BY_DAY,
    BY_CATEGORY
}

data class ViewAllExpensesUiState(
    val expenses: List<Expense> = emptyList(),
    val groupedExpensesByDay: Map<Int, List<Expense>> = emptyMap(),
    val groupedExpensesByCategory: Map<String, List<Expense>> = emptyMap(),
    val groupingMode: GroupingMode = GroupingMode.BY_DAY,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDeleteConfirmation: Boolean = false,
    val expenseToDelete: Expense? = null,
    val currencySettings: CurrencySettings = CurrencySettings(),
    val currentYear: Int = 0,
    val currentMonth: Int = 0,
    val filterByTag: String? = null
)

sealed class ViewAllExpensesEvent {
    object ClearError : ViewAllExpensesEvent()
    data class EditClicked(val expense: Expense) : ViewAllExpensesEvent()
    data class DeleteClicked(val expense: Expense) : ViewAllExpensesEvent()
    object ConfirmDelete : ViewAllExpensesEvent()
    object CancelDelete : ViewAllExpensesEvent()
    object ClearFilter : ViewAllExpensesEvent()
    object ToggleGrouping : ViewAllExpensesEvent()
    object BackClicked : ViewAllExpensesEvent()
}

sealed class ViewAllExpensesUiInteraction {
    data class NavigateToExpenseEdit(val expenseId: Long) : ViewAllExpensesUiInteraction()
    object NavigateBack : ViewAllExpensesUiInteraction()
}