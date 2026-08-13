package Accordion

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import buttons.QuimiaButton
import com.composables.icons.lucide.*
import theme.LightTokens
import theme.PaddingMedium
import theme.PaddingSmall
import theme.RadiusMedium
import theme.TextMedium
import theme.quimiaColorTokens
import theme.toDp
import theme.toSp

@Composable
fun QuimiaAccordion(
    title: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    titleColor: Color? = null,
    iconColor: Color? = null,
    content: @Composable () -> Unit
) {
    val isExpanded = remember { mutableStateOf(false) }
    val tokens = quimiaColorTokens()
    
    val actualTitleColor = titleColor ?: tokens.textPrimary
    val actualIconColor = iconColor ?: tokens.textSecondary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded.value = !isExpanded.value }
                .padding(PaddingMedium.toDp()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = TextMedium.toSp(),
                color = actualTitleColor,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (isExpanded.value) Lucide.ChevronUp else Lucide.ChevronDown,
                contentDescription = if (isExpanded.value) "Fechar" else "Abrir",
                tint = actualIconColor,
                modifier = Modifier.padding(start = PaddingSmall.toDp())
            )
        }
        Divider(color = Color.White, thickness = 1.dp, modifier = Modifier.fillMaxWidth())

        if (isExpanded.value) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = PaddingMedium.toDp(),
                        end = PaddingMedium.toDp(),
                        bottom = PaddingMedium.toDp()
                    )
            ) {
                content()
            }
        }
    
    }
}

@Composable
fun QuimiaAccordionGroup(
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    items: List<AccordionItem>
) {
    val tokens = quimiaColorTokens()
    val actualBgColor = backgroundColor ?: Color.Transparent

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(actualBgColor)
    ) {
        items.forEachIndexed { index, item ->
            QuimiaAccordion(
                title = item.title,
                backgroundColor = Color.Transparent,
                titleColor = item.titleColor,
                iconColor = item.iconColor,
                modifier = Modifier.fillMaxWidth(),
                content = item.content
            )
        }
    }
}


data class AccordionItem(
    val title: String,
    val content: @Composable () -> Unit,
    val titleColor: Color? = null,
    val iconColor: Color? = null
)

@Preview
@Composable
fun PreviewQuimiaAccordion() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightTokens.primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(PaddingMedium.toDp()),
        horizontalAlignment = Alignment.CenterHorizontally
        ) {
            QuimiaAccordionGroup(
                backgroundColor = Color.Transparent,
                items = listOf(
                    AccordionItem(
                        title = "Opções",
                        titleColor = LightTokens.textPrimary,
                        iconColor = LightTokens.black,
                        content = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                QuimiaButton(
                                    text = "Adicionar",
                                    iconLeftVector = Lucide.Plus,
                                    containerColor = LightTokens.primary,
                                    textColor = LightTokens.textPrimary,
                                    iconColor = LightTokens.black,
                                    iconSize = 24,
                                    onClick = { },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    ),
                    AccordionItem(
                        title = "Confirmação",
                        titleColor = LightTokens.textPrimary,
                        iconColor = LightTokens.black,
                        content = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                QuimiaButton(
                                    text = "Confirmar",
                                    iconLeftVector = Lucide.Check,
                                    containerColor = LightTokens.primary,
                                    textColor = LightTokens.textPrimary,
                                    iconColor = LightTokens.black,
                                    iconSize = 24,
                                    onClick = { },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                androidx.compose.foundation.layout.Spacer(
                                    modifier = Modifier.height(PaddingSmall.toDp())
                                )
                                QuimiaButton(
                                    text = "Cancelar",
                                    iconLeftVector = Lucide.X,
                                    containerColor = LightTokens.error,
                                    textColor = LightTokens.textPrimary,
                                    iconColor = LightTokens.black,
                                    iconSize = 24,
                                    onClick = { },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    ),
                    AccordionItem(
                        title = "Mais Informações",
                        titleColor = LightTokens.textPrimary,
                        iconColor = LightTokens.black,
                        content = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "HAUAHUAUHAUHAUHUHA SLA SEM IDEIA",
                                    color = LightTokens.textPrimary
                                )
                            }
                        }
                    )
                )
            )

            Spacer(modifier = Modifier.height(PaddingMedium.toDp()))
        }
    }
}

