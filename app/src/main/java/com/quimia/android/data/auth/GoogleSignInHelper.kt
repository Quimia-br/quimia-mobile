package com.quimia.android.data.auth

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.quimia.android.utils.AnalyticsLogger
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume

class GoogleSignInHelper(private val context: Context) {
    private val webClientId: String by lazy {
        val resource = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        check(resource != 0) { "O acesso Google ainda não foi configurado para este aplicativo." }
        context.getString(resource).also { check(it.isNotBlank()) { "O acesso Google ainda não foi configurado para este aplicativo." } }
    }
    private val googleSignInOptions by lazy { GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestProfile()
        .requestIdToken(webClientId)
        .build() }

    private val googleSignInClient: GoogleSignInClient by lazy { GoogleSignIn.getClient(context, googleSignInOptions) }

    suspend fun chooseAccountIntent(): android.content.Intent {
        googleSignInClient.signOut().await()
        return getSignInIntent()
    }

    fun accountFromResult(data: android.content.Intent?) = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)

    fun getSignInIntent() = googleSignInClient.signInIntent.apply {
        AnalyticsLogger.logGoogleSignInEvent("GET_SIGN_IN_INTENT")
    }

    suspend fun getIdTokenFromResult(data: android.content.Intent?): String? {
        return suspendCancellableCoroutine { continuation ->
            try {
                AnalyticsLogger.logGoogleSignInEvent("GET_ID_TOKEN")
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                AnalyticsLogger.logGoogleSignInEvent("ID_TOKEN_RECEIVED")
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
        AnalyticsLogger.logGoogleSignInEvent("SIGN_OUT")
        googleSignInClient.signOut()
    }
}
