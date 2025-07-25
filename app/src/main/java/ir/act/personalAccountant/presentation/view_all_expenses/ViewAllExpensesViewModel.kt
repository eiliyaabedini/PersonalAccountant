package ir.act.personalAccountant.presentation.view_all_expenses

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.act.personalAccountant.core.util.DateUtils
import ir.act.personalAccountant.domain.model.Expense
import ir.act.personalAccountant.domain.usecase.DeleteExpenseUseCase
import ir.act.personalAccountant.domain.usecase.GetCurrencySettingsUseCase
import ir.act.personalAccountant.domain.usecase.GetExpensesByMonthUseCase
import ir.act.personalAccountant.domain.usecase.GetExpensesByTagAndMonthUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ViewAllExpensesViewModel @Inject constructor(
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val getCurrencySettingsUseCase: GetCurrencySettingsUseCase,
    private val getExpensesByMonthUseCase: GetExpensesByMonthUseCase,
    private val getExpensesByTagAndMonthUseCase: GetExpensesByTagAndMonthUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ViewAllExpensesUiState())
    val uiState: StateFlow<ViewAllExpensesUiState> = _uiState.asStateFlow()

    private val _uiInteraction = Channel<ViewAllExpensesUiInteraction>()
    val uiInteraction = _uiInteraction.receiveAsFlow()

    private val filterByTag: String? = savedStateHandle.get<String>("filterTag")

    init {
        initializeCurrentMonth()
        initializeFilter()
        loadExpenses()
        loadCurrencySettings()
    }

    private fun initializeCurrentMonth() {
        _uiState.value = _uiState.value.copy(
            currentYear = DateUtils.getCurrentYear(),
            currentMonth = DateUtils.getCurrentMonth()
        )
    }

    private fun initializeFilter() {
        val initialGroupingMode = if (filterByTag != null) {
            GroupingMode.BY_CATEGORY
        } else {
            GroupingMode.BY_DAY
        }
        
        _uiState.value = _uiState.value.copy(
            filterByTag = filterByTag,
            groupingMode = initialGroupingMode
        )
    }

    fun onEvent(event: ViewAllExpensesEvent) {
        when (event) {
            ViewAllExpensesEvent.ClearError -> {
                _uiState.value = _uiState.value.copy(error = null)
            }
            is ViewAllExpensesEvent.EditClicked -> {
                viewModelScope.launch {
                    _uiInteraction.send(ViewAllExpensesUiInteraction.NavigateToExpenseEdit(event.expense.id))
                }
            }
            is ViewAllExpensesEvent.DeleteClicked -> {
                _uiState.value = _uiState.value.copy(
                    showDeleteConfirmation = true,
                    expenseToDelete = event.expense
                )
            }
            ViewAllExpensesEvent.ConfirmDelete -> {
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
            ViewAllExpensesEvent.CancelDelete -> {
                _uiState.value = _uiState.value.copy(
                    showDeleteConfirmation = false,
                    expenseToDelete = null
                )
            }
            ViewAllExpensesEvent.ClearFilter -> {
                _uiState.value = _uiState.value.copy(
                    filterByTag = null
                )
                loadExpenses()
            }
            ViewAllExpensesEvent.ToggleGrouping -> {
                val newGroupingMode = when (_uiState.value.groupingMode) {
                    GroupingMode.BY_DAY -> GroupingMode.BY_CATEGORY
                    GroupingMode.BY_CATEGORY -> GroupingMode.BY_DAY
                }
                _uiState.value = _uiState.value.copy(groupingMode = newGroupingMode)
            }
            ViewAllExpensesEvent.BackClicked -> {
                viewModelScope.launch {
                    _uiInteraction.send(ViewAllExpensesUiInteraction.NavigateBack)
                }
            }
        }
    }

    private fun loadExpenses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val currentState = _uiState.value
                
                val expensesFlow = if (currentState.filterByTag != null) {
                    getExpensesByTagAndMonthUseCase(
                        currentState.filterByTag,
                        currentState.currentYear,
                        currentState.currentMonth
                    )
                } else {
                    getExpensesByMonthUseCase(currentState.currentYear, currentState.currentMonth)
                }
                
                expensesFlow.collect { expenses ->
                    val groupedByDay = groupExpensesByDayOfMonth(expenses)
                    val groupedByCategory = groupExpensesByCategory(expenses)
                    _uiState.value = _uiState.value.copy(
                        expenses = expenses,
                        groupedExpensesByDay = groupedByDay,
                        groupedExpensesByCategory = groupedByCategory,
                        isLoading = false,
                        error = null
                    )
                }
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

    private fun groupExpensesByDayOfMonth(expenses: List<Expense>): Map<Int, List<Expense>> {
        return expenses.groupBy { expense ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = expense.timestamp
            calendar.get(Calendar.DAY_OF_MONTH)
        }.toSortedMap(compareByDescending { it })
    }

    private fun groupExpensesByCategory(expenses: List<Expense>): Map<String, List<Expense>> {
        return expenses.groupBy { expense ->
            expense.tag
        }.toList().sortedByDescending { (_, expensesInCategory) ->
            expensesInCategory.sumOf { it.amount }
        }.toMap()
    }
}