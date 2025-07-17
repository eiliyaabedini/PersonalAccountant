package ir.act.personalAccountant.presentation.financial_advisor

import ir.act.personalAccountant.domain.model.BudgetData
import ir.act.personalAccountant.domain.model.CurrencySettings
import ir.act.personalAccountant.domain.model.Expense
import ir.act.personalAccountant.domain.model.TagExpenseData

data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class FinancialAdvisorUiState(
    val expenses: List<Expense> = emptyList(),
    val totalExpenses: Double = 0.0,
    val tagExpenseData: List<TagExpenseData> = emptyList(),
    val budgetData: BudgetData? = null,
    val currencySettings: CurrencySettings = CurrencySettings(),
    val currentYear: Int = 0,
    val currentMonth: Int = 0,
    val chatMessages: List<ChatMessage> = emptyList(),
    val currentMessage: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isApiKeyConfigured: Boolean = false
)

sealed class FinancialAdvisorEvent {
    data class MessageChanged(val message: String) : FinancialAdvisorEvent()
    object SendMessage : FinancialAdvisorEvent()
    object GetGeneralAdvice : FinancialAdvisorEvent()
    object ClearError : FinancialAdvisorEvent()
    object ClearChat : FinancialAdvisorEvent()
}

sealed class FinancialAdvisorUiInteraction {
    object NavigateToAISettings : FinancialAdvisorUiInteraction()
}