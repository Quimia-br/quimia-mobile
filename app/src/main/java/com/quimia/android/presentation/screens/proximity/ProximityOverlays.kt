package com.quimia.android.presentation.screens.proximity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import buttons.QuimiaButton
import buttons.QuimiaIconButton
import buttons.QuimiaIconButtonSize
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.X
import com.quimia.android.presentation.screens.catalog.CatalogModalScrimColor
import com.quimia.android.presentation.system.HideSystemNavigationBar
import input.QuimiaInput
import listItem.QuimiaListItem
import states.QuimiaEmptyState
import theme.QuimiaTextStyles
import theme.quimiaColorTokens
import toast.QuimiaToast
import toast.QuimiaToastType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NearbyFloatingSheet(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val tokens = quimiaColorTokens()
    if (LocalInspectionMode.current) {
        Box(Modifier.fillMaxSize()) {
            ProximityScreen(initialHeaderExpanded = false)
            Box(Modifier.fillMaxSize().background(CatalogModalScrimColor), contentAlignment = Alignment.BottomCenter) {
                Surface(
                    modifier = Modifier.statusBarsPadding().padding(16.dp).widthIn(max = 560.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(36.dp), color = tokens.surfaceBase,
                ) { Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp), content = content) }
            }
        }
        return
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.statusBarsPadding().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        sheetMaxWidth = 560.dp, shape = RoundedCornerShape(36.dp), containerColor = tokens.surfaceBase,
        scrimColor = CatalogModalScrimColor, dragHandle = null,
    ) {
        HideSystemNavigationBar()
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp), content = content)
    }
}

@Composable
internal fun NearbySavedPointsSheet(ids: List<Int>, onDismiss: () -> Unit, onSelect: (Int) -> Unit, onRemove: (Int) -> Unit) {
    val tokens = quimiaColorTokens()
    NearbyFloatingSheet(onDismiss) {
        NearbySheetHeader("Pontos salvos", onDismiss)
        Spacer(Modifier.height(20.dp))
        val points = nearbyPoints.filter { it.id in ids }
        if (points.isEmpty()) QuimiaEmptyState(
            fillAvailableSpace = false, title = "Nenhum ponto salvo",
            description = "Selecione um ponto no mapa demonstrativo e toque em Salvar ponto.",
            icon = Lucide.MapPin,
        ) else points.forEach { point ->
            QuimiaListItem(
                title = point.name, subtitle = "${point.category.label} · ${point.distance}\n${point.address}",
                leftIcon = Lucide.MapPin, contentPadding = PaddingValues(16.dp),
                trailingContent = {
                    QuimiaIconButton(icon = Lucide.Heart, size = QuimiaIconButtonSize.Small,
                        containerColor = tokens.surfaceBase, contentDescription = "Remover ${point.name} dos salvos", onClick = { onRemove(point.id) })
                },
                onClick = { onSelect(point.id) },
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
internal fun NearbyAddressSheet(onDismiss: () -> Unit, onSave: (NearbyAddress) -> Unit, initialAddress: NearbyAddress? = null) {
    val preview = LocalInspectionMode.current
    val seed = initialAddress ?: if (preview) NearbyAddress("04001000", "SP", "São Paulo", "Vila Mariana", "Rua Exemplo", "100", "")
        else NearbyAddress("", "", "", "", "", "", "")
    var postalCode by rememberSaveable { mutableStateOf(seed.postalCode) }
    var state by rememberSaveable { mutableStateOf(seed.state) }
    var city by rememberSaveable { mutableStateOf(seed.city) }
    var district by rememberSaveable { mutableStateOf(seed.district) }
    var street by rememberSaveable { mutableStateOf(seed.street) }
    var number by rememberSaveable { mutableStateOf(seed.number) }
    var complement by rememberSaveable { mutableStateOf(seed.complement) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val tokens = quimiaColorTokens()
    NearbyFloatingSheet(onDismiss) {
        NearbySheetHeader("Alterar endereço", onDismiss)
        Spacer(Modifier.height(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (error != null) QuimiaToast(message = error.orEmpty(), type = QuimiaToastType.Error, onDismiss = { error = null })
            QuimiaInput(value = postalCode, onValueChange = { postalCode = it.filter(Char::isDigit).take(8); error = null },
                label = "CEP", placeholder = "00000000", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    QuimiaInput(value = state, onValueChange = { state = it.filter(Char::isLetter).uppercase().take(2); error = null }, label = "UF")
                }
                Column(Modifier.weight(2f)) { QuimiaInput(value = city, onValueChange = { city = it; error = null }, label = "Cidade") }
            }
            QuimiaInput(value = district, onValueChange = { district = it; error = null }, label = "Bairro")
            QuimiaInput(value = street, onValueChange = { street = it; error = null }, label = "Logradouro")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) { QuimiaInput(value = number, onValueChange = { number = it; error = null }, label = "Número") }
                Column(Modifier.weight(2f)) { QuimiaInput(value = complement, onValueChange = { complement = it }, label = "Complemento") }
            }
        }
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuimiaButton(modifier = Modifier.weight(1f).height(48.dp), text = "Cancelar", textStyle = QuimiaTextStyles.medium16,
                iconSize = 0, espacamento = 0, containerColor = tokens.secondary, onClick = onDismiss)
            QuimiaButton(modifier = Modifier.weight(1f).height(48.dp), text = "Salvar", textStyle = QuimiaTextStyles.medium16,
                iconSize = 0, espacamento = 0, onClick = {
                    val value = NearbyAddress(postalCode, state, city.trim(), district.trim(), street.trim(), number.trim(), complement.trim())
                    error = value.validationError()
                    if (error == null) onSave(value)
                })
        }
    }
}

@Composable
private fun NearbySheetHeader(title: String, onDismiss: () -> Unit) {
    val tokens = quimiaColorTokens()
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), style = QuimiaTextStyles.medium22, color = tokens.textPrimary)
        QuimiaIconButton(icon = Lucide.X, size = QuimiaIconButtonSize.Small, containerColor = tokens.secondary,
            contentDescription = "Fechar", onClick = onDismiss)
    }
}
