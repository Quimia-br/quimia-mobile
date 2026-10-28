package com.quimia.android.presentation.auth

import com.quimia.android.data.auth.UserProfile
import com.quimia.android.presentation.screens.auth.AuthDestination
import org.junit.Assert.*
import org.junit.Test

class GoogleEntryPolicyTest {
    private val first = UserProfile("u1", "Ana", "ana@example.invalid")
    private val registered = first.copy(dataNasc = "2000-04-20")

    @Test fun firstGoogleAccessCollectsDetails() {
        assertEquals(AuthDestination.GoogleDetails, googleEntryDestination(first, false, false))
    }
    @Test fun registeredGoogleUserGoesHomeEvenOnAnotherDevice() {
        assertNull(googleEntryDestination(registered, false, false))
    }
    @Test fun emailAccountEnteringWithGoogleKeepsItsCompletedRegistration() {
        assertNull(googleEntryDestination(registered, true, false))
    }
    @Test fun skippingOptionalAddressDoesNotRestartOnNextLogin() {
        // The policy deliberately does not require an address to consider this account ready.
        assertNull(googleEntryDestination(registered, true, false))
        assertNull(googleEntryDestination(registered, false, false))
    }
    @Test fun intentionalDateRemovalDoesNotRestartCompletedRegistrationOnThisDevice() {
        assertNull(googleEntryDestination(first, true, false))
    }
    @Test fun interruptedRegistrationResumesDetailsWithoutAnApiBirthDate() {
        assertEquals(AuthDestination.GoogleDetails, googleEntryDestination(first, false, true))
    }
    @Test fun interruptedAddressSaveResumesAddressWithoutRepeatingDetails() {
        assertEquals(AuthDestination.GoogleAddress, googleEntryDestination(registered, false, true))
    }
    @Test fun anotherAccountHasItsOwnRegistrationDecision() {
        assertNull(googleEntryDestination(registered, true, false))
        assertEquals(AuthDestination.GoogleDetails, googleEntryDestination(first.copy(id = "u2", email = "b@example.invalid"), false, false))
    }
}
