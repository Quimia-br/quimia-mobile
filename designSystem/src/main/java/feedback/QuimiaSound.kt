package feedback

import androidx.compose.runtime.staticCompositionLocalOf

enum class QuimiaSound { Tap, Navigation, Selection, Success, Error }

/** The app owns playback, preferences and the system silent-mode policy. */
val LocalQuimiaSound = staticCompositionLocalOf<(QuimiaSound) -> Unit> { {} }
