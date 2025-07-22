package ir.act.personalAccountant.util

object Constants {

    // Navigation Constants
    object Navigation {
        const val NAVIGATE_TO_EXPENSE_ENTRY = "expense_entry"
        const val NAVIGATE_TO_TOTAL_EXPENSES = "total_expenses"
        const val NAVIGATE_TO_KEY = "navigate_to"
    }

    // Notification Constants
    object Notifications {
        const val PERMISSION_GRANTED_MESSAGE = "Notifications enabled"
        const val PERMISSION_DENIED_MESSAGE = "Notification permission denied"
        const val PERMISSION_PERMANENTLY_DENIED_MESSAGE =
            "Enable notifications in app settings to use this feature"
    }

    // Settings Constants
    object Settings {
        const val NOTIFICATIONS_TITLE = "Notifications"
        const val DAILY_BUDGET_NOTIFICATIONS = "Daily Budget Notifications"
        const val ENABLED = "Enabled"
        const val DISABLED = "Disabled"
        const val CURRENCY_TITLE = "Currency"
        const val BUDGET_SETTINGS_TITLE = "Budget Settings"
        const val BUDGET_CONFIGURATION = "Budget Configuration"
        const val CONFIGURED = "Configured"
        const val NOT_CONFIGURED = "Not configured"
        const val CATEGORY_SETTINGS_TITLE = "Category Settings"
        const val MANAGE_CATEGORIES = "Manage and merge categories"
        const val AI_SETTINGS_TITLE = "AI Settings"
        const val CONFIGURE_OPENAI = "Configure OpenAI for receipt analysis"
        const val FINANCIAL_ADVISOR = "Financial Advisor"
        const val AI_FINANCIAL_INSIGHTS = "Get AI-powered financial insights"
        const val DATA_SYNC_TITLE = "Data Sync"
        const val GOOGLE_SHEETS = "Google Sheets"
        const val SYNC_TO_SHEETS = "Sync expenses to Google Sheets"
        const val SETTINGS_TITLE = "Settings"
        const val SELECT_CURRENCY = "Select Currency"
        const val CANCEL = "Cancel"
        const val ERROR_TITLE = "Error"
        const val OK = "OK"
    }

    // Notification Content
    object NotificationContent {
        const val DAY_PREFIX = "Day"
        const val APP_TITLE = "Personal Accountant"
        const val TODAY_EXPENSES_PREFIX = "Today's Expenses:"
        const val TOTAL_EXPENSES_PREFIX = "💯 Total Expenses:"
        const val DAILY_BUDGET_PREFIX = "💰 Daily Budget:"
        const val REMAINING_PREFIX = "🎯 Remaining:"
        const val TODAY_SUFFIX = "Today"
        const val SPENT = "Spent"
        const val TOTAL_SUFFIX = "total"
        const val BUDGET_SUFFIX = "budget"
    }
}