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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import buttons.QuimiaButton
import theme.QuimiaTypography
import theme.quimiaColorTokens

@Composable
fun Login1Screen(
    modifier: Modifier = Modifier,
    onContinue: () -> Unit = {}
) {
    val tokens = quimiaColorTokens()
    val (email, setEmail) = remember { mutableStateOf("") }
    val (password, setPassword) = remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(tokens.background)
            .padding(horizontal = 28.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Seja bem-vindo de volta!\nFaça login abaixo",
            style = QuimiaTypography.titleLarge,
            fontWeight = FontWeight.Medium,
            color = tokens.textPrimary
        )

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = email,
            onValueChange = setEmail,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp)),
            singleLine = true,
            placeholder = { Text(text = "exemplo@quimia.com", color = tokens.textSecondary) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = setPassword,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp)),
            singleLine = true,
            placeholder = { Text(text = "123456", color = tokens.textSecondary) }
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Esqueci a senha", color = tokens.textSecondary)

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(tokens.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text("G", color = tokens.textPrimary)
            }
            Spacer(modifier = Modifier.size(16.dp))
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(tokens.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text("■", color = tokens.textPrimary)
            }
            Spacer(modifier = Modifier.size(16.dp))
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(tokens.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text("@", color = tokens.textPrimary)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        QuimiaButton(
            text = "Continuar",
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            containerColor = Color(0xFFB7F2F0),
            textColor = Color(0xFF6FCFCB),
            onClick = onContinue,
            iconSize = 20,
            espacamento = 0
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Não tem conta no Quimia? ", color = tokens.textSecondary)
        Text(text = "Crie já", color = tokens.textPrimary, modifier = Modifier.padding(top = 4.dp))
    }
}

@Preview
@Composable
private fun Login1ScreenPreview() {
    Login1Screen()
}
