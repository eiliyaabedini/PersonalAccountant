package ir.act.personalAccountant.presentation.expense_edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.act.personalAccountant.domain.model.Expense
import ir.act.personalAccountant.domain.repository.ExpenseRepository
import ir.act.personalAccountant.domain.usecase.DeleteExpenseUseCase
import ir.act.personalAccountant.domain.usecase.GetExpenseByIdUseCase
import ir.act.personalAccountant.domain.usecase.UpdateExpenseUseCase
import ir.act.personalAccountant.presentation.expense_edit.ExpenseEditContract.Events
import ir.act.personalAccountant.presentation.expense_edit.ExpenseEditContract.UiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseEditViewModel @Inject constructor(
    private val getExpenseByIdUseCase: GetExpenseByIdUseCase,
    private val updateExpenseUseCase: UpdateExpenseUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var originalExpense: Expense? = null

    init {
        loadAvailableTags()
    }

    fun onEvent(event: Events) {
        when (event) {
            is Events.LoadExpense -> loadExpense(event.expenseId)
            is Events.NumberClicked -> handleNumberInput(event.number)
            Events.DecimalClicked -> handleDecimalInput()
            Events.BackspaceClicked -> handleBackspace()
            is Events.TagSelected -> selectTag(event.tag)
            Events.UpdateClicked -> updateExpense()
            Events.DeleteClicked -> showDeleteConfirmation()
            Events.ConfirmDelete -> deleteExpense()
            Events.CancelDelete -> hideDeleteConfirmation()
            Events.ClearError -> clearError()
            Events.AddTagClicked -> showAddTagDialog()
            is Events.NewTagNameChanged -> updateNewTagName(event.name)
            Events.ConfirmNewTag -> addNewTag()
            Events.DismissAddTagDialog -> dismissAddTagDialog()
            Events.DatePickerClicked -> showDatePicker()
            is Events.DateSelected -> selectDate(event.dateMillis)
            Events.DismissDatePicker -> dismissDatePicker()
        }
    }

    private fun loadExpense(expenseId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                getExpenseByIdUseCase(expenseId)?.let { expense ->
                    originalExpense = expense
                    // Format the amount to remove unnecessary decimal zeros
                    val formattedAmount = if (expense.amount % 1 == 0.0) {
                        expense.amount.toInt().toString()
                    } else {
                        String.format("%.2f", expense.amount).trimEnd('0').trimEnd('.')
                    }
                    _uiState.update { state ->
                        state.copy(
                            expenseId = expense.id,
                            amount = formattedAmount,
                            selectedTag = expense.tag,
                            selectedDate = expense.timestamp,
                            isLoading = false
                        )
                    }
                } ?: run {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            error = "Expense not found"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load expense"
                    )
                }
            }
        }
    }

    private fun loadAvailableTags() {
        viewModelScope.launch {
            repository.getAllTagsWithCount()
                .catch { /* Handle error */ }
                .collect { tags ->
                    _uiState.update { it.copy(availableTags = tags) }
                }
        }
    }

    private fun handleNumberInput(number: String) {
        val currentState = _uiState.value
        val currentAmount = currentState.amount
        
        // If this is the first edit of an existing value, clear it and start fresh
        if (currentState.isFirstEdit && currentAmount.isNotEmpty() && currentAmount != "0") {
            _uiState.update { it.copy(amount = number, isFirstEdit = false) }
            return
        }
        
        // If current amount is "0" or empty, replace with the new number
        if (currentAmount == "0" || currentAmount.isEmpty()) {
            _uiState.update { it.copy(amount = number, isFirstEdit = false) }
            return
        }
        
        // Check if we already have 2 decimal places
        val decimalIndex = currentAmount.indexOf('.')
        if (decimalIndex != -1) {
            val decimalPlaces = currentAmount.length - decimalIndex - 1
            if (decimalPlaces >= 2) {
                return // Don't allow more than 2 decimal places
            }
        }
        
        // Prevent the amount from being too large
        if (currentAmount.length >= 10) {
            return
        }
        
        _uiState.update { it.copy(amount = currentAmount + number, isFirstEdit = false) }
    }

    private fun handleDecimalInput() {
        val currentState = _uiState.value
        val currentAmount = currentState.amount
        
        // If this is the first edit, replace with "0."
        if (currentState.isFirstEdit && currentAmount.isNotEmpty() && currentAmount != "0") {
            _uiState.update { it.copy(amount = "0.", isFirstEdit = false) }
            return
        }
        
        // Don't add decimal if one already exists
        if (currentAmount.contains(".")) {
            return
        }
        
        // Add decimal point
        if (currentAmount.isEmpty()) {
            _uiState.update { it.copy(amount = "0.", isFirstEdit = false) }
        } else {
            _uiState.update { it.copy(amount = "$currentAmount.", isFirstEdit = false) }
        }
    }

    private fun handleBackspace() {
        val currentAmount = _uiState.value.amount
        if (currentAmount.isNotEmpty()) {
            val newAmount = currentAmount.dropLast(1)
            _uiState.update { 
                it.copy(
                    amount = if (newAmount.isEmpty()) "" else newAmount,
                    isFirstEdit = false
                )
            }
        }
    }

    private fun selectTag(tag: String) {
        _uiState.update { it.copy(selectedTag = tag) }
    }

    private fun updateExpense() {
        val state = _uiState.value
        val amount = state.amount.toDoubleOrNull()

        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(error = "Please enter a valid amount") }
            return
        }

        if (state.selectedTag.isEmpty()) {
            _uiState.update { it.copy(error = "Please select a tag") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                originalExpense?.let { original ->
                    updateExpenseUseCase(
                        original.copy(
                            amount = amount,
                            tag = state.selectedTag,
                            timestamp = state.selectedDate
                        )
                    )
                    _uiState.update { it.copy(navigateBack = true) }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to update expense"
                    )
                }
            }
        }
    }

    private fun showDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = true) }
    }

    private fun hideDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteConfirmation = false) }
    }

    private fun deleteExpense() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                deleteExpenseUseCase(_uiState.value.expenseId)
                _uiState.update { it.copy(navigateBack = true) }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        showDeleteConfirmation = false,
                        error = e.message ?: "Failed to delete expense"
                    )
                }
            }
        }
    }

    private fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun showAddTagDialog() {
        _uiState.update { it.copy(showAddTagDialog = true) }
    }

    private fun updateNewTagName(name: String) {
        _uiState.update { it.copy(newTagName = name) }
    }

    private fun addNewTag() {
        val tagName = _uiState.value.newTagName.trim()
        if (tagName.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    selectedTag = tagName,
                    showAddTagDialog = false,
                    newTagName = ""
                )
            }
        }
    }

    private fun dismissAddTagDialog() {
        _uiState.update { it.copy(showAddTagDialog = false, newTagName = "") }
    }

    private fun showDatePicker() {
        _uiState.update { it.copy(showDatePicker = true) }
    }

    private fun selectDate(dateMillis: Long) {
        _uiState.update { it.copy(selectedDate = dateMillis, showDatePicker = false) }
    }

    private fun dismissDatePicker() {
        _uiState.update { it.copy(showDatePicker = false) }
    }
}