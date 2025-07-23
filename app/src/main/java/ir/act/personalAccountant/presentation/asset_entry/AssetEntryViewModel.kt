package ir.act.personalAccountant.presentation.asset_entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.act.personalAccountant.domain.usecase.AssetImageAnalysisUseCase
import ir.act.personalAccountant.domain.usecase.AssetSnapshotUseCase
import ir.act.personalAccountant.domain.usecase.GetCurrencySettingsUseCase
import ir.act.personalAccountant.domain.usecase.ManageAssetsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssetEntryViewModel @Inject constructor(
    private val manageAssetsUseCase: ManageAssetsUseCase,
    private val assetSnapshotUseCase: AssetSnapshotUseCase,
    private val getCurrencySettingsUseCase: GetCurrencySettingsUseCase,
    private val assetImageAnalysisUseCase: AssetImageAnalysisUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val assetId: Long? = savedStateHandle.get<Long>("assetId")

    private val _uiState = MutableStateFlow(AssetEntryUiState())
    val uiState: StateFlow<AssetEntryUiState> = _uiState.asStateFlow()

    private val _uiInteraction = Channel<AssetEntryUiInteraction>()
    val uiInteraction = _uiInteraction.receiveAsFlow()

    init {
        loadCurrencySettings()
        loadAssetTypes()
        assetId?.let { loadAsset(it) }
    }

    fun onEvent(event: AssetEntryEvent) {
        when (event) {
            is AssetEntryEvent.AssetNameChanged -> {
                _uiState.value = _uiState.value.copy(assetName = event.name)
            }

            is AssetEntryEvent.AssetTypeChanged -> {
                _uiState.value = _uiState.value.copy(assetType = event.type)
            }

            is AssetEntryEvent.AmountChanged -> {
                _uiState.value = _uiState.value.copy(currentAmount = event.amount)
            }

            is AssetEntryEvent.NumberClicked -> {
                val currentAmount = _uiState.value.currentAmount
                val newAmount = if (currentAmount == "0") {
                    event.number
                } else {
                    currentAmount + event.number
                }
                _uiState.value = _uiState.value.copy(currentAmount = newAmount)
            }

            AssetEntryEvent.DecimalClicked -> {
                val currentAmount = _uiState.value.currentAmount
                if (!currentAmount.contains(".")) {
                    val newAmount = if (currentAmount.isEmpty()) "0." else "$currentAmount."
                    _uiState.value = _uiState.value.copy(currentAmount = newAmount)
                }
            }

            AssetEntryEvent.BackspaceClicked -> {
                val currentAmount = _uiState.value.currentAmount
                if (currentAmount.isNotEmpty()) {
                    val newAmount = currentAmount.dropLast(1)
                    _uiState.value = _uiState.value.copy(
                        currentAmount = if (newAmount.isEmpty()) "0" else newAmount
                    )
                }
            }

            is AssetEntryEvent.QuantityChanged -> {
                _uiState.value = _uiState.value.copy(quantity = event.quantity)
            }

            is AssetEntryEvent.NotesChanged -> {
                _uiState.value = _uiState.value.copy(notes = event.notes)
            }

            AssetEntryEvent.AddTypeClicked -> {
                _uiState.value = _uiState.value.copy(showAddTypeDialog = true)
            }

            is AssetEntryEvent.NewTypeNameChanged -> {
                _uiState.value = _uiState.value.copy(newTypeName = event.name)
            }

            AssetEntryEvent.ConfirmNewType -> {
                val newType = _uiState.value.newTypeName.trim()
                if (newType.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        assetType = newType,
                        showAddTypeDialog = false,
                        newTypeName = "",
                        availableAssetTypes = _uiState.value.availableAssetTypes + newType
                    )
                }
            }

            AssetEntryEvent.DismissAddTypeDialog -> {
                _uiState.value = _uiState.value.copy(
                    showAddTypeDialog = false,
                    newTypeName = ""
                )
            }

            AssetEntryEvent.AnalyzeImageClicked -> {
                viewModelScope.launch {
                    _uiInteraction.send(AssetEntryUiInteraction.OpenImagePicker)
                }
            }

            is AssetEntryEvent.ImageSelected -> {
                analyzeImage(event.imageUri)
            }

            AssetEntryEvent.ClearAiAnalysisMessage -> {
                _uiState.value = _uiState.value.copy(aiAnalysisMessage = null)
            }

            AssetEntryEvent.SaveAsset -> {
                saveAsset()
            }

            AssetEntryEvent.ClearError -> {
                _uiState.value = _uiState.value.copy(error = null)
            }
        }
    }

    private fun loadAsset(assetId: Long) {
        viewModelScope.launch {
            try {
                val asset = manageAssetsUseCase.getAssetById(assetId)
                asset?.let {
                    _uiState.value = _uiState.value.copy(
                        assetName = it.name,
                        assetType = it.type,
                        currentAmount = it.amount.toString(),
                        selectedCurrency = it.currency,
                        quantity = it.quantity.toString(),
                        notes = it.notes ?: "",
                        isEditMode = true,
                        assetId = it.id
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to load asset"
                )
            }
        }
    }

    private fun loadCurrencySettings() {
        viewModelScope.launch {
            getCurrencySettingsUseCase().collect { currencySettings ->
                _uiState.value = _uiState.value.copy(
                    currencySettings = currencySettings,
                    selectedCurrency = if (_uiState.value.selectedCurrency.isEmpty()) {
                        currencySettings.currencyCode
                    } else {
                        _uiState.value.selectedCurrency
                    }
                )
            }
        }
    }

    private fun loadAssetTypes() {
        viewModelScope.launch {
            manageAssetsUseCase.getAllAssetTypes().collect { types ->
                _uiState.value = _uiState.value.copy(availableAssetTypes = types)
            }
        }
    }

    private fun saveAsset() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val state = _uiState.value
                // Use BigDecimal for better precision handling before converting to Double
                val amount = try {
                    state.currentAmount.toBigDecimal().toDouble()
                } catch (e: Exception) {
                    0.0
                }
                val quantity = try {
                    state.quantity.toBigDecimal().toDouble()
                } catch (e: Exception) {
                    1.0
                }

                val newAssetId = if (state.isEditMode && state.assetId != null) {
                    manageAssetsUseCase.updateAsset(
                        id = state.assetId,
                        name = state.assetName,
                        type = state.assetType,
                        amount = amount,
                        currency = state.selectedCurrency,
                        quantity = quantity,
                        notes = state.notes.takeIf { it.isNotBlank() }
                    )
                    // Create asset snapshot for the updated asset
                    assetSnapshotUseCase.createAssetSnapshot(
                        state.assetId,
                        amount,
                        quantity,
                        state.selectedCurrency
                    )
                    state.assetId
                } else {
                    val assetId = manageAssetsUseCase.addAsset(
                        name = state.assetName,
                        type = state.assetType,
                        amount = amount,
                        currency = state.selectedCurrency,
                        quantity = quantity,
                        notes = state.notes.takeIf { it.isNotBlank() }
                    )
                    // Create initial asset snapshot for the new asset
                    assetSnapshotUseCase.createAssetSnapshot(
                        assetId,
                        amount,
                        quantity,
                        state.selectedCurrency
                    )
                    assetId
                }

                _uiState.value = _uiState.value.copy(isLoading = false)
                _uiInteraction.send(AssetEntryUiInteraction.NavigateBack(newAssetId))

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save asset"
                )
            }
        }
    }

    private fun analyzeImage(imageUri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAnalyzingImage = true,
                aiAnalysisMessage = null
            )

            assetImageAnalysisUseCase.analyzeAssetImage(imageUri)
                .onSuccess { analysisResult ->
                    // Auto-populate form with AI analysis results
                    val message =
                        "AI Analysis completed with ${(analysisResult.confidence * 100).toInt()}% confidence"

                    // Update form fields if analysis was successful and confidence is high enough
                    if (analysisResult.confidence > 0.5f) {
                        _uiState.value = _uiState.value.copy(
                            assetName = analysisResult.assetName,
                            assetType = analysisResult.assetType,
                            currentAmount = analysisResult.amountPerUnit, // Keep as string for precision
                            quantity = analysisResult.quantity, // Keep as string for precision
                            selectedCurrency = analysisResult.currency,
                            isAnalyzingImage = false,
                            aiAnalysisMessage = message,
                            // Add new asset type if it doesn't exist
                            availableAssetTypes = if (!_uiState.value.availableAssetTypes.contains(
                                    analysisResult.assetType
                                )
                            ) {
                                _uiState.value.availableAssetTypes + analysisResult.assetType
                            } else {
                                _uiState.value.availableAssetTypes
                            }
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isAnalyzingImage = false,
                            aiAnalysisMessage = "Analysis completed but confidence was low (${(analysisResult.confidence * 100).toInt()}%). Please verify the values."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isAnalyzingImage = false,
                        aiAnalysisMessage = "AI Analysis failed: ${error.message}"
                    )
                }
        }
    }
}