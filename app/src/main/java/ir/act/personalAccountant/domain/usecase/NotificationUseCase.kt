package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.domain.model.NotificationData
import kotlinx.coroutines.flow.Flow

interface NotificationUseCase {
    fun getNotificationData(): Flow<NotificationData>
}