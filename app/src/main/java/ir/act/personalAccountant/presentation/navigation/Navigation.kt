package ir.act.personalAccountant.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ir.act.personalAccountant.ai.presentation.settings.AISettingsScreen
import ir.act.personalAccountant.presentation.asset_entry.AssetEntryScreen
import ir.act.personalAccountant.presentation.asset_list.AssetListScreen
import ir.act.personalAccountant.presentation.budget_config.BudgetConfigScreen
import ir.act.personalAccountant.presentation.category_settings.CategorySettingsScreen
import ir.act.personalAccountant.presentation.expense_edit.ExpenseEditContract
import ir.act.personalAccountant.presentation.expense_edit.ExpenseEditScreen
import ir.act.personalAccountant.presentation.expense_entry.ExpenseEntryScreen
import ir.act.personalAccountant.presentation.expense_list.ExpenseListScreen
import ir.act.personalAccountant.presentation.financial_advisor.FinancialAdvisorScreen
import ir.act.personalAccountant.presentation.googlesheets.GoogleSheetsScreen
import ir.act.personalAccountant.presentation.googlesheets.GoogleSheetsViewModel
import ir.act.personalAccountant.presentation.net_worth_calculation.NetWorthCalculationScreen
import ir.act.personalAccountant.presentation.net_worth_dashboard.NetWorthDashboardScreen
import ir.act.personalAccountant.presentation.net_worth_history.NetWorthHistoryScreen
import ir.act.personalAccountant.presentation.settings.SettingsContract
import ir.act.personalAccountant.presentation.settings.SettingsScreen
import ir.act.personalAccountant.presentation.sync.SyncProgressScreen
import ir.act.personalAccountant.presentation.view_all_expenses.ViewAllExpensesScreen

object Routes {
    const val EXPENSE_ENTRY = "expense_entry"
    const val EXPENSE_LIST = "expense_list"
    const val EXPENSE_EDIT = "expense_edit/{expenseId}"
    const val SETTINGS = "settings"
    const val VIEW_ALL_EXPENSES = "view_all_expenses?filterTag={filterTag}"
    const val BUDGET_CONFIG = "budget_config"
    const val CATEGORY_SETTINGS = "category_settings"
    const val GOOGLE_SHEETS = "google_sheets"
    const val SYNC_PROGRESS = "sync_progress"
    const val AI_SETTINGS = "ai_settings"
    const val FINANCIAL_ADVISOR = "financial_advisor"
    const val NET_WORTH_DASHBOARD = "net_worth_dashboard"
    const val NET_WORTH_HISTORY = "net_worth_history"
    const val ASSET_LIST = "asset_list"
    const val ASSET_ENTRY = "asset_entry"
    const val ASSET_EDIT = "asset_edit/{assetId}"
    const val NET_WORTH_CALCULATION = "net_worth_calculation"

    fun expenseEdit(expenseId: Long) = "expense_edit/$expenseId"
    fun viewAllExpenses(filterTag: String? = null) = if (filterTag != null) {
        "view_all_expenses?filterTag=$filterTag"
    } else {
        "view_all_expenses"
    }
    fun assetEdit(assetId: Long) = "asset_edit/$assetId"
}

@Composable
fun PersonalAccountantNavigation(
    navController: NavHostController,
    googleSheetsViewModel: GoogleSheetsViewModel,
    onGoogleSignInClick: () -> Unit
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
                },
                onNavigateToGoogleSheets = {
                    navController.navigate(Routes.GOOGLE_SHEETS)
                },
                onNavigateToFinancialAdvisor = {
                    navController.navigate(Routes.FINANCIAL_ADVISOR)
                },
                onNavigateToNetWorth = {
                    navController.navigate(Routes.NET_WORTH_DASHBOARD)
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

                    override fun navigateToCategorySettings() {
                        navController.navigate(Routes.CATEGORY_SETTINGS)
                    }

                    override fun navigateToGoogleSheets() {
                        navController.navigate(Routes.GOOGLE_SHEETS)
                    }

                    override fun navigateToAISettings() {
                        navController.navigate(Routes.AI_SETTINGS)
                    }

                    override fun navigateToFinancialAdvisor() {
                        navController.navigate(Routes.FINANCIAL_ADVISOR)
                    }
                }
            )
        }

        composable(Routes.GOOGLE_SHEETS) {
            GoogleSheetsScreen(
                viewModel = googleSheetsViewModel,
                onSignInClick = onGoogleSignInClick,
                onNavigateToSyncProgress = {
                    navController.navigate(Routes.SYNC_PROGRESS)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.SYNC_PROGRESS) {
            SyncProgressScreen(
                onNavigateBack = {
                    navController.popBackStack()
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

        composable(Routes.CATEGORY_SETTINGS) {
            CategorySettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.AI_SETTINGS) {
            AISettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.FINANCIAL_ADVISOR) {
            FinancialAdvisorScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAISettings = {
                    navController.navigate(Routes.AI_SETTINGS)
                }
            )
        }

        composable(Routes.NET_WORTH_DASHBOARD) {
            NetWorthDashboardScreen(
                onNavigateToAssetEntry = {
                    navController.navigate(Routes.ASSET_ENTRY)
                },
                onNavigateToAssetEdit = { assetId ->
                    navController.navigate(Routes.assetEdit(assetId))
                },
                onNavigateToNetWorthHistory = {
                    navController.navigate(Routes.NET_WORTH_HISTORY)
                }
            )
        }

        composable(Routes.ASSET_LIST) {
            AssetListScreen(
                onNavigateToAssetEntry = {
                    navController.navigate(Routes.ASSET_ENTRY)
                },
                onNavigateToAssetEdit = { assetId ->
                    navController.navigate(Routes.assetEdit(assetId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.ASSET_ENTRY) {
            AssetEntryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.ASSET_EDIT,
            arguments = listOf(
                navArgument("assetId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val assetId = backStackEntry.arguments?.getLong("assetId") ?: 0L
            AssetEntryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.NET_WORTH_HISTORY) {
            NetWorthHistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.NET_WORTH_CALCULATION) {
            NetWorthCalculationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}