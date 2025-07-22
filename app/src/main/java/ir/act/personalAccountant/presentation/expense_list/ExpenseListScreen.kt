package ir.act.personalAccountant.presentation.expense_list

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.act.personalAccountant.R
import ir.act.personalAccountant.core.util.CurrencyFormatter
import ir.act.personalAccountant.core.util.DateUtils
import ir.act.personalAccountant.core.util.ImageFileManager
import ir.act.personalAccountant.domain.model.CurrencySettings
import ir.act.personalAccountant.domain.model.Expense
import ir.act.personalAccountant.presentation.components.BudgetDetailsList
import ir.act.personalAccountant.presentation.components.BudgetOwlDisplay
import ir.act.personalAccountant.presentation.components.DonutChart
import ir.act.personalAccountant.presentation.components.DonutChartLegend
import ir.act.personalAccountant.presentation.components.LayeredProgressBar
import ir.act.personalAccountant.presentation.components.ProgressLayer
import ir.act.personalAccountant.presentation.components.TripModeSetupDialog
import ir.act.personalAccountant.presentation.components.assignColorsToTagData
import ir.act.personalAccountant.ui.theme.BudgetGreenLight
import ir.act.personalAccountant.ui.theme.BudgetOrangeLight
import ir.act.personalAccountant.ui.theme.BudgetPurpleLight
import ir.act.personalAccountant.ui.theme.BudgetRedLight
import ir.act.personalAccountant.ui.theme.TextSecondary
import ir.act.personalAccountant.ui.theme.YellowPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ExpenseListScreen(
    onNavigateToExpenseEntry: () -> Unit,
    onNavigateToExpenseEdit: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToViewAllExpenses: (String?) -> Unit,
    onNavigateToBudgetConfig: () -> Unit,
    onNavigateToGoogleSheets: () -> Unit,
    onNavigateToFinancialAdvisor: () -> Unit,
    viewModel: ExpenseListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Use default currency settings for now - will be properly integrated with ViewModel later
    val currencySettings = uiState.currencySettings

    // Track the timestamp of when the screen was first shown
    val screenOpenTime = remember { System.currentTimeMillis() }

    // Snackbar state for showing messages
    val snackbarHostState = remember { SnackbarHostState() }

    // Camera launcher for direct receipt capture
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            // Handle successful camera capture
            viewModel.onEvent(ExpenseListEvent.CameraImageCaptured)
        }
    }

    LaunchedEffect(viewModel.uiInteraction) {
        viewModel.uiInteraction.collect { interaction ->
            when (interaction) {
                ExpenseListUiInteraction.NavigateToExpenseEntry -> {
                    onNavigateToExpenseEntry()
                }

                is ExpenseListUiInteraction.NavigateToExpenseEdit -> {
                    onNavigateToExpenseEdit(interaction.expenseId)
                }

                ExpenseListUiInteraction.NavigateToBudgetConfig -> {
                    onNavigateToBudgetConfig()
                }

                is ExpenseListUiInteraction.ShowSuccessMessage -> {
                    snackbarHostState.showSnackbar(
                        message = interaction.message,
                        withDismissAction = true
                    )
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // White top section with status bar, header and chart/budget display
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(
                                bottomStart = 30.dp,
                                bottomEnd = 30.dp
                            )
                        )
                        .padding(bottom = if (uiState.isBudgetMode) 0.dp else 20.dp)
                ) {
                    // Status bar space
                    Spacer(modifier = Modifier.height(8.dp))

                    // Top row with AI advisor, sync and settings icons
                    TopBar(
                        viewModel,
                        uiState,
                        onNavigateToFinancialAdvisor,
                        onNavigateToGoogleSheets,
                        onNavigateToSettings
                    )
                    // Month navigation header (only show in expense mode)
                    if (!uiState.isBudgetMode) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Previous month arrow
                            IconButton(
                                onClick = { viewModel.onEvent(ExpenseListEvent.PreviousMonthClicked) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.8f),
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Previous month",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Month/Year display
                            Text(
                                text = DateUtils.formatMonthYear(
                                    uiState.currentYear,
                                    uiState.currentMonth
                                ),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            // Next month arrow
                            IconButton(
                                onClick = { viewModel.onEvent(ExpenseListEvent.NextMonthClicked) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.8f),
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Next month",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = if (uiState.isBudgetMode) 0.dp else 20.dp)
                    ) {
                        if (uiState.isBudgetMode) {
                            // Budget mode: Show owl display
                            BudgetOwlDisplay(
                                budgetData = uiState.budgetData,
                                currencySettings = currencySettings,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            // Expense mode content (original)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left side: Donut chart
                                if (uiState.tagExpenseData.isNotEmpty() && uiState.totalExpenses > 0) {
                                    val coloredTagData =
                                        assignColorsToTagData(uiState.tagExpenseData)

                                    // Donut chart
                                    Box(
                                        modifier = Modifier
                                            .size(120.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        DonutChart(
                                            data = coloredTagData,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        // Center content
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "${uiState.expenses.size}",
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Text(
                                                text = "expenses",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                } else {
                                    // When no chart data, show empty space on left
                                    Spacer(modifier = Modifier.size(120.dp))
                                }

                                // Right side: Total expenses (centered vertically)
                                Column(
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "Total Expenses",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = CurrencyFormatter.formatCurrency(
                                            uiState.totalExpenses,
                                            currencySettings
                                        ),
                                        style = MaterialTheme.typography.displaySmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }

                    // Legend section (only show if chart exists AND not in budget mode)
                    if (!uiState.isBudgetMode && uiState.tagExpenseData.isNotEmpty() && uiState.totalExpenses > 0) {
                        val coloredTagData = assignColorsToTagData(uiState.tagExpenseData)

                        Spacer(modifier = Modifier.height(10.dp))

                        // Legend
                        DonutChartLegend(
                            data = coloredTagData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            currencySettings = currencySettings,
                            onTagClick = { tag ->
                                onNavigateToViewAllExpenses(tag)
                            }
                        )
                    }
                }

                // Bottom section - budget details in budget mode, expense list in expense mode
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    if (uiState.isBudgetMode) {
                        // Budget Details List
                        BudgetDetailsList(
                            budgetData = uiState.budgetData,
                            currencySettings = currencySettings,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Regular expense list
                    when {
                        uiState.isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        uiState.expenses.isEmpty() -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Image(
                                        painter = painterResource(R.mipmap.owl),
                                        contentDescription = "Owl waiting for expenses",
                                        modifier = Modifier.size(120.dp)
                                    )

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Text(
                                        text = "No expenses yet",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "Use the + button below to add your first expense and start tracking your finances",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextSecondary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }

                        else -> {
                            // Recent Expenses section
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(15.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 5.dp, vertical = 5.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Recent Expenses",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "View all →",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            modifier = Modifier.clickable {
                                                onNavigateToViewAllExpenses(null)
                                            }
                                        )
                                    }

                                    LazyColumn(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(0.dp)
                                    ) {
                                        // Show expenses grouped by day with sticky headers
                                        uiState.groupedExpensesByDay.forEach { (dayOfMonth, expensesForDay) ->
                                            stickyHeader(key = "day_header_$dayOfMonth") {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(MaterialTheme.colorScheme.surface)
                                                        .padding(vertical = 4.dp)
                                                ) {
                                                    DayGroupHeader(
                                                        dayOfMonth = dayOfMonth,
                                                        totalAmount = expensesForDay.sumOf { it.amount },
                                                        currencySettings = currencySettings,
                                                    )
                                                }
                                            }

                                            items(
                                                items = expensesForDay.take(5), // Limit to 5 per day for recent expenses
                                                key = { it.id }
                                            ) { expense ->
                                                SwipeToDeleteExpenseItem(
                                                    expense = expense,
                                                    isNewlyAdded = expense.timestamp > screenOpenTime,
                                                    currencySettings = currencySettings,
                                                    onEditClick = {
                                                        viewModel.onEvent(
                                                            ExpenseListEvent.EditClicked(
                                                                expense
                                                            )
                                                        )
                                                    },
                                                    onDeleteClick = {
                                                        viewModel.onEvent(
                                                            ExpenseListEvent.DeleteClicked(
                                                                expense
                                                            )
                                                        )
                                                    }
                                                )
                                            }

                                            item {
                                                Spacer(modifier = Modifier.height(8.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    }
                }
            }

            // FAB Buttons
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(30.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Camera FAB (smaller, on top)
                SmallFloatingActionButton(
                    shape = CircleShape,
                    onClick = {
                        if (!uiState.isAnalyzingReceipt) {
                            val imageFileManager = ImageFileManager()
                            val tempFile = imageFileManager.createTempImageFile(context)
                            val uri = imageFileManager.getFileProviderUri(context, tempFile)
                            viewModel.onEvent(ExpenseListEvent.CameraClicked(uri))
                            cameraLauncher.launch(uri)
                        }
                    },
                    containerColor = if (uiState.isAnalyzingReceipt)
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                    else
                        MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    if (uiState.isAnalyzingReceipt) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onSecondary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.baseline_camera_enhance_24),
                            contentDescription = "Take image from receipt"
                        )
                    }
                }

                // Add expense FAB (main, larger)
                FloatingActionButton(
                    onClick = { viewModel.onEvent(ExpenseListEvent.AddClicked) },
                    modifier = Modifier.size(56.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // Show error snackbar
            uiState.error?.let { error ->
                LaunchedEffect(error) {
                    snackbarHostState.showSnackbar(
                        message = error,
                        withDismissAction = true
                    )
                    viewModel.onEvent(ExpenseListEvent.ClearError)
                }
            }

            // Show AI analysis error snackbar
            uiState.aiAnalysisError?.let { error ->
                LaunchedEffect(error) {
                    snackbarHostState.showSnackbar(
                        message = error,
                        withDismissAction = true
                    )
                    viewModel.onEvent(ExpenseListEvent.ClearAIAnalysisError)
                }
            }

            // Delete confirmation dialog
            if (uiState.showDeleteConfirmation) {
                AlertDialog(
                    onDismissRequest = { viewModel.onEvent(ExpenseListEvent.CancelDelete) },
                    title = { Text("Delete Expense") },
                    text = {
                        uiState.expenseToDelete?.let { expense ->
                            Text(
                                "Are you sure you want to delete this ${
                                    CurrencyFormatter.formatCurrency(
                                        expense.amount,
                                        currencySettings
                                    )
                                } expense?"
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { viewModel.onEvent(ExpenseListEvent.ConfirmDelete) },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.onEvent(ExpenseListEvent.CancelDelete) }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Trip Mode Setup Dialog
            if (uiState.showTripModeSetup) {
                TripModeSetupDialog(
                    homeCurrency = uiState.currencySettings,
                    currentTripMode = uiState.tripModeSettings,
                    availableCurrencies = uiState.availableCurrencies,
                    onTripModeUpdate = { settings ->
                        viewModel.onEvent(ExpenseListEvent.TripModeSettingsUpdated(settings))
                    },
                    onDismiss = { viewModel.onEvent(ExpenseListEvent.DismissTripModeSetup) },
                    onAIExchangeRateRequested = { fromCurrency, toCurrency ->
                        viewModel.onEvent(
                            ExpenseListEvent.AIExchangeRateRequested(
                                fromCurrency,
                                toCurrency
                            )
                        )
                    },
                    isLoadingAIRate = uiState.isLoadingAIExchangeRate,
                    aiRateError = uiState.aiExchangeRateError,
                    aiExchangeRate = uiState.aiExchangeRate
                )
            }

            // Animated switch at bottom center
            AnimatedBudgetModeSwitch(
                viewModel = viewModel,
                uiState = uiState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            )
        }
    }
}

@Composable
private fun TopBar(
    viewModel: ExpenseListViewModel,
    uiState: ExpenseListUiState,
    onNavigateToFinancialAdvisor: () -> Unit,
    onNavigateToGoogleSheets: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TripModeToggleButton(viewModel, uiState)

        Spacer(modifier = Modifier.weight(1f))

        // Financial Advisor AI icon
        IconButton(
            onClick = { onNavigateToFinancialAdvisor() },
            modifier = Modifier.size(40.dp)
        ) {
            Image(
                painter = painterResource(R.mipmap.owl),
                contentDescription = null,
                Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Sync icon
        IconButton(
            onClick = { onNavigateToGoogleSheets() },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.outline_sync_24),
                contentDescription = "Sync with Google Sheets",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Settings icon
        IconButton(
            onClick = { onNavigateToSettings() },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun AnimatedBudgetModeSwitch(
    viewModel: ExpenseListViewModel,
    uiState: ExpenseListUiState,
    modifier: Modifier = Modifier
) {
    // Animated values
    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (uiState.isBudgetMode)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.surface,
        animationSpec = tween(300),
        label = "background_color"
    )

    val animatedThumbOffset by animateFloatAsState(
        targetValue = if (uiState.isBudgetMode) 1f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "thumb_offset"
    )

    Card(
        onClick = { viewModel.onEvent(ExpenseListEvent.BudgetModeToggled) },
        colors = CardDefaults.cardColors(
            containerColor = animatedBackgroundColor.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(50.dp), // More rounded for switch appearance
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
            pressedElevation = 12.dp
        ),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .width(140.dp)
                .height(48.dp)
                .padding(6.dp)
        ) {
            // Track background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(50.dp)
                    )
            )

            // Sliding thumb
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isBudgetMode)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(50.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .width(68.dp)
                    .height(36.dp)
                    .offset(x = (60.dp * animatedThumbOffset))
                    .align(Alignment.CenterStart)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (uiState.isBudgetMode) "Budget" else "Expense",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.isBudgetMode)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // Background labels
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Expense",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (!uiState.isBudgetMode)
                        Color.Transparent // Hidden when thumb is over it
                    else
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Budget",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (uiState.isBudgetMode)
                        Color.Transparent // Hidden when thumb is over it
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun TripModeToggleButton(
    viewModel: ExpenseListViewModel,
    uiState: ExpenseListUiState
) {
    IconButton(
        onClick = { viewModel.onEvent(ExpenseListEvent.TripModeToggled) },
        modifier = Modifier
            .size(40.dp)
            .background(
                if (uiState.tripModeSettings.isEnabled) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                } else {
                    Color.White.copy(alpha = 0.2f)
                },
                shape = CircleShape
            )
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_airplane),
            contentDescription = if (uiState.tripModeSettings.isEnabled) "Disable Trip Mode" else "Enable Trip Mode",
            tint = if (uiState.tripModeSettings.isEnabled) {
                Color.White
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            },
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ExpenseItem(expense: Expense, currencySettings: CurrencySettings) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = CurrencyFormatter.formatCurrency(expense.amount, currencySettings),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formatDate(expense.timestamp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            AssistChip(
                onClick = { },
                label = {
                    Text(
                        text = expense.tag,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                modifier = Modifier.height(24.dp),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }
    }
}


private fun formatDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val calendar = Calendar.getInstance()

    // Today
    calendar.timeInMillis = now
    val todayStart = calendar.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    // Yesterday
    calendar.add(Calendar.DAY_OF_YEAR, -1)
    val yesterdayStart = calendar.timeInMillis

    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

    return when {
        timestamp >= todayStart -> "Today ${timeFormat.format(Date(timestamp))}"
        timestamp >= yesterdayStart -> "Yesterday ${timeFormat.format(Date(timestamp))}"
        else -> dateFormat.format(Date(timestamp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteExpenseItem(
    expense: Expense,
    isNewlyAdded: Boolean,
    currencySettings: CurrencySettings,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // Animation for newly added items
    val highlightAlpha =
        remember { androidx.compose.animation.core.Animatable(if (isNewlyAdded) 0.3f else 0f) }
    val scale =
        remember { androidx.compose.animation.core.Animatable(if (isNewlyAdded) 0.95f else 1f) }

    LaunchedEffect(isNewlyAdded) {
        if (isNewlyAdded) {
            // Animate scale
            scale.animateTo(
                targetValue = 1f,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = 0.5f,
                    stiffness = 300f
                )
            )

            // Animate highlight
            highlightAlpha.animateTo(
                targetValue = 0f,
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 2000,
                    delayMillis = 500
                )
            )
        }
    }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDeleteClick()
                    false // Don't dismiss immediately, wait for confirmation
                }

                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            // Only show background when actually swiping
            if (dismissState.progress > 0.1f) {
                val color by animateColorAsState(
                    targetValue = when (dismissState.targetValue) {
                        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                        else -> Color.Transparent
                    },
                    label = "swipe background color"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onError
                    )
                }
            }
        },
        content = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                    }
            ) {
                // Highlight overlay
                if (highlightAlpha.value > 0f) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = highlightAlpha.value)
                            )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface) // Add solid background
                        .clickable { onEditClick() }
                        .padding(horizontal = 5.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Transaction icon
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = when (expense.tag.lowercase()) {
                                        "lunch", "food" -> MaterialTheme.colorScheme.primary
                                        "taxi", "transport" -> MaterialTheme.colorScheme.secondary
                                        else -> MaterialTheme.colorScheme.tertiary
                                    },
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (expense.tag.lowercase()) {
                                    "lunch", "food" -> "🍽️"
                                    "taxi", "transport" -> "🚕"
                                    else -> "💰"
                                },
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = expense.tag,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                // Show attachment icon if expense has image
                                if (!expense.imagePath.isNullOrEmpty()) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_attachment),
                                        contentDescription = "Has attachment",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                // Show travel indicator if expense is a travel expense
                                if (expense.isTravelExpense) {
                                    Text(
                                        text = "✈️",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = formatDate(expense.timestamp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = CurrencyFormatter.formatCurrency(
                                expense.amount,
                                currencySettings
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        // Show destination currency amount if it's a travel expense
                        if (expense.isTravelExpense && expense.destinationAmount != null && expense.destinationCurrency != null) {
                            Text(
                                text = "${CurrencySettings.getCurrencySymbol(expense.destinationCurrency!!)}${
                                    String.format(
                                        "%.2f",
                                        expense.destinationAmount!!
                                    )
                                }",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        },
        enableDismissFromStartToEnd = false
    )
}

@Composable
private fun BudgetModeContent(
    budgetData: ir.act.personalAccountant.domain.model.BudgetData?,
    currencySettings: CurrencySettings,
    modifier: Modifier = Modifier
) {
    if (budgetData == null) {
        // No budget data available
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Budget Not Configured",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Please configure your budget settings",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
        return
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Date progress with month name
        val calendar = Calendar.getInstance()
        val monthName =
            calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) ?: ""

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Month Progress",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Text(
                text = "$monthName ${budgetData.currentDay}/${budgetData.totalDaysInMonth}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        // Combined progress bar with three sections
        BudgetProgressBar(
            budgetData = budgetData,
            modifier = Modifier.fillMaxWidth()
        )

        // Budget status gauge
        BudgetStatusGauge(
            budgetStatus = budgetData.budgetStatus,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Budget summary with remaining budget in center
        val remainingBudget =
            budgetData.totalIncomeToDate - budgetData.totalExpensesToDate - budgetData.totalRentToDate
        val remainingBudgetColor = when (budgetData.budgetStatus) {
            ir.act.personalAccountant.domain.model.BudgetStatus.GOOD -> BudgetGreenLight
            ir.act.personalAccountant.domain.model.BudgetStatus.MIDDLE -> BudgetOrangeLight
            ir.act.personalAccountant.domain.model.BudgetStatus.RED -> BudgetRedLight
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Total Income So Far",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = CurrencyFormatter.formatCurrency(
                        budgetData.totalIncomeToDate,
                        currencySettings
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = BudgetGreenLight
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Remaining",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = CurrencyFormatter.formatCurrency(remainingBudget, currencySettings),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = remainingBudgetColor
                )
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Expenses + Rent",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = CurrencyFormatter.formatCurrency(
                        budgetData.totalExpensesToDate + budgetData.totalRentToDate,
                        currencySettings
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = BudgetRedLight
                )
            }
        }

        // Additional detailed breakdown
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Daily Income",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = CurrencyFormatter.formatCurrency(
                        budgetData.dailyIncome,
                        currencySettings
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Daily Rent",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = CurrencyFormatter.formatCurrency(budgetData.dailyRent, currencySettings),
                    style = MaterialTheme.typography.bodySmall,
                    color = BudgetPurpleLight
                )
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Other Expenses",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = CurrencyFormatter.formatCurrency(
                        budgetData.totalExpensesToDate,
                        currencySettings
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = BudgetRedLight.copy(alpha = 0.8f)
                )
            }
        }

        // Additional metrics row: Estimated balance, daily budget, and average daily expenses
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Est. End-Month Balance",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = CurrencyFormatter.formatCurrency(
                        budgetData.estimatedEndOfMonthBalance,
                        currencySettings
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = when (budgetData.budgetStatus) {
                        ir.act.personalAccountant.domain.model.BudgetStatus.GOOD -> BudgetGreenLight
                        ir.act.personalAccountant.domain.model.BudgetStatus.MIDDLE -> BudgetOrangeLight
                        ir.act.personalAccountant.domain.model.BudgetStatus.RED -> BudgetRedLight
                    }
                )
            }

            // Daily Budget (Saving Goal) - always show when salary is configured
            if (budgetData.savingGoalStatus != ir.act.personalAccountant.domain.model.SavingGoalStatus.NOT_SET) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Daily Budget",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = CurrencyFormatter.formatCurrency(
                            budgetData.recommendedDailyExpenseBudget,
                            currencySettings
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (budgetData.recommendedDailyExpenseBudget > 0) {
                            // New color logic: green if today's spending < daily budget, red if over
                            if (budgetData.todayExpenses <= budgetData.recommendedDailyExpenseBudget) {
                                BudgetGreenLight
                            } else {
                                BudgetRedLight
                            }
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Avg Daily Expenses",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = CurrencyFormatter.formatCurrency(
                        budgetData.averageDailyExpenses,
                        currencySettings
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

    }
}

@Composable
private fun BudgetProgressBar(
    budgetData: ir.act.personalAccountant.domain.model.BudgetData,
    modifier: Modifier = Modifier
) {
    val totalSalary = budgetData.dailyIncome * budgetData.totalDaysInMonth

    // Calculate percentages (0-100)
    val incomePercentage = if (totalSalary > 0)
        ((budgetData.totalIncomeToDate / totalSalary) * 100.0).toFloat().coerceIn(0f, 100f) else 0f
    val totalExpensePercentage = if (totalSalary > 0)
        (((budgetData.totalExpensesToDate + budgetData.totalRentToDate) / totalSalary) * 100.0).toFloat()
            .coerceIn(0f, 100f) else 0f
    val rentPercentage = if (totalSalary > 0)
        ((budgetData.totalRentToDate / totalSalary) * 100.0).toFloat().coerceIn(0f, 100f) else 0f

    // Create layers - drawn from largest to smallest (back to front)
    val layers = listOf(
        ProgressLayer(
            value = incomePercentage,
            color = BudgetGreenLight.copy(alpha = 0.8f),
            label = "Income"
        ),
        ProgressLayer(
            value = totalExpensePercentage,
            color = BudgetRedLight.copy(alpha = 0.8f),
            label = "Total Expenses"
        ),
        ProgressLayer(
            value = rentPercentage,
            color = BudgetPurpleLight,
            label = "Rent"
        )
    )

    LayeredProgressBar(
        layers = layers,
        modifier = modifier,
        height = 24.dp,
        backgroundColor = Color.Gray.copy(alpha = 0.3f),
        cornerRadius = 12.dp
    )
}

@Composable
private fun BudgetStatusGauge(
    budgetStatus: ir.act.personalAccountant.domain.model.BudgetStatus,
    modifier: Modifier = Modifier
) {
    val (color, text) = when (budgetStatus) {
        ir.act.personalAccountant.domain.model.BudgetStatus.GOOD -> BudgetGreenLight to "Good"
        ir.act.personalAccountant.domain.model.BudgetStatus.MIDDLE -> BudgetOrangeLight to "Moderate"
        ir.act.personalAccountant.domain.model.BudgetStatus.RED -> BudgetRedLight to "Over Budget"
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = when (budgetStatus) {
                ir.act.personalAccountant.domain.model.BudgetStatus.GOOD -> BudgetGreenLight.copy(
                    alpha = 0.1f
                )

                ir.act.personalAccountant.domain.model.BudgetStatus.MIDDLE -> BudgetOrangeLight.copy(
                    alpha = 0.1f
                )

                ir.act.personalAccountant.domain.model.BudgetStatus.RED -> BudgetRedLight.copy(alpha = 0.1f)
            }
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun DayGroupHeader(
    dayOfMonth: Int,
    totalAmount: Double,
    currencySettings: CurrencySettings,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = YellowPrimary // Use theme yellow from FAB
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = formatDayHeaderText(dayOfMonth),
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF5D4037) // Dark brown for good contrast with yellow
                )
            }

            Text(
                text = CurrencyFormatter.formatCurrency(totalAmount, currencySettings),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5D4037) // Dark brown for good contrast with yellow
            )
        }
    }
}

private fun formatDayHeaderText(dayOfMonth: Int): String {
    val now = Calendar.getInstance()
    val today = now.get(Calendar.DAY_OF_MONTH)

    // Create a calendar for the specific day in current month/year
    val targetCalendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, dayOfMonth)
    }

    // Format day name and month
    val dayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(targetCalendar.time)
    val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(targetCalendar.time)

    return when {
        dayOfMonth == today -> "Today $dayName $monthName $dayOfMonth"
        dayOfMonth == today - 1 -> "Yesterday $dayName $monthName $dayOfMonth"
        else -> "$dayName $monthName $dayOfMonth"
    }
}