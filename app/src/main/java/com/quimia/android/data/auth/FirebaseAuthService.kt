package com.quimia.android.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.quimia.android.domain.auth.AuthResult
import com.quimia.android.utils.AnalyticsLogger
import kotlinx.coroutines.tasks.await

class FirebaseAuthService {
    // The app can be previewed/run without google-services.json. In that case
    // Firebase Auth is unavailable, but the authentication screen must still open.
    private val auth: FirebaseAuth? = runCatching {
        FirebaseAuth.getInstance()
    }.getOrNull()

    suspend fun loginWithEmail(email: String, password: String): AuthResult {
        val firebaseAuth = auth ?: return firebaseUnavailable()

        return try {
            require(email.isNotBlank()) { "Informe um e-mail válido." }
            require(password.isNotBlank()) { "Informe a senha." }

            val result = firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await()
            AuthResult.Success(uid = result.user?.uid ?: "", email = result.user?.email)
        } catch (e: FirebaseAuthException) {
            AuthResult.Error(e.message ?: "Erro ao fazer login")
        } catch (e: IllegalArgumentException) {
            AuthResult.Error(e.message ?: "Dados inválidos")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Erro ao fazer login")
        }
    }

    suspend fun registerWithEmail(email: String, password: String): AuthResult {
        val firebaseAuth = auth ?: return firebaseUnavailable()

        return try {
            require(email.isNotBlank()) { "Informe um e-mail válido." }
            require(password.length >= 6) { "A senha precisa ter pelo menos 6 caracteres." }

            val result = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await()
            AuthResult.Success(uid = result.user?.uid ?: "", email = result.user?.email)
        } catch (e: FirebaseAuthException) {
            AuthResult.Error(e.message ?: "Erro ao registrar")
        } catch (e: IllegalArgumentException) {
            AuthResult.Error(e.message ?: "Dados inválidos")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Erro ao registrar")
        }
    }

    suspend fun loginWithGoogle(idToken: String): AuthResult {
        val firebaseAuth = auth ?: return firebaseUnavailable()

        return try {
            require(idToken.isNotBlank()) { "Token do Google inválido." }

            AnalyticsLogger.logInfo("FirebaseAuthService: Creating Google credential")
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            
            AnalyticsLogger.logInfo("FirebaseAuthService: Signing in with credential")
            val result = firebaseAuth.signInWithCredential(credential).await()
            
            val uid = result.user?.uid ?: ""
            val email = result.user?.email
            AnalyticsLogger.logInfo("FirebaseAuthService: Login success - UID: $uid, Email: $email")
            
            AuthResult.Success(uid = uid, email = email)
        } catch (e: FirebaseAuthException) {
            AnalyticsLogger.logError("FIREBASE_AUTH", e, "FirebaseAuthException: ${e.message}")
            AuthResult.Error(e.message ?: "Erro ao fazer login com Google")
        } catch (e: IllegalArgumentException) {
            AnalyticsLogger.logError("FIREBASE_AUTH", e, "IllegalArgumentException: ${e.message}")
            AuthResult.Error(e.message ?: "Token do Google inválido")
        } catch (e: Exception) {
            AnalyticsLogger.logError("FIREBASE_AUTH", e, "Generic Exception: ${e.message}")
            AuthResult.Error(e.message ?: "Erro ao fazer login com Google")
        }
    }

    fun isLoggedIn(): Boolean = auth?.currentUser != null

    fun getCurrentUser() = auth?.currentUser

    fun logout() {
        auth?.signOut()
    }

    private fun firebaseUnavailable(): AuthResult {
        return AuthResult.Error("Firebase não configurado. Adicione o google-services.json para habilitar o login.")
    }
}
