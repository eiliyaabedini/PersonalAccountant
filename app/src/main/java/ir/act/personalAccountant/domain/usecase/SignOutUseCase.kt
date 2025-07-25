package ir.act.personalAccountant.domain.usecase

import ir.act.personalAccountant.domain.model.AuthResult
import ir.act.personalAccountant.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): AuthResult {
        return authRepository.signOut()
    }
}