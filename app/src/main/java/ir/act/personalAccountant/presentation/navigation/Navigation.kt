package ir.act.personalAccountant.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ir.act.personalAccountant.presentation.expense_edit.ExpenseEditContract
import ir.act.personalAccountant.presentation.expense_edit.ExpenseEditScreen
import ir.act.personalAccountant.presentation.expense_entry.ExpenseEntryScreen
import ir.act.personalAccountant.presentation.expense_list.ExpenseListScreen

object Routes {
    const val EXPENSE_ENTRY = "expense_entry"
    const val EXPENSE_LIST = "expense_list"
    const val EXPENSE_EDIT = "expense_edit/{expenseId}"
    
    fun expenseEdit(expenseId: Long) = "expense_edit/$expenseId"
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
    }
}