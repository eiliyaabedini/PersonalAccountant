package ir.act.personalAccountant.presentation.asset_list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.act.personalAccountant.core.util.CurrencyFormatter
import ir.act.personalAccountant.domain.model.Asset
import ir.act.personalAccountant.domain.model.CurrencySettings
import ir.act.personalAccountant.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetListScreen(
    onNavigateToAssetEntry: () -> Unit,
    onNavigateToAssetEdit: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AssetListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.uiInteraction.collect { interaction ->
            when (interaction) {
                AssetListUiInteraction.NavigateToAssetEntry -> onNavigateToAssetEntry()
                is AssetListUiInteraction.NavigateToAssetEdit -> onNavigateToAssetEdit(interaction.assetId)
            }
        }
    }

    if (uiState.showDeleteConfirmation) {
        DeleteConfirmationDialog(
            assetName = uiState.assetToDelete?.name ?: "",
            onConfirm = { viewModel.onEvent(AssetListEvent.ConfirmDeleteAsset) },
            onDismiss = { viewModel.onEvent(AssetListEvent.CancelDeleteAsset) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assets") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onEvent(AssetListEvent.AddAssetClicked) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Asset")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Total Assets Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Total Assets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = CurrencyFormatter.formatCurrency(
                            uiState.totalAssets,
                            uiState.currencySettings
                        ),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Asset Type Filter
            if (uiState.assetTypes.isNotEmpty()) {
                Text(
                    text = "Filter by Type",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedAssetType == null,
                            onClick = { viewModel.onEvent(AssetListEvent.FilterByTypeClicked(null)) },
                            label = { Text("All") }
                        )
                    }

                    items(uiState.assetTypes) { type ->
                        FilterChip(
                            selected = uiState.selectedAssetType == type,
                            onClick = { viewModel.onEvent(AssetListEvent.FilterByTypeClicked(type)) },
                            label = { Text(type) }
                        )
                    }
                }
            }

            // Assets List
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.assets.isEmpty()) {
                EmptyAssetsState(
                    selectedType = uiState.selectedAssetType,
                    onAddAssetClicked = { viewModel.onEvent(AssetListEvent.AddAssetClicked) },
                    onClearFilter = { viewModel.onEvent(AssetListEvent.FilterByTypeClicked(null)) }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.assets) { asset ->
                        AssetItem(
                            asset = asset,
                            currencySettings = uiState.currencySettings,
                            onEditClicked = {
                                viewModel.onEvent(
                                    AssetListEvent.EditAssetClicked(
                                        asset
                                    )
                                )
                            },
                            onDeleteClicked = {
                                viewModel.onEvent(
                                    AssetListEvent.DeleteAssetClicked(
                                        asset
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show error snackbar or handle error
            viewModel.onEvent(AssetListEvent.ClearError)
        }
    }
}

@Composable
private fun AssetItem(
    asset: Asset,
    currencySettings: CurrencySettings,
    onEditClicked: () -> Unit,
    onDeleteClicked: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = asset.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = asset.type,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyFormatter.formatCurrency(asset.totalValue, currencySettings),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(onClick = onEditClicked) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit asset",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onDeleteClicked) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete asset",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            if (asset.quantity != 1.0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Quantity: ${asset.quantity} × ${
                        CurrencyFormatter.formatCurrency(
                            asset.amount,
                            currencySettings
                        )
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            asset.notes?.let { notes ->
                if (notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Notes: $notes",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyAssetsState(
    selectedType: String?,
    onAddAssetClicked: () -> Unit,
    onClearFilter: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Star,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (selectedType != null) "No $selectedType assets found" else "No assets yet",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        Text(
            text = if (selectedType != null) {
                "Try adding a $selectedType asset or clear the filter to view all assets"
            } else {
                "Start building your net worth by adding your first asset"
            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = onAddAssetClicked) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Asset")
            }

            if (selectedType != null) {
                OutlinedButton(onClick = onClearFilter) {
                    Text("Clear Filter")
                }
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    assetName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Delete Asset")
        },
        text = {
            Text("Are you sure you want to delete '$assetName'? This action cannot be undone.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}