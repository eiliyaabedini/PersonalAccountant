package ir.act.personalAccountant.presentation.expense_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.act.personalAccountant.core.util.DateUtils
import ir.act.personalAccountant.domain.usecase.BudgetUseCase
import ir.act.personalAccountant.domain.usecase.DeleteExpenseUseCase
import ir.act.personalAccountant.domain.usecase.GetAllExpensesUseCase
import ir.act.personalAccountant.domain.usecase.GetTotalExpensesUseCase
import ir.act.personalAccountant.domain.usecase.GetExpensesByTagUseCase
import ir.act.personalAccountant.domain.usecase.GetCurrencySettingsUseCase
import ir.act.personalAccountant.domain.usecase.GetExpensesByMonthUseCase
import ir.act.personalAccountant.domain.usecase.GetTotalExpensesByMonthUseCase
import ir.act.personalAccountant.domain.usecase.GetExpensesByTagForMonthUseCase
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
    private val getExpensesByTagUseCase: GetExpensesByTagUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val getCurrencySettingsUseCase: GetCurrencySettingsUseCase,
    private val getExpensesByMonthUseCase: GetExpensesByMonthUseCase,
    private val getTotalExpensesByMonthUseCase: GetTotalExpensesByMonthUseCase,
    private val getExpensesByTagForMonthUseCase: GetExpensesByTagForMonthUseCase,
    private val budgetUseCase: BudgetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseListUiState())
    val uiState: StateFlow<ExpenseListUiState> = _uiState.asStateFlow()

    private val _uiInteraction = Channel<ExpenseListUiInteraction>()
    val uiInteraction = _uiInteraction.receiveAsFlow()

    init {
        initializeCurrentMonth()
        loadExpenses()
        loadCurrencySettings()
        loadBudgetSettings()
    }

    private fun initializeCurrentMonth() {
        _uiState.value = _uiState.value.copy(
            currentYear = DateUtils.getCurrentYear(),
            currentMonth = DateUtils.getCurrentMonth()
        )
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
            is ExpenseListEvent.EditClicked -> {
                viewModelScope.launch {
                    _uiInteraction.send(ExpenseListUiInteraction.NavigateToExpenseEdit(event.expense.id))
                }
            }
            is ExpenseListEvent.DeleteClicked -> {
                _uiState.value = _uiState.value.copy(
                    showDeleteConfirmation = true,
                    expenseToDelete = event.expense
                )
            }
            ExpenseListEvent.ConfirmDelete -> {
                _uiState.value.expenseToDelete?.let { expense ->
                    viewModelScope.launch {
                        deleteExpenseUseCase(expense.id)
                        _uiState.value = _uiState.value.copy(
                            showDeleteConfirmation = false,
                            expenseToDelete = null
                        )
                    }
                }
            }
            ExpenseListEvent.CancelDelete -> {
                _uiState.value = _uiState.value.copy(
                    showDeleteConfirmation = false,
                    expenseToDelete = null
                )
            }
            ExpenseListEvent.NextMonthClicked -> {
                val (newYear, newMonth) = DateUtils.getNextMonth(_uiState.value.currentYear, _uiState.value.currentMonth)
                _uiState.value = _uiState.value.copy(
                    currentYear = newYear,
                    currentMonth = newMonth
                )
                loadExpenses()
            }
            ExpenseListEvent.PreviousMonthClicked -> {
                val (newYear, newMonth) = DateUtils.getPreviousMonth(_uiState.value.currentYear, _uiState.value.currentMonth)
                _uiState.value = _uiState.value.copy(
                    currentYear = newYear,
                    currentMonth = newMonth
                )
                loadExpenses()
            }
            ExpenseListEvent.BudgetModeToggled -> {
                val currentBudgetMode = _uiState.value.isBudgetMode
                if (!currentBudgetMode && !_uiState.value.budgetSettings.isConfigured) {
                    // Navigate to budget configuration
                    viewModelScope.launch {
                        _uiInteraction.send(ExpenseListUiInteraction.NavigateToBudgetConfig)
                    }
                } else {
                    // Toggle budget mode
                    _uiState.value = _uiState.value.copy(isBudgetMode = !currentBudgetMode)
                }
            }
        }
    }

    private fun loadExpenses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val currentState = _uiState.value
                combine(
                    getExpensesByMonthUseCase(currentState.currentYear, currentState.currentMonth),
                    getTotalExpensesByMonthUseCase(currentState.currentYear, currentState.currentMonth),
                    getExpensesByTagForMonthUseCase(currentState.currentYear, currentState.currentMonth)
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

    private fun loadCurrencySettings() {
        viewModelScope.launch {
            getCurrencySettingsUseCase().collect { currencySettings ->
                _uiState.value = _uiState.value.copy(currencySettings = currencySettings)
            }
        }
    }

    private fun loadBudgetSettings() {
        viewModelScope.launch {
            combine(
                budgetUseCase.getBudgetSettings(),
                budgetUseCase.getBudgetData()
            ) { budgetSettings, budgetData ->
                _uiState.value = _uiState.value.copy(
                    budgetSettings = budgetSettings,
                    budgetData = budgetData
                )
            }.collect { }
        }
    }
}