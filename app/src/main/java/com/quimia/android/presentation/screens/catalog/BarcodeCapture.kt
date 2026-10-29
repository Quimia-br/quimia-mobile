package com.quimia.android.presentation.screens.catalog

import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

internal fun catalogBarcodeNumber(value: String?): String? = value?.trim()?.takeIf {
    it.length in 1..64 && it.all { char -> char in '0'..'9' }
}
internal class BarcodeCaptureGate {
    private val finished = AtomicBoolean(false)
    fun accept(raw: String?): String? {
        val code = catalogBarcodeNumber(raw) ?: return null
        return code.takeIf { finished.compareAndSet(false, true) }
    }
    fun close() { finished.set(true) }
}

internal fun copyCatalogLuminance(buffer: ByteBuffer, width: Int, height: Int, rowStride: Int, pixelStride: Int,
    left: Int = 0, top: Int = 0): ByteArray {
    require(width > 0 && height > 0 && rowStride > 0 && pixelStride > 0 && left >= 0 && top >= 0)
    val start = buffer.position().toLong() + top.toLong() * rowStride + left.toLong() * pixelStride
    val end = start + (height - 1).toLong() * rowStride + (width - 1).toLong() * pixelStride
    require(end < buffer.limit() && width.toLong() * height <= Int.MAX_VALUE)
    return ByteArray(width * height) { index -> buffer.get((start + (index / width).toLong() * rowStride + (index % width).toLong() * pixelStride).toInt()) }
}

internal fun decodeCatalogLuminance(width: Int, height: Int, pixels: ByteArray): String? {
    require(width > 0 && height > 0 && width.toLong() * height == pixels.size.toLong())
    val hints = mapOf(DecodeHintType.TRY_HARDER to true, DecodeHintType.POSSIBLE_FORMATS to listOf(
        BarcodeFormat.EAN_13, BarcodeFormat.EAN_8, BarcodeFormat.UPC_A, BarcodeFormat.UPC_E, BarcodeFormat.CODE_128, BarcodeFormat.ITF))
    val reader = MultiFormatReader()
    var data = pixels
    var w = width
    var h = height
    repeat(4) {
        try {
            val source = PlanarYUVLuminanceSource(data, w, h, 0, 0, w, h, false)
            catalogBarcodeNumber(reader.decode(BinaryBitmap(HybridBinarizer(source)), hints).text)?.let { return it }
        } catch (_: NotFoundException) { } finally { reader.reset() }
        val rotated = ByteArray(data.size)
        for (y in 0 until h) for (x in 0 until w) rotated[x * h + h - y - 1] = data[y * w + x]
        data = rotated
        val previousWidth = w; w = h; h = previousWidth
    }
    return null
}
