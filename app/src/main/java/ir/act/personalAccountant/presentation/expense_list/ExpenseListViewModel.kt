package ir.act.personalAccountant.presentation.expense_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.act.personalAccountant.domain.usecase.GetAllExpensesUseCase
import ir.act.personalAccountant.domain.usecase.GetTotalExpensesUseCase
import ir.act.personalAccountant.domain.usecase.GetExpensesByTagUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseListViewModel @Inject constructor(
    private val getAllExpensesUseCase: GetAllExpensesUseCase,
    private val getTotalExpensesUseCase: GetTotalExpensesUseCase,
    private val getExpensesByTagUseCase: GetExpensesByTagUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseListUiState())
    val uiState: StateFlow<ExpenseListUiState> = _uiState.asStateFlow()

    private val _uiInteraction = Channel<ExpenseListUiInteraction>()
    val uiInteraction = _uiInteraction.receiveAsFlow()

    init {
        loadExpenses()
    }

    fun onEvent(event: ExpenseListEvent) {
        when (event) {
            ExpenseListEvent.AddClicked -> {
                viewModelScope.launch {
                    _uiInteraction.send(ExpenseListUiInteraction.NavigateToExpenseEntry)
                }
            }
            ExpenseListEvent.ClearError -> {
                _uiState.value = _uiState.value.copy(error = null)
            }
        }
    }

    private fun loadExpenses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                combine(
                    getAllExpensesUseCase(),
                    getTotalExpensesUseCase(),
                    getExpensesByTagUseCase()
                ) { expenses, total, tagData ->
                    _uiState.value = _uiState.value.copy(
                        expenses = expenses,
                        totalExpenses = total,
                        tagExpenseData = tagData,
                        isLoading = false,
                        error = null
                    )
                }.collect { }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred while loading expenses"
                )
            }
        }
    }
}