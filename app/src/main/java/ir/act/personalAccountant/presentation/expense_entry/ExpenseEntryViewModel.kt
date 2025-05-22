package ir.act.personalAccountant.presentation.expense_entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.act.personalAccountant.domain.usecase.AddExpenseUseCase
import ir.act.personalAccountant.domain.usecase.GetTotalExpensesUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseEntryViewModel @Inject constructor(
    private val addExpenseUseCase: AddExpenseUseCase,
    private val getTotalExpensesUseCase: GetTotalExpensesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseEntryUiState())
    val uiState: StateFlow<ExpenseEntryUiState> = _uiState.asStateFlow()

    private val _uiInteraction = Channel<ExpenseEntryUiInteraction>()
    val uiInteraction = _uiInteraction.receiveAsFlow()

    init {
        loadTotalExpenses()
    }

    fun onEvent(event: ExpenseEntryEvent) {
        when (event) {
            is ExpenseEntryEvent.NumberClicked -> {
                handleNumberClick(event.number)
            }
            ExpenseEntryEvent.DecimalClicked -> {
                handleDecimalClick()
            }
            ExpenseEntryEvent.BackspaceClicked -> {
                handleBackspaceClick()
            }
            ExpenseEntryEvent.AddClicked -> {
                handleAddClick()
            }
            ExpenseEntryEvent.ClearError -> {
                _uiState.value = _uiState.value.copy(error = null)
            }
        }
    }

    private fun handleNumberClick(number: String) {
        val currentAmount = _uiState.value.currentAmount
        if (currentAmount.length < 10) { // Prevent very large numbers
            _uiState.value = _uiState.value.copy(
                currentAmount = currentAmount + number
            )
        }
    }

    private fun handleDecimalClick() {
        val currentAmount = _uiState.value.currentAmount
        if (!currentAmount.contains(".") && currentAmount.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                currentAmount = currentAmount + "."
            )
        } else if (currentAmount.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                currentAmount = "0."
            )
        }
    }

    private fun handleBackspaceClick() {
        val currentAmount = _uiState.value.currentAmount
        if (currentAmount.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                currentAmount = currentAmount.dropLast(1)
            )
        }
    }

    private fun handleAddClick() {
        val currentAmount = _uiState.value.currentAmount
        if (currentAmount.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Please enter an amount")
            return
        }

        val amount = currentAmount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid amount")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                addExpenseUseCase(amount)
                _uiState.value = _uiState.value.copy(
                    currentAmount = "",
                    isLoading = false
                )
                _uiInteraction.send(ExpenseEntryUiInteraction.NavigateToExpenseList)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred"
                )
            }
        }
    }

    private fun loadTotalExpenses() {
        viewModelScope.launch {
            getTotalExpensesUseCase().collect { total ->
                _uiState.value = _uiState.value.copy(totalExpenses = total)
            }
        }
    }
}