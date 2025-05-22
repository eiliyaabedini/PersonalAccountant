package ir.act.personalAccountant.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ir.act.personalAccountant.presentation.expense_entry.ExpenseEntryScreen
import ir.act.personalAccountant.presentation.expense_list.ExpenseListScreen

object Routes {
    const val EXPENSE_ENTRY = "expense_entry"
    const val EXPENSE_LIST = "expense_list"
}

@Composable
fun PersonalAccountantNavigation(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Routes.EXPENSE_ENTRY
    ) {
        composable(Routes.EXPENSE_ENTRY) {
            ExpenseEntryScreen(
                onNavigateToExpenseList = {
                    navController.navigate(Routes.EXPENSE_LIST) {
                        popUpTo(Routes.EXPENSE_ENTRY) { inclusive = false }
                    }
                }
            )
        }
        
        composable(Routes.EXPENSE_LIST) {
            ExpenseListScreen(
                onNavigateToExpenseEntry = {
                    navController.navigate(Routes.EXPENSE_ENTRY) {
                        popUpTo(Routes.EXPENSE_LIST) { inclusive = false }
                    }
                }
            )
        }
    }
}