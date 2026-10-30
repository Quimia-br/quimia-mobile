package motion

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import theme.LocalQuimiaSkeleton

@Composable
fun QuimiaExpand(visible: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val enabled = LocalQuimiaMotionEnabled.current && !LocalQuimiaSkeleton.current
    AnimatedVisibility(visible, modifier,
        enter = expandVertically(QuimiaMotion.tweenSpec(enabled, QuimiaMotion.Expansion), expandFrom = Alignment.Top) + fadeIn(QuimiaMotion.tweenSpec(enabled, QuimiaMotion.Feedback)),
        exit = shrinkVertically(QuimiaMotion.tweenSpec(enabled, QuimiaMotion.Expansion), shrinkTowards = Alignment.Top) + fadeOut(QuimiaMotion.tweenSpec(enabled, QuimiaMotion.Quick))) {
        Column { content() }
    }
}

/** For compact confirmations and badges. Sheets already animate themselves and do not use this. */
@Composable
fun QuimiaReveal(visible: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val enabled = LocalQuimiaMotionEnabled.current && !LocalQuimiaSkeleton.current
    AnimatedVisibility(visible, modifier,
        enter = fadeIn(QuimiaMotion.tweenSpec(enabled, QuimiaMotion.Feedback)) + scaleIn(QuimiaMotion.settleSpec(enabled), initialScale = .9f),
        exit = fadeOut(QuimiaMotion.tweenSpec(enabled, QuimiaMotion.Quick))) { content() }
}

@Composable
fun <T> QuimiaStateContent(target: T, modifier: Modifier = Modifier, content: @Composable (T) -> Unit) {
    Crossfade(targetState = target, modifier = modifier,
        animationSpec = QuimiaMotion.tweenSpec(LocalQuimiaMotionEnabled.current && !LocalQuimiaSkeleton.current, QuimiaMotion.Feedback),
        label = "QuimiaStateContent") { state ->
        Box(if (state == target) Modifier else Modifier.clearAndSetSemantics { }) { content(state) }
    }
}
