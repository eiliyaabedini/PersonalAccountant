package ir.act.personalAccountant.presentation.googlesheets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.act.personalAccountant.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleSheetsScreen(
    viewModel: GoogleSheetsViewModel,
    onSignInClick: () -> Unit,
    onNavigateToSyncProgress: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Clear messages after showing them
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        if (uiState.successMessage != null || uiState.errorMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
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
                        onClick = onNavigateBack
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Text(
                        text = "Google Sheets Integration",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()

        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Connection Status
                ConnectionStatusCard(
                    isConnected = uiState.isConnected,
                    account = uiState.account,
                    spreadsheetId = uiState.spreadsheetId,
                    lastSyncTimestamp = uiState.lastSyncTimestamp
                )

                // Error/Success Messages
                uiState.errorMessage?.let { message ->
                    MessageCard(
                        message = message,
                        isError = true
                    )
                }

                uiState.successMessage?.let { message ->
                    MessageCard(
                        message = message,
                        isError = false
                    )
                }

                // Action Buttons
                if (uiState.isConnected) {
                    ConnectedContent(
                        uiState = uiState,
                        onSyncClick = { viewModel.syncExpenses() },
                        onAdvancedSyncClick = onNavigateToSyncProgress,
                        onDisconnectClick = { viewModel.disconnect() }
                    )
                } else {
                    DisconnectedContent(
                        isLoading = uiState.isLoading,
                        onSignInClick = onSignInClick,
                        onRefreshClick = { viewModel.refreshConnectionStatus() }
                    )
                }

                // Information Section
                InformationSection()
            }
        }
    }
}

@Composable
fun ConnectionStatusCard(
    isConnected: Boolean,
    account: com.google.android.gms.auth.api.signin.GoogleSignInAccount?,
    spreadsheetId: String?,
    lastSyncTimestamp: Long? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isConnected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
                Text(
                    text = if (isConnected) "Connected" else "Not Connected",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isConnected && account != null) {
                Text(
                    text = "Account: ${account.email}",
                    style = MaterialTheme.typography.bodyMedium
                )

                if (spreadsheetId != null) {
                    Text(
                        text = "Spreadsheet ID: ${spreadsheetId.take(20)}...",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (lastSyncTimestamp != null) {
                    val dateFormat =
                        SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
                    val formattedDate = dateFormat.format(Date(lastSyncTimestamp))
                    Text(
                        text = "Last sync: $formattedDate",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun MessageCard(
    message: String,
    isError: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isError)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (isError) Icons.Default.Warning else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (isError)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.secondary
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ConnectedContent(
    uiState: GoogleSheetsUiState,
    onSyncClick: () -> Unit,
    onAdvancedSyncClick: () -> Unit,
    onDisconnectClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onSyncClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSyncing
        ) {
            if (uiState.isSyncing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Syncing...")
            } else {
                Icon(
                    painter = painterResource(R.drawable.outline_sync_24),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Quick Sync")
            }
        }
//
//        OutlinedButton(
//            onClick = onAdvancedSyncClick,
//            modifier = Modifier.fillMaxWidth(),
//            enabled = !uiState.isSyncing
//        ) {
//            Text("Advanced Sync with Progress")
//        }

        OutlinedButton(
            onClick = onDisconnectClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Disconnect")
        }
    }
}

@Composable
fun DisconnectedContent(
    isLoading: Boolean,
    onSignInClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Connect your Google account to sync your expenses to Google Sheets",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onSignInClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Connecting...")
            } else {
                Text("Connect Google Sheets")
            }
        }

        OutlinedButton(
            onClick = onRefreshClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Refresh Connection Status")
        }
    }
}

@Composable
fun InformationSection() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "How it works",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = """
                • Creates a personal Google Sheets spreadsheet in your Google Drive
                • Organizes expenses by month in separate sheets
                • Automatically syncs your expense data
                • Includes expense images via Google Drive links
                • Data remains private in your Google account
                """.trimIndent(),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}