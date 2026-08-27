package buttons

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import theme.LightTokens
import theme.PaddingMedium
import theme.PaddingSmall
import theme.RadiusFull
import theme.RadiusMedium
import theme.quimiaColorTokens
import theme.toDp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.RangeSlider
import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.*
import theme.QuimiaFontFamily
import kotlin.String

@Composable
fun QuimiaIconButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    onClick: () -> Unit = {},
    text: String = "",
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
    fontFamily: FontFamily? = null,
    fontWeight: FontWeight? = null,
    containerColor: Color? = null,
    indicatorColor: Color? = null,
    textColor: Color? = null,
    iconColor: Color? = null,
    iconSize: Int,
    espacamento: Int? = PaddingSmall,
    curvaCirculo: Int? = RadiusFull
) {
    val tokens = quimiaColorTokens()
    val actualContainer = containerColor ?: tokens.primary
    val actualIndicator = indicatorColor ?: tokens.textSecondary
    val actualTextColor = textColor ?: tokens.textPrimary
    val actualIconColor = iconColor ?: tokens.textPrimary
    val buttonSize = (iconSize + (PaddingMedium * 2)).dp

    Button(
        onClick = onClick,
        modifier = modifier.size(buttonSize),
        colors = ButtonDefaults.buttonColors(
            containerColor = actualContainer
        ),
        shape = if (curvaCirculo == RadiusFull) {
            androidx.compose.foundation.shape.CircleShape
        } else {
            androidx.compose.foundation.shape.RoundedCornerShape(curvaCirculo?.toDp() ?: RadiusMedium.toDp())
        },
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = actualIconColor,
            modifier = Modifier.size(iconSize.dp)
        )
    }
}

@Preview
@Composable
fun PreviewQuimiaIconButton() {
    QuimiaIconButton(
        icon = Lucide.Circle,
        iconColor = LightTokens.foregroundPrimary,
        containerColor = LightTokens.primary,
        text = "Adicionar",
        textColor = LightTokens.textPrimary,
        modifier = Modifier.width(50.dp),
        iconSize = 20,
        espacamento = PaddingSmall,
        curvaCirculo = RadiusFull,
        fontFamily = QuimiaFontFamily,
        fontWeight = FontWeight.Medium
    )
}

