package ir.act.personalAccountant.presentation.net_worth_calculation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.act.personalAccountant.domain.usecase.CalculateNetWorthUseCase
import ir.act.personalAccountant.domain.usecase.GetCurrencySettingsUseCase
import ir.act.personalAccountant.domain.usecase.ManageAssetsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NetWorthCalculationViewModel @Inject constructor(
    private val manageAssetsUseCase: ManageAssetsUseCase,
    private val calculateNetWorthUseCase: CalculateNetWorthUseCase,
    private val getCurrencySettingsUseCase: GetCurrencySettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NetWorthCalculationUiState())
    val uiState: StateFlow<NetWorthCalculationUiState> = _uiState.asStateFlow()

    private val _uiInteraction = Channel<NetWorthCalculationUiInteraction>()
    val uiInteraction = _uiInteraction.receiveAsFlow()

    init {
        loadData()
    }

    fun onEvent(event: NetWorthCalculationEvent) {
        when (event) {
            is NetWorthCalculationEvent.AssetValueChanged -> {
                updateAssetValue(event.assetId, event.value)
            }

            is NetWorthCalculationEvent.TargetCurrencyChanged -> {
                _uiState.value = _uiState.value.copy(targetCurrency = event.currency)
            }

            NetWorthCalculationEvent.CalculateNetWorth -> {
                calculateNetWorth()
            }

            NetWorthCalculationEvent.ClearError -> {
                _uiState.value = _uiState.value.copy(error = null)
            }

            NetWorthCalculationEvent.DismissSuccessMessage -> {
                _uiState.value = _uiState.value.copy(showSuccessMessage = false)
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val assets = manageAssetsUseCase.getAllAssets().first()
                val currencySettings = getCurrencySettingsUseCase().first()

                val assetValueInputs = assets.map { asset ->
                    AssetValueInput(
                        asset = asset,
                        valueInTargetCurrency = asset.amount.toString()
                    )
                }

                _uiState.value = _uiState.value.copy(
                    assets = assets,
                    assetValueInputs = assetValueInputs,
                    targetCurrency = currencySettings.currencyCode,
                    currencySettings = currencySettings,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load assets"
                )
            }
        }
    }

    private fun updateAssetValue(assetId: Long, value: String) {
        val updatedInputs = _uiState.value.assetValueInputs.map { input ->
            if (input.asset.id == assetId) {
                input.copy(valueInTargetCurrency = value)
            } else {
                input
            }
        }

        _uiState.value = _uiState.value.copy(
            assetValueInputs = updatedInputs,
            calculatedNetWorth = calculateTotalValue(updatedInputs)
        )
    }

    private fun calculateTotalValue(inputs: List<AssetValueInput>): Double {
        return inputs.sumOf { input ->
            val valuePerUnit = input.valueInTargetCurrency.toDoubleOrNull() ?: 0.0
            valuePerUnit * input.asset.quantity
        }
    }

    private fun calculateNetWorth() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCalculating = true)

            try {
                val assetValues = _uiState.value.assetValueInputs.associate { input ->
                    input.asset.id to (input.valueInTargetCurrency.toDoubleOrNull() ?: 0.0)
                }

                val snapshotId = calculateNetWorthUseCase.calculateAndSaveNetWorth(
                    assetValues = assetValues,
                    targetCurrency = _uiState.value.targetCurrency
                )

                _uiState.value = _uiState.value.copy(
                    isCalculating = false,
                    showSuccessMessage = true
                )

                // Navigate back after a short delay
                kotlinx.coroutines.delay(1500)
                _uiInteraction.send(NetWorthCalculationUiInteraction.NavigateBack(snapshotId))

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCalculating = false,
                    error = e.message ?: "Failed to calculate net worth"
                )
            }
        }
    }
}