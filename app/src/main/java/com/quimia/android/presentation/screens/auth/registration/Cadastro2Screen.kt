package com.quimia.android.presentation.screens.auth.registration

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
import theme.QuimiaTheme

@Composable
fun Cadastro2Screen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSkip: () -> Unit = {},
    onEnter: () -> Unit = {}
) {
    val tokens = quimiaColorTokens(forceDark = false)
    var cep by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var neighborhood by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var complement by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(50.dp))
            .background(tokens.surfaceBackground)
            .padding(start = 20.dp, end = 20.dp, bottom = 38.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .padding(top = 72.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(tokens.secondary)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "‹",
                color = tokens.textPrimary,
                fontSize = 24.sp,
                lineHeight = 24.sp
            )
        }

        Spacer(modifier = Modifier.height(61.dp))

        Text(
            text = "Cadastre seu endereço\nabaixo",
            style = Typography.titleLarge.copy(
                fontSize = 19.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium
            ),
            color = tokens.textPrimary
        )

        Spacer(modifier = Modifier.height(24.dp))

        QuimiaInput(
            value = cep,
            onValueChange = { cep = it },
            label = "CEP",
            placeholder = "0000-00",
            textStyle = Typography.bodySmall,
            containerColor = tokens.secondary,
            primaryTextColor = tokens.textPrimary,
            secondaryTextColor = tokens.textSecondary,
            radius = 1000,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            UfField(
                modifier = Modifier.weight(0.38f),
                textColor = tokens.textPrimary,
                containerColor = tokens.secondary
            )
            Box(modifier = Modifier.weight(1f)) {
                QuimiaInput(
                    value = city,
                    onValueChange = { city = it },
                    label = "Cidade",
                    placeholder = "Osasco",
                    textStyle = Typography.bodySmall,
                    containerColor = tokens.secondary,
                    primaryTextColor = tokens.textPrimary,
                    secondaryTextColor = tokens.textSecondary,
                    radius = 1000,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Box(modifier = Modifier.weight(1.4f)) {
                QuimiaInput(
                    value = neighborhood,
                    onValueChange = { neighborhood = it },
                    label = "Bairro",
                    placeholder = "Parque Continental",
                    textStyle = Typography.bodySmall,
                    containerColor = tokens.secondary,
                    primaryTextColor = tokens.textPrimary,
                    secondaryTextColor = tokens.textSecondary,
                    radius = 1000,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                )
            }
            Box(modifier = Modifier.weight(0.6f)) {
                QuimiaInput(
                    value = number,
                    onValueChange = { number = it },
                    label = "Nº",
                    placeholder = "100",
                    textStyle = Typography.bodySmall,
                    containerColor = tokens.secondary,
                    primaryTextColor = tokens.textPrimary,
                    secondaryTextColor = tokens.textSecondary,
                    radius = 1000,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        QuimiaInput(
            value = complement,
            onValueChange = { complement = it },
            label = "Complemento",
            placeholder = "Apto 1234",
            textStyle = Typography.bodySmall,
            containerColor = tokens.secondary,
            primaryTextColor = tokens.textPrimary,
            secondaryTextColor = tokens.textSecondary,
            radius = 1000,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuimiaButton(
                text = "Pular",
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                containerColor = tokens.secondary,
                textColor = tokens.textPrimary,
                onClick = onSkip,
                iconSize = 20,
                espacamento = 0,
                textStyle = Typography.titleMedium.copy(fontWeight = FontWeight.Medium)
            )
            QuimiaButton(
                text = "Entrar",
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                containerColor = tokens.primary,
                textColor = tokens.onPrimary,
                onClick = onEnter,
                iconSize = 20,
                espacamento = 0,
                textStyle = Typography.titleMedium.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}

@Composable
private fun UfField(
    modifier: Modifier = Modifier,
    textColor: Color,
    containerColor: Color
) {
    Column(modifier = modifier) {
        Text(
            text = "UF",
            style = Typography.bodySmall,
            color = textColor,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(1000.dp))
                .background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "SP⌄", style = Typography.bodySmall, color = textColor)
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun Cadastro2ScreenPreview() {
    QuimiaTheme {
        Cadastro2Screen()
    }
}
