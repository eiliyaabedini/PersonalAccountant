package ir.act.personalAccountant.domain.model

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
    object SignedOut : AuthResult()
}