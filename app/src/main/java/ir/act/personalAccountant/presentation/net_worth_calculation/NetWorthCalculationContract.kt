package ir.act.personalAccountant.presentation.net_worth_calculation

import ir.act.personalAccountant.domain.model.Asset
import ir.act.personalAccountant.domain.model.CurrencySettings

data class AssetValueInput(
    val asset: Asset,
    val valueInTargetCurrency: String = ""
)

data class NetWorthCalculationUiState(
    val assets: List<Asset> = emptyList(),
    val assetValueInputs: List<AssetValueInput> = emptyList(),
    val targetCurrency: String = "",
    val calculatedNetWorth: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCalculating: Boolean = false,
    val showSuccessMessage: Boolean = false,
    val currencySettings: CurrencySettings = CurrencySettings()
)

sealed class NetWorthCalculationEvent {
    data class AssetValueChanged(val assetId: Long, val value: String) : NetWorthCalculationEvent()
    data class TargetCurrencyChanged(val currency: String) : NetWorthCalculationEvent()
    object CalculateNetWorth : NetWorthCalculationEvent()
    object ClearError : NetWorthCalculationEvent()
    object DismissSuccessMessage : NetWorthCalculationEvent()
}

sealed class NetWorthCalculationUiInteraction {
    data class NavigateBack(val netWorthSnapshot: Long? = null) : NetWorthCalculationUiInteraction()
}