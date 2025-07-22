package ir.act.personalAccountant.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ir.act.personalAccountant.data.repository.BudgetRepositoryImpl
import ir.act.personalAccountant.data.repository.ExpenseRepositoryImpl
import ir.act.personalAccountant.data.repository.GoogleSheetsRepositoryImpl
import ir.act.personalAccountant.data.repository.SyncStateRepositoryImpl
import ir.act.personalAccountant.data.repository.TripModeRepositoryImpl
import ir.act.personalAccountant.domain.repository.BudgetRepository
import ir.act.personalAccountant.domain.repository.ExpenseRepository
import ir.act.personalAccountant.domain.repository.GoogleSheetsRepository
import ir.act.personalAccountant.domain.repository.SyncStateRepository
import ir.act.personalAccountant.domain.repository.TripModeRepository
import ir.act.personalAccountant.domain.usecase.BudgetUseCase
import ir.act.personalAccountant.domain.usecase.BudgetUseCaseImpl
import ir.act.personalAccountant.domain.usecase.NotificationUseCase
import ir.act.personalAccountant.domain.usecase.NotificationUseCaseImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(
        expenseRepositoryImpl: ExpenseRepositoryImpl
    ): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(
        budgetRepositoryImpl: BudgetRepositoryImpl
    ): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindBudgetUseCase(
        budgetUseCaseImpl: BudgetUseCaseImpl
    ): BudgetUseCase

    @Binds
    @Singleton
    abstract fun bindGoogleSheetsRepository(
        googleSheetsRepositoryImpl: GoogleSheetsRepositoryImpl
    ): GoogleSheetsRepository

    @Binds
    @Singleton
    abstract fun bindSyncStateRepository(
        syncStateRepositoryImpl: SyncStateRepositoryImpl
    ): SyncStateRepository

    @Binds
    @Singleton
    abstract fun bindTripModeRepository(
        tripModeRepositoryImpl: TripModeRepositoryImpl
    ): TripModeRepository

    @Binds
    @Singleton
    abstract fun bindNotificationUseCase(
        notificationUseCaseImpl: NotificationUseCaseImpl
    ): NotificationUseCase
}