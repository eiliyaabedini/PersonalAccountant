package ir.act.personalAccountant.presentation.expense_entry

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.act.personalAccountant.ai.AIEngine
import ir.act.personalAccountant.ai.data.repository.AIRepository
import ir.act.personalAccountant.core.util.ImageFileManager
import ir.act.personalAccountant.domain.model.CurrencySettings
import ir.act.personalAccountant.domain.usecase.AIExchangeRateResult
import ir.act.personalAccountant.domain.usecase.AddExpenseUseCase
import ir.act.personalAccountant.domain.usecase.GetAIExchangeRateUseCase
import ir.act.personalAccountant.domain.usecase.GetAllTagsUseCase
import ir.act.personalAccountant.domain.usecase.GetCurrencySettingsUseCase
import ir.act.personalAccountant.domain.usecase.GetExpensesByTagUseCase
import ir.act.personalAccountant.domain.usecase.GetTotalExpensesUseCase
import ir.act.personalAccountant.domain.usecase.GetTripModeSettingsUseCase
import ir.act.personalAccountant.domain.usecase.ToggleTripModeUseCase
import ir.act.personalAccountant.domain.usecase.UpdateTripModeSettingsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseEntryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val getTotalExpensesUseCase: GetTotalExpensesUseCase,
    private val getAllTagsUseCase: GetAllTagsUseCase,
    private val getExpensesByTagUseCase: GetExpensesByTagUseCase,
    private val getCurrencySettingsUseCase: GetCurrencySettingsUseCase,
    private val imageFileManager: ImageFileManager,
    private val aiEngine: AIEngine,
    private val aiRepository: AIRepository,
    private val getTripModeSettingsUseCase: GetTripModeSettingsUseCase,
    private val toggleTripModeUseCase: ToggleTripModeUseCase,
    private val updateTripModeSettingsUseCase: UpdateTripModeSettingsUseCase,
    private val getAIExchangeRateUseCase: GetAIExchangeRateUseCase
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
        loadTripModeSettings()
        loadAvailableCurrencies()
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
            ExpenseEntryEvent.ImagePickerClicked -> {
                _uiState.update { it.copy(showImagePicker = true) }
            }
            is ExpenseEntryEvent.ImageSelected -> {
                handleImageSelected(event.uri)
            }
            ExpenseEntryEvent.ImageCaptured -> {
                handleImageCaptured()
            }
            is ExpenseEntryEvent.CameraLaunchRequested -> {
                handleCameraLaunchRequested(event.uri)
            }
            ExpenseEntryEvent.RemoveImage -> {
                handleRemoveImage()
            }
            ExpenseEntryEvent.DismissImagePicker -> {
                _uiState.update { it.copy(showImagePicker = false) }
            }
            ExpenseEntryEvent.ShowImageViewer -> {
                _uiState.update { it.copy(showImageViewer = true) }
            }
            ExpenseEntryEvent.DismissImageViewer -> {
                _uiState.update { it.copy(showImageViewer = false) }
            }
            ExpenseEntryEvent.AnalyzeReceiptClicked -> {
                analyzeReceipt()
            }

            ExpenseEntryEvent.ClearAIAnalysisError -> {
                _uiState.update { it.copy(aiAnalysisError = null) }
            }

            ExpenseEntryEvent.TripModeToggled -> {
                val currentTripMode = _uiState.value.tripModeSettings
                if (!currentTripMode.isEnabled) {
                    _uiState.update { it.copy(showTripModeSetup = true) }
                } else {
                    viewModelScope.launch {
                        toggleTripModeUseCase(false)
                    }
                }
            }

            ExpenseEntryEvent.ShowTripModeSetup -> {
                _uiState.update { it.copy(showTripModeSetup = true) }
            }

            ExpenseEntryEvent.DismissTripModeSetup -> {
                _uiState.update {
                    it.copy(
                        showTripModeSetup = false,
                        aiExchangeRateError = null,
                        aiExchangeRate = null,
                        isLoadingAIExchangeRate = false
                    )
                }
            }

            is ExpenseEntryEvent.TripModeSettingsUpdated -> {
                viewModelScope.launch {
                    updateTripModeSettingsUseCase(event.settings)
                    _uiState.update { it.copy(showTripModeSetup = false) }
                }
            }

            is ExpenseEntryEvent.AIExchangeRateRequested -> {
                println("DEBUG: AIExchangeRateRequested event received: ${event.fromCurrency} -> ${event.toCurrency}")
                handleAIExchangeRateRequest(event.fromCurrency, event.toCurrency)
            }

            ExpenseEntryEvent.ClearAIExchangeRateError -> {
                _uiState.update {
                    it.copy(
                        aiExchangeRateError = null,
                        aiExchangeRate = null
                    )
                }
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
                
                // Save image if selected
                val imagePath = currentState.selectedImageUri?.let { uri ->
                    saveSelectedImage(uri)
                }

                // Convert to home currency if trip mode is active
                val amountToSave = if (currentState.tripModeSettings.isEnabled) {
                    // Convert from destination currency to home currency
                    amount / currentState.tripModeSettings.exchangeRate
                } else {
                    amount
                }

                val newExpenseId = addExpenseUseCase(
                    amount = amountToSave,
                    tag = tag,
                    timestamp = currentState.selectedDate,
                    imagePath = imagePath,
                    originalDestinationAmount = if (currentState.tripModeSettings.isEnabled) amount else null
                )
                
                if (currentState.addMultipleExpenses) {
                    // Stay on the page, just clear the form
                    _uiState.update {
                        it.copy(
                            currentAmount = "",
                            isLoading = false,
                            selectedTag = "", // Reset to no selection
                            selectedDate = System.currentTimeMillis(), // Reset date to current time
                            selectedImageUri = null, // Clear selected image
                            tempCameraUri = null, // Clear temp camera URI
                            isProcessingImage = false
                        )
                    }
                } else {
                    // Navigate away as before
                    _uiState.update {
                        it.copy(
                            currentAmount = "",
                            isLoading = false,
                            selectedTag = "", // Reset to no selection
                            selectedDate = System.currentTimeMillis(), // Reset date to current time
                            selectedImageUri = null, // Clear selected image
                            tempCameraUri = null, // Clear temp camera URI
                            isProcessingImage = false
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
    
    private fun handleImageSelected(uri: Uri) {
        _uiState.update {
            it.copy(
                selectedImageUri = uri,
                showImagePicker = false,
                isProcessingImage = false
            )
        }
    }
    
    private fun handleImageCaptured() {
        // Use the temp camera URI as the selected image
        val tempUri = _uiState.value.tempCameraUri
        if (tempUri != null) {
            _uiState.update {
                it.copy(
                    selectedImageUri = tempUri,
                    showImagePicker = false,
                    isProcessingImage = false
                )
            }
        }
    }
    
    private fun handleCameraLaunchRequested(uri: Uri) {
        _uiState.update {
            it.copy(
                tempCameraUri = uri,
                showImagePicker = false,
                isProcessingImage = false
            )
        }
    }
    
    private fun handleRemoveImage() {
        _uiState.update {
            it.copy(
                selectedImageUri = null,
                tempCameraUri = null,
                isProcessingImage = false
            )
        }
    }
    
    private suspend fun saveSelectedImage(sourceUri: Uri): String? {
        return try {
            _uiState.update { it.copy(isProcessingImage = true) }
            val destinationFile = imageFileManager.createImageFile(context)
            val success = imageFileManager.copyImageFromUri(context, sourceUri, destinationFile)
            if (success) {
                destinationFile.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            _uiState.update { it.copy(isProcessingImage = false) }
        }
    }

    private fun analyzeReceipt() {
        val currentState = _uiState.value
        val selectedImageUri = currentState.selectedImageUri

        if (selectedImageUri == null) {
            _uiState.update {
                it.copy(aiAnalysisError = "No image selected for analysis")
            }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isAnalyzingReceipt = true, aiAnalysisError = null) }

                // Get API key
                val apiKey = aiRepository.apiKey.first()
                if (apiKey.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isAnalyzingReceipt = false,
                            aiAnalysisError = "OpenAI API key not configured. Please go to Settings > AI Settings to set up your API key."
                        )
                    }
                    return@launch
                }

                // Get available categories
                val availableCategories = currentState.availableTags.map { it.tag }
                if (availableCategories.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isAnalyzingReceipt = false,
                            aiAnalysisError = "No categories available for analysis"
                        )
                    }
                    return@launch
                }

                // Get currency symbol
                val currencySymbol =
                    CurrencySettings.getCurrencySymbol(currentState.currencySettings.currencyCode)

                // Analyze receipt
                aiEngine.analyzeReceiptImage(
                    imageUri = selectedImageUri,
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
                    _uiState.update { it.copy(isAnalyzingReceipt = false) }

                    if (result.success) {
                        // Update UI with analysis results
                        result.totalAmount?.let { amount ->
                            _uiState.update {
                                it.copy(currentAmount = amount.toString())
                            }
                        }

                        result.category?.let { category ->
                            _uiState.update {
                                it.copy(selectedTag = category)
                            }
                        }

                        // Check if detected currency is different from home currency
                        val detectedCurrency = result.detectedCurrency
                        val homeCurrency = currentState.currencySettings.currencyCode
                        val totalAmount = result.totalAmount

                        if (totalAmount != null && result.category != null && result.confidence > 0.7f) {
                            if (detectedCurrency != null && detectedCurrency != homeCurrency) {
                                // Currency is different - we need to handle this as a travel expense
                                handleTravelExpenseFromReceipt(
                                    totalAmount,
                                    detectedCurrency,
                                    homeCurrency,
                                    result.category!!
                                )
                            } else {
                                // Same currency or no currency detected - save normally
                                saveExpense(amount = totalAmount)
                            }
                        }
                    } else {
                        _uiState.update {
                            it.copy(aiAnalysisError = result.errorMessage ?: "Analysis failed")
                        }
                    }
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAnalyzingReceipt = false,
                        aiAnalysisError = "Analysis failed: ${e.message}"
                    )
                }
            }
        }
    }

    private suspend fun saveExpense(amount: Double) {
        val currentState = _uiState.value
        val selectedTag = currentState.selectedTag

        if (selectedTag.isEmpty()) {
            return
        }

        try {
            // Convert to home currency if trip mode is active
            val amountToSave = if (currentState.tripModeSettings.isEnabled) {
                // Convert from destination currency to home currency
                amount / currentState.tripModeSettings.exchangeRate
            } else {
                amount
            }
            
            val newExpenseId = addExpenseUseCase(
                amount = amountToSave,
                tag = selectedTag,
                timestamp = currentState.selectedDate,
                imagePath = currentState.selectedImageUri?.let { uri ->
                    saveSelectedImage(uri)
                },
                originalDestinationAmount = if (currentState.tripModeSettings.isEnabled) amount else null
            )

            // Navigate back to expense list
            _uiInteraction.send(ExpenseEntryUiInteraction.NavigateToExpenseList(newExpenseId))

        } catch (e: Exception) {
            _uiState.update {
                it.copy(aiAnalysisError = "Failed to save expense: ${e.message}")
            }
        }
    }

    private suspend fun handleTravelExpenseFromReceipt(
        amountInDestinationCurrency: Double,
        destinationCurrency: String,
        homeCurrency: String,
        category: String
    ) {
        try {
            _uiState.update {
                it.copy(
                    isAnalyzingReceipt = true,
                    aiAnalysisError = null
                )
            }

            // Get API key for exchange rate lookup
            val apiKey = aiRepository.apiKey.first()
            if (apiKey.isEmpty()) {
                _uiState.update {
                    it.copy(
                        isAnalyzingReceipt = false,
                        aiAnalysisError = "OpenAI API key not configured for exchange rate lookup."
                    )
                }
                return
            }

            // Fetch exchange rate from destination currency to home currency
            aiEngine.fetchCurrencyExchangeRate(
                fromCurrency = destinationCurrency,
                toCurrency = homeCurrency,
                apiKey = apiKey
            ).collect { exchangeResult ->
                _uiState.update { it.copy(isAnalyzingReceipt = false) }

                if (exchangeResult.success && exchangeResult.exchangeRate != null) {
                    // Convert amount to home currency
                    val amountInHomeCurrency =
                        amountInDestinationCurrency * exchangeResult.exchangeRate!!

                    // Save as travel expense with dual currency
                    val currentState = _uiState.value
                    val imagePath = currentState.selectedImageUri?.let { uri ->
                        saveSelectedImage(uri)
                    }

                    val newExpenseId = addExpenseUseCase(
                        amount = amountInHomeCurrency, // Save converted amount in home currency
                        tag = category,
                        timestamp = currentState.selectedDate,
                        imagePath = imagePath,
                        originalDestinationAmount = amountInDestinationCurrency, // Original amount in destination currency
                        destinationCurrency = destinationCurrency // Detected currency from receipt
                    )

                    // Clear the form
                    _uiState.update {
                        it.copy(
                            currentAmount = "",
                            selectedTag = "",
                            selectedDate = System.currentTimeMillis(),
                            selectedImageUri = null,
                            tempCameraUri = null,
                            isProcessingImage = false
                        )
                    }

                    // Navigate back to expense list
                    _uiInteraction.send(ExpenseEntryUiInteraction.NavigateToExpenseList(newExpenseId))

                } else {
                    _uiState.update {
                        it.copy(
                            aiAnalysisError = "Failed to get exchange rate: ${exchangeResult.errorMessage ?: "Unknown error"}"
                        )
                    }
                }
            }

        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isAnalyzingReceipt = false,
                    aiAnalysisError = "Failed to process travel expense: ${e.message}"
                )
            }
        }
    }

    private fun loadTripModeSettings() {
        viewModelScope.launch {
            getTripModeSettingsUseCase().collect { tripModeSettings ->
                _uiState.update { it.copy(tripModeSettings = tripModeSettings) }
            }
        }
    }

    private fun loadAvailableCurrencies() {
        _uiState.update {
            it.copy(availableCurrencies = CurrencySettings.SUPPORTED_CURRENCIES)
        }
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
                        _uiState.update {
                            it.copy(
                                isLoadingAIExchangeRate = true,
                                aiExchangeRateError = null,
                                aiExchangeRate = null
                            )
                        }
                    }

                    is AIExchangeRateResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoadingAIExchangeRate = false,
                                aiExchangeRate = result.exchangeRate,
                                aiExchangeRateError = null,
                                tripModeSettings = result.updatedTripModeSettings
                            )
                        }
                    }

                    is AIExchangeRateResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoadingAIExchangeRate = false,
                                aiExchangeRateError = result.message
                            )
                        }
                    }
                }
            }
        }
    }
}