package ir.act.personalAccountant.ai.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.act.personalAccountant.ai.AIEngine
import ir.act.personalAccountant.ai.data.repository.AIRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AISettingsViewModel @Inject constructor(
    private val aiRepository: AIRepository,
    private val aiEngine: AIEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(AISettingsContract.UiState())
    val uiState: StateFlow<AISettingsContract.UiState> = _uiState.asStateFlow()

    private val _uiInteractions = MutableSharedFlow<AISettingsContract.UiInteractions>()
    val uiInteractions: SharedFlow<AISettingsContract.UiInteractions> =
        _uiInteractions.asSharedFlow()

    init {
        viewModelScope.launch {
            aiRepository.apiKey.collect { apiKey ->
                _uiState.update { it.copy(apiKey = apiKey) }
            }
        }
    }

    fun onEvent(event: AISettingsContract.Events) {
        when (event) {
            is AISettingsContract.Events.OnApiKeyChanged -> {
                _uiState.update { it.copy(apiKey = event.apiKey) }
            }

            is AISettingsContract.Events.OnSaveClicked -> {
                saveApiKey()
            }

            is AISettingsContract.Events.OnTestConnectionClicked -> {
                testConnection()
            }

            is AISettingsContract.Events.OnClearApiKeyClicked -> {
                clearApiKey()
            }

            is AISettingsContract.Events.OnErrorDismissed -> {
                _uiState.update { it.copy(error = null) }
            }

            is AISettingsContract.Events.OnTestResultDismissed -> {
                _uiState.update { it.copy(testResult = null) }
            }
        }
    }

    private fun saveApiKey() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                val apiKey = _uiState.value.apiKey.trim()

                if (apiKey.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "API key cannot be empty"
                        )
                    }
                    return@launch
                }

                if (!aiEngine.isApiKeyValid(apiKey)) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Invalid API key format. OpenAI API keys start with 'sk-'"
                        )
                    }
                    return@launch
                }

                aiRepository.saveApiKey(apiKey)
                _uiState.update { it.copy(isLoading = false) }
                _uiInteractions.emit(AISettingsContract.UiInteractions.ShowMessage("API key saved successfully"))

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to save API key: ${e.message}"
                    )
                }
            }
        }
    }

    private fun testConnection() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isTesting = true) }

                val apiKey = _uiState.value.apiKey.trim()

                if (apiKey.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isTesting = false,
                            error = "Please enter an API key first"
                        )
                    }
                    return@launch
                }

                if (!aiEngine.isApiKeyValid(apiKey)) {
                    _uiState.update {
                        it.copy(
                            isTesting = false,
                            error = "Invalid API key format"
                        )
                    }
                    return@launch
                }

                val result = aiEngine.testConnection(apiKey)

                if (result.success) {
                    _uiState.update {
                        it.copy(
                            isTesting = false,
                            testResult = result.errorMessage // This contains the success message
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isTesting = false,
                            error = result.errorMessage ?: "Connection test failed"
                        )
                    }
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isTesting = false,
                        error = "Connection test failed: ${e.message}"
                    )
                }
            }
        }
    }

    private fun clearApiKey() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                aiRepository.clearApiKey()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        apiKey = "",
                        testResult = null
                    )
                }
                _uiInteractions.emit(AISettingsContract.UiInteractions.ShowMessage("API key cleared"))
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to clear API key: ${e.message}"
                    )
                }
            }
        }
    }
}