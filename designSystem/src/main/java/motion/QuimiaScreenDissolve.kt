package motion

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.zIndex

internal val LocalQuimiaScreenDissolve = staticCompositionLocalOf { false }
val LocalQuimiaNavigationChromeVisible = staticCompositionLocalOf { true }

/** Fades the actual outgoing and incoming destinations, including their initial loading state.
 * Shared navigation is rendered once, outside both fading layers, when both screens have it.
 */
@Composable
@OptIn(androidx.compose.animation.ExperimentalAnimationApi::class)
fun <T> QuimiaScreenDissolve(
    target: T,
    modifier: Modifier = Modifier,
    hasNavigation: (T) -> Boolean = { false },
    navigation: @Composable (T) -> Unit = {},
    content: @Composable (T) -> Unit,
) {
    val transition = updateTransition(target, label = "screenDissolve")
    val sharedNavigation = (transition.currentState != transition.targetState || transition.isRunning) &&
        hasNavigation(transition.currentState) && hasNavigation(transition.targetState)
    val navigationVisible = LocalQuimiaNavigationChromeVisible.current
    Box(modifier) {
        transition.Crossfade(
            modifier = Modifier.fillMaxSize(),
            animationSpec = QuimiaMotion.tweenSpec(LocalQuimiaMotionEnabled.current, QuimiaMotion.ScreenDissolve),
        ) { destination ->
            val current = destination == target
            val inactive = if (current) Modifier else Modifier.clearAndSetSemantics { }.pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
                }
            }
            Box(Modifier.fillMaxSize().zIndex(if (current) 1f else 0f).then(inactive)) {
                CompositionLocalProvider(
                    LocalQuimiaScreenDissolve provides true,
                    LocalQuimiaNavigationChromeVisible provides (navigationVisible && !sharedNavigation),
                ) { content(destination) }
            }
        }
        if (sharedNavigation) navigation(target)
    }
}
