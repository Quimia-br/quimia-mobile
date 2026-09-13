package com.quimia.android.presentation.screens.auth

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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import buttons.QuimiaButton
import com.quimia.android.R
import input.QuimiaInput
import theme.Typography
import theme.quimiaColorTokens

@Composable
fun Login1Screen(
    modifier: Modifier = Modifier,
    onContinue: () -> Unit = {},
    onCreateAccount: () -> Unit = {}
) {
    val tokens = quimiaColorTokens(forceDark = false)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(50.dp))
            .background(tokens.surfaceBackground)
            .padding(start = 20.dp, end = 20.dp, bottom = 50.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 150.dp),
            horizontalAlignment = Alignment.Start
        ) {

            Spacer(modifier = Modifier.height(80.dp))


            Text(
                text = "Seja bem-vindo de volta!\nFaça login abaixo",
                style = Typography.titleLarge.copy(
                    fontSize = 19.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = tokens.textPrimary
            )

            Spacer(modifier = Modifier.height(34.dp))

            QuimiaInput(
                value = email,
                onValueChange = { email = it },
                label = "E-mail",
                placeholder = "exemplo@quimia.com",
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
                label = "Senha",
                placeholder = "123456",
                textStyle = Typography.bodySmall,
                containerColor = tokens.secondary,
                primaryTextColor = tokens.textPrimary,
                secondaryTextColor = tokens.textSecondary,
                radius = 1000,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Esqueci a senha",
                style = Typography.bodySmall,
                color = tokens.textPrimary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(56.dp))

        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SocialIcon(containerColor = tokens.secondary) {
                Icon(
                    painter = painterResource(R.drawable.social_google),
                    contentDescription = "Continuar com Google",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Unspecified
                )
            }
            SocialIcon(containerColor = tokens.secondary) {
                Icon(
                    painter = painterResource(R.drawable.social_microsoft),
                    contentDescription = "Continuar com Microsoft",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Unspecified
                )
            }
            SocialIcon(containerColor = tokens.secondary) {
                Icon(
                    painter = painterResource(R.drawable.social_mail),
                    contentDescription = "Continuar com e-mail",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Unspecified
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))

        QuimiaButton(
            text = "Continuar",
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            containerColor = tokens.primary,
            textColor = tokens.onPrimary,
            onClick = onContinue,
            iconSize = 20,
            espacamento = 0,
            textStyle = Typography.titleMedium.copy(fontWeight = FontWeight.Medium)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Não tem conta no Quimia? ", style = Typography.bodySmall, color = tokens.textSecondary)
            Text(
                text = "Crie já",
                style = Typography.bodySmall,
                color = tokens.textPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onCreateAccount)
            )
        }
    }
}

@Composable
private fun SocialIcon(
    containerColor: Color,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun Login1ScreenPreview() {
    Login1Screen()
}
