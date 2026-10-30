package controls

import androidx.compose.animation.core.animateDpAsState
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
    Column(modifier.height(29.dp)) {
        Box(Modifier.fillMaxWidth().height(17.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.fillMaxWidth().height(3.dp).background(tokens.borderPrimary, CircleShape))
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                Slider(value = step.coerceIn(0, 2).toFloat(), onValueChange = { onStepChange(it.roundToInt().coerceIn(0, 2)) },
                    valueRange = 0f..2f, steps = 1,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 7.dp).height(17.dp).semantics {
                        contentDescription = "Tamanho do texto"
                        stateDescription = "${100 + step.coerceIn(0, 2) * 20} por cento"
                    },
                    thumb = { Box(Modifier.size(width = 28.dp, height = 17.dp).background(tokens.success, CircleShape)) },
                    track = { Spacer(Modifier.fillMaxWidth().height(0.dp)) })
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 19.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            repeat(3) { Box(Modifier.size(4.dp).background(tokens.borderPrimary, CircleShape)) }
        }
    }
}
