package ir.act.personalAccountant.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import ir.act.personalAccountant.domain.model.AuthResult
import ir.act.personalAccountant.domain.model.User
import ir.act.personalAccountant.domain.repository.AuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SignInWithGoogleUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var signInWithGoogleUseCase: SignInWithGoogleUseCase

    @Before
    fun setUp() {
        authRepository = mockk()
        signInWithGoogleUseCase = SignInWithGoogleUseCase(authRepository)
    }

    @Test
    fun `invoke should return success when repository returns success`() = runTest {
        // Given
        val user = User(
            uid = "test-uid",
            email = "test@example.com",
            displayName = "Test User",
            photoUrl = null
        )
        val expectedResult = AuthResult.Success(user)
        coEvery { authRepository.signInWithGoogle() } returns expectedResult

        // When
        val result = signInWithGoogleUseCase()

        // Then
        assertEquals(expectedResult, result)
        coVerify { authRepository.signInWithGoogle() }
    }

    @Test
    fun `invoke should return error when repository returns error`() = runTest {
        // Given
        val expectedResult = AuthResult.Error("Authentication failed")
        coEvery { authRepository.signInWithGoogle() } returns expectedResult

        // When
        val result = signInWithGoogleUseCase()

        // Then
        assertEquals(expectedResult, result)
        coVerify { authRepository.signInWithGoogle() }
    }

    @Test
    fun `invoke should return loading when repository returns loading`() = runTest {
        // Given
        val expectedResult = AuthResult.Loading
        coEvery { authRepository.signInWithGoogle() } returns expectedResult

        // When
        val result = signInWithGoogleUseCase()

        // Then
        assertEquals(expectedResult, result)
        coVerify { authRepository.signInWithGoogle() }
    }
}