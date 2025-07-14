package ir.act.personalAccountant.presentation.category_settings

import ir.act.personalAccountant.data.local.model.TagWithCount

object CategorySettingsContract {

    data class UiState(
        val tags: List<TagWithCount> = emptyList(),
        val selectedTags: List<String> = emptyList(),
        val newTagName: String = "",
        val isLoading: Boolean = false,
        val showMergeDialog: Boolean = false,
        val error: String? = null
    )

    sealed class Events {
        data class OnTagSelectionChanged(val tag: String, val isSelected: Boolean) : Events()
        data class OnNewTagNameChanged(val newName: String) : Events()
        object OnMergeTagsClicked : Events()
        object OnConfirmMerge : Events()
        object OnCancelMerge : Events()
        object OnClearSelection : Events()
        object OnErrorDismissed : Events()
    }

    sealed class UiInteractions {
        object NavigateBack : UiInteractions()
        data class ShowMessage(val message: String) : UiInteractions()
    }
}