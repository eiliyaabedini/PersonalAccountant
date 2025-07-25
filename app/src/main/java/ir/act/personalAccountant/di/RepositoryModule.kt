package ir.act.personalAccountant.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ir.act.personalAccountant.data.repository.AssetRepositoryImpl
import ir.act.personalAccountant.data.repository.BudgetRepositoryImpl
import ir.act.personalAccountant.data.repository.ExpenseRepositoryImpl
import ir.act.personalAccountant.data.repository.GoogleSheetsRepositoryImpl
import ir.act.personalAccountant.data.repository.SyncStateRepositoryImpl
import ir.act.personalAccountant.data.repository.TripModeRepositoryImpl
import ir.act.personalAccountant.domain.repository.AssetRepository
import ir.act.personalAccountant.domain.repository.BudgetRepository
import ir.act.personalAccountant.domain.repository.ExpenseRepository
import ir.act.personalAccountant.domain.repository.GoogleSheetsRepository
import ir.act.personalAccountant.domain.repository.SyncStateRepository
import ir.act.personalAccountant.domain.repository.TripModeRepository
import ir.act.personalAccountant.domain.usecase.AssetImageAnalysisUseCase
import ir.act.personalAccountant.domain.usecase.AssetImageAnalysisUseCaseImpl
import ir.act.personalAccountant.domain.usecase.AssetSnapshotUseCase
import ir.act.personalAccountant.domain.usecase.AssetSnapshotUseCaseImpl
import ir.act.personalAccountant.domain.usecase.BudgetUseCase
import ir.act.personalAccountant.domain.usecase.BudgetUseCaseImpl
import ir.act.personalAccountant.domain.usecase.DownloadUserExpensesUseCase
import ir.act.personalAccountant.domain.usecase.DownloadUserExpensesUseCaseImpl
import ir.act.personalAccountant.domain.usecase.NotificationUseCase
import ir.act.personalAccountant.domain.usecase.NotificationUseCaseImpl
import ir.act.personalAccountant.domain.usecase.SyncAllExpensesUseCase
import ir.act.personalAccountant.domain.usecase.SyncAllExpensesUseCaseImpl
import ir.act.personalAccountant.domain.usecase.SyncExpenseUseCase
import ir.act.personalAccountant.domain.usecase.SyncExpenseUseCaseImpl
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

    @Binds
    @Singleton
    abstract fun bindAssetRepository(
        assetRepositoryImpl: AssetRepositoryImpl
    ): AssetRepository

    @Binds
    @Singleton
    abstract fun bindAssetSnapshotUseCase(
        assetSnapshotUseCaseImpl: AssetSnapshotUseCaseImpl
    ): AssetSnapshotUseCase

    @Binds
    @Singleton
    abstract fun bindAssetImageAnalysisUseCase(
        assetImageAnalysisUseCaseImpl: AssetImageAnalysisUseCaseImpl
    ): AssetImageAnalysisUseCase

    @Binds
    @Singleton
    abstract fun bindSyncExpenseUseCase(
        syncExpenseUseCaseImpl: SyncExpenseUseCaseImpl
    ): SyncExpenseUseCase

    @Binds
    @Singleton
    abstract fun bindSyncAllExpensesUseCase(
        syncAllExpensesUseCaseImpl: SyncAllExpensesUseCaseImpl
    ): SyncAllExpensesUseCase

    @Binds
    @Singleton
    abstract fun bindDownloadUserExpensesUseCase(
        downloadUserExpensesUseCaseImpl: DownloadUserExpensesUseCaseImpl
    ): DownloadUserExpensesUseCase
}