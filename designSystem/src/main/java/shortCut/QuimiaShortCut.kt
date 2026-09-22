package shortCut

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Circle
import com.composables.icons.lucide.Lucide
import theme.quimiaColorTokens
import theme.QuimiaTheme

enum class QuimiaShortCutType {
    Large,
    Small,
}

enum class QuimiaShortCutColor {
    White,
    Gray,
    Green,
}

@Composable
fun QuimiaShortCut(
    type: QuimiaShortCutType,
    color: QuimiaShortCutColor,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    text: String = "ShortCut",
    tagText: String = "Tag",
    onClick: () -> Unit = {},
) {
    val tokens = quimiaColorTokens()
    val isGreen = color == QuimiaShortCutColor.Green

    val containerColor = when (color) {
        QuimiaShortCutColor.White -> tokens.surfaceBase
        QuimiaShortCutColor.Gray -> tokens.secondary
        QuimiaShortCutColor.Green -> tokens.brandAccent
    }
    val textColor = tokens.foregroundPrimary
    val iconColor = if (isGreen) tokens.white else tokens.foregroundDisabled
    val indicatorColor = if (isGreen) tokens.white else tokens.brandAccent
    val indicatorContentColor = if (isGreen) tokens.foregroundPrimary else tokens.white

    val componentModifier = when (type) {
        QuimiaShortCutType.Large -> modifier.size(width = 104.dp, height = 104.dp)
        QuimiaShortCutType.Small -> modifier.size(width = 160.dp, height = 56.dp)
    }
    val shape = when (type) {
        QuimiaShortCutType.Large -> RoundedCornerShape(24.dp)
        QuimiaShortCutType.Small -> CircleShape
    }

    Surface(
        onClick = onClick,
        modifier = componentModifier,
        shape = shape,
        color = containerColor,
    ) {
        when (type) {
            QuimiaShortCutType.Large -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp),
                        )

                        Box(
                            modifier = Modifier
                                .background(indicatorColor, CircleShape)
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = tagText,
                                color = indicatorContentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 14.sp,
                            )
                        }
                    }

                    Text(
                        text = text,
                        color = textColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 24.sp,
                    )
                }
            }

            QuimiaShortCutType.Small -> {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp),
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = text,
                        color = textColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 24.sp,
                        modifier = Modifier.weight(1f),
                    )

                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(indicatorColor, CircleShape),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 400)
@Composable
private fun QuimiaShortCutPreview() {
    QuimiaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            QuimiaShortCut(
                type = QuimiaShortCutType.Large,
                color = QuimiaShortCutColor.Gray,
                icon = Lucide.Circle,
                )
        }
    }
}
