package menuBar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import motion.*
import theme.quimiaColorTokens
import kotlin.math.roundToInt

internal fun liquidMenuPosition(x: Float, width: Float, count: Int, rtl: Boolean): Float {
    if (count <= 1 || width <= 0f) return 0f
    val relativeX = if (rtl) width - x else x
    return (relativeX / (width / count) - .5f).coerceIn(0f, (count - 1).toFloat())
}

internal fun Modifier.liquidMenuDrag(
    progress: QuimiaNavigationProgress, count: Int, rtl: Boolean,
    selected: State<Int>, onSelected: State<(Int) -> Unit>,
): Modifier = pointerInput(progress.drag, count, rtl) {
    var canCommit = false
    detectHorizontalDragGestures(
        onDragStart = { point ->
            if (count > 0 && selected.value in 0 until count) {
                canCommit = true
                progress.drag.begin(liquidMenuPosition(point.x, size.width.toFloat(), count, rtl))
            }
        },
        onHorizontalDrag = { change, _ ->
            if (progress.drag.dragging) {
                change.consume()
                canCommit = change.position.y in -size.height.toFloat()..size.height * 2f
                progress.drag.move(liquidMenuPosition(change.position.x, size.width.toFloat(), count, rtl))
            }
        },
        onDragEnd = {
            if (progress.drag.dragging) {
                val target = progress.drag.position.roundToInt().coerceIn(0, count - 1)
                progress.drag.finish()
                if (canCommit && target != selected.value) onSelected.value(target)
            }
        },
        onDragCancel = { progress.drag.finish() },
    )
}

/** Only a few vector shapes; no live blur, shaders, bitmaps or idle animation. */
@Composable
internal fun QuimiaLiquidMenuIndicator(progress: QuimiaNavigationProgress, count: Int, modifier: Modifier = Modifier) {
    val tokens = quimiaColorTokens()
    val motion = LocalQuimiaMotionEnabled.current
    val compact = LocalQuimiaRenderingBudget.current == QuimiaRenderingBudget.Compact
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val engagement = animateFloatAsState(if (progress.drag.dragging && motion) 1f else 0f,
        QuimiaMotion.tweenSpec(motion, 180), label = "liquidMenuTouch")
    Canvas(modifier) {
        if (count <= 0) return@Canvas
        val amount = engagement.value
        val itemWidth = size.width / count
        val value = progress.position.value.coerceIn(0f, (count - 1).toFloat())
        val logicalX = (value + .5f) * itemWidth
        val x = if (rtl) size.width - logicalX else logicalX
        val centerY = size.height / 2
        val direction = progress.drag.direction * if (rtl) -1f else 1f
        val pillWidth = itemWidth * (1f + .06f * amount)
        val pillHeight = size.height * (1f - .04f * amount)
        if (amount > .001f) {
            repeat(if (compact) 2 else 4) { index ->
                val radius = (8f - index * 1.5f).dp.toPx() * amount
                val distance = pillWidth * .42f + (index + 1) * 12.dp.toPx() * amount
                val bubbleX = (x - direction * distance).coerceIn(radius, (size.width - radius).coerceAtLeast(radius))
                val bubbleY = centerY + (if (index % 2 == 0) -1 else 1) * (5 + index * 2).dp.toPx() * amount
                val center = Offset(bubbleX, bubbleY)
                drawCircle(tokens.primary.copy(alpha = .22f * amount), radius, center)
                drawCircle(tokens.white.copy(alpha = .6f * amount), radius, center, style = Stroke(1.dp.toPx()))
                drawCircle(tokens.white.copy(alpha = .85f * amount), radius * .2f, center - Offset(radius * .3f, radius * .35f))
            }
        }
        val left = (x - pillWidth / 2).coerceIn(0f, (size.width - pillWidth).coerceAtLeast(0f))
        val top = (size.height - pillHeight) / 2
        drawRoundRect(tokens.white, Offset(left, top), Size(pillWidth.coerceAtMost(size.width), pillHeight), CornerRadius(pillHeight / 2))
        if (amount > .001f) {
            drawRoundRect(Brush.verticalGradient(listOf(tokens.white.copy(alpha = .8f * amount),
                tokens.primary.copy(alpha = .12f * amount)), startY = top, endY = top + pillHeight),
                Offset(left, top), Size(pillWidth.coerceAtMost(size.width), pillHeight), CornerRadius(pillHeight / 2))
            drawRoundRect(tokens.primary.copy(alpha = .32f * amount), Offset(left + 1.dp.toPx(), top + 1.dp.toPx()),
                Size((pillWidth - 2.dp.toPx()).coerceIn(0f, (size.width - 2.dp.toPx()).coerceAtLeast(0f)), pillHeight - 2.dp.toPx()),
                CornerRadius(pillHeight / 2), style = Stroke(1.dp.toPx()))
        }
    }
}
