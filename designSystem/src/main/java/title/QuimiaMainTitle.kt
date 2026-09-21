package title

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import theme.RadiusSmall
import theme.Typography
import theme.quimiaColorTokens

@Composable
fun QuimiaMainTitle(
    modifier: Modifier = Modifier,
    overline: String? = null,
    title: String? = null,
    description: String? = null,
) {
    val tokens = quimiaColorTokens()

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        overline?.let {
            Text(
                text = it,
                color = tokens.textPrimary,
                style = Typography.titleLarge.copy(
                    lineHeight = TextStyle.Default.lineHeight,
                    fontSize = Typography.titleLarge.fontSize
                )
            )
        }

        title?.let {
            Text(
                text = it,
                color = tokens.textPrimary,
                style = Typography.titleLarge.copy(
                    lineHeight = TextStyle.Default.lineHeight,
                    fontSize = Typography.titleLarge.fontSize * 1.8f
                )
            )
        }

        description?.let {
            Text(
                text = it,
                color = tokens.textSecondary,
                style = Typography.titleLarge.copy(
                    lineHeight = TextStyle.Default.lineHeight,
                    fontSize = Typography.titleLarge.fontSize * 1.5f
                )
            )
        }
    }
}

@Preview
@Composable
private fun QuimiaMainTitlePreview() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(
                color = quimiaColorTokens().surfaceBackground,
                shape = RoundedCornerShape(RadiusSmall.dp)
            )
            .width(420.dp)
            .height(360.dp)
            .padding(20.dp)
    ) {
        QuimiaMainTitle(
            overline = "Overline",
            title = "Title",
            description = "Description"
        )
    }
}
