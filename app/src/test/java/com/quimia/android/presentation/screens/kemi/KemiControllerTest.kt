package com.quimia.android.presentation.screens.kemi

import com.quimia.android.presentation.screens.errors.GenericErrorKind
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class KemiControllerTest {
    @Test fun blankMessagesAreIgnored() {
        val controller = KemiController()
        assertNull(controller.send(" \n "))
        assertTrue(controller.state.conversations.isEmpty())
    }

    @Test fun userTextIsQueuedWithoutInventingAnAnswer() {
        val controller = KemiController()
        val request = controller.send("  Como usar?  ", 100)!!
        assertEquals("Como usar?", request.conversation.messages.single().text)
        assertEquals(KemiRole.User, controller.state.current!!.messages.single().role)
        assertTrue(controller.state.pending)
        assertNull(controller.send("Segunda pergunta", 101))
    }

    @Test fun responseContainsExactlyWhatTheResponderReturned() {
        val controller = KemiController()
        val request = controller.send("Pergunta", 100)!!
        controller.complete(request, KemiReply.Answer("Resposta fornecida pelo serviço"), 200)
        assertFalse(controller.state.pending)
        assertEquals(listOf("Pergunta", "Resposta fornecida pelo serviço"), controller.state.current!!.messages.map { it.text })
        assertEquals(KemiDelivery.Delivered, controller.state.current!!.messages.first().delivery)
    }

    @Test fun failureDoesNotCreateAnAssistantMessageAndRetryReusesTheUserMessage() {
        val controller = KemiController()
        val request = controller.send("Pergunta")!!
        controller.complete(request, KemiReply.Failure(GenericErrorKind.Offline))
        assertEquals(KemiDelivery.Failed, controller.state.current!!.messages.single().delivery)
        val retry = controller.retry()!!
        assertEquals(request.messageId, retry.messageId)
        assertEquals(1, controller.state.current!!.messages.size)
        assertTrue(controller.state.pending)
        assertNull(controller.retry())
    }

    @Test fun emptyAnswerIsNotAccepted() {
        val controller = KemiController()
        val request = controller.send("Pergunta")!!
        controller.complete(request, KemiReply.Answer(" "))
        assertEquals(KemiDelivery.Failed, controller.state.current!!.messages.single().delivery)
    }

    @Test fun newChatArchivesTheCurrentChatAndRejectsLateResponses() {
        val controller = KemiController()
        val request = controller.send("Pergunta")!!
        controller.newChat()
        controller.complete(request, KemiReply.Answer("Chegou tarde"))
        assertNull(controller.state.current)
        val closed = controller.state.history.single()
        assertTrue(closed.closed)
        assertEquals(1, closed.messages.size)
        assertEquals(KemiDelivery.Failed, closed.messages.single().delivery)
    }

    @Test fun historicalChatsAreReadOnlyAndCanReturnToTheActiveChat() {
        val controller = KemiController()
        val old = controller.send("Antiga")!!
        controller.complete(old, KemiReply.Answer("Resposta antiga"))
        controller.newChat()
        val active = controller.send("Atual")!!
        controller.viewConversation(old.conversation.id)
        assertTrue(controller.state.readOnly)
        assertNull(controller.send("Não pode enviar aqui"))
        assertNull(controller.retry())
        controller.resumeActive()
        assertFalse(controller.state.readOnly)
        assertEquals(active.conversation.id, controller.state.current!!.id)
    }

    @Test fun newChatDoesNotCreateEmptyHistoryEntries() {
        val controller = KemiController()
        repeat(3) { controller.newChat() }
        assertTrue(controller.state.history.isEmpty())
        assertTrue(controller.state.conversations.isEmpty())
    }

    @Test fun duplicateCompletionIsIgnored() {
        val controller = KemiController()
        val request = controller.send("Pergunta")!!
        controller.complete(request, KemiReply.Answer("Resposta"))
        controller.complete(request, KemiReply.Answer("Duplicada"))
        assertEquals(2, controller.state.current!!.messages.size)
    }

    @Test fun interruptedOrRestoredPendingRequestBecomesRetryable() {
        val controller = KemiController()
        controller.send("Pergunta")
        val restored = KemiController(restoreKemiState(Json.encodeToString(controller.state)))
        assertFalse(restored.state.pending)
        assertEquals(KemiDelivery.Failed, restored.state.current!!.messages.single().delivery)
        assertNotNull(restored.retry())
        controller.interruptPending()
        assertFalse(controller.state.pending)
    }

    @Test fun invalidSavedStateStartsEmptyAndControllersDoNotShareConversations() {
        assertEquals(KemiState(), restoreKemiState("invalid JSON"))
        KemiController().send("Privada")
        assertTrue(KemiController().state.conversations.isEmpty())
    }
}
