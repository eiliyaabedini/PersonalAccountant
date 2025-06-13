package ir.act.personalAccountant.presentation.expense_edit

import ir.act.personalAccountant.data.local.model.TagWithCount
import ir.act.personalAccountant.domain.model.CurrencySettings

object ExpenseEditContract {
    
    data class UiState(
        val expenseId: Long = 0L,
        val amount: String = "",
        val selectedTag: String = "",
        val availableTags: List<TagWithCount> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val showDeleteConfirmation: Boolean = false,
        val navigateBack: Boolean = false,
        val showAddTagDialog: Boolean = false,
        val newTagName: String = "",
        val isFirstEdit: Boolean = true, // Track if this is the first edit
        val selectedDate: Long = System.currentTimeMillis(),
        val showDatePicker: Boolean = false,
        val currencySettings: CurrencySettings = CurrencySettings()
    )
    
    sealed class Events {
        data class LoadExpense(val expenseId: Long) : Events()
        data class NumberClicked(val number: String) : Events()
        object DecimalClicked : Events()
        object BackspaceClicked : Events()
        data class TagSelected(val tag: String) : Events()
        object UpdateClicked : Events()
        object DeleteClicked : Events()
        object ConfirmDelete : Events()
        object CancelDelete : Events()
        object ClearError : Events()
        object AddTagClicked : Events()
        data class NewTagNameChanged(val name: String) : Events()
        object ConfirmNewTag : Events()
        object DismissAddTagDialog : Events()
        object DatePickerClicked : Events()
        data class DateSelected(val dateMillis: Long) : Events()
        object DismissDatePicker : Events()
    }
    
    interface UiInteractions {
        fun navigateBack()
    }
}