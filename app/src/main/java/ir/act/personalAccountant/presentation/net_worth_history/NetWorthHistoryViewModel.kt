package ir.act.personalAccountant.presentation.net_worth_history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.act.personalAccountant.domain.usecase.GetCurrencySettingsUseCase
import ir.act.personalAccountant.domain.usecase.GetNetWorthHistoryUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class NetWorthHistoryViewModel @Inject constructor(
    private val getNetWorthHistoryUseCase: GetNetWorthHistoryUseCase,
    private val getCurrencySettingsUseCase: GetCurrencySettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NetWorthHistoryUiState())
    val uiState: StateFlow<NetWorthHistoryUiState> = _uiState.asStateFlow()

    private val _uiInteraction = Channel<NetWorthHistoryUiInteraction>()
    val uiInteraction = _uiInteraction.receiveAsFlow()

    init {
        loadCurrencySettings()
        loadNetWorthHistory()
    }

    fun onEvent(event: NetWorthHistoryEvent) {
        when (event) {
            is NetWorthHistoryEvent.TimeRangeChanged -> {
                _uiState.value = _uiState.value.copy(selectedTimeRange = event.timeRange)
                loadNetWorthHistory()
            }

            NetWorthHistoryEvent.ClearError -> {
                _uiState.value = _uiState.value.copy(error = null)
            }
        }
    }

    private fun loadCurrencySettings() {
        viewModelScope.launch {
            try {
                getCurrencySettingsUseCase().collect { settings ->
                    _uiState.value = _uiState.value.copy(currencySettings = settings)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private fun loadNetWorthHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val fromDate = getFromDateForTimeRange(_uiState.value.selectedTimeRange)
                getNetWorthHistoryUseCase(fromDate).collect { snapshots ->
                    _uiState.value = _uiState.value.copy(
                        snapshots = snapshots,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    private fun getFromDateForTimeRange(timeRange: TimeRange): Long? {
        val calendar = Calendar.getInstance()
        val currentTime = System.currentTimeMillis()

        return when (timeRange) {
            TimeRange.WEEK -> {
                calendar.timeInMillis = currentTime
                calendar.add(Calendar.WEEK_OF_YEAR, -1)
                calendar.timeInMillis
            }

            TimeRange.MONTH -> {
                calendar.timeInMillis = currentTime
                calendar.add(Calendar.MONTH, -1)
                calendar.timeInMillis
            }

            TimeRange.QUARTER -> {
                calendar.timeInMillis = currentTime
                calendar.add(Calendar.MONTH, -3)
                calendar.timeInMillis
            }

            TimeRange.YEAR -> {
                calendar.timeInMillis = currentTime
                calendar.add(Calendar.YEAR, -1)
                calendar.timeInMillis
            }

            TimeRange.ALL -> null // No filter
        }
    }
}