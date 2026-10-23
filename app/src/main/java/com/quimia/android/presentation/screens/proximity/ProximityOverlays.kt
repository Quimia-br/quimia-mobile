package com.quimia.android.presentation.screens.proximity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import buttons.QuimiaButton
import buttons.QuimiaIconButton
import buttons.QuimiaIconButtonSize
import chip.QuimiaChip
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.X
import com.quimia.android.R
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
    val sheetContent: @Composable () -> Unit = {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.fillMaxWidth().height(27.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.width(90.dp).height(3.dp).background(tokens.white, CircleShape))
            }
            content()
        }
    }
    if (LocalInspectionMode.current) {
        Box(Modifier.fillMaxSize()) {
            ProximityScreen(initialHeaderExpanded = false)
            Box(Modifier.fillMaxSize().background(CatalogModalScrimColor), contentAlignment = Alignment.BottomCenter) {
                Surface(Modifier.statusBarsPadding().padding(horizontal = 17.dp).widthIn(max = 560.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(40.dp), color = tokens.secondary, content = sheetContent)
            }
        }
    } else {
        ModalBottomSheet(
            onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            modifier = Modifier.statusBarsPadding().padding(horizontal = 17.dp),
            sheetMaxWidth = 560.dp, shape = RoundedCornerShape(40.dp), containerColor = tokens.secondary,
            scrimColor = CatalogModalScrimColor, dragHandle = null,
        ) { HideSystemNavigationBar(); sheetContent() }
    }
}

@Composable
internal fun NearbySavedPointsSheet(ids: List<Int>, onDismiss: () -> Unit, onSelect: (Int) -> Unit, onRemove: (Int) -> Unit) {
    val tokens = quimiaColorTokens()
    var category by rememberSaveable { mutableStateOf<NearbyCategory?>(NearbyCategory.Collection) }
    var routeNotice by rememberSaveable { mutableStateOf(false) }
    val points = nearbyPoints.filter { it.id in ids && (category == null || it.category == category) }
    NearbyFloatingSheet(onDismiss) {
        NearbySheetHeader("Pontos salvos", onDismiss)
        Column(Modifier.padding(vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(17.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(17.dp)) {
                NearbyCategory.entries.forEach { item ->
                    QuimiaChip(label = item.label, selected = category == item, centerContent = true,
                        modifier = Modifier.weight(1f), onClick = { category = if (category == item) null else item })
                }
            }
            if (points.isEmpty()) QuimiaEmptyState(
                fillAvailableSpace = false, title = "Nenhum ponto salvo",
                description = "Selecione um ponto no mapa de demonstração e toque em Salvar.", icon = Lucide.MapPin,
            ) else points.forEach { point ->
                Surface(shape = RoundedCornerShape(32.dp), color = tokens.surfaceBase) {
                    Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        QuimiaListItem(
                            title = point.name, subtitle = point.address, titleMaxLines = 1, subtitleMaxLines = 1,
                            contentPadding = PaddingValues(0.dp), containerColor = Color.Transparent, cornerRadius = 0.dp,
                            trailingContent = {
                                QuimiaIconButton(
                                    modifier = Modifier.size(45.dp).border(2.dp, Color(0xFFFF93D7), CircleShape),
                                    icon = Lucide.Heart, containerColor = tokens.surfaceBase, iconColor = Color.Unspecified,
                                    iconPainter = painterResource(R.drawable.figma_heart_filled),
                                    contentDescription = "Remover dos salvos",
                                    onClick = { onRemove(point.id) },
                                )
                            },
                            onClick = { onSelect(point.id) },
                        )
                        QuimiaButton(modifier = Modifier.fillMaxWidth().height(41.dp), text = "Abrir aplicativo de mapa",
                            textStyle = QuimiaTextStyles.medium14, iconSize = 0, espacamento = 0,
                            containerColor = tokens.secondary, onClick = { routeNotice = true })
                    }
                }
            }
            if (routeNotice) QuimiaToast(message = "Este ponto é de demonstração. Rotas reais ainda não estão conectadas.",
                type = QuimiaToastType.Info, onDismiss = { routeNotice = false })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuimiaButton(modifier = Modifier.weight(1f).height(52.dp), text = "Fechar", iconSize = 0, espacamento = 0,
                textStyle = QuimiaTextStyles.medium16, containerColor = tokens.surfaceBase, onClick = onDismiss)
            QuimiaButton(modifier = Modifier.weight(1f).height(52.dp), text = "Ver mapa", iconSize = 0, espacamento = 0,
                textStyle = QuimiaTextStyles.medium16, enabled = points.isNotEmpty(),
                onClick = { points.firstOrNull()?.let { onSelect(it.id) } })
        }
    }
}

@Composable
internal fun NearbyAddressSheet(onDismiss: () -> Unit, onSave: (NearbyAddress) -> Unit, initialAddress: NearbyAddress? = null) {
    if (LocalInspectionMode.current) NearbyAddressForm(onDismiss, onSave, initialAddress)
    else Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        HideSystemNavigationBar()
        NearbyAddressForm(onDismiss, onSave, initialAddress)
    }
}

