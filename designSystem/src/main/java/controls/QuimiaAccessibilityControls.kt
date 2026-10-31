package controls

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import theme.QuimiaTextStyles
import feedback.LocalQuimiaSound
import feedback.QuimiaSound
import motion.QuimiaMotion
import motion.LocalQuimiaMotionEnabled
import motion.quimiaAnimatedColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import theme.quimiaColorTokens
import theme.quimiaHighContrastEnabled
import kotlin.math.roundToInt

@Composable
fun QuimiaSwitch(
    checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier,
    contentDescription: String? = null,
    outline: Boolean = false,
) {
    val tokens = quimiaColorTokens()
    val thumbOffset by animateDpAsState(if (checked) 26.323.dp else 0.dp,
        animationSpec = QuimiaMotion.settleSpec(LocalQuimiaMotionEnabled.current), label = "QuimiaSwitchThumb")
    val thumbColor = quimiaAnimatedColor(if (checked) tokens.primary else tokens.foregroundSubtle)
    Box(modifier.width(50.323.dp).height(48.dp)
        .semantics {
            if (contentDescription != null) this.contentDescription = contentDescription
            stateDescription = if (checked) "Ativada" else "Desativada"
        }
        .toggleable(value = checked, role = Role.Switch, interactionSource = remember { MutableInteractionSource() },
            indication = null, onValueChange = onCheckedChange), contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxWidth().height(24.dp).background(tokens.secondary, CircleShape)
            .then(if (outline || quimiaHighContrastEnabled()) Modifier.border(3.dp, tokens.borderFocus, CircleShape) else Modifier)) {
            Box(Modifier.offset(x = thumbOffset).size(24.dp).background(thumbColor, CircleShape))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuimiaTextSizeSlider(step: Int, onStepChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    val tokens = quimiaColorTokens()
    val motion = LocalQuimiaMotionEnabled.current
    val sound = LocalQuimiaSound.current
    val haptic = LocalHapticFeedback.current
    var draft by remember { mutableFloatStateOf(step.coerceIn(0, 2).toFloat()) }
    var scrubbing by remember { mutableStateOf(false) }
    LaunchedEffect(step) { if (!scrubbing) draft = step.coerceIn(0, 2).toFloat() }
    val position by animateFloatAsState(draft,
        animationSpec = if (scrubbing) snap() else QuimiaMotion.controlSpec(motion), label = "textSizePosition")
    val thumbWidth by animateDpAsState(if (scrubbing) 34.dp else 28.dp,
        QuimiaMotion.controlSpec(motion), label = "textSizeThumbWidth")
    val thumbHeight by animateDpAsState(if (scrubbing) 24.dp else 20.dp,
        QuimiaMotion.controlSpec(motion), label = "textSizeThumbHeight")
    fun commit(value: Float) {
        if (!value.isFinite()) return
        val selected = value.roundToInt().coerceIn(0, 2)
        draft = selected.toFloat(); scrubbing = false
        if (selected != step) {
            onStepChange(selected)
            sound(QuimiaSound.Selection)
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }
    Column(modifier.heightIn(min = 66.dp)) {
        Slider(value = position, onValueChange = { draft = it; scrubbing = true },
            onValueChangeFinished = { commit(draft) }, valueRange = 0f..2f, steps = 0,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).onPreviewKeyEvent { event ->
                val direction = when (event.key) { Key.DirectionRight, Key.DirectionUp -> 1; Key.DirectionLeft, Key.DirectionDown -> -1; else -> 0 }
                if (direction == 0) false else {
                    if (event.type == KeyEventType.KeyDown) commit(step + direction.toFloat())
                    true
                }
            }.clearAndSetSemantics {
                contentDescription = "Tamanho do texto"
                stateDescription = "${100 + draft.roundToInt().coerceIn(0, 2) * 20} por cento"
                progressBarRangeInfo = ProgressBarRangeInfo(draft.coerceIn(0f, 2f), 0f..2f, 1)
                setProgress { value -> commit(value); true }
            },
            thumb = {
                Box(Modifier.size(thumbWidth, thumbHeight).background(tokens.success, CircleShape)
                    .then(if (quimiaHighContrastEnabled()) Modifier.border(2.dp, tokens.foregroundPrimary, CircleShape) else Modifier))
            }, track = {
                Canvas(Modifier.fillMaxWidth().height(4.dp)) {
                    val center = size.height / 2
                    drawLine(tokens.borderPrimary, Offset(0f, center), Offset(size.width, center), 4.dp.toPx(), StrokeCap.Round)
                    drawLine(tokens.success, Offset(0f, center), Offset(size.width * (position / 2).coerceIn(0f, 1f), center), 4.dp.toPx(), StrokeCap.Round)
                    repeat(3) { index ->
                        drawCircle(if (index <= draft.roundToInt()) tokens.success else tokens.foregroundSubtle,
                            3.dp.toPx(), Offset(size.width * index / 2, center))
                    }
                }
            })
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            repeat(3) { index -> Text("${100 + index * 20}%", style = QuimiaTextStyles.medium12,
                color = if (index == draft.roundToInt()) tokens.foregroundPrimary else tokens.foregroundSubtle) }
        }
    }
}
