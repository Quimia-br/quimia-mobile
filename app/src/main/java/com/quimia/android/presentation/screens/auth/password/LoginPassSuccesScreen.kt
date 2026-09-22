package com.quimia.android.presentation.screens.auth.password

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import buttons.QuimiaButton
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Puzzle
import com.composables.icons.lucide.ShieldCheck
import theme.QuimiaColorTokens
import theme.Typography
import theme.quimiaColorTokens
import theme.QuimiaTheme

@Composable
fun LoginPassSuccesScreen(
    modifier: Modifier = Modifier,
    onStart: () -> Unit = {}
) {
    val tokens = quimiaColorTokens(forceDark = false)

    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(60.dp))
            .background(tokens.surfaceBackground)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(128.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(60.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(tokens.secondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Lucide.Check,
                    contentDescription = "Senha alterada",
                    tint = tokens.textPrimary,
                    modifier = Modifier.size(41.dp)
                )
            }

            Text(
                text = "Senha alterada com sucesso!",
                style = Typography.headlineSmall.copy(
                    fontSize = 40.sp,
                    lineHeight = 48.sp
                ),
                color = tokens.textPrimary,
                modifier = Modifier.fillMaxWidth()
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                SuccessBenefit(
                    icon = Lucide.ShieldCheck,
                    text = "Segurança é o nosso pilar",
                    tokens = tokens
                )
                SuccessBenefit(
                    icon = Lucide.Puzzle,
                    text = "Organização personalizada para sua casa",
                    tokens = tokens
                )
            }
        }

        Spacer(modifier = Modifier.height(49.dp))

        QuimiaButton(
            text = "Começar",
            modifier = Modifier
                .width(325.dp)
                .height(58.dp),
            containerColor = tokens.primary,
            textColor = tokens.textPrimary,
            onClick = onStart,
            iconSize = 20,
            espacamento = 0,
            textStyle = Typography.titleMedium.copy(fontSize = 20.sp)
        )
    }
}

@Composable
private fun SuccessBenefit(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    tokens: QuimiaColorTokens
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(77.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(tokens.surfaceBase)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape)
                .background(tokens.secondary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tokens.textPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = text,
            style = Typography.titleSmall.copy(fontSize = 16.sp),
            color = tokens.textPrimary
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun LoginPassSuccesScreenPreview() {
    QuimiaTheme {
        LoginPassSuccesScreen()
    }
}
