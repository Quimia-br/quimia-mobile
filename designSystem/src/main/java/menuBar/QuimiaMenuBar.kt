package menuBar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import com.composables.icons.lucide.Bath
import com.composables.icons.lucide.GalleryVerticalEnd
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.SoapDispenserDroplet
import theme.QuimiaTheme
import theme.RadiusFull
import theme.quimiaColorTokens

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
    val icons = items ?: listOf(
        Lucide.House,
        SoapDispenserDroplet,
        Lucide.GalleryVerticalEnd,
        Lucide.MapPin,
        Lucide.Bath,
    )
    val labels = listOf("Início", "Misturas", "Minha estante", "Pontos de descarte", "Loja")
    val interactionSource = remember { MutableInteractionSource() }

    Box(modifier = modifier.fillMaxWidth().height(if (fab == null) 73.dp else 73.dp + 64.dp + fabSpacing)) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(73.dp)
                .background(tokens.secondary, RoundedCornerShape(RadiusFull.dp))
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icons.forEachIndexed { index, element ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(49.dp)
                        .clip(CircleShape)
                        .background(if (index == selectedIndex) tokens.white else tokens.secondary)
                        .clickable(interactionSource = interactionSource, indication = null) {
                            onItemSelected(index)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    when (element) {
                        is ImageVector -> Icon(
                            imageVector = element,
                            contentDescription = labels.getOrNull(index),
                            tint = tokens.foregroundSubtle,
                            modifier = Modifier.size(22.dp),
                        )
                        is Int -> Icon(
                            painter = painterResource(element),
                            contentDescription = labels.getOrNull(index),
                            tint = tokens.foregroundSubtle,
                            modifier = Modifier.size(22.dp),
                        )
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

@Preview
@Composable
private fun PreviewQuimiaMenuBar() {
    QuimiaTheme(darkTheme = false) { QuimiaMenuBar() }
}
