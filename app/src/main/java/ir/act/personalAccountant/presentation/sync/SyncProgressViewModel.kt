package ir.act.personalAccountant.presentation.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.act.personalAccountant.domain.model.SyncProgress
import ir.act.personalAccountant.domain.model.SyncStep
import ir.act.personalAccountant.domain.usecase.GoogleSheetsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncProgressViewModel @Inject constructor(
    private val googleSheetsUseCase: GoogleSheetsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SyncProgressUiState())
    val uiState: StateFlow<SyncProgressUiState> = _uiState.asStateFlow()

    private var syncJob: Job? = null

    fun startSync() {
        if (syncJob?.isActive == true) return

        syncJob = viewModelScope.launch {
            try {
                // Clear previous progress
                _uiState.value = SyncProgressUiState(
                    currentProgress = SyncProgress(currentStep = SyncStep.CONNECTING),
                    progressItems = listOf()
                )

                googleSheetsUseCase.syncAllExpensesWithProgress()
                    .collect { progress ->
                        _uiState.value = _uiState.value.copy(
                            currentProgress = progress,
                            progressItems = _uiState.value.progressItems + progress
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    currentProgress = SyncProgress(
                        currentStep = SyncStep.ERROR,
                        currentItem = "Sync failed",
                        error = e.message
                    )
                )
            }
        }
    }

    fun stopSync() {
        syncJob?.cancel()
        _uiState.value = _uiState.value.copy(
            currentProgress = SyncProgress(
                currentStep = SyncStep.IDLE,
                currentItem = "Sync stopped"
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        syncJob?.cancel()
    }
}

data class SyncProgressUiState(
    val currentProgress: SyncProgress = SyncProgress(),
    val progressItems: List<SyncProgress> = emptyList()
)