package ir.act.personalAccountant.presentation.budget_config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.act.personalAccountant.domain.usecase.BudgetUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetConfigViewModel @Inject constructor(
    private val budgetUseCase: BudgetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetConfigContract.UiState())
    val uiState = _uiState.asStateFlow()

    private val _uiInteraction = Channel<BudgetConfigContract.UiInteraction>()
    val uiInteraction: Flow<BudgetConfigContract.UiInteraction> = _uiInteraction.receiveAsFlow()

    init {
        observeBudgetSettings()
    }

    private fun observeBudgetSettings() {
        viewModelScope.launch {
            budgetUseCase.getBudgetSettings().collect { budgetSettings ->
                _uiState.value = _uiState.value.copy(
                    budgetSettings = budgetSettings,
                    netSalaryInput = if (budgetSettings.netSalary > 0) budgetSettings.netSalary.toString() else "",
                    rentInput = if (budgetSettings.totalRent > 0) budgetSettings.totalRent.toString() else ""
                )
            }
        }
    }

    fun handleEvent(event: BudgetConfigContract.Event) {
        when (event) {
            is BudgetConfigContract.Event.OnNetSalaryChanged -> {
                handleNetSalaryChanged(event.netSalary)
            }
            is BudgetConfigContract.Event.OnRentChanged -> {
                handleRentChanged(event.rent)
            }
            is BudgetConfigContract.Event.OnSaveClicked -> {
                handleSaveClicked()
            }
            is BudgetConfigContract.Event.OnDismissError -> {
                handleDismissError()
            }
            is BudgetConfigContract.Event.OnNavigateBack -> {
                handleNavigateBack()
            }
        }
    }

    private fun handleNetSalaryChanged(netSalary: String) {
        updateInputValidation(netSalary, _uiState.value.rentInput)
        _uiState.value = _uiState.value.copy(
            netSalaryInput = netSalary,
            errorMessage = null
        )
    }

    private fun handleRentChanged(rent: String) {
        updateInputValidation(_uiState.value.netSalaryInput, rent)
        _uiState.value = _uiState.value.copy(
            rentInput = rent,
            errorMessage = null
        )
    }

    private fun updateInputValidation(netSalary: String, rent: String) {
        val isNetSalaryValid = netSalary.toDoubleOrNull()?.let { it > 0 } == true
        val isRentValid = rent.isEmpty() || rent.toDoubleOrNull()?.let { it >= 0 } == true
        
        _uiState.value = _uiState.value.copy(
            isInputValid = isNetSalaryValid && isRentValid
        )
    }

    private fun handleSaveClicked() {
        val netSalary = _uiState.value.netSalaryInput.toDoubleOrNull()
        if (netSalary == null || netSalary <= 0) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please enter a valid net salary"
            )
            return
        }

        val rent = _uiState.value.rentInput.toDoubleOrNull() ?: 0.0
        if (rent < 0) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Rent amount cannot be negative"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                budgetUseCase.updateNetSalary(netSalary)
                budgetUseCase.updateTotalRent(rent)
                budgetUseCase.setBudgetConfigured(true)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
                _uiInteraction.send(BudgetConfigContract.UiInteraction.ShowSuccess)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to save budget settings: ${e.message}"
                )
            }
        }
    }

    private fun handleDismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun handleNavigateBack() {
        viewModelScope.launch {
            _uiInteraction.send(BudgetConfigContract.UiInteraction.NavigateBack)
        }
    }
}