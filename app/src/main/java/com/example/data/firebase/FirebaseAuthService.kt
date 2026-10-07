package com.example.data.firebase

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseAuthService(private val context: Context) {

    private val TAG = "FirebaseAuthService"

    private val auth: FirebaseAuth?
        get() = try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseAuth.getInstance()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error obtaining FirebaseAuth: ${e.message}")
            null
        }

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _currentUser.value = firebaseAuth.currentUser
    }

    init {
        try {
            auth?.addAuthStateListener(authStateListener)
            _currentUser.value = auth?.currentUser
        } catch (e: Exception) {
            Log.w(TAG, "Init auth listener warning: ${e.message}")
        }
    }

    fun isConnected(): Boolean {
        return auth != null
    }

    fun getCurrentUserId(): String {
        return auth?.currentUser?.uid ?: "local_user"
    }

    suspend fun signInWithGoogleCredential(idToken: String): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val firebaseAuth = auth ?: return@withContext Result.failure(Exception("Firebase Auth not initialized"))
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result: AuthResult = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user ?: return@withContext Result.failure(Exception("User is null after Google sign in"))
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Failed Google credential sign-in: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val firebaseAuth = auth ?: return@withContext Result.failure(Exception("Firebase Auth not initialized"))
        try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
            val user = result.user ?: return@withContext Result.failure(Exception("Sign in failed"))
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            // If user doesn't exist, try creating account automatically for seamless experience
            try {
                val createResult = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
                val newUser = createResult.user ?: return@withContext Result.failure(e)
                _currentUser.value = newUser
                Result.success(newUser)
            } catch (_: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun signInAnonymously(): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        val firebaseAuth = auth ?: return@withContext Result.failure(Exception("Firebase Auth not initialized"))
        try {
            val result = firebaseAuth.signInAnonymously().await()
            val user = result.user ?: return@withContext Result.failure(Exception("Anonymous login failed"))
            _currentUser.value = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
            _currentUser.value = null
        } catch (e: Exception) {
            Log.e(TAG, "Sign out error: ${e.message}")
        }
    }
}
