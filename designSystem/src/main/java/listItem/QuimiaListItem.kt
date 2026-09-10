package listItem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Bolt
import com.composables.icons.lucide.Droplet
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.PackageCheck
import com.composables.icons.lucide.Target
import com.composables.icons.lucide.Zap
import theme.QuimiaTheme
import theme.Typography
import theme.quimiaColorTokens
import com.composables.icons.lucide.ToolCase
import theme.QuimiaColorTokens

data class QuimiaListItemData(
    val icon: Any? = null,
    val leftIcon: Any? = null,
    val rightIcon: Any? = null,
    val title: String,
    val subtitle: String? = null,
    val iconBackgroundColor: Color? = null,
    val iconTint: Color? = null,
    val titleColor: Color? = null,
    val subtitleColor: Color? = null,
    val containerColor: Color? = null,
    val onClick: (() -> Unit)? = null
)

@Composable
private fun QuimiaIconSource(
    source: Any?,
    tint: Color,
    size: Int,
    modifier: Modifier = Modifier
) {
    when (source) {
        is ImageVector -> {
            Icon(
                imageVector = source,
                contentDescription = null,
                tint = tint,
                modifier = modifier.size(size.dp)
            )
        }
        is Int -> {
            Icon(
                painter = painterResource(id = source),
                contentDescription = null,
                tint = tint,
                modifier = modifier.size(size.dp)
            )
        }
    }
}

@Composable
fun QuimiaListItem(
    icon: Any? = null,
    leftIcon: Any? = null,
    rightIcon: Any? = null,
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    forceDark: Boolean? = null,
    iconBackgroundColor: Color? = null,
    iconTint: Color? = null,
    titleColor: Color? = null,
    subtitleColor: Color? = null,
    containerColor: Color? = null,
    onClick: (() -> Unit)? = null
) {
    val tokens = quimiaColorTokens(forceDark = forceDark)
    val resolvedLeftIcon = leftIcon ?: icon
    val resolvedContainerColor = containerColor ?: tokens.surfaceBackground
    val resolvedIconBackground = iconBackgroundColor ?: tokens.surfaceBase.copy(alpha = 0.8f)
    val resolvedIconTint = iconTint ?: tokens.foregroundPrimary
    val resolvedTitleColor = titleColor ?: tokens.textPrimary
    val resolvedSubtitleColor = subtitleColor ?: tokens.foregroundSubtle

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(resolvedContainerColor)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (resolvedLeftIcon != null) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(resolvedIconBackground),
                contentAlignment = Alignment.Center
            ) {
                QuimiaIconSource(
                    source = resolvedLeftIcon,
                    tint = resolvedIconTint,
                    size = 22,
                    modifier = Modifier
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            val titleStyle = Typography.titleMedium.copy(
                color = resolvedTitleColor,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = title,
                style = titleStyle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                val subtitleStyle = Typography.bodyMedium.copy(
                    color = resolvedSubtitleColor,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = subtitle,
                    style = subtitleStyle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (rightIcon != null) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(resolvedIconBackground.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                QuimiaIconSource(
                    source = rightIcon,
                    tint = resolvedIconTint,
                    size = 22,
                    modifier = Modifier
                )
            }
        }
    }
}

@Composable
fun QuimiaListItemGroup(
    modifier: Modifier = Modifier,
    items: List<QuimiaListItemData>
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items.forEach { item ->
            QuimiaListItem(
                icon = item.icon,
                leftIcon = item.leftIcon,
                rightIcon = item.rightIcon,
                title = item.title,
                subtitle = item.subtitle,
                modifier = Modifier.fillMaxWidth(),
                iconBackgroundColor = item.iconBackgroundColor,
                iconTint = item.iconTint,
                titleColor = item.titleColor,
                subtitleColor = item.subtitleColor,
                containerColor = item.containerColor,
                onClick = item.onClick
            )
        }
    }
}

@Preview
@Composable
private fun QuimiaListItemPreview() {
    QuimiaTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            QuimiaListItem(
                leftIcon = Lucide.Zap,
                title = "Agilidade nas suas\nlimpezas",
                modifier = Modifier.fillMaxWidth()
            )

            QuimiaListItem(
                leftIcon = Lucide.ToolCase,
                title = "Organização & Saúde",
                subtitle = "Utilize a funcionalidade cômodos",
                modifier = Modifier.fillMaxWidth()
            )

            QuimiaListItem(
                leftIcon = Lucide.Droplet,
                title = "Precisão",
                subtitle = "Saiba a quantidade ideal de cada produto",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