@Composable
private fun NearbyAddressForm(onDismiss: () -> Unit, onSave: (NearbyAddress) -> Unit, initialAddress: NearbyAddress?) {
    val preview = LocalInspectionMode.current
    val seed = initialAddress ?: if (preview) NearbyAddress("04001000", "SP", "São Paulo", "Vila Mariana", "", "100", "")
        else NearbyAddress("", "", "", "", "", "", "")
    var postalCode by rememberSaveable { mutableStateOf(seed.postalCode) }
    var state by rememberSaveable { mutableStateOf(seed.state) }
    var city by rememberSaveable { mutableStateOf(seed.city) }
    var district by rememberSaveable { mutableStateOf(seed.district) }
    var number by rememberSaveable { mutableStateOf(seed.number) }
    var complement by rememberSaveable { mutableStateOf(seed.complement) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var statesVisible by rememberSaveable { mutableStateOf(false) }
    val tokens = quimiaColorTokens()
    Surface(Modifier.fillMaxSize(), color = tokens.surfaceBackground) {
        Column(Modifier.widthIn(max = 600.dp).fillMaxWidth().statusBarsPadding().imePadding()) {
            Spacer(Modifier.height((79.dp - WindowInsets.statusBars.asPaddingValues().calculateTopPadding()).coerceAtLeast(0.dp)))
            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
                QuimiaIconButton(icon = Lucide.ChevronLeft, containerColor = tokens.secondary,
                    contentDescription = "Voltar sem alterar endereço", onClick = onDismiss)
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 32.dp)) {
                Spacer(Modifier.height(50.dp))
                Text("Alterar endereço", style = QuimiaTextStyles.medium24, color = tokens.textPrimary)
                Spacer(Modifier.height(31.dp))
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    if (error != null) QuimiaToast(message = error.orEmpty(), type = QuimiaToastType.Error, onDismiss = { error = null })
                    QuimiaInput(value = postalCode, onValueChange = { postalCode = it.filter(Char::isDigit).take(8); error = null },
                        label = "CEP", placeholder = "00000000", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = if (postalCode.isNotEmpty()) Lucide.X else null, onTrailingIconClick = { postalCode = "" })
                    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        Column(Modifier.weight(.26f)) {
                            Box {
                                QuimiaInput(value = state, onValueChange = { state = it.filter(Char::isLetter).uppercase().take(2); error = null }, label = "UF",
                                    trailingIcon = Lucide.ChevronDown, onTrailingIconClick = { statesVisible = true },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp))
                                DropdownMenu(expanded = statesVisible, onDismissRequest = { statesVisible = false }) {
                                    nearbyStates.forEach { uf -> DropdownMenuItem(text = { Text(uf, style = QuimiaTextStyles.medium16) },
                                        onClick = { state = uf; error = null; statesVisible = false }) }
                                }
                            }
                        }
                        Column(Modifier.weight(.74f)) {
                            QuimiaInput(value = city, onValueChange = { city = it; error = null }, label = "Cidade",
                                trailingIcon = if (city.isNotEmpty()) Lucide.X else null, onTrailingIconClick = { city = "" })
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        Column(Modifier.weight(.65f)) {
                            QuimiaInput(value = district, onValueChange = { district = it; error = null }, label = "Bairro",
                                trailingIcon = if (district.isNotEmpty()) Lucide.X else null, onTrailingIconClick = { district = "" })
                        }
                        Column(Modifier.weight(.35f)) {
                            QuimiaInput(value = number, onValueChange = { number = it; error = null }, label = "Nº",
                                trailingIcon = if (number.isNotEmpty()) Lucide.X else null, onTrailingIconClick = { number = "" })
                        }
                    }
                    QuimiaInput(value = complement, onValueChange = { complement = it }, label = "Complemento",
                        trailingIcon = if (complement.isNotEmpty()) Lucide.X else null, onTrailingIconClick = { complement = "" })
                }
                Spacer(Modifier.height(24.dp))
            }
            Row(Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 50.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                QuimiaButton(modifier = Modifier.weight(1f).height(58.dp), text = "Cancelar", textStyle = QuimiaTextStyles.medium16,
                    iconSize = 0, espacamento = 0, containerColor = tokens.secondary, onClick = onDismiss)
                QuimiaButton(modifier = Modifier.weight(1f).height(58.dp), text = "Salvar", textStyle = QuimiaTextStyles.medium16,
                    iconSize = 0, espacamento = 0, onClick = {
                        val value = NearbyAddress(postalCode, state, city.trim(), district.trim(), seed.street, number.trim(), complement.trim())
                        error = value.validationError()
                        if (error == null) onSave(value)
                    })
            }
        }
    }
}

@Composable
private fun NearbySheetHeader(title: String, onDismiss: () -> Unit) {
    val tokens = quimiaColorTokens()
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), style = QuimiaTextStyles.medium24, color = tokens.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        QuimiaIconButton(icon = Lucide.X, containerColor = tokens.surfaceBase, contentDescription = "Fechar", onClick = onDismiss)
    }
}
