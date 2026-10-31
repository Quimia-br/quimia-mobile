package com.quimia.android.presentation.components

import org.junit.Assert.*
import org.junit.Test

class ProductPhotoCutoutTest {
    private val white = 0xFFFFFFFF.toInt()
    private val bottle = 0xFF169F77.toInt()

    @Test fun removesTheBackdropAndPreservesAnEnclosedWhiteLabel() {
        val pixels = IntArray(49) { white }
        for (y in 2..4) for (x in 2..4) pixels[y * 7 + x] = bottle
        pixels[24] = white
        val result = productPhotoCutout(pixels, 7, 7)
        assertEquals(0, result[0])
        assertEquals(0, result[48])
        assertEquals(bottle, result[16])
        assertEquals(white, result[24])
        assertEquals(white, pixels[0])
    }

    @Test fun keepsAnAlreadyTransparentPngUnchanged() {
        val pixels = IntArray(25)
        pixels[12] = white
        assertSame(pixels, productPhotoCutout(pixels, 5, 5))
    }

    @Test fun doesNotEraseWhiteAreasInAnOrdinaryPhotograph() {
        val pixels = IntArray(25) { bottle }
        for (i in 0..6) pixels[i] = white
        assertSame(pixels, productPhotoCutout(pixels, 5, 5))
    }

    @Test fun respectsRowBoundariesDuringTheFloodFill() {
        val pixels = IntArray(49) { white }
        for (x in 1..5) pixels[7 + x] = bottle
        for (x in 1..5) pixels[35 + x] = bottle
        for (y in 1..5) { pixels[y * 7 + 1] = bottle; pixels[y * 7 + 5] = bottle }
        val result = productPhotoCutout(pixels, 7, 7)
        assertEquals(0, result[0])
        assertEquals(white, result[24])
    }
}
