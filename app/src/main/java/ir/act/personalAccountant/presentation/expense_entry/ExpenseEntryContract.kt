package ir.act.personalAccountant.presentation.expense_entry

import android.net.Uri
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
    val currencySettings: CurrencySettings = CurrencySettings(),
    val addMultipleExpenses: Boolean = false,
    val selectedImageUri: Uri? = null,
    val tempCameraUri: Uri? = null,
    val showImagePicker: Boolean = false,
    val isProcessingImage: Boolean = false,
    val showImageViewer: Boolean = false
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
    object AddMultipleExpensesToggled : ExpenseEntryEvent()
    object ImagePickerClicked : ExpenseEntryEvent()
    data class ImageSelected(val uri: Uri) : ExpenseEntryEvent()
    object ImageCaptured : ExpenseEntryEvent()
    data class CameraLaunchRequested(val uri: Uri) : ExpenseEntryEvent()
    object RemoveImage : ExpenseEntryEvent()
    object DismissImagePicker : ExpenseEntryEvent()
    object ShowImageViewer : ExpenseEntryEvent()
    object DismissImageViewer : ExpenseEntryEvent()
}

sealed class ExpenseEntryUiInteraction {
    data class NavigateToExpenseList(val newExpenseId: Long? = null) : ExpenseEntryUiInteraction()
}