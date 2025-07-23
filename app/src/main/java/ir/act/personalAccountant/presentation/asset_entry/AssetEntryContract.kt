package ir.act.personalAccountant.presentation.asset_entry

import ir.act.personalAccountant.domain.model.CurrencySettings

data class AssetEntryUiState(
    val assetName: String = "",
    val assetType: String = "",
    val currentAmount: String = "",
    val selectedCurrency: String = "",
    val quantity: String = "1",
    val notes: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val availableAssetTypes: List<String> = emptyList(),
    val showAddTypeDialog: Boolean = false,
    val newTypeName: String = "",
    val currencySettings: CurrencySettings = CurrencySettings(),
    val isEditMode: Boolean = false,
    val assetId: Long? = null,
    val isAnalyzingImage: Boolean = false,
    val aiAnalysisMessage: String? = null
)

sealed class AssetEntryEvent {
    data class AssetNameChanged(val name: String) : AssetEntryEvent()
    data class AssetTypeChanged(val type: String) : AssetEntryEvent()
    data class AmountChanged(val amount: String) : AssetEntryEvent()
    data class NumberClicked(val number: String) : AssetEntryEvent()
    object DecimalClicked : AssetEntryEvent()
    object BackspaceClicked : AssetEntryEvent()
    data class QuantityChanged(val quantity: String) : AssetEntryEvent()
    data class NotesChanged(val notes: String) : AssetEntryEvent()
    object AddTypeClicked : AssetEntryEvent()
    data class NewTypeNameChanged(val name: String) : AssetEntryEvent()
    object ConfirmNewType : AssetEntryEvent()
    object DismissAddTypeDialog : AssetEntryEvent()
    object AnalyzeImageClicked : AssetEntryEvent()
    data class ImageSelected(val imageUri: android.net.Uri) : AssetEntryEvent()
    object SaveAsset : AssetEntryEvent()
    object ClearError : AssetEntryEvent()
    object ClearAiAnalysisMessage : AssetEntryEvent()
}

sealed class AssetEntryUiInteraction {
    data class NavigateBack(val newAssetId: Long? = null) : AssetEntryUiInteraction()
    object OpenImagePicker : AssetEntryUiInteraction()
}