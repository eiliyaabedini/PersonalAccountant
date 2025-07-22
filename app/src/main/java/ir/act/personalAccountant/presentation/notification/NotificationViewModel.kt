package ir.act.personalAccountant.presentation.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.act.personalAccountant.data.notification.BudgetNotificationManager
import ir.act.personalAccountant.domain.usecase.NotificationUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationUseCase: NotificationUseCase,
    private val notificationManager: BudgetNotificationManager
) : ViewModel() {

    fun startNotificationUpdates() {
        notificationUseCase.getNotificationData()
            .onEach { data ->
                notificationManager.showBudgetNotification(data)
            }
            .launchIn(viewModelScope)
    }

    fun stopNotificationUpdates() {
        notificationManager.clearNotification()
    }
}