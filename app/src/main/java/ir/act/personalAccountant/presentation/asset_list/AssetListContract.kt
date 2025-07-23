package ir.act.personalAccountant.presentation.asset_list

import ir.act.personalAccountant.domain.model.Asset
import ir.act.personalAccountant.domain.model.CurrencySettings

data class AssetListUiState(
    val assets: List<Asset> = emptyList(),
    val totalAssets: Double = 0.0,
    val assetTypes: List<String> = emptyList(),
    val selectedAssetType: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDeleteConfirmation: Boolean = false,
    val assetToDelete: Asset? = null,
    val currencySettings: CurrencySettings = CurrencySettings()
)

sealed class AssetListEvent {
    object AddAssetClicked : AssetListEvent()
    object ClearError : AssetListEvent()
    data class EditAssetClicked(val asset: Asset) : AssetListEvent()
    data class DeleteAssetClicked(val asset: Asset) : AssetListEvent()
    object ConfirmDeleteAsset : AssetListEvent()
    object CancelDeleteAsset : AssetListEvent()
    data class FilterByTypeClicked(val type: String?) : AssetListEvent()
    object RefreshData : AssetListEvent()
}

sealed class AssetListUiInteraction {
    object NavigateToAssetEntry : AssetListUiInteraction()
    data class NavigateToAssetEdit(val assetId: Long) : AssetListUiInteraction()
}