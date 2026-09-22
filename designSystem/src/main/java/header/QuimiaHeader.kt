package header

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import buttons.QuimiaAvatarButton
import buttons.QuimiaIconButton
import buttons.QuimiaIconButtonSize
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mic
import theme.PaddingLarge
import theme.PaddingSmall
import theme.RadiusSmall
import theme.quimiaColorTokens
import theme.QuimiaTheme

@Composable
fun QuimiaHeader(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    searchBarVariant: QuimiaSearchBarVariant = QuimiaSearchBarVariant.Primary,
    searchBarPadding: Dp = PaddingSmall.dp,
    onSearch: (() -> Unit)? = null,
    onMicClick: (() -> Unit)? = null,
    micButtonSize: QuimiaIconButtonSize = QuimiaIconButtonSize.Small,
    onBellClick: (() -> Unit)? = null,
    onAvatarClick: (() -> Unit)? = null,
    avatarInitials: String = "",
    avatarPhoto: Painter? = null,
    actionButtonsSize: QuimiaIconButtonSize = QuimiaIconButtonSize.Medium,
) {
    val tokens = quimiaColorTokens()
    val colors = resolveSearchBarColors(searchBarVariant, tokens)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(PaddingLarge.dp, PaddingSmall.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuimiaSearchBar(
            modifier = Modifier.weight(1f),
            icon = Lucide.Mic,
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            onSearch = onSearch,
            onClick = onMicClick,
            iconButtonSize = micButtonSize,
            iconDescription = "Voice Search",
            variant = searchBarVariant,
            horizontalPadding = searchBarPadding,
            verticalPadding = searchBarPadding,
        )
        QuimiaIconButton(
            icon = Lucide.Bell,
            onClick = { onBellClick?.invoke() },
            containerColor = colors.sticky,
            iconColor = colors.icon,
            size = actionButtonsSize,
            contentDescription = "Notification"
        )
        QuimiaAvatarButton(
            size = actionButtonsSize.buttonSize,
            backgroundColor = colors.sticky,
            initials = avatarInitials,
            photo = avatarPhoto,
            onClick = { onAvatarClick?.invoke() }
        )
    }
}

@Preview
@Composable
private fun PreviewQuimiaHeader() {
    QuimiaTheme {
        Column(
            modifier = Modifier
                .width(360.dp)
                .height(160.dp)
                .clip(RoundedCornerShape(RadiusSmall.dp))
                .background(quimiaColorTokens().surfaceBackground)
        ) {
            QuimiaHeader(
                value = "",
                onValueChange = {},
                placeholder = "Pesquisar"
            )
            QuimiaHeader(
                value = "",
                onValueChange = {},
                placeholder = "Pesquisar",
                searchBarVariant = QuimiaSearchBarVariant.Secondary
            )
        }
    }
}
