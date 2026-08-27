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
import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.*
import theme.QuimiaFontFamily
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuimiaButton(
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    @DrawableRes iconLeft: Int? = null,
    @DrawableRes iconRight: Int? = null,
    iconLeftVector: ImageVector? = null,
    iconRightVector: ImageVector? = null,
    text: String,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
    fontFamily: FontFamily? = null,
    fontWeight: FontWeight? = null,
    onClick: () -> Unit = { },
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

    Button(
        modifier = modifier,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = actualContainer
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(curvaCirculo?.toDp() ?: RadiusMedium.toDp()),
        contentPadding = PaddingValues(
            horizontal = PaddingMedium.toDp(),
            vertical = PaddingSmall.toDp()
        )
    ) {
        val isCentered = (espacamento == 0)
        Row(
            horizontalArrangement = if (isCentered) Arrangement.Center else Arrangement.Start,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            iconLeftVector?.let {
               Icon(
                   imageVector = it,
                   contentDescription = null,
                   tint = actualIconColor,
                   modifier = Modifier.size(iconSize.dp)
               )
            } ?: iconLeft?.let {
               Icon(
                   painter = painterResource(id = it),
                   contentDescription = null,
                   tint = actualIconColor,
                   modifier = Modifier.size(iconSize.dp)
               )
            }
            if (!isCentered) {
                Spacer(modifier = Modifier.width(espacamento?.toDp() ?: PaddingSmall.toDp()))
            }
            val resolvedTextStyle = textStyle.copy(
                color = actualTextColor,
                fontFamily = fontFamily ?: textStyle.fontFamily,
                fontWeight = fontWeight ?: textStyle.fontWeight
            )

            Text(
                text = text,
                style = resolvedTextStyle
            )
            iconRightVector?.let {
               Icon(
                   imageVector = it,
                   contentDescription = null,
                   tint = actualIconColor,
                   modifier = Modifier.size(24.dp)
               )
            } ?: iconRight?.let {
               Icon(
                   painter = painterResource(id = it),
                   contentDescription = null,
                   tint = actualIconColor,
                   modifier = Modifier.size(24.dp)
               )
            }
        }

    }
}

@Preview
@Composable
fun PreviewQuimiaButton() {
    QuimiaButton(
        iconLeftVector = Lucide.Circle,
        iconColor = LightTokens.foregroundPrimary,
        containerColor = LightTokens.primary,
        text = "Adicionar",
        textColor = LightTokens.textPrimary,
        modifier = Modifier.width(200.dp),
        iconSize = 20,
        espacamento = PaddingSmall,
        curvaCirculo = RadiusMedium,
        fontFamily = QuimiaFontFamily,
        fontWeight = FontWeight.Medium
    )
}