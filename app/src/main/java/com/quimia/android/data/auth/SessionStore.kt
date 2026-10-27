package com.quimia.android.data.auth

import kotlinx.serialization.Serializable

@Serializable
class StoredSession(val accessToken: String, val refreshToken: String, val expiresAt: Long, val userId: String) {
    override fun toString(): String = "StoredSession([REDACTED])"
}

interface SessionStore {
    fun read(): StoredSession?
    fun write(session: StoredSession)
    fun clear()
}
