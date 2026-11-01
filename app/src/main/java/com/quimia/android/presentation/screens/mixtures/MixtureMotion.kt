package com.quimia.android.presentation.screens.mixtures

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import motion.LocalQuimiaMotionEnabled
import theme.QuimiaTextStyles
import theme.quimiaColorTokens
import kotlin.math.PI
import kotlin.math.sin

/** Visual feedback for the real request; never encodes a chemical result or simulates progress. */
@Composable
internal fun MixtureAnalysisMotion(ingredients: List<MixtureIngredient>) {
    val tokens = quimiaColorTokens()
    val enabled = LocalQuimiaMotionEnabled.current
    val entrance = remember { Animatable(if (enabled) 0f else 1f) }
    LaunchedEffect(enabled) { if (enabled) entrance.animateTo(1f, tween(550, easing = FastOutSlowInEasing)) }
    val phase = if (enabled) rememberInfiniteTransition(label = "mixtureAnalysis").animateFloat(0f, 1f,
        infiniteRepeatable(tween(1800, easing = LinearEasing)), label = "mixtureBreathing").value else .5f
    Box(Modifier.fillMaxSize().background(tokens.surfaceBackground)
        .pointerInput(Unit) { detectTapGestures { } }
        .semantics { liveRegion = LiveRegionMode.Polite; contentDescription = "Analisando combinação" },
        contentAlignment = Alignment.Center) {
        Column(Modifier.widthIn(max = 400.dp).fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp)) {
            Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(200.dp)) {
                    repeat(5) { index ->
                        val point = (phase + index / 5f) % 1f
                        drawCircle(tokens.brandSecondary.copy(alpha = (1f - point) * .36f),
                            radius = (5f + index % 2 * 3f) * density,
                            center = androidx.compose.ui.geometry.Offset(size.width * (.28f + index * .11f), size.height * (.9f - point * .8f)))
                    }
                }
                ingredients.take(2).forEachIndexed { index, ingredient ->
                    val side = if (index == 0) -1f else 1f
                    MixtureIngredientImage(ingredient,
                        Modifier.size(96.dp, 160.dp).graphicsLayer {
                            translationX = side * (42f + 24f * (1f - entrance.value)) * density
                            translationY = sin(phase * 2f * PI.toFloat() + index * PI.toFloat()) * 4f * density
                            rotationZ = side * (9f + 3f * sin(phase * 2f * PI.toFloat())) * entrance.value
                            alpha = entrance.value
                        }, history = true)
                }
            }
            Text("Analisando combinação", style = QuimiaTextStyles.medium24, color = tokens.foregroundPrimary)
            Text("Conferindo as informações dos itens selecionados…", style = QuimiaTextStyles.medium16,
                color = tokens.foregroundSubtle, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

/** Staggered result entrance also respects Android's reduced-motion setting. */
internal fun Modifier.mixtureResultEntrance(index: Int = 0): Modifier = composed {
    val enabled = LocalQuimiaMotionEnabled.current
    val progress = remember { Animatable(if (enabled) 0f else 1f) }
    LaunchedEffect(enabled) {
        if (enabled) { delay(index * 55L); progress.animateTo(1f, tween(320, easing = FastOutSlowInEasing)) }
        else progress.snapTo(1f)
    }
    graphicsLayer { alpha = progress.value; translationY = (1f - progress.value) * 14.dp.toPx() }
}
