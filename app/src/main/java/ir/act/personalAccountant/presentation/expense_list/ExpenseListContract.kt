package ir.act.personalAccountant.presentation.expense_list

import android.net.Uri
import ir.act.personalAccountant.domain.model.BudgetData
import ir.act.personalAccountant.domain.model.BudgetSettings
import ir.act.personalAccountant.domain.model.CurrencySettings
import ir.act.personalAccountant.domain.model.Expense
import ir.act.personalAccountant.domain.model.TagExpenseData
import ir.act.personalAccountant.domain.model.TripModeSettings

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
    val currentMonth: Int = 0,
    val isBudgetMode: Boolean = false,
    val budgetSettings: BudgetSettings = BudgetSettings(),
    val budgetData: BudgetData? = null,
    val tempCameraUri: Uri? = null,
    val isAnalyzingReceipt: Boolean = false,
    val aiAnalysisError: String? = null,
    val tripModeSettings: TripModeSettings = TripModeSettings.DEFAULT,
    val showTripModeSetup: Boolean = false,
    val availableCurrencies: List<CurrencySettings> = emptyList()
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
    object BudgetModeToggled : ExpenseListEvent()
    data class CameraClicked(val uri: Uri) : ExpenseListEvent()
    object CameraImageCaptured : ExpenseListEvent()
    object ClearAIAnalysisError : ExpenseListEvent()
    object TripModeToggled : ExpenseListEvent()
    object ShowTripModeSetup : ExpenseListEvent()
    object DismissTripModeSetup : ExpenseListEvent()
    data class TripModeSettingsUpdated(val settings: TripModeSettings) : ExpenseListEvent()
}

sealed class ExpenseListUiInteraction {
    object NavigateToExpenseEntry : ExpenseListUiInteraction()
    data class NavigateToExpenseEdit(val expenseId: Long) : ExpenseListUiInteraction()
    object NavigateToBudgetConfig : ExpenseListUiInteraction()
    data class ShowSuccessMessage(val message: String) : ExpenseListUiInteraction()
}