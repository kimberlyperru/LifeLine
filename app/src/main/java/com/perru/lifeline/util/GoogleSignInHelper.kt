package com.perru.lifeline.util

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GoogleSignInHelper {
    // Replace with your actual Web Client ID from Google Cloud Console / Firebase
    // Format: 874021857367-xxxxxxxxxxxx.apps.googleusercontent.com
    private const val WEB_CLIENT_ID = "AIzaSyBH978hrqzie6YCwzdbdFZ0DneFzssK5oI"


    suspend fun signInWithGoogle(context: Context): Result<String> = withContext(Dispatchers.Main) {
        val credentialManager = CredentialManager.create(context)

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result = credentialManager.getCredential(
                context = context,
                request = request
            )
            val credential = result.credential
            if (credential is GoogleIdTokenCredential) {
                Result.success(credential.idToken)
            } else {
                Result.failure(Exception("Unsupported credential type: ${credential.type}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
