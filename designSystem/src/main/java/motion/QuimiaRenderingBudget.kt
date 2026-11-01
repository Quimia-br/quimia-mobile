package motion

import androidx.compose.runtime.staticCompositionLocalOf

/** A compact device keeps feedback, but avoids composing two complete destinations at once. */
enum class QuimiaRenderingBudget { Standard, Compact }

val LocalQuimiaRenderingBudget = staticCompositionLocalOf { QuimiaRenderingBudget.Standard }
