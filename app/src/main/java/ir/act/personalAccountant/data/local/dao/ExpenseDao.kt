package ir.act.personalAccountant.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ir.act.personalAccountant.data.local.entity.ExpenseEntity
import ir.act.personalAccountant.data.local.model.TagWithCount
import ir.act.personalAccountant.data.local.model.TagStatistics
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    
    @Insert
    suspend fun insertExpense(expense: ExpenseEntity)
    
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
}