package ir.act.personalAccountant.presentation.net_worth_dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.act.personalAccountant.domain.usecase.AssetSnapshotUseCase
import ir.act.personalAccountant.domain.usecase.GetCurrencySettingsUseCase
import ir.act.personalAccountant.domain.usecase.ManageAssetsUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class NetWorthDashboardViewModel @Inject constructor(
    private val manageAssetsUseCase: ManageAssetsUseCase,
    private val assetSnapshotUseCase: AssetSnapshotUseCase,
    private val getCurrencySettingsUseCase: GetCurrencySettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NetWorthDashboardUiState())
    val uiState: StateFlow<NetWorthDashboardUiState> = _uiState.asStateFlow()

    private val _uiInteraction = Channel<NetWorthDashboardUiInteraction>()
    val uiInteraction = _uiInteraction.receiveAsFlow()

    init {
        loadAssetSnapshots()
        loadCurrencySettings()
        loadNetWorthHistory()
    }

    fun onEvent(event: NetWorthDashboardEvent) {
        when (event) {
            NetWorthDashboardEvent.AddAssetClicked -> {
                viewModelScope.launch {
                    _uiInteraction.send(NetWorthDashboardUiInteraction.NavigateToAssetEntry)
                }
            }

            NetWorthDashboardEvent.GraphClicked -> {
                viewModelScope.launch {
                    _uiInteraction.send(NetWorthDashboardUiInteraction.NavigateToNetWorthHistory)
                }
            }

            is NetWorthDashboardEvent.TimeRangeChanged -> {
                _uiState.value = _uiState.value.copy(selectedTimeRange = event.timeRange)
                loadNetWorthHistory()
            }

            is NetWorthDashboardEvent.StartEditingAsset -> {
                _uiState.value = _uiState.value.copy(
                    editingAssetId = event.assetSnapshot.assetId,
                    editingAmount = event.assetSnapshot.amount.toString(),
                    editingQuantity = event.assetSnapshot.quantity.toString()
                )
            }

            is NetWorthDashboardEvent.AmountChanged -> {
                _uiState.value = _uiState.value.copy(editingAmount = event.amount)
            }

            is NetWorthDashboardEvent.QuantityChanged -> {
                _uiState.value = _uiState.value.copy(editingQuantity = event.quantity)
            }

            NetWorthDashboardEvent.SaveAssetSnapshot -> {
                saveAssetSnapshot()
            }

            NetWorthDashboardEvent.CancelEditing -> {
                _uiState.value = _uiState.value.copy(
                    editingAssetId = null,
                    editingAmount = "",
                    editingQuantity = ""
                )
            }

            is NetWorthDashboardEvent.EditAssetName -> {
                viewModelScope.launch {
                    _uiInteraction.send(NetWorthDashboardUiInteraction.NavigateToAssetEdit(event.assetSnapshot.assetId))
                }
            }

            is NetWorthDashboardEvent.DeleteAssetClicked -> {
                _uiState.value = _uiState.value.copy(
                    showDeleteConfirmation = true,
                    assetToDelete = event.assetSnapshot
                )
            }

            NetWorthDashboardEvent.ConfirmDeleteAsset -> {
                _uiState.value.assetToDelete?.let { assetSnapshot ->
                    viewModelScope.launch {
                        try {
                            manageAssetsUseCase.deleteAsset(assetSnapshot.assetId)
                            _uiState.value = _uiState.value.copy(
                                showDeleteConfirmation = false,
                                assetToDelete = null
                            )
                            loadAssetSnapshots()
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

            NetWorthDashboardEvent.CancelDeleteAsset -> {
                _uiState.value = _uiState.value.copy(
                    showDeleteConfirmation = false,
                    assetToDelete = null
                )
            }

            NetWorthDashboardEvent.ClearError -> {
                _uiState.value = _uiState.value.copy(error = null)
            }

            NetWorthDashboardEvent.RefreshData -> {
                loadAssetSnapshots()
                loadNetWorthHistory()
            }
        }
    }

    private fun loadAssetSnapshots() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // First, create missing snapshots for any assets that don't have them
                assetSnapshotUseCase.createMissingAssetSnapshots()

                val assetSnapshots = assetSnapshotUseCase.getLatestAssetSnapshots()
                val currentNetWorth = assetSnapshotUseCase.getCurrentNetWorth()

                _uiState.value = _uiState.value.copy(
                    assetSnapshots = assetSnapshots,
                    currentNetWorth = currentNetWorth,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred while loading data"
                )
            }
        }
    }

    private fun saveAssetSnapshot() {
        val currentState = _uiState.value
        val editingAssetId = currentState.editingAssetId ?: return

        viewModelScope.launch {
            try {
                val amount = currentState.editingAmount.toDoubleOrNull() ?: return@launch
                val quantity = currentState.editingQuantity.toDoubleOrNull() ?: return@launch
                val currency = currentState.currencySettings.currencyCode

                assetSnapshotUseCase.createAssetSnapshot(editingAssetId, amount, quantity, currency)

                _uiState.value = _uiState.value.copy(
                    editingAssetId = null,
                    editingAmount = "",
                    editingQuantity = ""
                )

                loadAssetSnapshots()
                loadNetWorthHistory()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to save asset snapshot"
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

    private fun loadNetWorthHistory() {
        viewModelScope.launch {
            try {
                val fromDate = getFromDateForTimeRange(_uiState.value.selectedTimeRange)
                val dailyHistory = assetSnapshotUseCase.getDailyNetWorthHistory(fromDate)
                _uiState.value = _uiState.value.copy(netWorthHistory = dailyHistory)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private fun getFromDateForTimeRange(timeRange: DashboardTimeRange): Long? {
        val calendar = Calendar.getInstance()
        val currentTime = System.currentTimeMillis()

        return when (timeRange) {
            DashboardTimeRange.MONTH -> {
                calendar.timeInMillis = currentTime
                calendar.add(Calendar.MONTH, -1)
                calendar.timeInMillis
            }

            DashboardTimeRange.QUARTER -> {
                calendar.timeInMillis = currentTime
                calendar.add(Calendar.MONTH, -3)
                calendar.timeInMillis
            }

            DashboardTimeRange.YEAR -> {
                calendar.timeInMillis = currentTime
                calendar.add(Calendar.YEAR, -1)
                calendar.timeInMillis
            }

            DashboardTimeRange.ALL -> null // No filter
        }
    }
}