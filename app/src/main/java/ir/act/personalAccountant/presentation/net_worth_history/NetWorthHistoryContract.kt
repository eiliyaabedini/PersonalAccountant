package ir.act.personalAccountant.presentation.net_worth_history

import ir.act.personalAccountant.domain.model.CurrencySettings
import ir.act.personalAccountant.domain.model.NetWorthSnapshot

data class NetWorthHistoryUiState(
    val snapshots: List<NetWorthSnapshot> = emptyList(),
    val currencySettings: CurrencySettings = CurrencySettings(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTimeRange: TimeRange = TimeRange.ALL
)

enum class TimeRange(val displayName: String) {
    WEEK("Last Week"),
    MONTH("Last Month"),
    QUARTER("Last 3 Months"),
    YEAR("Last Year"),
    ALL("All Time")
}

sealed class NetWorthHistoryEvent {
    data class TimeRangeChanged(val timeRange: TimeRange) : NetWorthHistoryEvent()
    object ClearError : NetWorthHistoryEvent()
}

sealed class NetWorthHistoryUiInteraction {
    object NavigateBack : NetWorthHistoryUiInteraction()
}