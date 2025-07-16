package ir.act.personalAccountant.ai.presentation.settings

object AISettingsContract {
    data class UiState(
        val apiKey: String = "",
        val isLoading: Boolean = false,
        val isTesting: Boolean = false,
        val testResult: String? = null,
        val error: String? = null
    )

    sealed class Events {
        data class OnApiKeyChanged(val apiKey: String) : Events()
        object OnSaveClicked : Events()
        object OnTestConnectionClicked : Events()
        object OnClearApiKeyClicked : Events()
        object OnErrorDismissed : Events()
        object OnTestResultDismissed : Events()
    }

    sealed class UiInteractions {
        object NavigateBack : UiInteractions()
        data class ShowMessage(val message: String) : UiInteractions()
    }
}