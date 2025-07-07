package ir.act.personalAccountant.presentation.expense_entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.act.personalAccountant.domain.usecase.AddExpenseUseCase
import ir.act.personalAccountant.domain.usecase.GetTotalExpensesUseCase
import ir.act.personalAccountant.domain.usecase.GetAllTagsUseCase
import ir.act.personalAccountant.domain.usecase.GetExpensesByTagUseCase
import ir.act.personalAccountant.domain.usecase.GetCurrencySettingsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseEntryViewModel @Inject constructor(
    private val addExpenseUseCase: AddExpenseUseCase,
    private val getTotalExpensesUseCase: GetTotalExpensesUseCase,
    private val getAllTagsUseCase: GetAllTagsUseCase,
    private val getExpensesByTagUseCase: GetExpensesByTagUseCase,
    private val getCurrencySettingsUseCase: GetCurrencySettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseEntryUiState())
    val uiState: StateFlow<ExpenseEntryUiState> = _uiState.asStateFlow()

    private val _uiInteraction = Channel<ExpenseEntryUiInteraction>()
    val uiInteraction = _uiInteraction.receiveAsFlow()

    init {
        loadTotalExpenses()
        loadAvailableTags()
        loadTagExpenseData()
        loadCurrencySettings()
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
            ExpenseEntryEvent.ClearError -> {
                _uiState.update { it.copy(error = null) }
            }
            is ExpenseEntryEvent.TagSelected -> {
                handleTagSelection(event.tag)
            }
            ExpenseEntryEvent.AddTagClicked -> {
                _uiState.update { it.copy(showAddTagDialog = true) }
            }
            is ExpenseEntryEvent.NewTagNameChanged -> {
                _uiState.update { it.copy(newTagName = event.name) }
            }
            ExpenseEntryEvent.ConfirmNewTag -> {
                handleConfirmNewTag()
            }
            ExpenseEntryEvent.DismissAddTagDialog -> {
                _uiState.update {
                    it.copy(
                        showAddTagDialog = false,
                        newTagName = ""
                    )
                }
            }
            ExpenseEntryEvent.DatePickerClicked -> {
                _uiState.update { it.copy(showDatePicker = true) }
            }
            is ExpenseEntryEvent.DateSelected -> {
                _uiState.update {
                    it.copy(
                        selectedDate = event.dateMillis,
                        showDatePicker = false
                    )
                }
            }
            ExpenseEntryEvent.DismissDatePicker -> {
                _uiState.update { it.copy(showDatePicker = false) }
            }
            ExpenseEntryEvent.AddMultipleExpensesToggled -> {
                _uiState.update { it.copy(addMultipleExpenses = !it.addMultipleExpenses) }
            }
        }
    }

    private fun handleNumberClick(number: String) {
        val currentAmount = _uiState.value.currentAmount
        if (currentAmount.length < 10) { // Prevent very large numbers
            _uiState.update {
                it.copy(currentAmount = currentAmount + number)
            }
        }
    }

    private fun handleDecimalClick() {
        val currentAmount = _uiState.value.currentAmount
        if (!currentAmount.contains(".") && currentAmount.isNotEmpty()) {
            _uiState.update {
                it.copy(currentAmount = currentAmount + ".")
            }
        } else if (currentAmount.isEmpty()) {
            _uiState.update {
                it.copy(currentAmount = "0.")
            }
        }
    }

    private fun handleBackspaceClick() {
        val currentAmount = _uiState.value.currentAmount
        if (currentAmount.isNotEmpty()) {
            _uiState.update {
                it.copy(currentAmount = currentAmount.dropLast(1))
            }
        }
    }

    private fun handleTagSelection(tag: String) {
        val currentAmount = _uiState.value.currentAmount
        if (currentAmount.isEmpty()) {
            _uiState.update { it.copy(error = "Please enter an amount first") }
            return
        }

        val amount = currentAmount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(error = "Please enter a valid amount") }
            return
        }

        _uiState.update { it.copy(selectedTag = tag) }
        
        // Automatically save the expense when tag is selected
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                val currentState = _uiState.value
                val newExpenseId = addExpenseUseCase(amount, tag, currentState.selectedDate)
                
                if (currentState.addMultipleExpenses) {
                    // Stay on the page, just clear the form
                    _uiState.update {
                        it.copy(
                            currentAmount = "",
                            isLoading = false,
                            selectedTag = "", // Reset to no selection
                            selectedDate = System.currentTimeMillis() // Reset date to current time
                        )
                    }
                } else {
                    // Navigate away as before
                    _uiState.update {
                        it.copy(
                            currentAmount = "",
                            isLoading = false,
                            selectedTag = "", // Reset to no selection
                            selectedDate = System.currentTimeMillis() // Reset date to current time
                        )
                    }
                    _uiInteraction.send(ExpenseEntryUiInteraction.NavigateToExpenseList(newExpenseId))
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An error occurred"
                    )
                }
            }
        }
    }

    private fun handleConfirmNewTag() {
        val newTagName = _uiState.value.newTagName.trim()
        if (newTagName.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    showAddTagDialog = false,
                    newTagName = "",
                    selectedTag = newTagName
                )
            }
            // Select the new tag, which will also save the expense
            handleTagSelection(newTagName)
        }
    }

    private fun loadTotalExpenses() {
        viewModelScope.launch {
            getTotalExpensesUseCase().collect { total ->
                _uiState.update { it.copy(totalExpenses = total) }
            }
        }
    }

    private fun loadAvailableTags() {
        viewModelScope.launch {
            getAllTagsUseCase().collect { tags ->
                _uiState.update { it.copy(availableTags = tags) }
            }
        }
    }

    private fun loadTagExpenseData() {
        viewModelScope.launch {
            getExpensesByTagUseCase().collect { tagData ->
                _uiState.update { it.copy(tagExpenseData = tagData) }
            }
        }
    }

    private fun loadCurrencySettings() {
        viewModelScope.launch {
            getCurrencySettingsUseCase().collect { currencySettings ->
                _uiState.update { it.copy(currencySettings = currencySettings) }
            }
        }
    }
}