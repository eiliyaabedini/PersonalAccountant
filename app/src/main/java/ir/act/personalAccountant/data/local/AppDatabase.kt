package ir.act.personalAccountant.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ir.act.personalAccountant.data.local.dao.ExpenseDao
import ir.act.personalAccountant.data.local.entity.ExpenseEntity

@Database(
    entities = [ExpenseEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun expenseDao(): ExpenseDao
    
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
    }
}