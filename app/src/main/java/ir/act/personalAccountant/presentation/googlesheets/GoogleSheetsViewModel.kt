package ir.act.personalAccountant.presentation.googlesheets

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.act.personalAccountant.data.repository.GoogleAuthRepository
import ir.act.personalAccountant.domain.usecase.GoogleSheetsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoogleSheetsViewModel @Inject constructor(
    private val googleAuthRepository: GoogleAuthRepository,
    private val googleSheetsUseCase: GoogleSheetsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoogleSheetsUiState())
    val uiState: StateFlow<GoogleSheetsUiState> = _uiState.asStateFlow()

    init {
        checkExistingAuth()
    }

    private fun checkExistingAuth() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                val isConnected = googleSheetsUseCase.isConnectedToGoogleSheets()
                val account = googleAuthRepository.getLastSignedInAccount()
                val spreadsheetId = googleSheetsUseCase.getSpreadsheetId()
                val lastSyncTimestamp = googleSheetsUseCase.getLastSyncTimestamp()

                _uiState.value = _uiState.value.copy(
                    isConnected = isConnected,
                    account = account,
                    spreadsheetId = spreadsheetId,
                    lastSyncTimestamp = lastSyncTimestamp,
                    isLoading = false
                )
            } catch (e: Exception) {
                android.util.Log.e("GoogleSheetsViewModel", "Error checking connection status", e)
                _uiState.value = _uiState.value.copy(
                    isConnected = false,
                    account = null,
                    spreadsheetId = null,
                    isLoading = false,
                    errorMessage = "Failed to check connection status: ${e.message}"
                )
            }
        }
    }

    fun getSignInIntent(): Intent {
        return googleAuthRepository.getSignInIntent()
    }

    fun handleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val account = googleAuthRepository.handleSignInResult(data)
                if (account != null) {
                    // Setup user's spreadsheet
                    val result = googleSheetsUseCase.setupUserSpreadsheet(
                        account.displayName ?: "User"
                    )

                    if (result.isSuccess) {
                        val spreadsheetId = result.getOrNull()
                        _uiState.value = _uiState.value.copy(
                            isConnected = true,
                            account = account,
                            spreadsheetId = spreadsheetId,
                            isLoading = false,
                            successMessage = "Successfully connected to Google Sheets!"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Failed to setup spreadsheet: ${result.exceptionOrNull()?.message}"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Sign-in failed"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Authentication error: ${e.message}"
                )
            }
        }
    }

    fun syncExpenses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true, errorMessage = null)

            val result = googleSheetsUseCase.syncAllExpenses()

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    successMessage = "Expenses synced successfully!"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    errorMessage = "Sync failed: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            try {
                googleAuthRepository.signOut()
                _uiState.value = GoogleSheetsUiState()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to disconnect: ${e.message}"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    fun refreshConnectionStatus() {
        checkExistingAuth()
    }
}

data class GoogleSheetsUiState(
    val isConnected: Boolean = false,
    val account: GoogleSignInAccount? = null,
    val spreadsheetId: String? = null,
    val lastSyncTimestamp: Long? = null,
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)