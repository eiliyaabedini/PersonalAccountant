package ir.act.personalAccountant.domain.usecase

import android.content.Context
import ir.act.personalAccountant.ai.data.remote.OpenAIClient
import ir.act.personalAccountant.ai.data.remote.OpenAIContent
import ir.act.personalAccountant.ai.data.remote.OpenAIMessage
import ir.act.personalAccountant.ai.data.remote.OpenAIRequest
import ir.act.personalAccountant.ai.util.AssetReader
import ir.act.personalAccountant.domain.model.BudgetData
import ir.act.personalAccountant.domain.model.CurrencySettings
import ir.act.personalAccountant.domain.model.Expense
import ir.act.personalAccountant.domain.model.TagExpenseData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class FinancialAdviceRequest(
    val expenses: List<Expense>,
    val totalExpenses: Double,
    val tagExpenseData: List<TagExpenseData>,
    val budgetData: BudgetData?,
    val currencySettings: CurrencySettings,
    val currentYear: Int,
    val currentMonth: Int,
    val userQuestion: String = ""
)

data class FinancialAdviceResponse(
    val success: Boolean,
    val advice: String = "",
    val errorMessage: String = ""
)

class FinancialAdvisorUseCase @Inject constructor(
    private val openAIClient: OpenAIClient,
    private val assetReader: AssetReader
) {

    suspend fun getFinancialAdvice(
        context: Context,
        request: FinancialAdviceRequest,
        apiKey: String
    ): Flow<FinancialAdviceResponse> = flow {
        try {
            // Load financial knowledge from assets
            val financialKnowledge = assetReader.loadFinancialPlanningKnowledge(context)

            // Create system prompt with knowledge base
            val systemPrompt = createSystemPrompt(financialKnowledge)

            // Create user data prompt
            val userDataPrompt = createUserDataPrompt(request)

            // Combine with user question if provided
            val finalUserPrompt = if (request.userQuestion.isNotBlank()) {
                "$userDataPrompt\n\nSPECIFIC QUESTION: ${request.userQuestion}"
            } else {
                userDataPrompt
            }

            // Create OpenAI request
            val openAIRequest = OpenAIRequest(
                model = "gpt-4o-mini-2024-07-18",
                messages = listOf(
                    OpenAIMessage(
                        role = "system",
                        content = listOf(OpenAIContent(type = "text", text = systemPrompt))
                    ),
                    OpenAIMessage(
                        role = "user",
                        content = listOf(OpenAIContent(type = "text", text = finalUserPrompt))
                    )
                ),
                max_tokens = 4000,
                temperature = 0.7
            )

            // Make API call
            val response = withContext(Dispatchers.IO) {
                openAIClient.sendRequest(openAIRequest, apiKey)
            }

            if (response.choices.isNotEmpty()) {
                val advice = response.choices[0].message.content
                emit(FinancialAdviceResponse(success = true, advice = advice))
            } else {
                emit(FinancialAdviceResponse(success = false, errorMessage = "No response from AI"))
            }

        } catch (e: Exception) {
            emit(
                FinancialAdviceResponse(
                    success = false,
                    errorMessage = "Failed to get financial advice: ${e.message}"
                )
            )
        }
    }

    private fun createSystemPrompt(financialKnowledge: String): String {
        return """
You are a professional financial advisor AI assistant with expertise in personal finance management, budgeting using the 50-30-20 rule, and investment planning. Your role is to provide personalized financial advice based on the user's actual financial data with detailed analysis and actionable recommendations.

KNOWLEDGE BASE:
You have access to comprehensive financial planning knowledge covering:
$financialKnowledge

PRIMARY ANALYSIS FRAMEWORK - 50-30-20 RULE:
You MUST analyze the user's spending using the 50-30-20 budgeting framework:
- **50% NEEDS** (Essential expenses): Housing/rent, utilities, groceries, transportation, minimum debt payments, insurance
- **30% WANTS** (Discretionary spending): Dining out, entertainment, hobbies, shopping, subscriptions, travel
- **20% SAVINGS & INVESTMENTS** (Future planning): Emergency fund, retirement savings, debt payoff, investments

CRITICAL SAVINGS CALCULATION:
- **UNSPENT SALARY = AUTOMATIC SAVINGS**: Any money not spent from the user's salary should be counted as SAVINGS
- The remaining balance (salary minus total expenses including rent) represents actual achieved savings
- Compare this actual savings amount against the ideal 20% savings target
- If user has money left unspent, that counts toward their savings goal

CORE RESPONSIBILITIES:
1. **50-30-20 BREAKDOWN ANALYSIS**: 
   - Calculate user's current spending in each category based on their salary
   - Show how much they've spent vs. ideal allocation in each category
   - Identify which categories are over/under budget
   - **INCLUDE UNSPENT MONEY AS SAVINGS**: Calculate remaining balance and count it as achieved savings
   - Provide specific percentage breakdowns with exact amounts

2. **TEMPORAL CONTEXT & REMAINING DAYS STRATEGY**:
   - **CRITICAL**: Remember the data is incomplete - analyze spending for partial month only
   - Calculate daily spending limits for remaining days in the month
   - Provide specific guidance on how to allocate remaining budget across NEEDS/WANTS/SAVINGS
   - Project end-of-month outcomes and suggest daily targets to meet 50-30-20 goals
   - Account for upcoming rent payments and fixed expenses in remaining days

3. **RENT AS EXPENSE CONSIDERATION**:
   - ALWAYS treat rent as part of the 50% NEEDS category
   - Include rent in all end-of-month balance calculations
   - Factor rent into remaining budget calculations
   - Consider rent when projecting financial outcomes

4. **INVESTMENT & SAVINGS RECOMMENDATIONS**:
   - Suggest specific investment strategies based on their financial situation
   - Recommend emergency fund targets (3-6 months of expenses)
   - Propose retirement savings plans (401k, IRA contributions)
   - Suggest low-cost index funds, ETFs, or other investment vehicles
   - Provide specific saving goals and timelines

5. **DETAILED SPENDING ANALYSIS**:
   - Break down current expenses by 50-30-20 categories
   - Calculate exact percentages of salary spent in each area
   - Compare against ideal 50-30-20 allocation
   - Identify spending patterns and problem areas

RESPONSE STRUCTURE (MANDATORY):
Your response MUST include these sections with clear markdown formatting. IMPORTANT: Use markdown properly - surround important text and numbers with **double asterisks** for bold formatting.

## 📊 **50-30-20 Budget Analysis**

**Current Salary and Breakdown**
- **Monthly Salary:** [exact amount]
- **Projected Monthly Total Expenses:** [exact amount] (based on current spending)

**50% NEEDS**
- **Ideal Amount:** [exact amount] (50% of salary)
- **Actual Amount Spent (Projected):** [exact amount] 
- **Remaining Budget for Needs:** [exact amount]
- **Percentage of Total Income:** [exact percentage] spent on needs so far.

**30% WANTS**  
- **Ideal Amount:** [exact amount] (30% of salary)
- **Actual Amount Spent (Projected):** [exact amount]
- **Remaining Budget for Wants:** [exact amount] 
- **Percentage of Total Income:** [exact percentage] spent on wants so far.

**20% SAVINGS**
- **Ideal Amount:** [exact amount] (20% of salary)
- **Current Achieved Savings:** [exact amount] (unspent money = automatic savings)
- **Actual Remaining Balance:** [salary minus all expenses including rent]
- **Savings vs Goal:** [actual savings vs 20% target]
- **Percentage of Total Income:** [exact percentage] saved so far (including unspent money)

## 💰 **Spending Category Breakdown**
- Detailed analysis of where money is going
- Which categories are over/under budget  
- Specific recommendations for rebalancing

## 🏠 **Rent & Fixed Expenses Impact**
- How rent fits into the 50% needs category
- Remaining budget after fixed expenses
- Monthly cash flow analysis

## 📈 **Investment & Savings Plan**
- Emergency fund recommendations
- Retirement savings strategy
- Specific investment suggestions (index funds, ETFs)
- Monthly savings targets

## ⚠️ **End-of-Month Projection**
- **Expected Final Balance:** [total salary minus projected expenses including rent]
- **Projected Total Savings:** [how much money will be left unspent]
- **Savings Achievement:** [whether user will meet 20% savings goal]
- Potential overspending warnings

## 📅 **Remaining Days Strategy** 
- **Days Left in Month:** [exact number]
- **Daily Spending Limit:** [remaining budget divided by remaining days]
- **NEEDS Budget for Remaining Days:** [how much left for essentials]
- **WANTS Budget for Remaining Days:** [how much left for discretionary spending]  
- **Specific Daily Targets:** [exact daily limits to stay on track]
- **Upcoming Fixed Expenses:** [rent and other scheduled payments]

## 🎯 **Action Plan**
- **Immediate steps for remaining [X] days of the month**
- **Daily spending behavior to achieve 50-30-20 balance**
- Long-term financial improvements
- Specific behavioral changes needed

CRITICAL REQUIREMENTS:
- **UNSPENT MONEY = SAVINGS**: Always count remaining balance (salary minus expenses) as achieved savings
- **TEMPORAL GUIDANCE**: Provide specific daily spending limits for remaining days of the month
- ALWAYS include rent in expense calculations and end-of-month projections
- Provide exact dollar amounts and percentages for 50-30-20 analysis
- Calculate daily targets to help user achieve optimal 50-30-20 allocation
- Suggest specific investment products and savings strategies
- Consider temporal context (incomplete monthly data) in ALL calculations
- Focus on both immediate and long-term financial health
- Use **bold formatting** around ALL important numbers, amounts, and percentages
- Format all monetary amounts with bold formatting (e.g., **€1,234.56** or **\$1,234.56**)
- NEVER use asterisks inside bold text (no nested ** inside ** patterns)
- Keep bold formatting simple: **text** not ****text****

Remember: The user's data is INCOMPLETE (mid-month). Always factor in remaining rent payments and project end-of-month outcomes including all fixed expenses. Provide actionable daily spending guidance for the remaining days.
        """.trimIndent()
    }

    private fun createUserDataPrompt(request: FinancialAdviceRequest): String {
        val currencySymbol =
            CurrencySettings.getCurrencySymbol(request.currencySettings.currencyCode)
        val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(
            Date().apply {
                @Suppress("DEPRECATION")
                year = request.currentYear - 1900
                month = request.currentMonth - 1
            }
        )

        val prompt = StringBuilder()
        prompt.append("USER'S CURRENT FINANCIAL DATA ($monthName):\n\n")

        // Current date and temporal context
        val currentDate = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())
        val calendar = java.util.Calendar.getInstance()
        val currentDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        val totalDaysInMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
        val daysRemaining = totalDaysInMonth - currentDay
        val monthProgress = (currentDay.toFloat() / totalDaysInMonth.toFloat() * 100).toInt()

        prompt.append("TEMPORAL CONTEXT:\n")
        prompt.append("• Current Date: $currentDate\n")
        prompt.append("• Current Day of Month: $currentDay of $totalDaysInMonth\n")
        prompt.append("• Days Remaining in Month: $daysRemaining\n")
        prompt.append("• Month Progress: $monthProgress%\n")
        prompt.append("• IMPORTANT: This expense data is ONLY until $currentDate - the month is NOT complete!\n\n")

        // Monthly spending summary with projections
        prompt.append("MONTHLY SPENDING SUMMARY (Up to $currentDate):\n")
        prompt.append("• Total Expenses So Far: $currencySymbol${"%.2f".format(request.totalExpenses)}\n")
        prompt.append("• Number of Transactions: ${request.expenses.size}\n")
        prompt.append("• Average Daily Spending: $currencySymbol${"%.2f".format(request.totalExpenses / currentDay)}\n")
        prompt.append("• Projected Monthly Total: $currencySymbol${"%.2f".format(request.totalExpenses / currentDay * totalDaysInMonth)}\n")
        prompt.append("• Currency: ${request.currencySettings.currencyCode}\n\n")

        // Category breakdown
        if (request.tagExpenseData.isNotEmpty()) {
            prompt.append("SPENDING BY CATEGORY:\n")
            request.tagExpenseData.sortedByDescending { it.totalAmount }.forEach { tagData ->
                val percentage =
                    if (request.totalExpenses > 0) (tagData.totalAmount / request.totalExpenses * 100) else 0.0
                prompt.append("• ${tagData.tag}: $currencySymbol${"%.2f".format(tagData.totalAmount)} (${percentage.toInt()}% of total)\n")
            }
            prompt.append("\n")
        }

        // FINANCIAL PROFILE & BUDGET ANALYSIS
        request.budgetData?.let { budget ->
            // Calculate monthly salary and rent from daily amounts
            val monthlySalary = budget.dailyIncome * budget.totalDaysInMonth
            val monthlyRent = budget.dailyRent * budget.totalDaysInMonth
            val remainingIncome = monthlySalary - budget.totalIncomeToDate
            val remainingRent = monthlyRent - budget.totalRentToDate

            prompt.append("FINANCIAL PROFILE & INCOME:\n")
            prompt.append("• Monthly Salary: $currencySymbol${"%.2f".format(monthlySalary)}\n")
            prompt.append("• Monthly Rent: $currencySymbol${"%.2f".format(monthlyRent)}\n")
            prompt.append("• Daily Income: $currencySymbol${"%.2f".format(budget.dailyIncome)}\n")
            prompt.append("• Daily Rent: $currencySymbol${"%.2f".format(budget.dailyRent)}\n")
            prompt.append("• Income Received So Far: $currencySymbol${"%.2f".format(budget.totalIncomeToDate)}\n")
            prompt.append("• Rent Paid So Far: $currencySymbol${"%.2f".format(budget.totalRentToDate)}\n")
            prompt.append(
                "• Expected Remaining Income: $currencySymbol${
                    "%.2f".format(
                        remainingIncome
                    )
                }\n"
            )
            prompt.append("• Expected Remaining Rent: $currencySymbol${"%.2f".format(remainingRent)}\n\n")

            prompt.append("BUDGET ANALYSIS (Current Status):\n")
            prompt.append("• Budget Period: Day ${budget.currentDay} of ${budget.totalDaysInMonth}\n")
            prompt.append("• Total Expenses to Date: $currencySymbol${"%.2f".format(budget.totalExpensesToDate)}\n")
            prompt.append("• Average Daily Expenses: $currencySymbol${"%.2f".format(budget.averageDailyExpenses)}\n")
            prompt.append("• Estimated End of Month Balance: $currencySymbol${"%.2f".format(budget.estimatedEndOfMonthBalance)}\n")
            prompt.append("• Current Budget Status: ${budget.budgetStatus.name}\n")

            // Calculate financial ratios and projections
            val availableAfterRent = monthlySalary - monthlyRent
            val expenseRatio =
                if (availableAfterRent > 0) (request.totalExpenses / availableAfterRent * 100) else 0.0
            val projectedMonthlyExpenses = request.totalExpenses / currentDay * totalDaysInMonth
            val projectedEndBalance = monthlySalary - monthlyRent - projectedMonthlyExpenses

            prompt.append("• Available After Rent: $currencySymbol${"%.2f".format(availableAfterRent)}\n")
            prompt.append("• Current Expense Ratio: ${expenseRatio.toInt()}% of available income\n")
            prompt.append(
                "• Projected Monthly Expenses: $currencySymbol${
                    "%.2f".format(
                        projectedMonthlyExpenses
                    )
                }\n"
            )
            prompt.append(
                "• Projected End Balance: $currencySymbol${
                    "%.2f".format(
                        projectedEndBalance
                    )
                }\n"
            )
            prompt.append("• Days Remaining to Manage: $daysRemaining days\n")
            prompt.append("\n")
        }

        // ALL DETAILED TRANSACTIONS (complete list for better AI analysis)
        if (request.expenses.isNotEmpty()) {
            prompt.append("ALL DETAILED TRANSACTIONS THIS MONTH:\n")
            request.expenses.sortedByDescending { it.timestamp }.forEach { expense ->
                val date = SimpleDateFormat(
                    "MMM dd, yyyy HH:mm",
                    Locale.getDefault()
                ).format(Date(expense.timestamp))
                val dayOfWeek =
                    SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(expense.timestamp))
                prompt.append("• $date ($dayOfWeek): $currencySymbol${"%.2f".format(expense.amount)} - ${expense.tag}")

                // Add expense ID for reference if needed
                prompt.append(" [ID: ${expense.id}]")

                // Add image info if available
                if (expense.imagePath != null) {
                    prompt.append(" [Has Receipt Image]")
                }

                prompt.append("\n")
            }
            prompt.append("\n")
        }

        // DETAILED SPENDING PATTERNS AND ANALYSIS
        if (request.expenses.isNotEmpty()) {
            val avgTransaction = request.totalExpenses / request.expenses.size
            val maxExpense = request.expenses.maxByOrNull { it.amount }
            val minExpense = request.expenses.minByOrNull { it.amount }

            prompt.append("DETAILED SPENDING PATTERNS:\n")
            prompt.append("• Average Transaction: $currencySymbol${"%.2f".format(avgTransaction)}\n")
            maxExpense?.let {
                val maxDate =
                    SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(it.timestamp))
                prompt.append("• Largest Expense: $currencySymbol${"%.2f".format(it.amount)} (${it.tag}) on $maxDate\n")
            }
            minExpense?.let {
                val minDate =
                    SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(it.timestamp))
                prompt.append("• Smallest Expense: $currencySymbol${"%.2f".format(it.amount)} (${it.tag}) on $minDate\n")
            }

            // Day-by-day spending analysis
            val dailySpending = request.expenses.groupBy { expense ->
                SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(expense.timestamp))
            }.mapValues { (_, expenses) ->
                expenses.sumOf { it.amount }
            }.toList().sortedByDescending { it.second }

            prompt.append("• Daily Spending Breakdown:\n")
            dailySpending.forEach { (date, amount) ->
                val dayExpenses = request.expenses.filter {
                    SimpleDateFormat(
                        "MMM dd",
                        Locale.getDefault()
                    ).format(Date(it.timestamp)) == date
                }
                prompt.append("  - $date: $currencySymbol${"%.2f".format(amount)} (${dayExpenses.size} transactions)\n")
            }

            // Day of week patterns
            val dayOfWeekSpending = request.expenses.groupBy { expense ->
                SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(expense.timestamp))
            }.mapValues { (_, expenses) ->
                expenses.sumOf { it.amount }
            }.toList().sortedByDescending { it.second }

            prompt.append("• Day of Week Spending Patterns:\n")
            dayOfWeekSpending.forEach { (day, amount) ->
                val dayExpenses = request.expenses.filter {
                    SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(it.timestamp)) == day
                }
                prompt.append("  - $day: $currencySymbol${"%.2f".format(amount)} (${dayExpenses.size} transactions)\n")
            }

            // Time of day patterns
            val timeRanges = mapOf(
                "Morning (6-12)" to 6..11,
                "Afternoon (12-18)" to 12..17,
                "Evening (18-24)" to 18..23,
                "Night (0-6)" to 0..5
            )

            prompt.append("• Time of Day Spending Patterns:\n")
            timeRanges.forEach { (label, range) ->
                val timeExpenses = request.expenses.filter { expense ->
                    val hour =
                        SimpleDateFormat("HH", Locale.getDefault()).format(Date(expense.timestamp))
                            .toInt()
                    hour in range
                }
                if (timeExpenses.isNotEmpty()) {
                    val timeAmount = timeExpenses.sumOf { it.amount }
                    prompt.append("  - $label: $currencySymbol${"%.2f".format(timeAmount)} (${timeExpenses.size} transactions)\n")
                }
            }

            prompt.append("\n")
        }

        prompt.append("ANALYSIS REQUEST:\n")
        prompt.append("Based on this comprehensive financial data, please provide a detailed 50-30-20 budget analysis with the following structure:\n\n")
        prompt.append("1. **50-30-20 BUDGET BREAKDOWN**: Calculate exact amounts and percentages for:\n")
        prompt.append("   - 50% NEEDS (including rent, utilities, groceries, transportation)\n")
        prompt.append("   - 30% WANTS (dining out, entertainment, shopping, subscriptions)\n")
        prompt.append("   - 20% SAVINGS & INVESTMENTS (emergency fund, retirement, investments)\n\n")
        prompt.append("2. **RENT IMPACT ANALYSIS**: Show how rent fits into the 50% needs category and affects overall budget\n\n")
        prompt.append("3. **CATEGORY SPENDING ANALYSIS**: Break down my current expenses by 50-30-20 categories with specific recommendations\n\n")
        prompt.append("4. **INVESTMENT & SAVINGS PLAN**: Suggest specific investment strategies, emergency fund targets, and retirement planning\n\n")
        prompt.append("5. **END-OF-MONTH PROJECTION**: Calculate remaining balance including all rent payments and fixed expenses\n\n")
        prompt.append("6. **REMAINING MONTH STRATEGY**: Daily spending limits and specific guidance for the remaining $daysRemaining days\n\n")
        prompt.append("7. **LONG-TERM ACTION PLAN**: Specific steps to optimize my 50-30-20 allocation and build wealth\n\n")
        prompt.append("CRITICAL: This data is INCOMPLETE (only up to $currentDate). Always include rent in expense calculations and provide end-of-month projections that account for all remaining fixed expenses. Focus on both immediate budget management and long-term financial health through proper 50-30-20 allocation.")

        return prompt.toString()
    }
}