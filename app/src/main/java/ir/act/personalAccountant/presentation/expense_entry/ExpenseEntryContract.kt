package ir.act.personalAccountant.presentation.expense_entry

data class ExpenseEntryUiState(
    val currentAmount: String = "",
    val totalExpenses: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class ExpenseEntryEvent {
    data class NumberClicked(val number: String) : ExpenseEntryEvent()
    object DecimalClicked : ExpenseEntryEvent()
    object BackspaceClicked : ExpenseEntryEvent()
    object AddClicked : ExpenseEntryEvent()
    object ClearError : ExpenseEntryEvent()
}

sealed class ExpenseEntryUiInteraction {
    object NavigateToExpenseList : ExpenseEntryUiInteraction()
}