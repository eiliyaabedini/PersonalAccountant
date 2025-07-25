package ir.act.personalAccountant.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ir.act.personalAccountant.data.local.AppDatabase
import ir.act.personalAccountant.data.local.dao.AssetDao
import ir.act.personalAccountant.data.local.dao.AssetSnapshotDao
import ir.act.personalAccountant.data.local.dao.ExpenseDao
import ir.act.personalAccountant.data.local.dao.NetWorthSnapshotDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5,
            )
            .build()
    }

    @Provides
    fun provideExpenseDao(database: AppDatabase): ExpenseDao {
        return database.expenseDao()
    }

    @Provides
    fun provideAssetDao(database: AppDatabase): AssetDao {
        return database.assetDao()
    }

    @Provides
    fun provideNetWorthSnapshotDao(database: AppDatabase): NetWorthSnapshotDao {
        return database.netWorthSnapshotDao()
    }

    @Provides
    fun provideAssetSnapshotDao(database: AppDatabase): AssetSnapshotDao {
        return database.assetSnapshotDao()
    }
}