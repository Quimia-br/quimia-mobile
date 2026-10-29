package com.quimia.android.presentation.auth

import com.quimia.android.data.auth.UserProfile
import org.junit.Assert.*
import org.junit.Test

class InitialProfilePolicyTest {
    private val completed = UserProfile("user-1", "Ana Silva", "ana@example.com", "2000-04-20")

    @Test fun `returning account skips details and address even with a stale pending marker`() {
        assertFalse(needsInitialProfile(completed))
        assertFalse(shouldResumeInitialProfile(completed, locallyPending = true))
        assertFalse(shouldResumeInitialProfile(completed, locallyPending = false))
    }

    @Test fun `first Google login without persisted birthday must complete the profile`() {
        val firstLogin = completed.copy(dataNasc = null)
        assertTrue(needsInitialProfile(firstLogin))
        assertTrue(shouldResumeInitialProfile(firstLogin, locallyPending = true))
    }

    @Test fun `an optional skipped address does not force onboarding on another device`() {
        // Only the API profile is required; this device has no local completion marker or address.
        assertFalse(shouldResumeInitialProfile(completed, locallyPending = false))
        assertFalse(needsInitialProfile(completed))
    }

    @Test fun `missing name or invalid birthday never counts as a completed profile`() {
        assertTrue(needsInitialProfile(completed.copy(nome = " ")))
        assertTrue(needsInitialProfile(completed.copy(dataNasc = "2000-02-31")))
        assertTrue(needsInitialProfile(completed.copy(dataNasc = "")))
    }
}
