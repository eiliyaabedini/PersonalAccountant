package ir.act.personalAccountant.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.act.personalAccountant.data.local.NotificationPreferences
import ir.act.personalAccountant.data.repository.FirebaseAuthRepositoryImpl
import ir.act.personalAccountant.domain.model.AuthResult
import ir.act.personalAccountant.domain.sync.SyncResult
import ir.act.personalAccountant.domain.usecase.DownloadUserExpensesUseCase
import ir.act.personalAccountant.domain.usecase.GetCurrentUserUseCase
import ir.act.personalAccountant.domain.usecase.SignOutUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val firebaseAuthRepository: FirebaseAuthRepositoryImpl,
    private val downloadUserExpensesUseCase: DownloadUserExpensesUseCase,
    private val notificationPreferences: NotificationPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginContract.UiState())
    val uiState: StateFlow<LoginContract.UiState> = _uiState.asStateFlow()

    private val _uiInteraction = MutableSharedFlow<LoginContract.UiInteraction>()
    val uiInteraction: SharedFlow<LoginContract.UiInteraction> = _uiInteraction.asSharedFlow()

    init {
        observeCurrentUser()
        checkIfUserIsSignedIn()
    }

    fun handleEvent(event: LoginContract.Event) {
        when (event) {
            is LoginContract.Event.SignInWithGoogle -> {
                startGoogleSignIn()
            }

            is LoginContract.Event.GoogleSignInResult -> {
                handleGoogleSignInResult(event.idToken)
            }

            is LoginContract.Event.SignOut -> {
                signOut()
            }

            is LoginContract.Event.ClearError -> {
                _uiState.value = _uiState.value.copy(errorMessage = null)
            }

            is LoginContract.Event.SkipLogin -> {
                skipLogin()
            }

            is LoginContract.Event.RetryDataRestore -> {
                retryDataRestore()
            }

            is LoginContract.Event.SkipDataRestore -> {
                skipDataRestore()
            }
        }
    }

    private fun observeCurrentUser() {
        getCurrentUserUseCase()
            .onEach { user ->
                _uiState.value = _uiState.value.copy(
                    user = user,
                    isSignedIn = user != null
                )
                // Only navigate automatically if user is signed in and not in data restoration flow
                if (user != null && !_uiState.value.isRestoringData) {
                    _uiInteraction.emit(LoginContract.UiInteraction.NavigateToMain)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun checkIfUserIsSignedIn() {
        val isSignedIn = getCurrentUserUseCase.isUserSignedIn()
        if (isSignedIn) {
            _uiInteraction.tryEmit(LoginContract.UiInteraction.NavigateToMain)
        }
    }

    private fun startGoogleSignIn() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            _uiInteraction.emit(LoginContract.UiInteraction.StartGoogleSignIn)
        }
    }

    private fun handleGoogleSignInResult(idToken: String?) {
        viewModelScope.launch {
            if (idToken != null) {
                when (val result = firebaseAuthRepository.signInWithGoogleCredential(idToken)) {
                    is AuthResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            user = result.user,
                            isSignedIn = true,
                            errorMessage = null
                        )
                        // Start data restoration flow after successful login
                        startDataRestoration()
                    }

                    is AuthResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                        _uiInteraction.emit(LoginContract.UiInteraction.ShowError(result.message))
                    }

                    else -> {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Google Sign-In was cancelled"
                )
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = signOutUseCase()) {
                is AuthResult.SignedOut -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = null,
                        isSignedIn = false,
                        errorMessage = null
                    )
                }

                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }

                else -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    private fun skipLogin() {
        viewModelScope.launch {
            _uiInteraction.emit(LoginContract.UiInteraction.NavigateToMain)
        }
    }

    private fun startDataRestoration() {
        viewModelScope.launch {
            // Automatically enable cloud sync when user logs in
            notificationPreferences.isCloudSyncEnabled = true

            _uiState.value = _uiState.value.copy(
                isRestoringData = true,
                dataRestoreProgress = "Connecting to cloud...",
                dataRestoreError = null
            )

            performDataRestoration()
        }
    }

    private fun performDataRestoration() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    dataRestoreProgress = "Downloading your data..."
                )

                when (val result = downloadUserExpensesUseCase()) {
                    is SyncResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            dataRestoreProgress = "Data restored successfully!"
                        )

                        // Wait a moment to show success message, then navigate
                        kotlinx.coroutines.delay(1000)
                        _uiState.value = _uiState.value.copy(isRestoringData = false)
                        _uiInteraction.emit(LoginContract.UiInteraction.NavigateToMain)
                    }

                    is SyncResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            dataRestoreProgress = null,
                            dataRestoreError = "Failed to restore data: ${result.message}"
                        )
                    }

                    else -> {
                        _uiState.value = _uiState.value.copy(
                            dataRestoreProgress = null,
                            dataRestoreError = "Unexpected error during data restoration"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    dataRestoreProgress = null,
                    dataRestoreError = "Failed to restore data: ${e.message}"
                )
            }
        }
    }

    private fun retryDataRestore() {
        _uiState.value = _uiState.value.copy(
            dataRestoreError = null
        )
        performDataRestoration()
    }

    private fun skipDataRestore() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRestoringData = false)
            _uiInteraction.emit(LoginContract.UiInteraction.NavigateToMain)
        }
    }
}