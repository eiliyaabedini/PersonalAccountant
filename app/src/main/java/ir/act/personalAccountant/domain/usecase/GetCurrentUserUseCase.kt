package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.domain.model.User
import ir.act.personalAccountant.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<User?> {
        return authRepository.currentUser
    }

    fun isUserSignedIn(): Boolean {
        return authRepository.isUserSignedIn()
    }
}