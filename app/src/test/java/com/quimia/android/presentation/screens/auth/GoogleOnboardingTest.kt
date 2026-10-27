package com.quimia.android.presentation.screens.auth

import org.junit.Assert.*
import org.junit.Test

class GoogleOnboardingTest {
    private val profile = GoogleOnboarding("google-user", "user@example.invalid", "Nome Google", "20/04/2000")

    @Test fun cannotSkipDetailsWithAnIncompleteGoogleProfile() {
        assertNull(profile.copy(name = "").continueToAddress())
        assertNull(profile.copy(birthDate = "").continueToAddress())
        assertNull(profile.copy(birthDate = "31/02/2000").continueToAddress())
        assertNull(profile.copy(uid = "").continueToAddress())
        assertNull(profile.copy(email = "").continueToAddress())
    }
    @Test fun googleAuthenticationStartsInDetailsAndThenGoesToAddress() {
        assertEquals(AuthDestination.GoogleDetails, profile.destination)
        assertFalse(profile.addressStep)
        assertEquals(AuthDestination.GoogleAddress, requireNotNull(profile.continueToAddress()).destination)
    }
    @Test fun returningFromAddressPreservesTheGoogleIdentityAndEnteredData() {
        val edited = profile.copy(name = "Nome editado", birthDate = "01/01/2001", photoUrl = "https://example.invalid/photo")
        val returned = requireNotNull(edited.continueToAddress()).backToDetails()
        assertEquals(edited, returned)
        assertEquals(AuthDestination.GoogleDetails, AuthDestination.GoogleAddress.back())
        assertEquals(AuthDestination.Login, AuthDestination.GoogleDetails.back())
    }
}
