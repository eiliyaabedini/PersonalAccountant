package ir.act.personalAccountant.presentation.settings

import ir.act.personalAccountant.domain.model.BudgetSettings
import ir.act.personalAccountant.domain.model.CurrencySettings

object SettingsContract {
    
    data class UiState(
        val currentCurrencySettings: CurrencySettings = CurrencySettings.DEFAULT,
        val availableCurrencies: List<CurrencySettings> = CurrencySettings.SUPPORTED_CURRENCIES,
        val showCurrencyPicker: Boolean = false,
        val budgetSettings: BudgetSettings = BudgetSettings(),
        val isLoading: Boolean = false,
        val error: String? = null
    )
    
    sealed class Events {
        object CurrencyPickerClicked : Events()
        object DismissCurrencyPicker : Events()
        data class CurrencySelected(val currencySettings: CurrencySettings) : Events()
        object BudgetConfigClicked : Events()
        object ClearError : Events()
    }
    
    interface UiInteractions {
        fun navigateBack()
        fun navigateToBudgetConfig()
    }
}