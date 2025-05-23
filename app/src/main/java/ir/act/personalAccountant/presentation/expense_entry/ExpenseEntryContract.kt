package ir.act.personalAccountant.presentation.expense_entry

import ir.act.personalAccountant.data.local.model.TagWithCount

data class ExpenseEntryUiState(
    val currentAmount: String = "",
    val totalExpenses: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val availableTags: List<TagWithCount> = emptyList(),
    val selectedTag: String = "General",
    val showAddTagDialog: Boolean = false,
    val newTagName: String = ""
)

sealed class ExpenseEntryEvent {
    data class NumberClicked(val number: String) : ExpenseEntryEvent()
    object DecimalClicked : ExpenseEntryEvent()
    object BackspaceClicked : ExpenseEntryEvent()
    object AddClicked : ExpenseEntryEvent()
    object ClearError : ExpenseEntryEvent()
    data class TagSelected(val tag: String) : ExpenseEntryEvent()
    object AddTagClicked : ExpenseEntryEvent()
    data class NewTagNameChanged(val name: String) : ExpenseEntryEvent()
    object ConfirmNewTag : ExpenseEntryEvent()
    object DismissAddTagDialog : ExpenseEntryEvent()
}

sealed class ExpenseEntryUiInteraction {
    object NavigateToExpenseList : ExpenseEntryUiInteraction()
}