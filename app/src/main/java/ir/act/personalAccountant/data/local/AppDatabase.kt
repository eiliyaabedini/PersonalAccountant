package ir.act.personalAccountant.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ir.act.personalAccountant.data.local.dao.AssetDao
import ir.act.personalAccountant.data.local.dao.AssetSnapshotDao
import ir.act.personalAccountant.data.local.dao.ExpenseDao
import ir.act.personalAccountant.data.local.dao.NetWorthSnapshotDao
import ir.act.personalAccountant.data.local.entity.AssetEntity
import ir.act.personalAccountant.data.local.entity.AssetSnapshotEntity
import ir.act.personalAccountant.data.local.entity.ExpenseEntity
import ir.act.personalAccountant.data.local.entity.NetWorthSnapshotEntity

@Database(
    entities = [ExpenseEntity::class, AssetEntity::class, NetWorthSnapshotEntity::class, AssetSnapshotEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun expenseDao(): ExpenseDao
    abstract fun assetDao(): AssetDao
    abstract fun netWorthSnapshotDao(): NetWorthSnapshotDao
    abstract fun assetSnapshotDao(): AssetSnapshotDao

    companion object {
        const val DATABASE_NAME = "personal_accountant_database"
        
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE expenses ADD COLUMN tag TEXT NOT NULL DEFAULT 'General'")
            }
        }
        
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE expenses ADD COLUMN imagePath TEXT")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE expenses ADD COLUMN destinationAmount REAL")
                database.execSQL("ALTER TABLE expenses ADD COLUMN destinationCurrency TEXT")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create assets table with currency column from the start
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS assets (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        type TEXT NOT NULL,
                        amount REAL NOT NULL,
                        currency TEXT NOT NULL,
                        quantity REAL NOT NULL DEFAULT 1.0,
                        notes TEXT,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """
                )

                // Create net worth snapshots table
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS net_worth_snapshots (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        totalAssets REAL NOT NULL,
                        netWorth REAL NOT NULL,
                        currency TEXT NOT NULL,
                        calculatedAt INTEGER NOT NULL
                    )
                """
                )

                // Create asset snapshots table with currency column and proper foreign keys
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS asset_snapshots (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        assetId INTEGER NOT NULL,
                        amount REAL NOT NULL,
                        quantity REAL NOT NULL,
                        totalValue REAL NOT NULL,
                        currency TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        FOREIGN KEY (assetId) REFERENCES assets(id) ON DELETE CASCADE
                    )
                """
                )

                // Create indexes for asset snapshots
                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_asset_snapshots_assetId 
                    ON asset_snapshots (assetId)
                """
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_asset_snapshots_timestamp 
                    ON asset_snapshots (timestamp)
                """
                )
            }
        }
    }
}