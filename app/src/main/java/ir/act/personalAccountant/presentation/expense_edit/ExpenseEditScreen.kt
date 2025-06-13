package ir.act.personalAccountant.presentation.expense_edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import ir.act.personalAccountant.core.util.CurrencyFormatter
import ir.act.personalAccountant.domain.model.CurrencySettings
import ir.act.personalAccountant.presentation.components.NumberKeypad
import ir.act.personalAccountant.presentation.expense_edit.ExpenseEditContract.Events
import ir.act.personalAccountant.presentation.expense_edit.ExpenseEditContract.UiInteractions
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEditScreen(
    expenseId: Long,
    viewModel: ExpenseEditViewModel = hiltViewModel(),
    uiInteractions: UiInteractions
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Use currency settings from ViewModel
    val currencySettings = uiState.currencySettings
    
    LaunchedEffect(expenseId) {
        viewModel.onEvent(Events.LoadExpense(expenseId))
    }
    
    LaunchedEffect(uiState.navigateBack) {
        if (uiState.navigateBack) {
            uiInteractions.navigateBack()
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // White header section with rounded bottom corners
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
                    
                    // Header with back arrow and title
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        IconButton(
                            onClick = { uiInteractions.navigateBack() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Text(
                            text = "Edit Expense",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                // Dark content section  
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(top = 20.dp)
                ) {
                
                // Amount section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = CurrencyFormatter.getCurrencySymbol(currencySettings),
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    
                    val amount = uiState.amount.toDoubleOrNull() ?: 0.0
                    Text(
                        text = if (uiState.amount.isEmpty()) "0" else String.format("%.2f", amount),
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                }
                // Number keypad
                NumberKeypad(
                    onNumberClick = { viewModel.onEvent(Events.NumberClicked(it)) },
                    onDecimalClick = { viewModel.onEvent(Events.DecimalClicked) },
                    onBackspaceClick = { viewModel.onEvent(Events.BackspaceClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp)
                        .padding(bottom = 16.dp)
                )

                // Date section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "DATE",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Card(
                        onClick = { viewModel.onEvent(Events.DatePickerClicked) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(uiState.selectedDate)),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select Date",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Categories section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "CATEGORY",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.availableTags) { tagWithCount ->
                            val isSelected = uiState.selectedTag == tagWithCount.tag
                            Card(
                                onClick = { viewModel.onEvent(Events.TagSelected(tagWithCount.tag)) },
                                modifier = Modifier
                                    .height(50.dp)
                                    .wrapContentWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.surface
                                ),
                                border = if (isSelected) 
                                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                else null,
                                shape = RoundedCornerShape(25.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${tagWithCount.tag} (${tagWithCount.count})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                        
                        item {
                            Card(
                                onClick = { viewModel.onEvent(Events.AddTagClicked) },
                                modifier = Modifier.size(50.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Transparent
                                ),
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(25.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
                }
            }
        }
        
        // Action buttons at bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 30.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.onEvent(Events.DeleteClicked) },
                modifier = Modifier.weight(1f).height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "DELETE",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Button(
                onClick = { viewModel.onEvent(Events.UpdateClicked) },
                modifier = Modifier.weight(2f).height(50.dp),
                enabled = uiState.amount.isNotEmpty() && uiState.selectedTag.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "UPDATE",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        // Error handling
        uiState.error?.let { error ->
            AlertDialog(
                onDismissRequest = { viewModel.onEvent(Events.ClearError) },
                title = { Text("Error") },
                text = { Text(error) },
                confirmButton = {
                    TextButton(onClick = { viewModel.onEvent(Events.ClearError) }) {
                        Text("OK")
                    }
                }
            )
        }
        
        // Delete confirmation dialog
        if (uiState.showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { viewModel.onEvent(Events.CancelDelete) },
                title = { Text("Delete Expense") },
                text = { Text("Are you sure you want to delete this expense?") },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.onEvent(Events.ConfirmDelete) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onEvent(Events.CancelDelete) }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        // Add new tag dialog
        if (uiState.showAddTagDialog) {
            AddTagDialog(
                tagName = uiState.newTagName,
                onTagNameChange = { viewModel.onEvent(Events.NewTagNameChanged(it)) },
                onConfirm = { viewModel.onEvent(Events.ConfirmNewTag) },
                onDismiss = { viewModel.onEvent(Events.DismissAddTagDialog) }
            )
        }
        
        // Date picker dialog
        if (uiState.showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = uiState.selectedDate
            )
            DatePickerDialog(
                onDateSelected = { dateMillis ->
                    dateMillis?.let { viewModel.onEvent(Events.DateSelected(it)) }
                },
                onDismiss = { viewModel.onEvent(Events.DismissDatePicker) },
                datePickerState = datePickerState
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTagDialog(
    tagName: String,
    onTagNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Add New Tag",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = tagName,
                    onValueChange = onTagNameChange,
                    label = { Text("Tag name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            onConfirm()
                        }
                    )
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = onConfirm,
                        enabled = tagName.isNotBlank()
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    datePickerState: DatePickerState
) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}