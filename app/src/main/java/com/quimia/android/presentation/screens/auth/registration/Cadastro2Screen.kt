package com.quimia.android.presentation.screens.auth.registration

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import buttons.QuimiaButton
import com.quimia.android.data.address.normalizedPostalCode
import com.quimia.android.presentation.components.*
import com.quimia.android.presentation.screens.auth.*
import input.QuimiaInput
import theme.QuimiaTextStyles
import theme.QuimiaTheme
import theme.quimiaColorTokens
import title.QuimiaMainTitle
import com.quimia.android.presentation.screens.proximity.NearbyAddress

@Composable
fun Cadastro2Screen(modifier: Modifier = Modifier, onBack: () -> Unit = {}, onSkip: () -> Unit = {}, onEnter: () -> Unit = {},
    onAddressContinue: ((NearbyAddress) -> Unit)? = null) {
    var cep by rememberSaveable { mutableStateOf("") }
    var number by rememberSaveable { mutableStateOf("") }
    var complement by rememberSaveable { mutableStateOf("") }
    val lookup = rememberPostalLookup(cep)
    val address = lookup.state.address
    val tokens = quimiaColorTokens()
    AuthScreenLayout(modifier, onBack = onBack, bottomPadding = 50.dp, bodyVerticalPadding = 0.dp, footer = {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            QuimiaButton(text = "Pular", onClick = onSkip, modifier = Modifier.weight(1f).height(58.dp),
                containerColor = tokens.secondary, iconSize = 0, espacamento = 0, textStyle = QuimiaTextStyles.medium16)
            AuthPrimaryButton("Entrar", {
                if (onAddressContinue == null) onEnter() else address?.let {
                    onAddressContinue(NearbyAddress(it.postalCode, it.state, it.city, it.district, it.street, number.trim(), complement.trim()))
                }
            }, modifier = Modifier.weight(1f),
                enabled = lookup.state.ready && number.isNotBlank())
        }
    }) {
        QuimiaMainTitle(title = "Cadastre seu endereço\nabaixo")
        Spacer(Modifier.height(31.dp))
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            QuimiaInput(cep, { cep = normalizedPostalCode(it) }, label = "CEP", placeholder = "00000-000",
                visualTransformation = PostalCodeVisualTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            if (lookup.state.loading || lookup.state.error != null) PostalLookupNotice(lookup)
            PostalCityFields(address)
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                Box(Modifier.weight(1.9f)) { PostalReadOnlyInput(address?.district.orEmpty(), "Bairro") }
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
