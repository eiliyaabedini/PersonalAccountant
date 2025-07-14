package ir.act.personalAccountant.presentation.budget_config

import ir.act.personalAccountant.domain.model.BudgetSettings

object BudgetConfigContract {
    data class UiState(
        val isLoading: Boolean = false,
        val budgetSettings: BudgetSettings = BudgetSettings(),
        val netSalaryInput: String = "",
        val rentInput: String = "",
        val isInputValid: Boolean = false,
        val errorMessage: String? = null,
        val isSuccess: Boolean = false
    )

    sealed class Event {
        data class OnNetSalaryChanged(val netSalary: String) : Event()
        data class OnRentChanged(val rent: String) : Event()
        data object OnSaveClicked : Event()
        data object OnDismissError : Event()
        data object OnNavigateBack : Event()
    }

    sealed class UiInteraction {
        data object NavigateBack : UiInteraction()
        data object ShowSuccess : UiInteraction()
    }
}