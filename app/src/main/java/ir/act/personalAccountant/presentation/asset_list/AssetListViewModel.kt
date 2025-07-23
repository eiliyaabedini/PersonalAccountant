package ir.act.personalAccountant.presentation.asset_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.act.personalAccountant.domain.usecase.GetCurrencySettingsUseCase
import ir.act.personalAccountant.domain.usecase.ManageAssetsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssetListViewModel @Inject constructor(
    private val manageAssetsUseCase: ManageAssetsUseCase,
    private val getCurrencySettingsUseCase: GetCurrencySettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssetListUiState())
    val uiState: StateFlow<AssetListUiState> = _uiState.asStateFlow()

    private val _uiInteraction = Channel<AssetListUiInteraction>()
    val uiInteraction = _uiInteraction.receiveAsFlow()

    init {
        loadAssets()
        loadCurrencySettings()
    }

    fun onEvent(event: AssetListEvent) {
        when (event) {
            AssetListEvent.AddAssetClicked -> {
                viewModelScope.launch {
                    _uiInteraction.send(AssetListUiInteraction.NavigateToAssetEntry)
                }
            }

            AssetListEvent.ClearError -> {
                _uiState.value = _uiState.value.copy(error = null)
            }

            is AssetListEvent.EditAssetClicked -> {
                viewModelScope.launch {
                    _uiInteraction.send(AssetListUiInteraction.NavigateToAssetEdit(event.asset.id))
                }
            }

            is AssetListEvent.DeleteAssetClicked -> {
                _uiState.value = _uiState.value.copy(
                    showDeleteConfirmation = true,
                    assetToDelete = event.asset
                )
            }

            AssetListEvent.ConfirmDeleteAsset -> {
                _uiState.value.assetToDelete?.let { asset ->
                    viewModelScope.launch {
                        try {
                            manageAssetsUseCase.deleteAsset(asset.id)
                            _uiState.value = _uiState.value.copy(
                                showDeleteConfirmation = false,
                                assetToDelete = null
                            )
                        } catch (e: Exception) {
                            _uiState.value = _uiState.value.copy(
                                showDeleteConfirmation = false,
                                assetToDelete = null,
                                error = e.message ?: "Failed to delete asset"
                            )
                        }
                    }
                }
            }

            AssetListEvent.CancelDeleteAsset -> {
                _uiState.value = _uiState.value.copy(
                    showDeleteConfirmation = false,
                    assetToDelete = null
                )
            }

            is AssetListEvent.FilterByTypeClicked -> {
                _uiState.value = _uiState.value.copy(selectedAssetType = event.type)
                loadAssets()
            }

            AssetListEvent.RefreshData -> {
                loadAssets()
            }
        }
    }

    private fun loadAssets() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val selectedType = _uiState.value.selectedAssetType
                val assetsFlow = if (selectedType != null) {
                    manageAssetsUseCase.getAssetsByType(selectedType)
                } else {
                    manageAssetsUseCase.getAllAssets()
                }

                combine(
                    assetsFlow,
                    manageAssetsUseCase.getTotalAssets(),
                    manageAssetsUseCase.getAllAssetTypes()
                ) { assets, totalAssets, assetTypes ->
                    _uiState.value = _uiState.value.copy(
                        assets = assets,
                        totalAssets = totalAssets,
                        assetTypes = assetTypes,
                        isLoading = false,
                        error = null
                    )
                }.collect { }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred while loading assets"
                )
            }
        }
    }

    private fun loadCurrencySettings() {
        viewModelScope.launch {
            getCurrencySettingsUseCase().collect { currencySettings ->
                _uiState.value = _uiState.value.copy(currencySettings = currencySettings)
            }
        }
    }
}