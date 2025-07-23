package ir.act.personalAccountant.presentation.asset_entry

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.act.personalAccountant.core.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetEntryScreen(
    onNavigateBack: () -> Unit,
    viewModel: AssetEntryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.onEvent(AssetEntryEvent.ImageSelected(it))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiInteraction.collect { interaction ->
            when (interaction) {
                is AssetEntryUiInteraction.NavigateBack -> onNavigateBack()
                AssetEntryUiInteraction.OpenImagePicker -> {
                    imagePickerLauncher.launch("image/*")
                }
            }
        }
    }

    if (uiState.showAddTypeDialog) {
        AddTypeDialog(
            typeName = uiState.newTypeName,
            onTypeNameChanged = { viewModel.onEvent(AssetEntryEvent.NewTypeNameChanged(it)) },
            onConfirm = { viewModel.onEvent(AssetEntryEvent.ConfirmNewType) },
            onDismiss = { viewModel.onEvent(AssetEntryEvent.DismissAddTypeDialog) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Edit Asset" else "Add Asset") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.onEvent(AssetEntryEvent.SaveAsset) },
                        enabled = uiState.assetName.isNotBlank() &&
                                uiState.assetType.isNotBlank() &&
                                uiState.currentAmount.isNotBlank() &&
                                !uiState.isLoading
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // AI Image Analysis Button
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "AI Asset Analysis",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Scan trading app screenshot to auto-fill asset details",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }

                            TextButton(
                                onClick = {
                                    viewModel.onEvent(AssetEntryEvent.AnalyzeImageClicked)
                                },
                                enabled = !uiState.isAnalyzingImage
                            ) {
                                if (uiState.isAnalyzingImage) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .padding(end = 4.dp)
                                            .width(16.dp)
                                            .height(16.dp)
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                }
                                Text(if (uiState.isAnalyzingImage) "Analyzing..." else "Scan Image")
                            }
                        }

                        // Show AI analysis message if available
                        uiState.aiAnalysisMessage?.let { message ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(
                                    onClick = { viewModel.onEvent(AssetEntryEvent.ClearAiAnalysisMessage) }
                                ) {
                                    Text("Dismiss", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }

            item {
                // Asset Name
                OutlinedTextField(
                    value = uiState.assetName,
                    onValueChange = { viewModel.onEvent(AssetEntryEvent.AssetNameChanged(it)) },
                    label = { Text("Asset Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g., Cash, House, Car") }
                )
            }

            item {
                // Asset Type
                AssetTypeDropdown(
                    selectedType = uiState.assetType,
                    availableTypes = uiState.availableAssetTypes,
                    onTypeSelected = { viewModel.onEvent(AssetEntryEvent.AssetTypeChanged(it)) },
                    onAddNewType = { viewModel.onEvent(AssetEntryEvent.AddTypeClicked) }
                )
            }

            item {
                // Amount Input
                OutlinedTextField(
                    value = uiState.currentAmount,
                    onValueChange = { viewModel.onEvent(AssetEntryEvent.AmountChanged(it)) },
                    label = { Text("Amount per unit") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    placeholder = { Text("0.00") }
                )
            }


            item {
                // Quantity
                OutlinedTextField(
                    value = uiState.quantity,
                    onValueChange = { viewModel.onEvent(AssetEntryEvent.QuantityChanged(it)) },
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    placeholder = { Text("1.0") }
                )
            }

            item {
                // Notes
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = { viewModel.onEvent(AssetEntryEvent.NotesChanged(it)) },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    placeholder = { Text("Additional details about this asset") }
                )
            }

            item {
                // Total Value Display
                if (uiState.quantity.isNotBlank() && uiState.currentAmount.isNotBlank()) {
                    val quantity = uiState.quantity.toDoubleOrNull() ?: 1.0
                    val amount = uiState.currentAmount.toDoubleOrNull() ?: 0.0
                    val totalValue = quantity * amount

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Total Value",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = CurrencyFormatter.formatCurrency(
                                    totalValue,
                                    uiState.currencySettings
                                ),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${quantity} × ${
                                    CurrencyFormatter.formatCurrency(
                                        amount,
                                        uiState.currencySettings
                                    )
                                }",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Note: NumberPad component removed - amount entry is handled via the outlined text field above

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show error snackbar
            viewModel.onEvent(AssetEntryEvent.ClearError)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssetTypeDropdown(
    selectedType: String,
    availableTypes: List<String>,
    onTypeSelected: (String) -> Unit,
    onAddNewType: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedType,
            onValueChange = onTypeSelected,
            label = { Text("Asset Type") },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            placeholder = { Text("e.g., Cash, Investment, Property") }
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableTypes.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type) },
                    onClick = {
                        onTypeSelected(type)
                        expanded = false
                    }
                )
            }

            if (availableTypes.isNotEmpty()) {
                Divider()
            }

            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add New Type")
                    }
                },
                onClick = {
                    onAddNewType()
                    expanded = false
                }
            )
        }
    }
}


@Composable
private fun AddTypeDialog(
    typeName: String,
    onTypeNameChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Asset Type") },
        text = {
            Column {
                Text("Enter a name for the new asset type:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = typeName,
                    onValueChange = onTypeNameChanged,
                    label = { Text("Type Name") },
                    singleLine = true,
                    placeholder = { Text("e.g., Cryptocurrency, Jewelry") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = typeName.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}