package ir.act.personalAccountant.presentation.financial_advisor

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.act.personalAccountant.ai.data.repository.AIRepository
import ir.act.personalAccountant.core.util.DateUtils
import ir.act.personalAccountant.domain.usecase.BudgetUseCase
import ir.act.personalAccountant.domain.usecase.FinancialAdviceRequest
import ir.act.personalAccountant.domain.usecase.FinancialAdvisorUseCase
import ir.act.personalAccountant.domain.usecase.GetCurrencySettingsUseCase
import ir.act.personalAccountant.domain.usecase.GetExpensesByMonthUseCase
import ir.act.personalAccountant.domain.usecase.GetExpensesByTagForMonthUseCase
import ir.act.personalAccountant.domain.usecase.GetTotalExpensesByMonthUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FinancialAdvisorViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val financialAdvisorUseCase: FinancialAdvisorUseCase,
    private val aiRepository: AIRepository,
    private val getExpensesByMonthUseCase: GetExpensesByMonthUseCase,
    private val getTotalExpensesByMonthUseCase: GetTotalExpensesByMonthUseCase,
    private val getExpensesByTagForMonthUseCase: GetExpensesByTagForMonthUseCase,
    private val getCurrencySettingsUseCase: GetCurrencySettingsUseCase,
    private val budgetUseCase: BudgetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinancialAdvisorUiState())
    val uiState: StateFlow<FinancialAdvisorUiState> = _uiState.asStateFlow()

    private val _uiInteraction = Channel<FinancialAdvisorUiInteraction>()
    val uiInteraction = _uiInteraction.receiveAsFlow()

    init {
        initializeCurrentMonth()
        loadFinancialData()
        checkApiKeyConfiguration()
    }

    private fun initializeCurrentMonth() {
        _uiState.update {
            it.copy(
                currentYear = DateUtils.getCurrentYear(),
                currentMonth = DateUtils.getCurrentMonth()
            )
        }
    }

    private fun loadFinancialData() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                combine(
                    getExpensesByMonthUseCase(currentState.currentYear, currentState.currentMonth),
                    getTotalExpensesByMonthUseCase(
                        currentState.currentYear,
                        currentState.currentMonth
                    ),
                    getExpensesByTagForMonthUseCase(
                        currentState.currentYear,
                        currentState.currentMonth
                    ),
                    getCurrencySettingsUseCase(),
                    budgetUseCase.getBudgetData()
                ) { expenses, totalExpenses, tagExpenseData, currencySettings, budgetData ->
                    _uiState.update {
                        it.copy(
                            expenses = expenses,
                            totalExpenses = totalExpenses,
                            tagExpenseData = tagExpenseData,
                            currencySettings = currencySettings,
                            budgetData = budgetData
                        )
                    }
                }.collect { }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to load financial data: ${e.message}")
                }
            }
        }
    }

    private fun checkApiKeyConfiguration() {
        viewModelScope.launch {
            try {
                val apiKey = aiRepository.apiKey.first()
                _uiState.update {
                    it.copy(isApiKeyConfigured = apiKey.isNotBlank())
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isApiKeyConfigured = false)
                }
            }
        }
    }

    fun onEvent(event: FinancialAdvisorEvent) {
        when (event) {
            is FinancialAdvisorEvent.MessageChanged -> {
                _uiState.update { it.copy(currentMessage = event.message) }
            }

            FinancialAdvisorEvent.SendMessage -> {
                val message = _uiState.value.currentMessage.trim()
                if (message.isNotBlank()) {
                    sendMessage(message)
                }
            }

            FinancialAdvisorEvent.GetGeneralAdvice -> {
                getGeneralFinancialAdvice()
            }

            FinancialAdvisorEvent.ClearError -> {
                _uiState.update { it.copy(error = null) }
            }

            FinancialAdvisorEvent.ClearChat -> {
                _uiState.update { it.copy(chatMessages = emptyList()) }
            }
        }
    }

    private fun sendMessage(message: String) {
        if (!_uiState.value.isApiKeyConfigured) {
            viewModelScope.launch {
                _uiInteraction.send(FinancialAdvisorUiInteraction.NavigateToAISettings)
            }
            return
        }

        // Add user message to chat
        val userMessage = ChatMessage(
            content = message,
            isUser = true
        )

        _uiState.update {
            it.copy(
                chatMessages = it.chatMessages + userMessage,
                currentMessage = "",
                isLoading = true,
                error = null
            )
        }

        viewModelScope.launch {
            try {
                val apiKey = aiRepository.apiKey.first()
                val currentState = _uiState.value

                val request = FinancialAdviceRequest(
                    expenses = currentState.expenses,
                    totalExpenses = currentState.totalExpenses,
                    tagExpenseData = currentState.tagExpenseData,
                    budgetData = currentState.budgetData,
                    currencySettings = currentState.currencySettings,
                    currentYear = currentState.currentYear,
                    currentMonth = currentState.currentMonth,
                    userQuestion = message
                )

                financialAdvisorUseCase.getFinancialAdvice(context, request, apiKey)
                    .collect { response ->
                        _uiState.update { it.copy(isLoading = false) }

                        if (response.success) {
                            val aiMessage = ChatMessage(
                                content = response.advice,
                                isUser = false
                            )

                            _uiState.update {
                                it.copy(chatMessages = it.chatMessages + aiMessage)
                            }
                        } else {
                            _uiState.update {
                                it.copy(error = response.errorMessage)
                            }
                        }
                    }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to get AI response: ${e.message}"
                    )
                }
            }
        }
    }

    private fun getGeneralFinancialAdvice() {
        if (!_uiState.value.isApiKeyConfigured) {
            viewModelScope.launch {
                _uiInteraction.send(FinancialAdvisorUiInteraction.NavigateToAISettings)
            }
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                error = null
            )
        }

        viewModelScope.launch {
            try {
                val apiKey = aiRepository.apiKey.first()
                val currentState = _uiState.value

                val request = FinancialAdviceRequest(
                    expenses = currentState.expenses,
                    totalExpenses = currentState.totalExpenses,
                    tagExpenseData = currentState.tagExpenseData,
                    budgetData = currentState.budgetData,
                    currencySettings = currentState.currencySettings,
                    currentYear = currentState.currentYear,
                    currentMonth = currentState.currentMonth,
                    userQuestion = ""
                )

                financialAdvisorUseCase.getFinancialAdvice(context, request, apiKey)
                    .collect { response ->
                        _uiState.update { it.copy(isLoading = false) }

                        if (response.success) {
                            val aiMessage = ChatMessage(
                                content = response.advice,
                                isUser = false
                            )

                            _uiState.update {
                                it.copy(chatMessages = it.chatMessages + aiMessage)
                            }
                        } else {
                            _uiState.update {
                                it.copy(error = response.errorMessage)
                            }
                        }
                    }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to get AI response: ${e.message}"
                    )
                }
            }
        }
    }
}