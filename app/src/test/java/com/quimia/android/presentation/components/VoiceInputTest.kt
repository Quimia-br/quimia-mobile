package com.quimia.android.presentation.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VoiceInputTest {
    @Test fun returnsTheBestRecognitionResult() {
        assertEquals("limpador multiuso", recognizedVoiceText(true, listOf("limpador multiuso", "limpar multiuso")))
    }

    @Test fun ignoresBlankAlternativesAndTrimsTheRecognizedText() {
        assertEquals("Banheiro", recognizedVoiceText(true, listOf("", "  ", "  Banheiro  ", "Cozinha")))
    }

    @Test fun cancellingDoesNotProvideAReplacementForTheCurrentInput() {
        assertNull(recognizedVoiceText(false, listOf("texto que não foi confirmado")))
    }

    @Test fun ignoresMissingEmptyAndBlankResults() {
        assertNull(recognizedVoiceText(true, null))
        assertNull(recognizedVoiceText(true, emptyList()))
        assertNull(recognizedVoiceText(true, listOf("", "  ", "\n")))
    }

    @Test fun preservesAccentsPunctuationAndCaseForEveryField() {
        assertEquals("Álcool 70%, área de serviço", recognizedVoiceText(true, listOf("Álcool 70%, área de serviço")))
    }
}
