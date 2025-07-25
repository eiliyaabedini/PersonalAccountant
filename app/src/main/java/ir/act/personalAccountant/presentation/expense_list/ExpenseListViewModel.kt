package ir.act.personalAccountant.presentation.expense_list

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.act.personalAccountant.ai.AIEngine
import ir.act.personalAccountant.ai.data.repository.AIRepository
import ir.act.personalAccountant.core.util.DateUtils
import ir.act.personalAccountant.core.util.ImageFileManager
import ir.act.personalAccountant.data.local.UserPreferences
import ir.act.personalAccountant.domain.model.CurrencySettings
import ir.act.personalAccountant.domain.model.Expense
import ir.act.personalAccountant.domain.usecase.AIExchangeRateResult
import ir.act.personalAccountant.domain.usecase.AddExpenseUseCase
import ir.act.personalAccountant.domain.usecase.BudgetUseCase
import ir.act.personalAccountant.domain.usecase.DeleteExpenseUseCase
import ir.act.personalAccountant.domain.usecase.GetAIExchangeRateUseCase
import ir.act.personalAccountant.domain.usecase.GetAllExpensesUseCase
import ir.act.personalAccountant.domain.usecase.GetAllTagsUseCase
import ir.act.personalAccountant.domain.usecase.GetCurrencySettingsUseCase
import ir.act.personalAccountant.domain.usecase.GetExpensesByMonthUseCase
import ir.act.personalAccountant.domain.usecase.GetExpensesByTagForMonthUseCase
import ir.act.personalAccountant.domain.usecase.GetExpensesByTagUseCase
import ir.act.personalAccountant.domain.usecase.GetTotalExpensesByMonthUseCase
import ir.act.personalAccountant.domain.usecase.GetTotalExpensesUseCase
import ir.act.personalAccountant.domain.usecase.GetTripModeSettingsUseCase
import ir.act.personalAccountant.domain.usecase.ToggleTripModeUseCase
import ir.act.personalAccountant.domain.usecase.UpdateTripModeSettingsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ExpenseListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferences: UserPreferences,
    private val getAllExpensesUseCase: GetAllExpensesUseCase,
    private val getTotalExpensesUseCase: GetTotalExpensesUseCase,
    private val getExpensesByTagUseCase: GetExpensesByTagUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    private val getCurrencySettingsUseCase: GetCurrencySettingsUseCase,
    private val getExpensesByMonthUseCase: GetExpensesByMonthUseCase,
    private val getTotalExpensesByMonthUseCase: GetTotalExpensesByMonthUseCase,
    private val getExpensesByTagForMonthUseCase: GetExpensesByTagForMonthUseCase,
    private val budgetUseCase: BudgetUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val getAllTagsUseCase: GetAllTagsUseCase,
    private val aiEngine: AIEngine,
    private val aiRepository: AIRepository,
    private val imageFileManager: ImageFileManager,
    private val getTripModeSettingsUseCase: GetTripModeSettingsUseCase,
    private val toggleTripModeUseCase: ToggleTripModeUseCase,
    private val updateTripModeSettingsUseCase: UpdateTripModeSettingsUseCase,
    private val getAIExchangeRateUseCase: GetAIExchangeRateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseListUiState())
    val uiState: StateFlow<ExpenseListUiState> = _uiState.asStateFlow()

    private val _uiInteraction = Channel<ExpenseListUiInteraction>()
    val uiInteraction = _uiInteraction.receiveAsFlow()

    init {
        initializeCurrentMonth()
        loadUserPreferences()
        loadExpenses()
        loadCurrencySettings()
        loadBudgetSettings()
        loadTripModeSettings()
        loadAvailableCurrencies()
    }

    private fun initializeCurrentMonth() {
        _uiState.value = _uiState.value.copy(
            currentYear = DateUtils.getCurrentYear(),
            currentMonth = DateUtils.getCurrentMonth()
        )
    }

    private fun loadUserPreferences() {
        _uiState.value = _uiState.value.copy(
            isBudgetMode = userPreferences.isBudgetMode
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
                    val newBudgetMode = !currentBudgetMode
                    _uiState.value = _uiState.value.copy(isBudgetMode = newBudgetMode)
                    // Save the new state to preferences
                    userPreferences.isBudgetMode = newBudgetMode
                }
            }
            is ExpenseListEvent.CameraClicked -> {
                _uiState.value = _uiState.value.copy(
                    tempCameraUri = event.uri,
                    aiAnalysisError = null
                )
            }

            ExpenseListEvent.CameraImageCaptured -> {
                analyzeReceiptAndAutoSave()
            }

            ExpenseListEvent.ClearAIAnalysisError -> {
                _uiState.value = _uiState.value.copy(aiAnalysisError = null)
            }

            ExpenseListEvent.TripModeToggled -> {
                val currentTripMode = _uiState.value.tripModeSettings
                if (!currentTripMode.isEnabled) {
                    _uiState.value = _uiState.value.copy(showTripModeSetup = true)
                } else {
                    viewModelScope.launch {
                        toggleTripModeUseCase(false)
                    }
                }
            }

            ExpenseListEvent.ShowTripModeSetup -> {
                _uiState.value = _uiState.value.copy(showTripModeSetup = true)
            }

            ExpenseListEvent.DismissTripModeSetup -> {
                _uiState.value = _uiState.value.copy(
                    showTripModeSetup = false,
                    aiExchangeRateError = null,
                    aiExchangeRate = null,
                    isLoadingAIExchangeRate = false
                )
            }

            is ExpenseListEvent.TripModeSettingsUpdated -> {
                viewModelScope.launch {
                    updateTripModeSettingsUseCase(event.settings)
                    _uiState.value = _uiState.value.copy(showTripModeSetup = false)
                }
            }

            is ExpenseListEvent.AIExchangeRateRequested -> {
                handleAIExchangeRateRequest(event.fromCurrency, event.toCurrency)
            }

            ExpenseListEvent.ClearAIExchangeRateError -> {
                _uiState.value = _uiState.value.copy(
                    aiExchangeRateError = null,
                    aiExchangeRate = null
                )
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
                    val groupedByDay = groupExpensesByDayOfMonth(expenses)
                    _uiState.value = _uiState.value.copy(
                        expenses = expenses,
                        groupedExpensesByDay = groupedByDay,
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

    private fun analyzeReceiptAndAutoSave() {
        viewModelScope.launch {
            try {
                val tempCameraUri = _uiState.value.tempCameraUri
                if (tempCameraUri == null) {
                    _uiState.value = _uiState.value.copy(
                        isAnalyzingReceipt = false,
                        aiAnalysisError = "No image selected"
                    )
                    return@launch
                }

                val apiKey = aiRepository.apiKey.first()
                if (apiKey.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isAnalyzingReceipt = false,
                        aiAnalysisError = "OpenAI API key not configured. Please go to Settings -> AI Settings to configure it."
                    )
                    return@launch
                }

                val availableCategories = getAllTagsUseCase().first().map { it.tag }
                val currencySettings = getCurrencySettingsUseCase().first()
                val currencySymbol =
                    CurrencySettings.getCurrencySymbol(currencySettings.currencyCode)

                _uiState.value = _uiState.value.copy(
                    isAnalyzingReceipt = true,
                )
                aiEngine.analyzeReceiptImage(
                    imageUri = tempCameraUri,
                    availableCategories = availableCategories,
                    apiKey = apiKey,
                    currencySymbol = currencySymbol,
                    inputStreamProvider = { uri ->
                        try {
                            context.contentResolver.openInputStream(uri)
                        } catch (e: Exception) {
                            null
                        }
                    }
                ).collect { result ->
                    _uiState.value = _uiState.value.copy(isAnalyzingReceipt = false)

                    if (result.success) {
                        val totalAmount = result.totalAmount
                        val category = result.category
                        val detectedCurrency = result.detectedCurrency
                        val homeCurrency = _uiState.value.currencySettings.currencyCode

                        // If we have both amount and category with good confidence, auto-save
                        if (totalAmount != null && category != null && result.confidence > 0.7f) {
                            if (detectedCurrency != null && detectedCurrency != homeCurrency) {
                                // Currency is different - handle as travel expense
                                handleTravelExpenseFromCamera(
                                    totalAmount,
                                    detectedCurrency,
                                    homeCurrency,
                                    category,
                                    tempCameraUri
                                )
                            } else {
                                // Same currency or no currency detected - save normally
                                try {
                                    val imagePath = saveImageToStorage(tempCameraUri)
                                    val newExpenseId = addExpenseUseCase(
                                        amount = totalAmount,
                                        tag = category,
                                        timestamp = System.currentTimeMillis(),
                                        imagePath = imagePath
                                    )

                                    // Refresh the expense list
                                    loadExpenses()

                                    // Clear the temp URI
                                    _uiState.value = _uiState.value.copy(tempCameraUri = null)

                                    // Show success message
                                    _uiInteraction.send(
                                        ExpenseListUiInteraction.ShowSuccessMessage(
                                            "Expense added successfully! Amount: ${
                                                CurrencySettings.getCurrencySymbol(
                                                    _uiState.value.currencySettings.currencyCode
                                                )
                                            }$totalAmount, Category: $category"
                                        )
                                    )

                                } catch (e: Exception) {
                                    _uiState.value = _uiState.value.copy(
                                        isAnalyzingReceipt = false,
                                        aiAnalysisError = "Failed to save expense: ${e.message}"
                                    )
                                }
                            }
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isAnalyzingReceipt = false,
                                aiAnalysisError = "Analysis confidence too low (${(result.confidence * 100).toInt()}%). Please add manually."
                            )
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isAnalyzingReceipt = false,
                            aiAnalysisError = result.errorMessage ?: "Analysis failed"
                        )
                    }
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAnalyzingReceipt = false,
                    aiAnalysisError = "Analysis failed: ${e.message}"
                )
            }
        }
    }

    private suspend fun saveImageToStorage(uri: Uri): String? {
        return try {
            val destinationFile = imageFileManager.createImageFile(context)
            val success = imageFileManager.copyImageFromUri(context, uri, destinationFile)
            if (success) {
                destinationFile.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun handleTravelExpenseFromCamera(
        amountInDestinationCurrency: Double,
        destinationCurrency: String,
        homeCurrency: String,
        category: String,
        tempCameraUri: Uri
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isAnalyzingReceipt = true,
                    aiAnalysisError = null
                )

                // Get API key for exchange rate lookup
                val apiKey = aiRepository.apiKey.first()
                if (apiKey.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isAnalyzingReceipt = false,
                        aiAnalysisError = "OpenAI API key not configured for exchange rate lookup."
                    )
                    return@launch
                }

                // Fetch exchange rate from destination currency to home currency
                aiEngine.fetchCurrencyExchangeRate(
                    fromCurrency = destinationCurrency,
                    toCurrency = homeCurrency,
                    apiKey = apiKey
                ).collect { exchangeResult ->
                    _uiState.value = _uiState.value.copy(isAnalyzingReceipt = false)

                    if (exchangeResult.success && exchangeResult.exchangeRate != null) {
                        // Convert amount to home currency
                        val amountInHomeCurrency =
                            amountInDestinationCurrency * exchangeResult.exchangeRate!!

                        // Save as travel expense with dual currency
                        try {
                            val imagePath = saveImageToStorage(tempCameraUri)
                            val newExpenseId = addExpenseUseCase(
                                amount = amountInHomeCurrency, // Save converted amount in home currency
                                tag = category,
                                timestamp = System.currentTimeMillis(),
                                imagePath = imagePath,
                                originalDestinationAmount = amountInDestinationCurrency, // Original amount in destination currency
                                destinationCurrency = destinationCurrency // Detected currency from receipt
                            )

                            // Refresh the expense list
                            loadExpenses()

                            // Clear the temp URI
                            _uiState.value = _uiState.value.copy(tempCameraUri = null)

                            // Show success message with dual currency info
                            _uiInteraction.send(
                                ExpenseListUiInteraction.ShowSuccessMessage(
                                    "Travel expense added! ${
                                        CurrencySettings.getCurrencySymbol(
                                            destinationCurrency
                                        )
                                    }$amountInDestinationCurrency → ${
                                        CurrencySettings.getCurrencySymbol(
                                            homeCurrency
                                        )
                                    }${
                                        String.format(
                                            "%.2f",
                                            amountInHomeCurrency
                                        )
                                    }, Category: $category"
                                )
                            )

                        } catch (e: Exception) {
                            _uiState.value = _uiState.value.copy(
                                isAnalyzingReceipt = false,
                                aiAnalysisError = "Failed to save travel expense: ${e.message}"
                            )
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            aiAnalysisError = "Failed to get exchange rate: ${exchangeResult.errorMessage ?: "Unknown error"}"
                        )
                    }
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAnalyzingReceipt = false,
                    aiAnalysisError = "Failed to process travel expense: ${e.message}"
                )
            }
        }
    }

    private fun loadTripModeSettings() {
        viewModelScope.launch {
            getTripModeSettingsUseCase().collect { tripModeSettings ->
                _uiState.value = _uiState.value.copy(tripModeSettings = tripModeSettings)
            }
        }
    }

    private fun loadAvailableCurrencies() {
        _uiState.value = _uiState.value.copy(
            availableCurrencies = CurrencySettings.SUPPORTED_CURRENCIES
        )
    }

    private fun handleAIExchangeRateRequest(fromCurrency: String, toCurrency: String) {
        viewModelScope.launch {
            getAIExchangeRateUseCase(
                fromCurrency = fromCurrency,
                toCurrency = toCurrency,
                currentTripModeSettings = _uiState.value.tripModeSettings
            ).collect { result ->
                when (result) {
                    is AIExchangeRateResult.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoadingAIExchangeRate = true,
                            aiExchangeRateError = null,
                            aiExchangeRate = null
                        )
                    }

                    is AIExchangeRateResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoadingAIExchangeRate = false,
                            aiExchangeRate = result.exchangeRate,
                            aiExchangeRateError = null,
                            tripModeSettings = result.updatedTripModeSettings
                        )
                    }

                    is AIExchangeRateResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoadingAIExchangeRate = false,
                            aiExchangeRateError = result.message
                        )
                    }
                }
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
}