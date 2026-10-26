package com.quimia.android.presentation.screens.auth.registration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import buttons.QuimiaButton
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.Lucide
import com.quimia.android.presentation.screens.auth.*
import input.QuimiaInput
import theme.QuimiaTextStyles
import theme.QuimiaTheme
import theme.quimiaColorTokens
import title.QuimiaMainTitle

@Composable
fun Cadastro2Screen(modifier: Modifier = Modifier, onBack: () -> Unit = {}, onSkip: () -> Unit = {}, onEnter: () -> Unit = {}) {
    var cep by rememberSaveable { mutableStateOf("") }
    var state by rememberSaveable { mutableStateOf("SP") }
    var city by rememberSaveable { mutableStateOf("") }
    var neighborhood by rememberSaveable { mutableStateOf("") }
    var number by rememberSaveable { mutableStateOf("") }
    var complement by rememberSaveable { mutableStateOf("") }
    var stateMenu by remember { mutableStateOf(false) }
    val tokens = quimiaColorTokens()
    AuthScreenLayout(modifier, onBack = onBack, bottomPadding = 50.dp, bodyVerticalPadding = 0.dp, footer = {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            QuimiaButton(text = "Pular", onClick = onSkip, modifier = Modifier.weight(1f).height(58.dp),
                containerColor = tokens.secondary, iconSize = 0, espacamento = 0, textStyle = QuimiaTextStyles.medium16)
            AuthPrimaryButton("Entrar", onEnter, modifier = Modifier.weight(1f),
                enabled = cep.length == 8 && city.isNotBlank() && neighborhood.isNotBlank() && number.isNotBlank())
        }
    }) {
        QuimiaMainTitle(title = "Cadastre seu endereço\nabaixo")
        Spacer(Modifier.height(31.dp))
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            QuimiaInput(cep, { cep = it.filter(Char::isDigit).take(8) }, label = "CEP", placeholder = "00000-000",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                Box(Modifier.width(85.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("UF", style = QuimiaTextStyles.medium16, color = tokens.foregroundPrimary)
                        QuimiaButton(text = state, onClick = { stateMenu = true }, iconRightVector = Lucide.ChevronDown,
                            containerColor = tokens.secondary, modifier = Modifier.fillMaxWidth().height(56.dp),
                            iconSize = 16, espacamento = 0, fillTextWidth = true, textStyle = QuimiaTextStyles.medium16)
                    }
                    DropdownMenu(expanded = stateMenu, onDismissRequest = { stateMenu = false }) {
                        BrazilianStates.forEach { item -> DropdownMenuItem(text = { Text(item) }, onClick = { state = item; stateMenu = false }) }
                    }
                }
                Box(Modifier.weight(1f)) { QuimiaInput(city, { city = it }, label = "Cidade", placeholder = "Osasco") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                Box(Modifier.weight(1.9f)) { QuimiaInput(neighborhood, { neighborhood = it }, label = "Bairro", placeholder = "Parque Continental") }
                Box(Modifier.weight(1f)) { QuimiaInput(number, { number = it }, label = "Nº", placeholder = "100", showClearButton = false,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)) }
            }
            QuimiaInput(complement, { complement = it }, label = "Complemento", placeholder = "Apto 1234")
        }
    }
}

@com.quimia.android.presentation.preview.QuimiaScreenPreviews
@Composable
private fun Cadastro2ScreenPreview() { QuimiaTheme { Cadastro2Screen() } }
