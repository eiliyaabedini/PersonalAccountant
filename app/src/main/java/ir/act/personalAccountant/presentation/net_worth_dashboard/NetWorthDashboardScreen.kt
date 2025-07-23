package ir.act.personalAccountant.presentation.net_worth_dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import ir.act.personalAccountant.core.util.CurrencyFormatter
import ir.act.personalAccountant.domain.model.CurrencySettings
import ir.act.personalAccountant.presentation.components.DailyNetWorthLineChart
import ir.act.personalAccountant.presentation.components.InlineAssetEditor
import ir.act.personalAccountant.presentation.components.SimpleTimeRangeSelector
import ir.act.personalAccountant.ui.theme.TextSecondary
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetWorthDashboardScreen(
    onNavigateToAssetEntry: () -> Unit,
    onNavigateToAssetEdit: (Long) -> Unit,
    onNavigateToNetWorthHistory: () -> Unit,
    viewModel: NetWorthDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.uiInteraction.collect { interaction ->
            when (interaction) {
                NetWorthDashboardUiInteraction.NavigateToAssetEntry -> onNavigateToAssetEntry()
                NetWorthDashboardUiInteraction.NavigateToNetWorthHistory -> onNavigateToNetWorthHistory()
                is NetWorthDashboardUiInteraction.NavigateToAssetEdit -> onNavigateToAssetEdit(
                    interaction.assetId
                )
            }
        }
    }

    if (uiState.showDeleteConfirmation) {
        DeleteConfirmationDialog(
            assetName = uiState.assetToDelete?.assetName ?: "",
            onConfirm = { viewModel.onEvent(NetWorthDashboardEvent.ConfirmDeleteAsset) },
            onDismiss = { viewModel.onEvent(NetWorthDashboardEvent.CancelDeleteAsset) }
        )
    }

    val density = LocalDensity.current

    // Track header size
    var headerSize by remember { mutableStateOf(IntSize(0, 0)) }
    val headerHeight by remember(headerSize) {
        mutableStateOf(with(density) { headerSize.height.toDp() })
    }

    // Header offset for collapsing animation
    val headerOffsetHeightPx = remember { mutableFloatStateOf(0f) }

    // Nested scroll connection for collapsing behavior
    val nestedScrollConnection = remember(headerSize) {
        object : NestedScrollConnection {
            val headerHeightPx = headerSize.height.toFloat()

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = headerOffsetHeightPx.floatValue + delta
                headerOffsetHeightPx.floatValue = newOffset.coerceIn(-headerHeightPx, 0f)
                return Offset.Zero
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
    ) {
        // Collapsible Header
        Column(
            modifier = Modifier
                .zIndex(1f)
                .offset { IntOffset(x = 0, y = headerOffsetHeightPx.floatValue.roundToInt()) }
                .fillMaxWidth()
                .onSizeChanged { headerSize = it }
        ) {
            // Top App Bar
            TopAppBar(
                title = {
                    Text(
                        text = "Net Worth Dashboard",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            )

            // Chart Card
            NetWorthChartCard(
                currentNetWorth = uiState.currentNetWorth,
                currencySettings = uiState.currencySettings,
                netWorthHistory = uiState.netWorthHistory,
                selectedTimeRange = uiState.selectedTimeRange,
                onTimeRangeChanged = { range ->
                    viewModel.onEvent(NetWorthDashboardEvent.TimeRangeChanged(range))
                },
                onGraphClicked = { viewModel.onEvent(NetWorthDashboardEvent.GraphClicked) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Scrollable Content
        LazyColumn(
            contentPadding = PaddingValues(top = headerHeight + 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                // Action Button
                Button(
                    onClick = { viewModel.onEvent(NetWorthDashboardEvent.AddAssetClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Asset")
                }
            }

            item {
                // Assets Section Header
                Text(
                    text = "Your Assets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Assets List or Loading/Empty State
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.assetSnapshots.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        EmptyAssetsState(
                            onAddAssetClicked = { viewModel.onEvent(NetWorthDashboardEvent.AddAssetClicked) }
                        )
                    }
                }
            } else {
                items(uiState.assetSnapshots) { assetSnapshot ->
                    InlineAssetEditor(
                        assetSnapshot = assetSnapshot,
                        currencySettings = uiState.currencySettings,
                        isEditing = uiState.editingAssetId == assetSnapshot.assetId,
                        editingAmount = uiState.editingAmount,
                        editingQuantity = uiState.editingQuantity,
                        onStartEditing = {
                            viewModel.onEvent(
                                NetWorthDashboardEvent.StartEditingAsset(
                                    assetSnapshot
                                )
                            )
                        },
                        onAmountChanged = { amount ->
                            viewModel.onEvent(NetWorthDashboardEvent.AmountChanged(amount))
                        },
                        onQuantityChanged = { quantity ->
                            viewModel.onEvent(NetWorthDashboardEvent.QuantityChanged(quantity))
                        },
                        onSaveSnapshot = {
                            viewModel.onEvent(NetWorthDashboardEvent.SaveAssetSnapshot)
                        },
                        onCancelEditing = {
                            viewModel.onEvent(NetWorthDashboardEvent.CancelEditing)
                        },
                        onEditName = {
                            viewModel.onEvent(NetWorthDashboardEvent.EditAssetName(assetSnapshot))
                        },
                        onDeleteAsset = {
                            viewModel.onEvent(
                                NetWorthDashboardEvent.DeleteAssetClicked(
                                    assetSnapshot
                                )
                            )
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show error snackbar or handle error
            viewModel.onEvent(NetWorthDashboardEvent.ClearError)
        }
    }
}

@Composable
private fun NetWorthChartCard(
    currentNetWorth: Double,
    currencySettings: CurrencySettings,
    netWorthHistory: List<ir.act.personalAccountant.domain.usecase.DailyNetWorth>,
    selectedTimeRange: DashboardTimeRange,
    onTimeRangeChanged: (DashboardTimeRange) -> Unit,
    onGraphClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Time Range Selector
            SimpleTimeRangeSelector(
                selectedTimeRange = selectedTimeRange,
                timeRanges = DashboardTimeRange.entries.toTypedArray(),
                getDisplayName = { it.displayName },
                onTimeRangeSelected = onTimeRangeChanged
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Net Worth Line Graph (if data exists)
            if (netWorthHistory.isNotEmpty()) {
                DailyNetWorthLineChart(
                    dailyNetWorth = netWorthHistory,
                    currencySettings = currencySettings,
                    height = 120.dp,
                    isClickable = true,
                    onClick = onGraphClicked
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Current Net Worth Value (centered below chart)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Current Net Worth",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = CurrencyFormatter.formatCurrency(currentNetWorth, currencySettings),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Automatically calculated from latest asset values",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


@Composable
private fun EmptyAssetsState(
    onAddAssetClicked: () -> Unit
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
            text = "No assets yet",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Start tracking your net worth by adding your first asset",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

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