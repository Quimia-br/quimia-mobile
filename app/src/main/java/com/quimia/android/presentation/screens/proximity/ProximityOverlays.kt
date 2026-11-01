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
import com.quimia.android.data.address.PostalAddress
import com.quimia.android.data.address.normalizedPostalCode
import com.quimia.android.presentation.components.*
import listItem.QuimiaListItem
import states.QuimiaEmptyState
import theme.QuimiaTextStyles
import theme.quimiaColorTokens
import theme.quimiaContrastBorder
import toast.QuimiaToast
import toast.QuimiaToastType
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import com.quimia.android.data.auth.authErrorMessage

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
                Surface(Modifier.statusBarsPadding().padding(horizontal = 17.dp).widthIn(max = com.quimia.android.presentation.components.quimiaOverlayWidth(560.dp, 840.dp)).fillMaxWidth(),
                    shape = RoundedCornerShape(40.dp), color = tokens.secondary, border = quimiaContrastBorder(), content = sheetContent)
            }
        }
    } else {
        ModalBottomSheet(
            onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            modifier = Modifier.statusBarsPadding().padding(horizontal = 17.dp),
            sheetMaxWidth = com.quimia.android.presentation.components.quimiaOverlayWidth(560.dp, 840.dp), shape = RoundedCornerShape(40.dp), containerColor = tokens.secondary,
            scrimColor = CatalogModalScrimColor, dragHandle = null,
        ) {
            HideSystemNavigationBar()
            Surface(shape = RoundedCornerShape(40.dp), color = tokens.secondary, border = quimiaContrastBorder(), content = sheetContent)
        }
    }
}

@Composable
internal fun NearbySavedPointsSheet(ids: List<Int>, onDismiss: () -> Unit, onSelect: (Int) -> Unit, onRemove: (Int) -> Unit) {
    var routeNotice by rememberSaveable { mutableStateOf(false) }
    NearbyPointsSheet(points = nearbyPoints.filter { it.id in ids }.map { point ->
        NearbyPointListEntry(point.id, point.name, point.address,
            if (point.category == NearbyCategory.Collection) "descarte" else "compra", true, true)
    }, initialCategory = "descarte", onDismiss = onDismiss, onSelect = onSelect,
        onOpenMap = { routeNotice = true }, onToggleSaved = onRemove,
        error = if (routeNotice) "Este ponto é de demonstração. Rotas reais ainda não estão conectadas." else null,
        onErrorDismiss = { routeNotice = false })
}

@Composable
internal fun NearbyAddressSheet(onDismiss: () -> Unit, onSave: (NearbyAddress) -> Unit, initialAddress: NearbyAddress? = null, saveRemote: (suspend (NearbyAddress) -> Unit)? = null) {
    if (LocalInspectionMode.current) NearbyAddressForm(onDismiss, onSave, initialAddress, saveRemote)
    else Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        HideSystemNavigationBar()
        NearbyAddressForm(onDismiss, onSave, initialAddress, saveRemote)
    }
}

@Composable
private fun NearbyAddressForm(onDismiss: () -> Unit, onSave: (NearbyAddress) -> Unit, initialAddress: NearbyAddress?, saveRemote: (suspend (NearbyAddress) -> Unit)? = null) {
    val scope = rememberCoroutineScope()
    var saving by remember { mutableStateOf(false) }
    val close: () -> Unit = { if (!saving) onDismiss() }
    androidx.activity.compose.BackHandler(enabled = saving) { }
    val preview = LocalInspectionMode.current
    val seed = initialAddress ?: if (preview) NearbyAddress("04001000", "SP", "São Paulo", "Vila Mariana", "", "100", "")
        else NearbyAddress("", "", "", "", "", "", "")
    var postalCode by rememberSaveable { mutableStateOf(normalizedPostalCode(seed.postalCode)) }
    var number by rememberSaveable { mutableStateOf(seed.number) }
    var complement by rememberSaveable { mutableStateOf(seed.complement) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val lookup = rememberPostalLookup(postalCode, previewAddress = if (preview)
        PostalAddress(seed.postalCode, seed.state, seed.city, seed.district, seed.street) else null)
    val address = lookup.state.address
    val tokens = quimiaColorTokens()
    Surface(Modifier.fillMaxSize(), color = tokens.surfaceBackground) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 600.dp).fillMaxSize().statusBarsPadding().imePadding()) {
            Spacer(Modifier.height((79.dp - WindowInsets.statusBars.asPaddingValues().calculateTopPadding()).coerceAtLeast(0.dp)))
            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
                QuimiaIconButton(icon = Lucide.ChevronLeft, containerColor = tokens.secondary,
                    contentDescription = "Voltar sem alterar endereço", onClick = close)
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 32.dp)) {
                Spacer(Modifier.height(50.dp))
                Text("Alterar endereço", style = QuimiaTextStyles.medium24, color = tokens.textPrimary)
                Spacer(Modifier.height(31.dp))
                Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    toast.QuimiaToastHost(error, type = QuimiaToastType.Error, onDismiss = { error = null })
                    toast.QuimiaToastHost(if (saving) "Salvando endereço…" else null, type = QuimiaToastType.Loading)
                    QuimiaInput(value = normalizedPostalCode(postalCode), onValueChange = { postalCode = normalizedPostalCode(it); error = null },
                        label = "CEP", placeholder = "00000-000", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = PostalCodeVisualTransformation,
                        trailingIcon = if (postalCode.isNotEmpty()) Lucide.X else null, onTrailingIconClick = { postalCode = "" })
                    if (lookup.state.loading || lookup.state.error != null) PostalLookupNotice(lookup)
                    PostalReadOnlyInput(address?.street.orEmpty(), "Rua")
                    PostalCityFields(address)
                    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        Column(Modifier.weight(.65f)) {
                            PostalReadOnlyInput(address?.district.orEmpty(), "Bairro")
                        }
                        Column(Modifier.weight(.35f)) {
                            QuimiaInput(value = number, onValueChange = { number = it.filter(Char::isDigit).take(10); error = null }, label = "Nº",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    iconSize = 0, espacamento = 0, containerColor = tokens.secondary, onClick = close)
                QuimiaButton(modifier = Modifier.weight(1f).height(58.dp), text = if (saving) "Salvando…" else "Salvar", textStyle = QuimiaTextStyles.medium16,
                    iconSize = 0, espacamento = 0, enabled = !saving && lookup.state.ready && number.toIntOrNull()?.let { it > 0 } == true && complement.length <= 255, onClick = {
                        val resolved = address ?: return@QuimiaButton
                        val value = NearbyAddress(postalCode, resolved.state, resolved.city, resolved.district, resolved.street, number.trim(), complement.trim())
                        error = value.validationError()
                        if (error == null) {
                            if (saveRemote == null) onSave(value)
                            else if (!saving) scope.launch {
                                saving = true
                                try { saveRemote(value); onSave(value) }
                                catch (cancelled: CancellationException) { throw cancelled }
                                catch (failure: Exception) { error = if (failure is IllegalArgumentException) failure.message else authErrorMessage(failure) }
                                finally { saving = false }
                            }
                        }
                    })
            }
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
