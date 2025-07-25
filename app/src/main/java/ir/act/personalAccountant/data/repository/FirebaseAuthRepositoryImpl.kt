package ir.act.personalAccountant.data.repository

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.act.personalAccountant.R
import ir.act.personalAccountant.domain.model.AuthResult
import ir.act.personalAccountant.domain.model.User
import ir.act.personalAccountant.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            val user = firebaseUser?.let {
                User(
                    uid = it.uid,
                    email = it.email,
                    displayName = it.displayName,
                    photoUrl = it.photoUrl?.toString()
                )
            }
            trySend(user)
        }

        firebaseAuth.addAuthStateListener(authStateListener)

        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }

    override suspend fun signInWithGoogle(): AuthResult {
        return try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val googleSignInClient = GoogleSignIn.getClient(context, gso)
            val signInIntent = googleSignInClient.signInIntent

            // Note: The actual sign-in flow should be handled from the Activity/Fragment
            // This method should be called after getting the result from the sign-in intent
            AuthResult.Loading
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    suspend fun signInWithGoogleCredential(idToken: String): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()

            result.user?.let { firebaseUser ->
                val user = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email,
                    displayName = firebaseUser.displayName,
                    photoUrl = firebaseUser.photoUrl?.toString()
                )
                AuthResult.Success(user)
            } ?: AuthResult.Error("Authentication failed")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Authentication failed")
        }
    }

    override suspend fun signOut(): AuthResult {
        return try {
            firebaseAuth.signOut()

            // Also sign out from Google Sign-In
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            GoogleSignIn.getClient(context, gso).signOut().await()

            AuthResult.SignedOut
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Sign out failed")
        }
    }

    override fun isUserSignedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }
}