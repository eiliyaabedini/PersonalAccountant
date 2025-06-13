package ir.act.personalAccountant.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.act.personalAccountant.domain.usecase.GetCurrencySettingsUseCase
import ir.act.personalAccountant.domain.usecase.UpdateCurrencySettingsUseCase
import ir.act.personalAccountant.presentation.settings.SettingsContract.Events
import ir.act.personalAccountant.presentation.settings.SettingsContract.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getCurrencySettingsUseCase: GetCurrencySettingsUseCase,
    private val updateCurrencySettingsUseCase: UpdateCurrencySettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadCurrencySettings()
    }

    fun onEvent(event: Events) {
        when (event) {
            is Events.CurrencyPickerClicked -> {
                _uiState.value = _uiState.value.copy(showCurrencyPicker = true)
            }
            
            is Events.DismissCurrencyPicker -> {
                _uiState.value = _uiState.value.copy(showCurrencyPicker = false)
            }
            
            is Events.CurrencySelected -> {
                updateCurrency(event.currencySettings)
            }
            
            is Events.ClearError -> {
                _uiState.value = _uiState.value.copy(error = null)
            }
        }
    }

    private fun loadCurrencySettings() {
        viewModelScope.launch {
            getCurrencySettingsUseCase()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Failed to load settings",
                        isLoading = false
                    )
                }
                .collect { currencySettings ->
                    _uiState.value = _uiState.value.copy(
                        currentCurrencySettings = currencySettings,
                        isLoading = false
                    )
                }
        }
    }

    private fun updateCurrency(currencySettings: ir.act.personalAccountant.domain.model.CurrencySettings) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, showCurrencyPicker = false)
            
            try {
                updateCurrencySettingsUseCase(currencySettings)
                _uiState.value = _uiState.value.copy(
                    currentCurrencySettings = currencySettings,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update currency",
                    isLoading = false
                )
            }
        }
    }
}