package ir.act.personalAccountant.ai.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssetReader @Inject constructor() {

    suspend fun loadFinancialPlanningKnowledge(context: Context): String =
        withContext(Dispatchers.IO) {
            val stringBuilder = StringBuilder()

            try {
                // Load all 15 financial planning files - FULL CONTENT, NO OPTIMIZATION
                for (i in 1..15) {
                    val fileName = "FinancialPlanning$i.txt"
                    try {
                        val content =
                            context.assets.open(fileName).bufferedReader().use { it.readText() }
                        stringBuilder.append("=== Financial Planning Lesson $i ===\n")
                        stringBuilder.append(content)
                        stringBuilder.append("\n\n")
                    } catch (e: IOException) {
                        // Skip missing files
                        continue
                    }
                }
            } catch (e: Exception) {
                return@withContext ""
            }

            return@withContext stringBuilder.toString()
        }
}