package theme

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val LocalQuimiaSkeleton = staticCompositionLocalOf { false }

/** Keep the real component's measured bounds, hiding its data and interactions while loading. */
fun Modifier.quimiaSkeleton(radius: Dp = 24.dp): Modifier = composed {
    if (!LocalQuimiaSkeleton.current) return@composed this
    val color = quimiaColorTokens().textPrimary.copy(alpha = .16f)
    this.drawWithContent { drawRoundRect(color, cornerRadius = CornerRadius(radius.toPx())) }
        .clearAndSetSemantics { stateDescription = "Carregando" }
        .pointerInput(Unit) {
            awaitEachGesture { do { val event = awaitPointerEvent(); event.changes.forEach { it.consume() } } while (event.changes.any { it.pressed }) }
        }
}
