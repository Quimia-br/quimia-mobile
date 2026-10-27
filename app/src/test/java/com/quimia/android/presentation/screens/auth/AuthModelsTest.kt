package com.quimia.android.presentation.screens.auth

import org.junit.Assert.*
import org.junit.Test

class AuthModelsTest {
    @Test fun registrationAlwaysHasARouteBackToLogin() {
        assertEquals(AuthDestination.Registration, AuthDestination.Address.back())
        assertEquals(AuthDestination.Login, AuthDestination.Address.back().back())
        assertEquals(AuthDestination.Login, AuthDestination.Registration.back())
    }
    @Test fun recoveryReturnsThroughEachPreviousStep() {
        assertEquals(AuthDestination.RecoveryCode, AuthDestination.NewPassword.back())
        assertEquals(AuthDestination.RecoveryEmail, AuthDestination.RecoveryCode.back())
        assertEquals(AuthDestination.Login, AuthDestination.RecoveryEmail.back())
        assertEquals(AuthDestination.Login, AuthDestination.RecoverySuccess.back())
        assertEquals(AuthDestination.Welcome, AuthDestination.Login.back())
    }
    @Test fun registrationRequiresNameEmailPasswordAndCompleteBirthDate() {
        assertNotNull(authNameError("  "))
        assertNull(authNameError("Daniel Filho"))
        assertNull(authEmailError(" exemplo@quimia.com "))
        assertNotNull(authEmailError("exemplo@"))
        assertNotNull(authBirthDateError(""))
        assertNotNull(authBirthDateError("31/04"))
        assertNull(authBirthDateError("20/04/2000"))
        assertNotNull(authBirthDateError("20/04"))
        assertNotNull(authBirthDateError("29/02"))
        assertNull(authBirthDateError("29/02/2000"))
    }
    @Test fun passwordConfirmationCannotProceedWithBlankShortOrDifferentPasswords() {
        assertNotNull(authPasswordError(""))
        assertNotNull(authPasswordError("12345"))
        assertNotNull(authPasswordError("123456"))
        assertNotNull(authPasswordError("1234567"))
        assertNotNull(authPasswordError("12345678"))
        assertNull(authPasswordError("NovaSenha42!"))
        assertNotNull(authPasswordConfirmationError("NovaSenha42!", "OutraSenha42!"))
        assertNotNull(authPasswordConfirmationError("", ""))
        assertNull(authPasswordConfirmationError("NovaSenha42!", "NovaSenha42!"))
    }
    @Test fun addressSelectorContainsAllTwentySevenBrazilianStates() {
        assertEquals(27, BrazilianStates.size)
        assertEquals(27, BrazilianStates.distinct().size)
        assertTrue(BrazilianStates.containsAll(listOf("SP", "RJ", "DF", "AC", "TO")))
    }
}
