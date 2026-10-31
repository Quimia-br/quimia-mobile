package com.quimia.android.presentation.components

import android.graphics.Bitmap
import coil.size.Size
import coil.transform.Transformation

/** Removes only an almost-white backdrop connected to the image boundary.
 * Existing transparency, enclosed white labels and ordinary photographic backgrounds survive.
 */
internal fun productPhotoCutout(pixels: IntArray, width: Int, height: Int): IntArray {
    require(width > 0 && height > 0 && pixels.size == width * height)
    fun white(pixel: Int): Boolean {
        val r = (pixel ushr 16) and 255; val g = (pixel ushr 8) and 255; val b = pixel and 255
        return (pixel ushr 24) >= 250 && minOf(r, g, b) >= 235 && maxOf(r, g, b) - minOf(r, g, b) <= 16
    }
    val edge = (pixels.indices).filter { it < width || it >= pixels.size - width || it % width == 0 || it % width == width - 1 }
    // Do not mistake a patch of white sky or an already-transparent PNG for a studio backdrop.
    if (edge.count { white(pixels[it]) } < edge.size * .85) return pixels
    val output = pixels.copyOf()
    val seen = BooleanArray(pixels.size)
    val queue = IntArray(pixels.size)
    var start = 0; var end = 0
    fun enqueue(index: Int) {
        if (!seen[index] && white(pixels[index])) {
            seen[index] = true; queue[end++] = index
        }
    }
    edge.forEach(::enqueue)
    while (start < end) {
        val index = queue[start++]
        output[index] = 0
        if (index >= width) enqueue(index - width)
        if (index < pixels.size - width) enqueue(index + width)
        if (index % width > 0) enqueue(index - 1)
        if (index % width < width - 1) enqueue(index + 1)
    }
    return output
}

internal class ProductPhotoCutout : Transformation {
    override val cacheKey = "product-photo-edge-cutout-v1"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val pixels = IntArray(input.width * input.height)
        input.getPixels(pixels, 0, input.width, 0, 0, input.width, input.height)
        val cutout = productPhotoCutout(pixels, input.width, input.height)
        if (cutout === pixels) return input
        val output = Bitmap.createBitmap(input.width, input.height, Bitmap.Config.ARGB_8888)
        output.setHasAlpha(true)
        output.setPixels(cutout, 0, input.width, 0, 0, input.width, input.height)
        return output
    }
}
