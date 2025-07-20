package ir.act.personalAccountant.presentation.view_all_expenses

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.act.personalAccountant.core.util.CurrencyFormatter
import ir.act.personalAccountant.core.util.DateUtils
import ir.act.personalAccountant.domain.model.CurrencySettings
import ir.act.personalAccountant.domain.model.Expense
import ir.act.personalAccountant.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewAllExpensesScreen(
    onNavigateToExpenseEdit: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ViewAllExpensesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Track the timestamp of when the screen was first shown
    val screenOpenTime = remember { System.currentTimeMillis() }

    LaunchedEffect(viewModel.uiInteraction) {
        viewModel.uiInteraction.collect { interaction ->
            when (interaction) {
                is ViewAllExpensesUiInteraction.NavigateToExpenseEdit -> {
                    onNavigateToExpenseEdit(interaction.expenseId)
                }
                ViewAllExpensesUiInteraction.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header with back button
        TopAppBar(
            title = {
                Text(
                    text = if (uiState.filterByTag != null) {
                        "Expenses: ${uiState.filterByTag}"
                    } else {
                        "All Expenses"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.onEvent(ViewAllExpensesEvent.BackClicked) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
        
        // Month display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = DateUtils.formatMonthYear(uiState.currentYear, uiState.currentMonth),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                if (uiState.filterByTag != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filtered by: ${uiState.filterByTag}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Clear ✕",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.clickable {
                                viewModel.onEvent(ViewAllExpensesEvent.ClearFilter)
                            }
                        )
                    }
                }
            }
        }

        // Content
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
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (uiState.filterByTag != null) {
                                "No expenses found for \"${uiState.filterByTag}\""
                            } else {
                                "No expenses for this month"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (uiState.filterByTag != null) {
                                "Try a different filter or add some expenses"
                            } else {
                                "Add your first expense to get started"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(
                        items = uiState.expenses,
                        key = { it.id }
                    ) { expense ->
                        SwipeToDeleteExpenseItem(
                            expense = expense,
                            isNewlyAdded = expense.timestamp > screenOpenTime,
                            currencySettings = uiState.currencySettings,
                            onEditClick = { viewModel.onEvent(ViewAllExpensesEvent.EditClicked(expense)) },
                            onDeleteClick = { viewModel.onEvent(ViewAllExpensesEvent.DeleteClicked(expense)) }
                        )
                    }
                }
            }
        }
    }

    // Show error snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            viewModel.onEvent(ViewAllExpensesEvent.ClearError)
        }
    }
    
    // Delete confirmation dialog
    if (uiState.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(ViewAllExpensesEvent.CancelDelete) },
            title = { Text("Delete Expense") },
            text = { 
                uiState.expenseToDelete?.let { expense ->
                    Text("Are you sure you want to delete this ${CurrencyFormatter.formatCurrency(expense.amount, uiState.currencySettings)} expense?")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.onEvent(ViewAllExpensesEvent.ConfirmDelete) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(ViewAllExpensesEvent.CancelDelete) }) {
                    Text("Cancel")
                }
            }
        )
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                // Highlight overlay
                if (highlightAlpha.value > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = highlightAlpha.value)
                            )
                    )
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEditClick() }
                        .padding(16.dp),
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
                                .size(48.dp)
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
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = expense.tag,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

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
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
                            fontWeight = FontWeight.Bold,
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