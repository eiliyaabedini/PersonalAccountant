package ir.act.personalAccountant.core.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    
    fun getCurrentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1
    
    fun getCurrentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)
    
    fun getStartOfMonth(year: Int, month: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1) // Calendar.MONTH is 0-based
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    fun getEndOfMonth(year: Int, month: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1) // Calendar.MONTH is 0-based
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
    
    fun getMonthFromTimestamp(timestamp: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.MONTH) + 1 // Convert to 1-based
    }
    
    fun getYearFromTimestamp(timestamp: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.YEAR)
    }
    
    fun getNextMonth(year: Int, month: Int): Pair<Int, Int> {
        return if (month == 12) {
            Pair(year + 1, 1)
        } else {
            Pair(year, month + 1)
        }
    }
    
    fun getPreviousMonth(year: Int, month: Int): Pair<Int, Int> {
        return if (month == 1) {
            Pair(year - 1, 12)
        } else {
            Pair(year, month - 1)
        }
    }
    
    fun formatMonthYear(year: Int, month: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1) // Calendar.MONTH is 0-based
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        
        val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return formatter.format(calendar.time)
    }
    
    fun formatMonthShort(year: Int, month: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1) // Calendar.MONTH is 0-based
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        
        val formatter = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        return formatter.format(calendar.time)
    }
}