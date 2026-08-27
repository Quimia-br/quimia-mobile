package com.quimia.android.data.auth

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.quimia.android.utils.AnalyticsLogger
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class GoogleSignInHelper(private val context: Context) {
    private val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .build()

    private val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)

    fun getSignInIntent() = googleSignInClient.signInIntent.apply {
        AnalyticsLogger.logGoogleSignInEvent("GET_SIGN_IN_INTENT", "Creating sign-in intent")
    }

    suspend fun getIdTokenFromResult(data: android.content.Intent?): String? {
        return suspendCancellableCoroutine { continuation ->
            try {
                AnalyticsLogger.logGoogleSignInEvent("GET_ID_TOKEN", "Processing sign-in result")
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                AnalyticsLogger.logGoogleSignInEvent("GET_ID_TOKEN", "Account: ${account?.email}, IdToken: ${account?.idToken?.substring(0, 20)}...")
                continuation.resume(account?.idToken)
            } catch (e: ApiException) {
                AnalyticsLogger.logGoogleSignInError("GET_ID_TOKEN", e)
                continuation.resume(null)
            } catch (e: Exception) {
                AnalyticsLogger.logGoogleSignInError("GET_ID_TOKEN", e)
                continuation.resume(null)
            }
        }
    }

    fun signOut() {
        AnalyticsLogger.logGoogleSignInEvent("SIGN_OUT", "Signing out")
        googleSignInClient.signOut()
    }
}
