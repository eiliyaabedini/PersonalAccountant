package ir.act.personalAccountant.presentation.expense_entry

import ir.act.personalAccountant.data.local.model.TagWithCount
import ir.act.personalAccountant.domain.model.TagExpenseData
import ir.act.personalAccountant.domain.model.CurrencySettings

data class ExpenseEntryUiState(
    val currentAmount: String = "",
    val totalExpenses: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val availableTags: List<TagWithCount> = emptyList(),
    val selectedTag: String = "",
    val showAddTagDialog: Boolean = false,
    val newTagName: String = "",
    val tagExpenseData: List<TagExpenseData> = emptyList(),
    val selectedDate: Long = System.currentTimeMillis(),
    val showDatePicker: Boolean = false,
    val currencySettings: CurrencySettings = CurrencySettings()
)

sealed class ExpenseEntryEvent {
    data class NumberClicked(val number: String) : ExpenseEntryEvent()
    object DecimalClicked : ExpenseEntryEvent()
    object BackspaceClicked : ExpenseEntryEvent()
    object ClearError : ExpenseEntryEvent()
    data class TagSelected(val tag: String) : ExpenseEntryEvent()
    object AddTagClicked : ExpenseEntryEvent()
    data class NewTagNameChanged(val name: String) : ExpenseEntryEvent()
    object ConfirmNewTag : ExpenseEntryEvent()
    object DismissAddTagDialog : ExpenseEntryEvent()
    object DatePickerClicked : ExpenseEntryEvent()
    data class DateSelected(val dateMillis: Long) : ExpenseEntryEvent()
    object DismissDatePicker : ExpenseEntryEvent()
}

sealed class ExpenseEntryUiInteraction {
    data class NavigateToExpenseList(val newExpenseId: Long? = null) : ExpenseEntryUiInteraction()
}