package com.quimia.android.presentation.auth

import com.quimia.android.presentation.screens.auth.authLoginInputError
import com.quimia.android.presentation.screens.auth.authPasswordError
import org.junit.Assert.*
import org.junit.Test

class LoginValidationTest {
    @Test fun loginDoesNotImposeRegistrationPasswordRulesOnExistingAccounts() {
        assertNull(authLoginInputError(" usuario@example.com ", "123"))
        assertNotNull(authPasswordError("123"))
        assertNotNull(authLoginInputError("usuario@example.com", "   "))
        assertNotNull(authLoginInputError("usuario@example.com", "a".repeat(73)))
        assertNull(authLoginInputError("usuario@example.com", "a".repeat(72)))
        assertNotNull(authLoginInputError("invalido", "password"))
        assertNotNull(authLoginInputError("a".repeat(250) + "@example.com", "password"))
    }
    @Test fun backendBirthDateKeepsTheExistingDisplayFormatWithoutInventingMissingData() {
        assertEquals("20/04/2000", displayBirthDate("2000-04-20"))
        assertEquals("", displayBirthDate(null))
        assertEquals("", displayBirthDate("20/04"))
    }
}
