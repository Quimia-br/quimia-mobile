package com.quimia.android.presentation.screens.errors

import org.junit.Assert.*
import org.junit.Test

class GenericErrorKindTest {
    @Test fun transientFailuresAllowRetry() {
        listOf(GenericErrorKind.Unknown, GenericErrorKind.Offline, GenericErrorKind.ServiceUnavailable, GenericErrorKind.Timeout)
            .forEach { assertEquals(GenericErrorAction.Retry, it.action) }
    }

    @Test fun expiredSessionRequiresSigningInAgain() {
        assertEquals(GenericErrorAction.SignIn, GenericErrorKind.SessionExpired.action)
        assertEquals("Entrar novamente", GenericErrorKind.SessionExpired.action.label)
    }

    @Test fun inaccessibleContentReturnsHome() {
        assertEquals(GenericErrorAction.Home, GenericErrorKind.AccessDenied.action)
        assertEquals(GenericErrorAction.Home, GenericErrorKind.ContentUnavailable.action)
        assertEquals("Voltar ao início", GenericErrorAction.Home.label)
    }

    @Test fun allSevenStatesHaveTheirOwnCopy() {
        assertEquals(7, GenericErrorKind.entries.size)
        assertEquals(7, GenericErrorKind.entries.map { it.title }.distinct().size)
        assertTrue(GenericErrorKind.entries.all { it.title.isNotBlank() && it.description.isNotBlank() })
        assertEquals("Por favor, tente novamente\ndaqui a pouco", GenericErrorKind.Unknown.description)
    }
}
