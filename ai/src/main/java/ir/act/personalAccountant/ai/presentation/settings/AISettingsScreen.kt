package ir.act.personalAccountant.ai.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AISettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        viewModel.uiInteractions.collect { interaction ->
            when (interaction) {
                is AISettingsContract.UiInteractions.NavigateBack -> onNavigateBack()
                is AISettingsContract.UiInteractions.ShowMessage -> {
                    snackbarHostState.showSnackbar(interaction.message)
                }
            }
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.onEvent(AISettingsContract.Events.OnErrorDismissed)
        }
    }

    LaunchedEffect(uiState.testResult) {
        uiState.testResult?.let { result ->
            snackbarHostState.showSnackbar(result)
            viewModel.onEvent(AISettingsContract.Events.OnTestResultDismissed)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "OpenAI Configuration",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Configure your OpenAI API key to enable receipt analysis features.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // API Key Input
            APIKeyInputSection(
                apiKey = uiState.apiKey,
                onApiKeyChanged = { viewModel.onEvent(AISettingsContract.Events.OnApiKeyChanged(it)) },
                focusRequester = focusRequester,
                focusManager = focusManager
            )

            // Action Buttons
            ActionButtonsSection(
                apiKey = uiState.apiKey,
                isLoading = uiState.isLoading,
                isTesting = uiState.isTesting,
                onSaveClicked = { viewModel.onEvent(AISettingsContract.Events.OnSaveClicked) },
                onTestClicked = { viewModel.onEvent(AISettingsContract.Events.OnTestConnectionClicked) },
                onClearClicked = { viewModel.onEvent(AISettingsContract.Events.OnClearApiKeyClicked) }
            )

            // Info Section
            InfoSection()
        }
    }
}

@Composable
private fun APIKeyInputSection(
    apiKey: String,
    onApiKeyChanged: (String) -> Unit,
    focusRequester: FocusRequester,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "OpenAI API Key",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        OutlinedTextField(
            value = apiKey,
            onValueChange = onApiKeyChanged,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            label = { Text("Enter your OpenAI API key") },
            placeholder = { Text("sk-...") },
            visualTransformation = if (isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                Row {
                    if (apiKey.isNotEmpty()) {
                        IconButton(
                            onClick = { onApiKeyChanged("") }
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                    IconButton(
                        onClick = { isPasswordVisible = !isPasswordVisible }
                    ) {
                        Icon(
                            if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            singleLine = true
        )
    }
}

@Composable
private fun ActionButtonsSection(
    apiKey: String,
    isLoading: Boolean,
    isTesting: Boolean,
    onSaveClicked: () -> Unit,
    onTestClicked: () -> Unit,
    onClearClicked: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Save Button
        Button(
            onClick = onSaveClicked,
            modifier = Modifier.fillMaxWidth(),
            enabled = apiKey.isNotEmpty() && !isLoading && !isTesting
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Save API Key")
        }

        // Test Connection Button
        OutlinedButton(
            onClick = onTestClicked,
            modifier = Modifier.fillMaxWidth(),
            enabled = apiKey.isNotEmpty() && !isLoading && !isTesting
        ) {
            if (isTesting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Test Connection")
        }

        // Clear Button
        if (apiKey.isNotEmpty()) {
            TextButton(
                onClick = onClearClicked,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && !isTesting
            ) {
                Text("Clear API Key")
            }
        }
    }
}

@Composable
private fun InfoSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "How to get your OpenAI API Key:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "1. Visit https://platform.openai.com/api-keys",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "2. Sign in to your OpenAI account",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "3. Click 'Create new secret key'",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "4. Copy the key and paste it here",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Note: Your API key is stored securely on your device and is only used for OpenAI API calls.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}