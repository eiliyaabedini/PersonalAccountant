package ir.act.personalAccountant.presentation.expense_entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.act.personalAccountant.domain.usecase.AddExpenseUseCase
import ir.act.personalAccountant.domain.usecase.GetTotalExpensesUseCase
import ir.act.personalAccountant.domain.usecase.GetAllTagsUseCase
import ir.act.personalAccountant.domain.usecase.GetExpensesByTagUseCase
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
    private val getTotalExpensesUseCase: GetTotalExpensesUseCase,
    private val getAllTagsUseCase: GetAllTagsUseCase,
    private val getExpensesByTagUseCase: GetExpensesByTagUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseEntryUiState())
    val uiState: StateFlow<ExpenseEntryUiState> = _uiState.asStateFlow()

    private val _uiInteraction = Channel<ExpenseEntryUiInteraction>()
    val uiInteraction = _uiInteraction.receiveAsFlow()

    init {
        loadTotalExpenses()
        loadAvailableTags()
        loadTagExpenseData()
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
            is ExpenseEntryEvent.TagSelected -> {
                handleTagSelection(event.tag)
            }
            ExpenseEntryEvent.AddTagClicked -> {
                _uiState.value = _uiState.value.copy(showAddTagDialog = true)
            }
            is ExpenseEntryEvent.NewTagNameChanged -> {
                _uiState.value = _uiState.value.copy(newTagName = event.name)
            }
            ExpenseEntryEvent.ConfirmNewTag -> {
                handleConfirmNewTag()
            }
            ExpenseEntryEvent.DismissAddTagDialog -> {
                _uiState.value = _uiState.value.copy(
                    showAddTagDialog = false,
                    newTagName = ""
                )
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
                addExpenseUseCase(amount, _uiState.value.selectedTag)
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

    private fun handleTagSelection(tag: String) {
        val currentAmount = _uiState.value.currentAmount
        if (currentAmount.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Please enter an amount first")
            return
        }

        val amount = currentAmount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid amount")
            return
        }

        _uiState.value = _uiState.value.copy(selectedTag = tag)
        
        // Automatically save the expense when tag is selected
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                addExpenseUseCase(amount, tag)
                _uiState.value = _uiState.value.copy(
                    currentAmount = "",
                    isLoading = false,
                    selectedTag = "General" // Reset to default
                )
                loadAvailableTags() // Refresh tags to update counts
                loadTagExpenseData() // Refresh chart data
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred"
                )
            }
        }
    }

    private fun handleConfirmNewTag() {
        val newTagName = _uiState.value.newTagName.trim()
        if (newTagName.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                showAddTagDialog = false,
                newTagName = "",
                selectedTag = newTagName
            )
            // Select the new tag, which will also save the expense
            handleTagSelection(newTagName)
        }
    }

    private fun loadTotalExpenses() {
        viewModelScope.launch {
            getTotalExpensesUseCase().collect { total ->
                _uiState.value = _uiState.value.copy(totalExpenses = total)
            }
        }
    }

    private fun loadAvailableTags() {
        viewModelScope.launch {
            getAllTagsUseCase().collect { tags ->
                _uiState.value = _uiState.value.copy(availableTags = tags)
            }
        }
    }

    private fun loadTagExpenseData() {
        viewModelScope.launch {
            getExpensesByTagUseCase().collect { tagData ->
                _uiState.value = _uiState.value.copy(tagExpenseData = tagData)
            }
        }
    }
}