package ir.act.personalAccountant.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ir.act.personalAccountant.presentation.budget_config.BudgetConfigScreen
import ir.act.personalAccountant.presentation.expense_edit.ExpenseEditContract
import ir.act.personalAccountant.presentation.expense_edit.ExpenseEditScreen
import ir.act.personalAccountant.presentation.expense_entry.ExpenseEntryScreen
import ir.act.personalAccountant.presentation.expense_list.ExpenseListScreen
import ir.act.personalAccountant.presentation.settings.SettingsContract
import ir.act.personalAccountant.presentation.settings.SettingsScreen
import ir.act.personalAccountant.presentation.view_all_expenses.ViewAllExpensesScreen

object Routes {
    const val EXPENSE_ENTRY = "expense_entry"
    const val EXPENSE_LIST = "expense_list"
    const val EXPENSE_EDIT = "expense_edit/{expenseId}"
    const val SETTINGS = "settings"
    const val VIEW_ALL_EXPENSES = "view_all_expenses?filterTag={filterTag}"
    const val BUDGET_CONFIG = "budget_config"
    
    fun expenseEdit(expenseId: Long) = "expense_edit/$expenseId"
    fun viewAllExpenses(filterTag: String? = null) = if (filterTag != null) {
        "view_all_expenses?filterTag=$filterTag"
    } else {
        "view_all_expenses"
    }
}

@Composable
fun PersonalAccountantNavigation(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Routes.EXPENSE_LIST
    ) {
        composable(Routes.EXPENSE_ENTRY) {
            ExpenseEntryScreen(
                onNavigateToExpenseList = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Routes.EXPENSE_LIST) {
            ExpenseListScreen(
                onNavigateToExpenseEntry = {
                    navController.navigate(Routes.EXPENSE_ENTRY)
                },
                onNavigateToExpenseEdit = { expenseId ->
                    navController.navigate(Routes.expenseEdit(expenseId))
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
                onNavigateToViewAllExpenses = { filterTag ->
                    navController.navigate(Routes.viewAllExpenses(filterTag))
                },
                onNavigateToBudgetConfig = {
                    navController.navigate(Routes.BUDGET_CONFIG)
                }
            )
        }
        
        composable(
            route = Routes.EXPENSE_EDIT,
            arguments = listOf(
                navArgument("expenseId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getLong("expenseId") ?: 0L
            ExpenseEditScreen(
                expenseId = expenseId,
                uiInteractions = object : ExpenseEditContract.UiInteractions {
                    override fun navigateBack() {
                        navController.popBackStack()
                    }
                }
            )
        }
        
        composable(Routes.SETTINGS) {
            SettingsScreen(
                uiInteractions = object : SettingsContract.UiInteractions {
                    override fun navigateBack() {
                        navController.popBackStack()
                    }
                    
                    override fun navigateToBudgetConfig() {
                        navController.navigate(Routes.BUDGET_CONFIG)
                    }
                }
            )
        }
        
        composable(
            route = Routes.VIEW_ALL_EXPENSES,
            arguments = listOf(
                navArgument("filterTag") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            ViewAllExpensesScreen(
                onNavigateToExpenseEdit = { expenseId ->
                    navController.navigate(Routes.expenseEdit(expenseId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Routes.BUDGET_CONFIG) {
            BudgetConfigScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}