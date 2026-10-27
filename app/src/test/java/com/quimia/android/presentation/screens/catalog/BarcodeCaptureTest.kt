package com.quimia.android.presentation.screens.catalog

import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import org.junit.Assert.*
import org.junit.Test
import java.nio.ByteBuffer
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class BarcodeCaptureTest {
    @Test fun `numbers preserve leading zeros and invalid input does not consume capture`() {
        val gate = BarcodeCaptureGate()
        assertNull(gate.accept("https://example.com")); assertNull(gate.accept("")); assertNull(gate.accept("123 456"))
        assertNull(gate.accept("1".repeat(65))); assertNull(gate.accept("１２３４"))
        assertEquals("000123456789", gate.accept(" 000123456789 "))
        assertNull(gate.accept("999999999999"))
        val closed = BarcodeCaptureGate(); closed.close(); assertNull(closed.accept("12345678"))
    }
    @Test fun `concurrent frames can trigger only one product lookup`() {
        val gate = BarcodeCaptureGate()
        val executor = Executors.newFixedThreadPool(8)
        try {
            val captures = executor.invokeAll((1..24).map { Callable { gate.accept("001234567890") } }).map { it.get() }
            assertEquals(1, captures.count { it != null })
        } finally { executor.shutdownNow() }
    }
    @Test fun `camera row padding pixel stride buffer offset and crop do not corrupt barcode`() {
        val width = 300; val height = 100; val left = 3; val top = 2; val stride = (width + left) * 2 + 17
        val matrix = MultiFormatWriter().encode("0012345678901234", BarcodeFormat.CODE_128, width, height)
        val buffer = ByteBuffer.allocate(7 + stride * (height + top))
        buffer.position(7)
        val expected = ByteArray(width * height)
        for (y in 0 until height) for (x in 0 until width) {
            val pixel = if (matrix[x, y]) 0.toByte() else 255.toByte()
            expected[y * width + x] = pixel
            buffer.put(7 + (top + y) * stride + (left + x) * 2, pixel)
        }
        val packed = copyCatalogLuminance(buffer, width, height, stride, 2, left, top)
        assertArrayEquals(expected, packed)
        assertEquals("0012345678901234", decodeCatalogLuminance(width, height, packed))
    }
    @Test fun `live reader supports portrait and landscape and ignores blank frames`() {
        val code = "7891234567895"
        val matrix = MultiFormatWriter().encode(code, BarcodeFormat.EAN_13, 420, 180)
        var width = matrix.width; var height = matrix.height
        var pixels = ByteArray(width * height) { if (matrix[it % width, it / width]) 0 else 255.toByte() }
        repeat(4) {
            assertEquals(code, decodeCatalogLuminance(width, height, pixels))
            val rotated = ByteArray(pixels.size)
            for (y in 0 until height) for (x in 0 until width) rotated[x * height + height - y - 1] = pixels[y * width + x]
            pixels = rotated
            val oldWidth = width; width = height; height = oldWidth
        }
        assertNull(decodeCatalogLuminance(30, 30, ByteArray(900) { 255.toByte() }))
    }
}
