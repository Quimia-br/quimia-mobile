package com.quimia.android.domain.auth

sealed class AuthResult {
    data class Success(val uid: String, val email: String?) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
}
