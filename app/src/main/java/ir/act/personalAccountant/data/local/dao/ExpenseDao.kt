package ir.act.personalAccountant.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ir.act.personalAccountant.data.local.entity.ExpenseEntity
import ir.act.personalAccountant.data.local.model.TagWithCount
import ir.act.personalAccountant.data.local.model.TagStatistics
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    
    @Insert
    suspend fun insertExpense(expense: ExpenseEntity): Long
    
    @Update
    suspend fun updateExpense(expense: ExpenseEntity)
    
    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)
    
    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): ExpenseEntity?
    
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>
    
    @Query("SELECT SUM(amount) FROM expenses")
    fun getTotalExpenses(): Flow<Double?>
    
    @Query("""
        SELECT tag, COUNT(*) as count 
        FROM expenses 
        GROUP BY tag 
        ORDER BY count DESC
    """)
    fun getAllTagsWithCount(): Flow<List<TagWithCount>>
    
    @Query("SELECT DISTINCT tag FROM expenses ORDER BY tag")
    fun getAllTags(): Flow<List<String>>
    
    @Query("""
        SELECT tag, 
               COUNT(*) as usageCount,
               MAX(timestamp) as lastUsed
        FROM expenses 
        GROUP BY tag 
        ORDER BY usageCount DESC, lastUsed DESC
    """)
    fun getTagStatistics(): Flow<List<TagStatistics>>
    
    @Query("SELECT * FROM expenses WHERE timestamp >= :startOfMonth AND timestamp <= :endOfMonth ORDER BY timestamp DESC")
    fun getExpensesByMonth(startOfMonth: Long, endOfMonth: Long): Flow<List<ExpenseEntity>>
    
    @Query("SELECT SUM(amount) FROM expenses WHERE timestamp >= :startOfMonth AND timestamp <= :endOfMonth")
    fun getTotalExpensesByMonth(startOfMonth: Long, endOfMonth: Long): Flow<Double?>
    
    @Query("SELECT * FROM expenses WHERE tag = :tag AND timestamp >= :startOfMonth AND timestamp <= :endOfMonth ORDER BY timestamp DESC")
    fun getExpensesByTagAndMonth(tag: String, startOfMonth: Long, endOfMonth: Long): Flow<List<ExpenseEntity>>
}