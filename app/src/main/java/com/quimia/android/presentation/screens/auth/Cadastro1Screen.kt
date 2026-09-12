package com.quimia.android.presentation.screens.auth

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import buttons.QuimiaButton
import input.QuimiaInput
import theme.Typography
import theme.quimiaColorTokens

@Composable
fun Cadastro1Screen(
    modifier: Modifier = Modifier,
    onContinue: () -> Unit = {}
) {
    val tokens = quimiaColorTokens(forceDark = false)
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(50.dp))
            .background(tokens.surfaceBackground)
            .padding(start = 20.dp, end = 20.dp, bottom = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 105.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Seja bem-vindo!\nFaça cadastro abaixo",
                style = Typography.titleLarge.copy(
                    fontSize = 19.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = tokens.textPrimary
            )

            Spacer(modifier = Modifier.height(26.dp))

            QuimiaInput(
                value = name,
                onValueChange = { name = it },
                label = "Nome",
                placeholder = "Gabriela Benfica",
                textStyle = Typography.bodySmall,
                containerColor = tokens.secondary,
                primaryTextColor = tokens.textPrimary,
                secondaryTextColor = tokens.textSecondary,
                radius = 1000,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            QuimiaInput(
                value = email,
                onValueChange = { email = it },
                label = "E-mail",
                placeholder = "gabrielabenfica@gmail.com.br",
                textStyle = Typography.bodySmall,
                containerColor = tokens.secondary,
                primaryTextColor = tokens.textPrimary,
                secondaryTextColor = tokens.textSecondary,
                radius = 1000,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            QuimiaInput(
                value = password,
                onValueChange = { password = it },
                label = "Crie uma senha",
                placeholder = "12345",
                textStyle = Typography.bodySmall,
                containerColor = tokens.secondary,
                primaryTextColor = tokens.textPrimary,
                secondaryTextColor = tokens.textSecondary,
                focusColor = Color(0xFF00DC9D),
                radius = 1000,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CadastroSocialIcon { Text("G", color = tokens.textPrimary, fontWeight = FontWeight.Bold) }
            CadastroSocialIcon { MicrosoftMark(color = tokens.textPrimary) }
            CadastroSocialIcon { Text("✉", color = tokens.textPrimary, fontSize = 16.sp) }
        }

        Spacer(modifier = Modifier.height(48.dp))

        QuimiaButton(
            text = "Continuar",
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            containerColor = Color(0xFF5ADFEA),
            textColor = Color(0xFF3F3F3F),
            onClick = onContinue,
            iconSize = 20,
            espacamento = 0,
            textStyle = Typography.titleMedium.copy(fontWeight = FontWeight.Medium)
        )

        Spacer(modifier = Modifier.height(13.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Não tem conta no Quimia? ", style = Typography.bodySmall, color = tokens.textSecondary)
            Text("Crie já", style = Typography.bodySmall, color = tokens.textPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CadastroSocialIcon(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(0xFFECECEC)),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun MicrosoftMark(color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Box(Modifier.size(6.dp).background(color))
            Box(Modifier.size(6.dp).background(color))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Box(Modifier.size(6.dp).background(color))
            Box(Modifier.size(6.dp).background(color))
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun Cadastro1ScreenPreview() {
    Cadastro1Screen()
}
