package com.quimia.android.presentation.screens.kemi

import com.quimia.android.presentation.screens.errors.GenericErrorKind
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.net.SocketTimeoutException

class KemiResponderTest {
    private val request = KemiRequest(KemiConversation("chat", 100), "message")

    @Test fun missingServiceIsExplicitlyUnavailable() = runBlocking {
        assertEquals(KemiReply.Failure(GenericErrorKind.ServiceUnavailable), requestKemiReply(null, request))
    }

    @Test fun typedErrorsArePreserved() = runBlocking {
        GenericErrorKind.entries.forEach { kind ->
            assertEquals(KemiReply.Failure(kind), requestKemiReply({ KemiReply.Failure(kind) }, request))
        }
    }

    @Test fun blankAnswerIsUnavailableContent() = runBlocking {
        assertEquals(KemiReply.Failure(GenericErrorKind.ContentUnavailable), requestKemiReply({ KemiReply.Answer(" \n") }, request))
    }

    @Test fun realAnswerIsNotReplacedWithASample() = runBlocking {
        val answer = KemiReply.Answer("Conteúdo do serviço")
        assertEquals(answer, requestKemiReply({ answer }, request))
    }

    @Test fun timeoutsAndUnexpectedExceptionsHaveSafeErrorStates() = runBlocking {
        assertEquals(KemiReply.Failure(GenericErrorKind.Timeout), requestKemiReply({ throw SocketTimeoutException() }, request))
        assertEquals(KemiReply.Failure(GenericErrorKind.Unknown), requestKemiReply({ error("private details") }, request))
    }

    @Test fun cancellationIsNotReportedAsAnAppError() = runBlocking {
        try { requestKemiReply({ throw CancellationException() }, request); fail("Cancellation must propagate") }
        catch (_: CancellationException) { }
    }
}
