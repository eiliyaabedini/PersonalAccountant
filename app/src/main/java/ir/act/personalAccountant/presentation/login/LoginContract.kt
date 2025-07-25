package ir.act.personalAccountant.presentation.login

import ir.act.personalAccountant.domain.model.User

object LoginContract {
    data class UiState(
        val isLoading: Boolean = false,
        val user: User? = null,
        val errorMessage: String? = null,
        val isSignedIn: Boolean = false
    )

    sealed class Event {
        object SignInWithGoogle : Event()
        object SignOut : Event()
        object ClearError : Event()
        object SkipLogin : Event()
        data class GoogleSignInResult(val idToken: String?) : Event()
    }

    sealed class UiInteraction {
        object NavigateToMain : UiInteraction()
        data class ShowError(val message: String) : UiInteraction()
        object StartGoogleSignIn : UiInteraction()
    }
}