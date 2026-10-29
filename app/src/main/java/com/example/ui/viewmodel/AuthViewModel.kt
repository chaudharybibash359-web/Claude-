package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.repository.FirestoreClaudeRepository
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "AuthViewModel"

data class AuthUiState(
    val user: FirebaseUser? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth? by lazy {
        try {
            Firebase.auth
        } catch (e: Throwable) {
            Log.w(TAG, "FirebaseAuth unavailable: ${e.message}")
            null
        }
    }

    private val firestoreRepo = FirestoreClaudeRepository(application)

    private val _uiState = MutableStateFlow(
        AuthUiState(user = runCatching { auth?.currentUser }.getOrNull())
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val authStateListener: FirebaseAuth.AuthStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        _uiState.value = _uiState.value.copy(
            user = user,
            isLoading = false
        )
        if (user != null) {
            viewModelScope.launch {
                try {
                    firestoreRepo.syncUserProfile(
                        userId = user.uid,
                        displayName = user.displayName ?: "Claude User",
                        email = user.email ?: "",
                        photoUrl = user.photoUrl?.toString()
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Error syncing user profile to Firestore: ${e.message}")
                }
            }
        }
    }

    init {
        try {
            auth?.addAuthStateListener(authStateListener)
        } catch (e: Throwable) {
            Log.w(TAG, "Could not attach authStateListener: ${e.message}")
        }
    }

    fun signInWithGoogle(context: Context) {
        val currentAuth = auth
        if (currentAuth == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Authentication service is unavailable on this device."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val credentialManager = CredentialManager.create(context)
                val serverClientId = context.getString(R.string.default_web_client_id)

                val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                    currentAuth.signInWithCredential(authCredential).await()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Unexpected credential type returned."
                    )
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google sign-in was cancelled by user.", e)
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                Log.e(TAG, "Google sign-in failed", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Sign-in failed. Please try again."
                )
            }
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Throwable) {
            Log.w(TAG, "Error during signOut: ${e.message}")
        }
        _uiState.value = AuthUiState(user = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        try {
            auth?.removeAuthStateListener(authStateListener)
        } catch (e: Throwable) {
            // Ignore on cleanup
        }
    }
}
