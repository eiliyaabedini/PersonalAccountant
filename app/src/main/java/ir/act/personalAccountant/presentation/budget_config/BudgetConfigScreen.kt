package ir.act.personalAccountant.presentation.budget_config

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun BudgetConfigScreen(
    onNavigateBack: () -> Unit,
    viewModel: BudgetConfigViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.uiInteraction.collect { interaction ->
            when (interaction) {
                is BudgetConfigContract.UiInteraction.NavigateBack -> {
                    onNavigateBack()
                }
                is BudgetConfigContract.UiInteraction.ShowSuccess -> {
                    onNavigateBack()
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)
                )
                .padding(top = 40.dp, bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.handleEvent(BudgetConfigContract.Event.OnNavigateBack) }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Text(
                    text = "Budget Configuration",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title
            Text(
                text = "Set up your budget",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Description
            Text(
                text = "Configure your budget settings to start tracking your financial progress",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Net Salary Input
            OutlinedTextField(
                value = uiState.netSalaryInput,
                onValueChange = { 
                    viewModel.handleEvent(BudgetConfigContract.Event.OnNetSalaryChanged(it))
                },
                label = { Text("Net Monthly Salary *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.errorMessage != null
            )

            // Monthly Rent Input
            OutlinedTextField(
                value = uiState.rentInput,
                onValueChange = { 
                    viewModel.handleEvent(BudgetConfigContract.Event.OnRentChanged(it))
                },
                label = { Text("Monthly Rent (Optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    Text(
                        text = "Leave empty if you don't pay rent",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )

            // Saving Goal Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Saving Goal Header
                    Text(
                        text = "Monthly Saving Goal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    // 50/30/20 Rule Explanation
                    val explanationText = if (uiState.recommendedSavingGoal > 0) {
                        "Based on the 50/30/20 rule, we recommend saving 20% of your income (${
                            String.format(
                                "%.2f",
                                uiState.recommendedSavingGoal
                            )
                        }). You can adjust this amount below:"
                    } else {
                        "Based on the 50/30/20 rule, we recommend saving 20% of your income. Enter your salary above to see the suggested amount."
                    }
                    Text(
                        text = explanationText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Recommended Amount with Button
                    if (uiState.recommendedSavingGoal > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Suggested (20%): ${
                                    String.format(
                                        "%.2f",
                                        uiState.recommendedSavingGoal
                                    )
                                }",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            TextButton(
                                onClick = {
                                    viewModel.handleEvent(BudgetConfigContract.Event.OnUseRecommendedSavingGoal)
                                }
                            ) {
                                Text("Use This")
                            }
                        }
                    }

                    // Saving Goal Input
                    OutlinedTextField(
                        value = uiState.savingGoalInput,
                        onValueChange = {
                            viewModel.handleEvent(BudgetConfigContract.Event.OnSavingGoalChanged(it))
                        },
                        label = { Text("Monthly Saving Goal (Optional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            Text(
                                text = "Leave empty to use automatic 20% calculation",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    )
                }
            }

            // Error message
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Save Button
            Button(
                onClick = { viewModel.handleEvent(BudgetConfigContract.Event.OnSaveClicked) },
                enabled = uiState.isInputValid && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Save Configuration",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    // Error Dialog
    if (uiState.errorMessage != null) {
        AlertDialog(
            onDismissRequest = {
                viewModel.handleEvent(BudgetConfigContract.Event.OnDismissError)
            },
            title = { Text("Error") },
            text = { Text(uiState.errorMessage!!) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.handleEvent(BudgetConfigContract.Event.OnDismissError)
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}