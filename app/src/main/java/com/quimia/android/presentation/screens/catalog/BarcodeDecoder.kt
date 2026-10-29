package com.quimia.android.presentation.screens.catalog

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

internal suspend fun decodeCatalogBarcode(context: Context, uri: String): String = withContext(Dispatchers.IO) {
    val image = Uri.parse(uri)
    require(image.scheme == "content")
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(image).use { BitmapFactory.decodeStream(it, null, bounds) }
    require(bounds.outWidth > 0 && bounds.outHeight > 0)
    var sample = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 2000) sample *= 2
    val bitmap = context.contentResolver.openInputStream(image).use {
        BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
    } ?: throw IllegalArgumentException("Imagem inválida")
    try {
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        decodeCatalogPixels(bitmap.width, bitmap.height, pixels).also { coroutineContext.ensureActive() }
            ?: throw NotFoundException.getNotFoundInstance()
    } finally { bitmap.recycle() }
}

internal fun decodeCatalogPixels(width: Int, height: Int, pixels: IntArray): String? {
    val hints = mapOf(DecodeHintType.TRY_HARDER to true, DecodeHintType.POSSIBLE_FORMATS to listOf(
        BarcodeFormat.EAN_13, BarcodeFormat.EAN_8, BarcodeFormat.UPC_A, BarcodeFormat.UPC_E, BarcodeFormat.CODE_128, BarcodeFormat.ITF))
    val reader = MultiFormatReader()
    var currentPixels = pixels
    var currentWidth = width
    var currentHeight = height
    repeat(4) {
        val source = RGBLuminanceSource(currentWidth, currentHeight, currentPixels)
        try {
            val result = reader.decode(BinaryBitmap(HybridBinarizer(source)), hints).text
                ?.takeIf { it.length in 1..64 && it.all { char -> char in '0'..'9' } }
            if (result != null) return result
        } catch (_: NotFoundException) { } finally { reader.reset() }
        val rotated = IntArray(currentPixels.size)
        for (y in 0 until currentHeight) for (x in 0 until currentWidth) {
            rotated[x * currentHeight + currentHeight - y - 1] = currentPixels[y * currentWidth + x]
        }
        currentPixels = rotated
        val oldWidth = currentWidth
        currentWidth = currentHeight
        currentHeight = oldWidth
    }
    return null
}
