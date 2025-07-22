package ir.act.personalAccountant.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "user_preferences",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_IS_BUDGET_MODE = "is_budget_mode"
    }

    var isBudgetMode: Boolean
        get() = prefs.getBoolean(KEY_IS_BUDGET_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_BUDGET_MODE, value).apply()
}