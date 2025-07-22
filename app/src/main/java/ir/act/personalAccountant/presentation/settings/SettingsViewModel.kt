package ir.act.personalAccountant.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.act.personalAccountant.data.local.NotificationPreferences
import ir.act.personalAccountant.data.notification.NotificationService
import ir.act.personalAccountant.data.worker.DailyReminderScheduler
import ir.act.personalAccountant.domain.usecase.BudgetUseCase
import ir.act.personalAccountant.domain.usecase.GetCurrencySettingsUseCase
import ir.act.personalAccountant.domain.usecase.UpdateCurrencySettingsUseCase
import ir.act.personalAccountant.presentation.settings.SettingsContract.Events
import ir.act.personalAccountant.presentation.settings.SettingsContract.UiState
import ir.act.personalAccountant.util.Constants
import ir.act.personalAccountant.util.NotificationPermissionHelper
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getCurrencySettingsUseCase: GetCurrencySettingsUseCase,
    private val updateCurrencySettingsUseCase: UpdateCurrencySettingsUseCase,
    private val budgetUseCase: BudgetUseCase,
    private val notificationPreferences: NotificationPreferences,
    private val notificationService: NotificationService,
    private val dailyReminderScheduler: DailyReminderScheduler,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _navigationEvents = Channel<NavigationEvent>()
    val navigationEvents = _navigationEvents.receiveAsFlow()

    sealed class NavigationEvent {
        object NavigateToBudgetConfig : NavigationEvent()
        object NavigateToCategorySettings : NavigationEvent()
    }

    sealed class NotificationEvent {
        object PermissionGranted : NotificationEvent()
        object PermissionDenied : NotificationEvent()
        object PermissionPermanentlyDenied : NotificationEvent()
        data class ShowToast(val message: String) : NotificationEvent()
    }

    private val _notificationEvents = Channel<NotificationEvent>()
    val notificationEvents = _notificationEvents.receiveAsFlow()

    init {
        loadSettings()
        checkNotificationPermissionStatus()
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
            
            is Events.BudgetConfigClicked -> {
                viewModelScope.launch {
                    _navigationEvents.send(NavigationEvent.NavigateToBudgetConfig)
                }
            }

            is Events.CategorySettingsClicked -> {
                viewModelScope.launch {
                    _navigationEvents.send(NavigationEvent.NavigateToCategorySettings)
                }
            }

            is Events.NotificationToggleClicked -> {
                handleNotificationToggle(event.enabled)
            }

            is Events.DailyReminderToggleClicked -> {
                handleDailyReminderToggle(event.enabled)
            }
            
            is Events.ClearError -> {
                _uiState.value = _uiState.value.copy(error = null)
            }
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                getCurrencySettingsUseCase(),
                budgetUseCase.getBudgetSettings()
            ) { currencySettings, budgetSettings ->
                Pair(currencySettings, budgetSettings)
            }
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Failed to load settings",
                        isLoading = false
                    )
                }
                .collect { (currencySettings, budgetSettings) ->
                    _uiState.value = _uiState.value.copy(
                        currentCurrencySettings = currencySettings,
                        budgetSettings = budgetSettings,
                        isNotificationEnabled = notificationPreferences.isNotificationEnabled,
                        isDailyReminderEnabled = notificationPreferences.isDailyReminderEnabled,
                        hasNotificationPermission = NotificationPermissionHelper.hasNotificationPermission(
                            context
                        ),
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

    private fun checkNotificationPermissionStatus() {
        val hasPermission = NotificationPermissionHelper.hasNotificationPermission(context)
        val userPreference = notificationPreferences.isNotificationEnabled

        // If user had notifications enabled but permission is now revoked, turn off the preference
        if (userPreference && !hasPermission) {
            notificationPreferences.isNotificationEnabled = false
            notificationService.stopNotificationUpdates()
        }

        _uiState.value = _uiState.value.copy(
            isNotificationEnabled = notificationPreferences.isNotificationEnabled,
            isDailyReminderEnabled = notificationPreferences.isDailyReminderEnabled,
            hasNotificationPermission = hasPermission
        )
    }

    private fun handleNotificationToggle(enabled: Boolean) {
        if (enabled) {
            // User wants to enable notifications
            if (NotificationPermissionHelper.hasNotificationPermission(context)) {
                // Permission already granted, just enable
                enableNotifications()
            } else {
                // Need to request permission
                viewModelScope.launch {
                    _notificationEvents.send(NotificationEvent.PermissionDenied) // This will trigger permission request in UI
                }
            }
        } else {
            // User wants to disable notifications
            disableNotifications()
        }
    }

    private fun enableNotifications() {
        notificationPreferences.isNotificationEnabled = true
        notificationService.startNotificationUpdates()
        _uiState.value = _uiState.value.copy(isNotificationEnabled = true)
    }

    private fun disableNotifications() {
        notificationPreferences.isNotificationEnabled = false
        notificationService.stopNotificationUpdates()

        // Also disable daily reminders if notifications are disabled
        if (notificationPreferences.isDailyReminderEnabled) {
            notificationPreferences.isDailyReminderEnabled = false
            dailyReminderScheduler.cancelDailyReminder()
        }

        _uiState.value = _uiState.value.copy(
            isNotificationEnabled = false,
            isDailyReminderEnabled = false
        )
    }

    fun onPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            enableNotifications()
            viewModelScope.launch {
                _notificationEvents.send(NotificationEvent.PermissionGranted)
            }
        } else {
            // Permission denied, keep switch off
            disableNotifications()
            viewModelScope.launch {
                _notificationEvents.send(NotificationEvent.ShowToast(Constants.Notifications.PERMISSION_DENIED_MESSAGE))
            }
        }
        // Refresh service status based on new permission state
        notificationService.refreshNotificationStatus()
    }

    fun onPermissionPermanentlyDenied() {
        disableNotifications()
        viewModelScope.launch {
            _notificationEvents.send(NotificationEvent.ShowToast(Constants.Notifications.PERMISSION_PERMANENTLY_DENIED_MESSAGE))
        }
        // Refresh service status 
        notificationService.refreshNotificationStatus()
    }

    private fun handleDailyReminderToggle(enabled: Boolean) {
        notificationPreferences.isDailyReminderEnabled = enabled

        if (enabled) {
            dailyReminderScheduler.scheduleDailyReminder()
        } else {
            dailyReminderScheduler.cancelDailyReminder()
        }

        _uiState.value = _uiState.value.copy(isDailyReminderEnabled = enabled)
    }

}