package com.quimia.android.presentation.screens.kemi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.quimia.android.R

/** The original high-resolution image with the crop from Figma 2035:6273. */
@Composable
internal fun kemiPortraitPainter(): Painter {
    val original = painterResource(R.drawable.kemi_character)
    return remember(original) {
        object : Painter() {
            override val intrinsicSize = Size(140f, 139f)
            override fun DrawScope.onDraw() {
                val imageSize = Size(size.width * 1.345f, size.height * 1.0128f)
                withTransform({ translate(size.width * -.0646f, size.height * -.0128f) }) {
                    with(original) { draw(imageSize) }
                }
            }
        }
    }
}
