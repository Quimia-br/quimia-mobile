package com.quimia.android.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.quimia.android.data.address.*
import input.QuimiaInput
import kotlinx.coroutines.ensureActive
import theme.QuimiaTextStyles
import theme.quimiaColorTokens

internal data class PostalLookupState(val postalCode: String, val loading: Boolean = false,
    val address: PostalAddress? = null, val error: String? = null) {
    val ready: Boolean get() = !loading && address?.complete == true && address.postalCode == postalCode && error == null
}

internal fun postalLookupState(code: String, result: PostalLookupResult): PostalLookupState = when (result) {
    is PostalLookupResult.Found -> if (result.address.postalCode != code) PostalLookupState(code, error = "A consulta retornou outro CEP. Tente novamente.")
        else PostalLookupState(code, address = result.address, error = if (result.address.complete) null
            else "Este CEP não informa o bairro. Informe um CEP mais específico; os dados não serão preenchidos manualmente.")
    PostalLookupResult.NotFound -> PostalLookupState(code, error = "CEP não encontrado. Confira os 8 dígitos.")
    PostalLookupResult.Unavailable -> PostalLookupState(code, error = "Não foi possível consultar o CEP. Confira a conexão e tente novamente.")
    PostalLookupResult.InvalidResponse -> PostalLookupState(code, error = "O serviço de CEP retornou dados inválidos. Tente novamente.")
}

internal fun currentPostalState(code: String, previous: PostalLookupState): PostalLookupState =
    if (previous.postalCode == code) previous else PostalLookupState(code, loading = code.length == 8)

internal class PostalLookup(val state: PostalLookupState, val retry: () -> Unit)

@Composable
internal fun rememberPostalLookup(value: String, previewAddress: PostalAddress? = null): PostalLookup {
    val code = normalizedPostalCode(value)
    val inspection = LocalInspectionMode.current
    var retry by remember { mutableIntStateOf(0) }
    var state by remember { mutableStateOf(PostalLookupState("")) }
    LaunchedEffect(code, retry, inspection) {
        if (inspection) return@LaunchedEffect
        if (code.length != 8) { state = PostalLookupState(code); return@LaunchedEffect }
        state = PostalLookupState(code, loading = true)
        val result = ViaCepLookup.lookup(code)
        ensureActive()
        state = postalLookupState(code, result)
    }
    val visible = if (inspection && previewAddress?.postalCode == code)
        PostalLookupState(code, address = previewAddress) else currentPostalState(code, state)
    return PostalLookup(visible) { retry++ }
}

@Composable
internal fun PostalLookupNotice(lookup: PostalLookup) {
    val state = lookup.state
    val tokens = quimiaColorTokens()
    Column(Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }) {
        if (state.loading) Text("Consultando CEP…", style = QuimiaTextStyles.medium14, color = tokens.foregroundSubtle)
        state.error?.let {
            Text(it, style = QuimiaTextStyles.medium14, color = tokens.error)
            TextButton(onClick = lookup.retry) { Text("Consultar novamente", style = QuimiaTextStyles.medium14, color = tokens.foregroundPrimary) }
        }
    }
}

@Composable
internal fun PostalCityFields(address: PostalAddress?) {
    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        Box(Modifier.width(85.dp)) { PostalReadOnlyInput(address?.state.orEmpty(), "UF") }
        Box(Modifier.weight(1f)) { PostalReadOnlyInput(address?.city.orEmpty(), "Cidade") }
    }
}

@Composable
internal fun PostalReadOnlyInput(value: String, label: String) {
    QuimiaInput(value, {}, label = label, placeholder = if (label == "UF") "—" else "Pelo CEP", enabled = false, showClearButton = false)
}
