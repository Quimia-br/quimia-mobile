package motion

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun QuimiaExpand(visible: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val enabled = LocalQuimiaMotionEnabled.current
    AnimatedVisibility(visible, modifier,
        enter = expandVertically(QuimiaMotion.tweenSpec(enabled)) + fadeIn(QuimiaMotion.tweenSpec(enabled)),
        exit = shrinkVertically(QuimiaMotion.tweenSpec(enabled)) + fadeOut(QuimiaMotion.tweenSpec(enabled, QuimiaMotion.Quick))) {
        content()
    }
}
