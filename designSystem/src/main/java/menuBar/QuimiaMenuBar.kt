package menuBar

import motion.LocalQuimiaMotionEnabled
import motion.LocalQuimiaNavigationProgress
import motion.QuimiaMotion
import motion.quimiaPress
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.semantics.Role
import kotlin.math.roundToInt
import buttons.QuimiaFABSize
import com.composables.icons.lucide.Bath
import com.composables.icons.lucide.GalleryVerticalEnd
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.SoapDispenserDroplet
import theme.QuimiaTheme
import theme.RadiusFull
import theme.quimiaColorTokens
import theme.DarkTokens
import theme.QuimiaColorTokens
import theme.quimiaHighContrastEnabled
import theme.quimiaContrastOutline

fun resolveQuimiaMenuBarIconColor(tokens: QuimiaColorTokens, highContrast: Boolean) =
    if (highContrast) DarkTokens.foregroundSubtle else tokens.foregroundSubtle

val QuimiaMenuBarHeight = 84.dp
val QuimiaMenuBarBottomSpacing = 16.dp

@Composable
fun QuimiaMenuBarBackdrop(
    modifier: Modifier = Modifier,
    barHeight: Dp = QuimiaMenuBarHeight,
) {
    val background = quimiaColorTokens().surfaceBackground
    Column(modifier.fillMaxWidth()) {
        Box(
            Modifier.fillMaxWidth().height(48.dp).background(
                Brush.verticalGradient(
                    listOf(
                        background.copy(alpha = 0f),
                        background.copy(alpha = 0.55f),
                        background.copy(alpha = 0.9f),
                        background.copy(alpha = 0.98f),
                        background,
                    ),
                ),
            ),
        )
        Box(
            Modifier.fillMaxWidth().background(background).navigationBarsPadding()
                .height(barHeight + QuimiaMenuBarBottomSpacing),
        )
    }
}

@Composable
fun QuimiaMenuBar(
    modifier: Modifier = Modifier,
    items: List<Any>? = null,
    selectedIndex: Int = 0,
    onItemSelected: (index: Int) -> Unit = {},
    fabSpacing: Dp = 12.dp,
    fab: (@Composable () -> Unit)? = null,
) {
    val tokens = quimiaColorTokens()
    val iconColor = resolveQuimiaMenuBarIconColor(tokens, quimiaHighContrastEnabled())
    val icons = items ?: listOf(
        Lucide.House,
        SoapDispenserDroplet,
        Lucide.GalleryVerticalEnd,
        Lucide.MapPin,
        Lucide.Bath,
    )
    val labels = listOf("Início", "Misturas", "Minha estante", "Pontos de descarte", "Loja")
    val localPosition = animateFloatAsState(selectedIndex.toFloat(), QuimiaMotion.settleSpec(LocalQuimiaMotionEnabled.current), label = "QuimiaMenuPosition")
    val sharedPosition = LocalQuimiaNavigationProgress.current?.takeIf { it.target == selectedIndex }?.position
    val position = sharedPosition ?: localPosition

    androidx.compose.runtime.CompositionLocalProvider(theme.LocalQuimiaSkeleton provides false) {
    Box(modifier = modifier.fillMaxWidth().height(if (fab == null) QuimiaMenuBarHeight else QuimiaMenuBarHeight + QuimiaFABSize + fabSpacing)) {
        BoxWithConstraints(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(QuimiaMenuBarHeight)
                .background(tokens.secondary, RoundedCornerShape(RadiusFull.dp))
                .quimiaContrastOutline(RoundedCornerShape(RadiusFull.dp))
                .padding(horizontal = 10.dp, vertical = 14.dp),
        ) {
            val itemWidth = maxWidth / icons.size.coerceAtLeast(1)
            if (selectedIndex in icons.indices) Box(
                Modifier.offset { IntOffset((position.value.coerceIn(0f, (icons.size - 1).toFloat()) * itemWidth.toPx()).roundToInt(), 0) }
                    .width(itemWidth).height(56.dp).background(tokens.white, CircleShape),
            )
            Row(Modifier.fillMaxWidth().selectableGroup(), verticalAlignment = Alignment.CenterVertically) {
            icons.forEachIndexed { index, element ->
                val interactions = remember(index) { MutableInteractionSource() }
                val pressed by interactions.collectIsPressedAsState()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .quimiaPress(pressed)
                        .clip(CircleShape)
                        .selectable(selected = index == selectedIndex, role = Role.Tab, interactionSource = interactions, indication = null) {
                            onItemSelected(index)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    when (element) {
                        is ImageVector -> Icon(
                            imageVector = element,
                            contentDescription = labels.getOrNull(index),
                            tint = iconColor,
                            modifier = Modifier.size(30.dp),
                        )
                        is Int -> Icon(
                            painter = painterResource(element),
                            contentDescription = labels.getOrNull(index),
                            tint = iconColor,
                            modifier = Modifier.size(30.dp),
                        )
                    }
                }
            }
            }
        }
        fab?.let {
            Box(
                modifier = Modifier.align(Alignment.TopEnd),
            ) { it() }
        }
    }
    }
}

@preview.QuimiaDevicePreviews
@Composable
private fun PreviewQuimiaMenuBar() {
    QuimiaTheme(darkTheme = false) { QuimiaMenuBar() }
}
