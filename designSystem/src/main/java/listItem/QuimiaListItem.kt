package listItem

import theme.QuimiaTextStyles

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Bolt
import com.composables.icons.lucide.Droplet
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.PackageCheck
import com.composables.icons.lucide.Target
import com.composables.icons.lucide.Zap
import theme.QuimiaTheme
import theme.quimiaColorTokens
import theme.quimiaContrastOutline
import theme.quimiaHighContrastEnabled
import com.composables.icons.lucide.ToolCase
import theme.QuimiaColorTokens

enum class QuimiaListItemTheme { Secondary, Primary, OnColor }

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
    val theme: QuimiaListItemTheme = QuimiaListItemTheme.Secondary,
    val leadingContent: (@Composable () -> Unit)? = null,
    val trailingContent: (@Composable () -> Unit)? = null,
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
    theme: QuimiaListItemTheme = QuimiaListItemTheme.Secondary,
    contentPadding: PaddingValues = PaddingValues(24.dp),
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    titleMaxLines: Int = 2,
    subtitleMaxLines: Int = 2,
    cornerRadius: Dp = 32.dp,
    horizontalSpacing: Dp = 10.dp,
    onClick: (() -> Unit)? = null,
    titleTextStyle: TextStyle = QuimiaTextStyles.medium16,
    subtitleTextStyle: TextStyle = QuimiaTextStyles.medium14,
    textSpacing: Dp = 8.dp,
) {
    val tokens = quimiaColorTokens(forceDark = forceDark)
    val resolvedLeftIcon = leftIcon ?: icon
    val resolvedContainerColor = containerColor ?: when (theme) {
        QuimiaListItemTheme.Secondary -> tokens.surfaceBackground
        QuimiaListItemTheme.Primary -> tokens.surfaceBase
        QuimiaListItemTheme.OnColor -> tokens.secondary
    }
    val resolvedIconBackground = iconBackgroundColor ?: tokens.surfaceBase
    val resolvedIconTint = iconTint ?: tokens.foregroundPrimary
    val resolvedTitleColor = titleColor ?: tokens.foregroundPrimary
    val resolvedSubtitleColor = subtitleColor ?: tokens.foregroundSubtle

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius))
            .background(resolvedContainerColor)
            .quimiaContrastOutline(RoundedCornerShape(cornerRadius), quimiaHighContrastEnabled() && resolvedContainerColor != Color.Transparent)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
    ) {
        if (leadingContent != null) {
            leadingContent()
        } else if (resolvedLeftIcon != null) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(resolvedIconBackground),
                contentAlignment = Alignment.Center
            ) {
                QuimiaIconSource(
                    source = resolvedLeftIcon,
                    tint = resolvedIconTint,
                    size = 24,
                    modifier = Modifier
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            val titleStyle = titleTextStyle.copy(
                color = resolvedTitleColor,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.sp,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
            )
            Text(
                text = title,
                style = titleStyle,
                maxLines = titleMaxLines,
                overflow = TextOverflow.Ellipsis
            )

            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(textSpacing))
                val subtitleStyle = subtitleTextStyle.copy(
                    color = resolvedSubtitleColor,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = subtitleStyle,
                    maxLines = subtitleMaxLines,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (trailingContent != null) {
            trailingContent()
        } else if (rightIcon != null) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBackgroundColor ?: Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                QuimiaIconSource(
                    source = rightIcon,
                    tint = resolvedIconTint,
                    size = 24,
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
                theme = item.theme,
                leadingContent = item.leadingContent,
                trailingContent = item.trailingContent,
                onClick = item.onClick
            )
        }
    }
}

@preview.QuimiaDevicePreviews
@Composable
private fun QuimiaListItemPreview() {
    QuimiaTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            QuimiaListItemTheme.entries.forEach { theme ->
                QuimiaListItem(title = "Title", subtitle = "Description", theme = theme,
                    trailingContent = { Box(Modifier.size(44.dp)) })
            }
        }
    }
}
