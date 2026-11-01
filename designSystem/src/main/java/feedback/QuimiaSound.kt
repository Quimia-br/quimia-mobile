package feedback

import androidx.compose.runtime.staticCompositionLocalOf

enum class QuimiaSound { Tap, Navigation, Selection, Success, Error }

/** The app owns playback, preferences and the system silent-mode policy. */
val LocalQuimiaSound = staticCompositionLocalOf<(QuimiaSound) -> Unit> { {} }

/** Optional feedback for the explicit sound-preview control, without platform dependencies in the UI kit. */
val LocalQuimiaSoundStatus = staticCompositionLocalOf<() -> String?> { { null } }
