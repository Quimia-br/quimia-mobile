package com.composables.icons.lucide

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Lucide Astroid icon.
 *
 * Kept locally because this icon was released after the Lucide Compose
 * dependency currently used by the project.
 */
val Lucide.Astroid: ImageVector
    get() {
        if (_astroid != null) return _astroid!!

        _astroid = ImageVector.Builder(
            name = "Astroid",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(12.983f, 21.186f)
                arcToRelative(1f, 1f, 0f, false, true, -1.966f, 0f)
                arcToRelative(10f, 10f, 0f, false, false, -8.203f, -8.203f)
                arcToRelative(1f, 1f, 0f, false, true, 0f, -1.966f)
                arcToRelative(10f, 10f, 0f, false, false, 8.203f, -8.203f)
                arcToRelative(1f, 1f, 0f, false, true, 1.966f, 0f)
                arcToRelative(10f, 10f, 0f, false, false, 8.203f, 8.203f)
                arcToRelative(1f, 1f, 0f, false, true, 0f, 1.966f)
                arcToRelative(10f, 10f, 0f, false, false, -8.203f, 8.203f)
                close()
            }
        }.build()

        return _astroid!!
    }

private var _astroid: ImageVector? = null
