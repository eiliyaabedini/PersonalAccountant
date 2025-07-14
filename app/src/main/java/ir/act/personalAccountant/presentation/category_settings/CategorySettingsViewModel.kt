package ir.act.personalAccountant.presentation.category_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.act.personalAccountant.domain.usecase.GetAllTagsUseCase
import ir.act.personalAccountant.domain.usecase.MergeTagsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategorySettingsViewModel @Inject constructor(
    private val getAllTagsUseCase: GetAllTagsUseCase,
    private val mergeTagsUseCase: MergeTagsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategorySettingsContract.UiState())
    val uiState: StateFlow<CategorySettingsContract.UiState> = _uiState.asStateFlow()

    private val _uiInteractions = MutableSharedFlow<CategorySettingsContract.UiInteractions>()
    val uiInteractions: SharedFlow<CategorySettingsContract.UiInteractions> =
        _uiInteractions.asSharedFlow()

    init {
        loadTags()
    }

    fun onEvent(event: CategorySettingsContract.Events) {
        when (event) {
            is CategorySettingsContract.Events.OnTagSelectionChanged -> {
                handleTagSelectionChanged(event.tag, event.isSelected)
            }

            is CategorySettingsContract.Events.OnNewTagNameChanged -> {
                _uiState.value = _uiState.value.copy(newTagName = event.newName)
            }

            CategorySettingsContract.Events.OnMergeTagsClicked -> {
                if (_uiState.value.selectedTags.size >= 2) {
                    _uiState.value = _uiState.value.copy(showMergeDialog = true)
                }
            }

            CategorySettingsContract.Events.OnConfirmMerge -> {
                performMerge()
            }

            CategorySettingsContract.Events.OnCancelMerge -> {
                _uiState.value = _uiState.value.copy(showMergeDialog = false)
            }

            CategorySettingsContract.Events.OnClearSelection -> {
                _uiState.value = _uiState.value.copy(
                    selectedTags = emptyList(),
                    newTagName = ""
                )
            }

            CategorySettingsContract.Events.OnErrorDismissed -> {
                _uiState.value = _uiState.value.copy(error = null)
            }
        }
    }

    private fun loadTags() {
        getAllTagsUseCase()
            .onEach { tags ->
                _uiState.value = _uiState.value.copy(
                    tags = tags,
                    isLoading = false
                )
            }
            .catch { error ->
                _uiState.value = _uiState.value.copy(
                    error = error.message ?: "Failed to load tags",
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
    }

    private fun handleTagSelectionChanged(tag: String, isSelected: Boolean) {
        val currentSelection = _uiState.value.selectedTags.toMutableList()

        if (isSelected) {
            currentSelection.add(tag)
        } else {
            currentSelection.remove(tag)
        }

        _uiState.value = _uiState.value.copy(selectedTags = currentSelection)
    }

    private fun performMerge() {
        val selectedTags = _uiState.value.selectedTags
        val newTagName = _uiState.value.newTagName.trim()

        if (selectedTags.size < 2) {
            _uiState.value = _uiState.value.copy(error = "Please select at least 2 tags to merge")
            return
        }

        if (newTagName.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Please enter a name for the merged tag")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, showMergeDialog = false)

        viewModelScope.launch {
            try {
                val updatedCount = mergeTagsUseCase(selectedTags, newTagName)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    selectedTags = emptyList(),
                    newTagName = ""
                )
                _uiInteractions.emit(
                    CategorySettingsContract.UiInteractions.ShowMessage(
                        "Successfully merged ${selectedTags.size} tags. Updated $updatedCount expenses."
                    )
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to merge tags"
                )
            }
        }
    }
}