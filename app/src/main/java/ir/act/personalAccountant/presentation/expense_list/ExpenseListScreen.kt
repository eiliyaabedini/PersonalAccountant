package ir.act.personalAccountant.presentation.expense_list

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.act.personalAccountant.core.util.CurrencyFormatter
import ir.act.personalAccountant.core.util.DateUtils
import ir.act.personalAccountant.domain.model.CurrencySettings
import ir.act.personalAccountant.domain.model.Expense
import ir.act.personalAccountant.presentation.components.DonutChart
import ir.act.personalAccountant.presentation.components.DonutChartLegend
import ir.act.personalAccountant.presentation.components.assignColorsToTagData
import ir.act.personalAccountant.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    onNavigateToExpenseEntry: () -> Unit,
    onNavigateToExpenseEdit: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToViewAllExpenses: (String?) -> Unit,
    viewModel: ExpenseListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Use default currency settings for now - will be properly integrated with ViewModel later
    val currencySettings = uiState.currencySettings
    
    // Track the timestamp of when the screen was first shown
    val screenOpenTime = remember { System.currentTimeMillis() }

    LaunchedEffect(viewModel.uiInteraction) {
        viewModel.uiInteraction.collect { interaction ->
            when (interaction) {
                ExpenseListUiInteraction.NavigateToExpenseEntry -> {
                    onNavigateToExpenseEntry()
                }
                is ExpenseListUiInteraction.NavigateToExpenseEdit -> {
                    onNavigateToExpenseEdit(interaction.expenseId)
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // White top section with status bar, header and donut chart
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(
                            bottomStart = 30.dp,
                            bottomEnd = 30.dp
                        )
                    )
                    .padding(bottom = 20.dp)
            ) {
                // Status bar space
                Spacer(modifier = Modifier.height(40.dp))

                // Month navigation header (moved to top)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
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
                                shape = androidx.compose.foundation.shape.CircleShape
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
                        text = DateUtils.formatMonthYear(uiState.currentYear, uiState.currentMonth),
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
                                shape = androidx.compose.foundation.shape.CircleShape
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

                // Main content row: Donut chart on left, total expenses on right
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 15.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left side: Donut chart
                        if (uiState.tagExpenseData.isNotEmpty() && uiState.totalExpenses > 0) {
                            val coloredTagData = assignColorsToTagData(uiState.tagExpenseData)
                            
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
                                text = CurrencyFormatter.formatCurrency(uiState.totalExpenses, currencySettings),
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    // Settings icon positioned at top-right of the entire box
                    IconButton(
                        onClick = { onNavigateToSettings() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Legend section (only show if chart exists)
                if (uiState.tagExpenseData.isNotEmpty() && uiState.totalExpenses > 0) {
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

            // Dark bottom section with expense list
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
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
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No expenses yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Add your first expense to get started",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 8.dp)
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
                                    items(
                                        items = uiState.expenses.take(10), // Show max 10 recent items
                                        key = { it.id }
                                    ) { expense ->
                                        SwipeToDeleteExpenseItem(
                                            expense = expense,
                                            isNewlyAdded = expense.timestamp > screenOpenTime,
                                            currencySettings = currencySettings,
                                            onEditClick = { viewModel.onEvent(ExpenseListEvent.EditClicked(expense)) },
                                            onDeleteClick = { viewModel.onEvent(ExpenseListEvent.DeleteClicked(expense)) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // FAB Button
        FloatingActionButton(
            onClick = { viewModel.onEvent(ExpenseListEvent.AddClicked) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(30.dp)
                .size(56.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = androidx.compose.foundation.shape.CircleShape
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Normal
            )
        }

        // Show error snackbar
        uiState.error?.let { error ->
            LaunchedEffect(error) {
                viewModel.onEvent(ExpenseListEvent.ClearError)
            }
        }
        
        // Delete confirmation dialog
        if (uiState.showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { viewModel.onEvent(ExpenseListEvent.CancelDelete) },
                title = { Text("Delete Expense") },
                text = { 
                    uiState.expenseToDelete?.let { expense ->
                        Text("Are you sure you want to delete this ${CurrencyFormatter.formatCurrency(expense.amount, currencySettings)} expense?")
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
    val highlightAlpha = remember { androidx.compose.animation.core.Animatable(if (isNewlyAdded) 0.3f else 0f) }
    val scale = remember { androidx.compose.animation.core.Animatable(if (isNewlyAdded) 0.95f else 1f) }
    
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
                            Text(
                                text = expense.tag,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = formatDate(expense.timestamp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                    
                    Text(
                        text = CurrencyFormatter.formatCurrency(expense.amount, currencySettings),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        enableDismissFromStartToEnd = false
    )
}