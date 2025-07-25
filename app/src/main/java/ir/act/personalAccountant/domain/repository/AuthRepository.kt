package ir.act.personalAccountant.domain.repository

import ir.act.personalAccountant.domain.model.AuthResult
import ir.act.personalAccountant.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun signInWithGoogle(): AuthResult
    suspend fun signOut(): AuthResult
    fun isUserSignedIn(): Boolean
}