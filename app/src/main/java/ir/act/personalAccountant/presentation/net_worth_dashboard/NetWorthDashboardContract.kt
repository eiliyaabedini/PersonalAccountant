package ir.act.personalAccountant.presentation.net_worth_dashboard

import ir.act.personalAccountant.domain.model.AssetSnapshot
import ir.act.personalAccountant.domain.model.CurrencySettings
import ir.act.personalAccountant.domain.usecase.DailyNetWorth

enum class DashboardTimeRange(val displayName: String) {
    MONTH("Month"),
    QUARTER("Quarter"),
    YEAR("Year"),
    ALL("All")
}

data class NetWorthDashboardUiState(
    val assetSnapshots: List<AssetSnapshot> = emptyList(),
    val currentNetWorth: Double = 0.0,
    val netWorthHistory: List<DailyNetWorth> = emptyList(),
    val selectedTimeRange: DashboardTimeRange = DashboardTimeRange.ALL,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDeleteConfirmation: Boolean = false,
    val assetToDelete: AssetSnapshot? = null,
    val currencySettings: CurrencySettings = CurrencySettings(),
    val editingAssetId: Long? = null,
    val editingAmount: String = "",
    val editingQuantity: String = ""
)

sealed class NetWorthDashboardEvent {
    object AddAssetClicked : NetWorthDashboardEvent()
    object GraphClicked : NetWorthDashboardEvent()
    data class TimeRangeChanged(val timeRange: DashboardTimeRange) : NetWorthDashboardEvent()
    object ClearError : NetWorthDashboardEvent()
    data class StartEditingAsset(val assetSnapshot: AssetSnapshot) : NetWorthDashboardEvent()
    data class AmountChanged(val amount: String) : NetWorthDashboardEvent()
    data class QuantityChanged(val quantity: String) : NetWorthDashboardEvent()
    object SaveAssetSnapshot : NetWorthDashboardEvent()
    object CancelEditing : NetWorthDashboardEvent()
    data class EditAssetName(val assetSnapshot: AssetSnapshot) : NetWorthDashboardEvent()
    data class DeleteAssetClicked(val assetSnapshot: AssetSnapshot) : NetWorthDashboardEvent()
    object ConfirmDeleteAsset : NetWorthDashboardEvent()
    object CancelDeleteAsset : NetWorthDashboardEvent()
    object RefreshData : NetWorthDashboardEvent()
}

sealed class NetWorthDashboardUiInteraction {
    object NavigateToAssetEntry : NetWorthDashboardUiInteraction()
    object NavigateToNetWorthHistory : NetWorthDashboardUiInteraction()
    data class NavigateToAssetEdit(val assetId: Long) : NetWorthDashboardUiInteraction()
}