package shapeIcon

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Circle
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import theme.quimiaColorTokens
import theme.quimiaContrastOutline
import theme.QuimiaTheme


enum class QuimiaShapeIconSize(val value: Dp) {
    Big(100.dp),
    Medium(76.dp),
    Small(50.dp)
}

enum class QuimiaShapeIconTheme { Secondary, Primary, Critical, Warning, Success }

@Composable
fun QuimiaShapeIcon(
    size: Dp,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    theme: QuimiaShapeIconTheme = QuimiaShapeIconTheme.Secondary,
    containerColor: Color? = null,
    iconColor: Color? = null,
    iconSize: Dp = 24.dp,
    contentDescription: String? = null,
    iconPainter: Painter? = null,
) {
    val tokens = quimiaColorTokens()
    val background = containerColor ?: when (theme) {
        QuimiaShapeIconTheme.Secondary -> tokens.surfaceBackground
        QuimiaShapeIconTheme.Primary -> tokens.surfaceBase
        QuimiaShapeIconTheme.Critical -> tokens.error
        QuimiaShapeIconTheme.Warning -> tokens.warning
        QuimiaShapeIconTheme.Success -> tokens.secondary
    }
    val foreground = iconColor ?: if (theme == QuimiaShapeIconTheme.Critical)
        tokens.foregroundError else tokens.foregroundPrimary

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
            .then(
                if (theme == QuimiaShapeIconTheme.Success) {
                    Modifier.border(width = 2.dp, color = tokens.success, shape = CircleShape)
                } else {
                    Modifier
                }
            )
            .quimiaContrastOutline(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (iconPainter != null) Icon(
            painter = iconPainter,
            contentDescription = contentDescription,
            tint = foreground,
            modifier = Modifier.size(iconSize),
        ) else Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = foreground,
            modifier = Modifier.size(iconSize),
        )
    }
}

@Preview(name = "Celular compacto", showBackground = true, backgroundColor = 0xFFE6E6E6, widthDp = 320, heightDp = 720)
@Composable
private fun QuimiaShapeIconCompactPhonePreview() {
    QuimiaShapeIconPreviewContent()
}

@Preview(name = "Celular", showBackground = true, backgroundColor = 0xFFE6E6E6, widthDp = 393, heightDp = 852)
@Composable
private fun QuimiaShapeIconPhonePreview() {
    QuimiaShapeIconPreviewContent()
}

@Preview(name = "Tablet", showBackground = true, backgroundColor = 0xFFE6E6E6, widthDp = 800, heightDp = 1280)
@Composable
private fun QuimiaShapeIconTabletPreview() {
    QuimiaShapeIconPreviewContent()
}

@Composable
private fun QuimiaShapeIconPreviewContent() {
    QuimiaTheme {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            QuimiaShapeIconSize.entries.forEach { size ->
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuimiaShapeIconTheme.entries.chunked(2).forEach { themes ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            themes.forEach { theme ->
                                Box(Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                                    QuimiaShapeIcon(
                                        size = size.value,
                                        icon = if (theme == QuimiaShapeIconTheme.Success) Lucide.Check else Lucide.Circle,
                                        theme = theme,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}
