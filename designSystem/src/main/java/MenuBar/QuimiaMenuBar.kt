package MenuBar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.GalleryVerticalEnd
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.ToolCase
import theme.RadiusFull
import theme.quimiaColorTokens

@Composable
fun QuimiaMenuBar(
    modifier: Modifier = Modifier,
    items: List<Any>? = null,
    defaultIndex: Int = 0,
    onItemSelected: (index: Int) -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }

    val tokens = quimiaColorTokens()

    val icons = items ?: listOf(
        Lucide.House,
        Lucide.GalleryVerticalEnd,
        Lucide.ToolCase,
        Lucide.MapPin,
        Lucide.Sparkles
    )

    var selectedIndex by remember {
        mutableIntStateOf(
            defaultIndex.coerceIn(
                0,
                icons.lastIndex.coerceAtLeast(0)
            )
        )
    }

    Row(
        modifier = modifier
            .width(317.dp)
            .height(73.dp)
            .background(
                color = tokens.white,
                shape = RoundedCornerShape(RadiusFull.dp)
            )
            .padding(
                horizontal = 10.dp,
                vertical = 12.dp
            ),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {

        icons.forEachIndexed { index, element ->

            val isActive = index == selectedIndex

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(49.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        selectedIndex = index
                        onItemSelected(index)
                    },
                contentAlignment = Alignment.Center
            ) {

                if (isActive) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                color = tokens.background,
                                shape = RoundedCornerShape(RadiusFull.dp)
                            )
                    )
                }

                if (element is ImageVector) {
                    Icon(
                        imageVector = element,
                        contentDescription = null,
                        tint = tokens.foregroundSubtle,
                        modifier = Modifier
                            .width(22.dp)
                            .height(22.dp)
                    )
                } else if (element is Int) {
                    Icon(
                        painter = painterResource(id = element),
                        contentDescription = null,
                        tint = tokens.foregroundSubtle,
                        modifier = Modifier
                            .width(22.dp)
                            .height(22.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewQuimiaMenuBar() {
    QuimiaMenuBar()
}