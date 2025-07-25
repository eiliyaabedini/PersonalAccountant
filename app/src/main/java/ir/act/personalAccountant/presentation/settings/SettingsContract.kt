package ir.act.personalAccountant.presentation.settings

import ir.act.personalAccountant.domain.model.BudgetSettings
import ir.act.personalAccountant.domain.model.CurrencySettings
import ir.act.personalAccountant.domain.model.User

object SettingsContract {
    
    data class UiState(
        val currentCurrencySettings: CurrencySettings = CurrencySettings.DEFAULT,
        val availableCurrencies: List<CurrencySettings> = CurrencySettings.SUPPORTED_CURRENCIES,
        val showCurrencyPicker: Boolean = false,
        val budgetSettings: BudgetSettings = BudgetSettings(),
        val isNotificationEnabled: Boolean = false,
        val isDailyReminderEnabled: Boolean = false,
        val hasNotificationPermission: Boolean = false,
        val currentUser: User? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    )
    
    sealed class Events {
        object CurrencyPickerClicked : Events()
        object DismissCurrencyPicker : Events()
        data class CurrencySelected(val currencySettings: CurrencySettings) : Events()
        object BudgetConfigClicked : Events()
        object CategorySettingsClicked : Events()
        object AccountSettingsClicked : Events()
        object SignOutClicked : Events()
        data class NotificationToggleClicked(val enabled: Boolean) : Events()
        data class DailyReminderToggleClicked(val enabled: Boolean) : Events()
        object ClearError : Events()
    }
    
    interface UiInteractions {
        fun navigateBack()
        fun navigateToBudgetConfig()
        fun navigateToCategorySettings()
        fun navigateToGoogleSheets()
        fun navigateToAISettings()
        fun navigateToFinancialAdvisor()
        fun navigateToNetWorth()
        fun navigateToLogin()
    }
}