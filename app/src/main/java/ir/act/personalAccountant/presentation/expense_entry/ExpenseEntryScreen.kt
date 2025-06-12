package ir.act.personalAccountant.presentation.expense_entry

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import ir.act.personalAccountant.presentation.components.DonutChart
import ir.act.personalAccountant.presentation.components.NumberKeypad
import ir.act.personalAccountant.presentation.components.assignColorsToTagData
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEntryScreen(
    onNavigateToExpenseList: () -> Unit,
    viewModel: ExpenseEntryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel.uiInteraction) {
        viewModel.uiInteraction.collect { interaction ->
            when (interaction) {
                is ExpenseEntryUiInteraction.NavigateToExpenseList -> {
                    onNavigateToExpenseList()
                }
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show error snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(error)
            viewModel.onEvent(ExpenseEntryEvent.ClearError)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header with back arrow and title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 50.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                IconButton(
                    onClick = onNavigateToExpenseList,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Text(
                    text = "Add Expense",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            // Amount section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                
                Text(
                    text = if (uiState.currentAmount.isEmpty()) "0" else uiState.currentAmount,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 30.dp)
                )
            }
            // Number keypad
            NumberKeypad(
                onNumberClick = { viewModel.onEvent(ExpenseEntryEvent.NumberClicked(it)) },
                onDecimalClick = { viewModel.onEvent(ExpenseEntryEvent.DecimalClicked) },
                onBackspaceClick = { viewModel.onEvent(ExpenseEntryEvent.BackspaceClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
                    .padding(bottom = 30.dp)
            )

            // Categories section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 30.dp)
            ) {
                Text(
                    text = "CATEGORY",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 15.dp)
                )
                
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.availableTags) { tagWithCount ->
                        val isSelected = uiState.selectedTag == tagWithCount.tag
                        Card(
                            onClick = { 
                                viewModel.onEvent(ExpenseEntryEvent.TagSelected(tagWithCount.tag))
                                // Auto-save when both amount and tag are present
                                if (uiState.currentAmount.isNotEmpty() && uiState.currentAmount != "0") {
                                    viewModel.onEvent(ExpenseEntryEvent.AddClicked)
                                }
                            },
                            modifier = Modifier
                                .height(50.dp)
                                .wrapContentWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.surface
                            ),
                            border = if (isSelected) 
                                androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
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
                            onClick = { viewModel.onEvent(ExpenseEntryEvent.AddTagClicked) },
                            modifier = Modifier
                                .size(50.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Transparent
                            ),
                            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)),
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
            
            // Loading indicator
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Show error snackbar
        uiState.error?.let { error ->
            LaunchedEffect(error) {
                // This will be handled by the parent or show a snackbar
            }
        }
        
        // Add new tag dialog
        if (uiState.showAddTagDialog) {
            AddTagDialog(
                tagName = uiState.newTagName,
                onTagNameChange = { viewModel.onEvent(ExpenseEntryEvent.NewTagNameChanged(it)) },
                onConfirm = { viewModel.onEvent(ExpenseEntryEvent.ConfirmNewTag) },
                onDismiss = { viewModel.onEvent(ExpenseEntryEvent.DismissAddTagDialog) }
            )
        }
    }
}

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(amount)
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
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            onConfirm()
                        },
                        enabled = tagName.trim().isNotEmpty()
                    ) {
                        Text("Add Tag")
                    }
                }
            }
        }
    }
}