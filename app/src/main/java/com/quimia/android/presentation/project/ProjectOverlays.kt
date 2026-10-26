package com.quimia.android.presentation.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import card.QuimiaCard
import com.quimia.android.presentation.screens.catalog.CatalogProduct
import com.quimia.android.data.project.ApiMixture
import theme.QuimiaTextStyles
import theme.quimiaColorTokens

@Composable
fun ApiChatDialog(messages: List<ApiChatEntry>, busy: Boolean, error: String?, onSend: (String) -> Unit, onClose: () -> Unit) {
    var draft by remember { mutableStateOf("") }
    val scroll = rememberScrollState()
    LaunchedEffect(messages.size) { if (messages.isNotEmpty()) draft = ""; scroll.animateScrollTo(scroll.maxValue) }
    AlertDialog(onDismissRequest = onClose, title = { Text("Kemi") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.heightIn(max = 320.dp).verticalScroll(scroll), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                messages.forEach { entry ->
                    QuimiaCard(title = if (entry.fromAssistant) "Kemi" else "Você", description = entry.text,
                        primaryActionLabel = null, secondaryActionLabel = null, endIcon = null, contentPadding = 16.dp)
                }
                if (busy) Text("Aguardando resposta da API…")
                if (error != null) Text(error)
            }
            input.QuimiaInput(value = draft, onValueChange = { draft = it.take(2000) }, label = "Mensagem", enabled = !busy)
        }
    }, confirmButton = { TextButton(onClick = { onSend(draft) }, enabled = !busy && draft.isNotBlank()) { Text("Enviar") } },
        dismissButton = { TextButton(onClick = onClose) { Text("Fechar") } })
}

@Composable
fun ApiMixturePicker(products: List<CatalogProduct>, selected: List<String>, onSelection: (List<String>) -> Unit,
    onClose: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(onDismissRequest = onClose, title = { Text("Consultar compatibilidade na API") },
        text = { Column(Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
            Text("Selecione exatamente 2 produtos da sua estante.")
            if (products.isEmpty()) Text("A API não retornou produtos guardados.")
            products.forEach { product ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = product.id in selected, onCheckedChange = { checked ->
                        if (!checked) onSelection(selected - product.id)
                        else if (selected.size < 2) onSelection(selected + product.id)
                    })
                    Text(product.name, style = QuimiaTextStyles.medium14)
                }
            }
        } },
        confirmButton = { TextButton(onClick = onConfirm, enabled = selected.size == 2 && selected.all { id -> products.any { it.id == id } }) { Text("Consultar") } },
        dismissButton = { TextButton(onClick = onClose) { Text("Cancelar") } })
}

@Composable
fun ApiHistoryContent(items: List<ApiMixture>, available: Boolean = true) {
    val tokens = quimiaColorTokens()
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (!available) { Text("O histórico ainda não foi confirmado pela API. Confira o carregamento ou erro do serviço.", style = QuimiaTextStyles.medium14); return@Column }
        if (items.isEmpty()) Text("Nenhuma consulta de mistura retornada pela API.", style = QuimiaTextStyles.medium16, color = tokens.textSecondary)
        items.forEach { mixture ->
            QuimiaCard(title = "${mixture.produtoA.nome} + ${mixture.produtoB.nome}", description = mixture.resultado,
                primaryActionLabel = null, secondaryActionLabel = null, endIcon = null, contentPadding = 24.dp) {
                mixture.descricaoRisco?.let { Text(it, style = QuimiaTextStyles.medium14, color = tokens.textSecondary) }
            }
        }
    }
}

@Composable
fun ApiRequestOverlay(busy: Boolean, loading: Boolean, error: String?, onRetry: () -> Unit) {
    val tokens = quimiaColorTokens()
    if (loading || error != null) Box(Modifier.fillMaxSize().statusBarsPadding().padding(16.dp), contentAlignment = Alignment.TopCenter) {
        Surface(shadowElevation = 3.dp, shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp), color = tokens.surfaceBase) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                Text(error ?: "Consultando a API…", Modifier.weight(1f), style = QuimiaTextStyles.medium14, color = tokens.foregroundPrimary)
                if (error != null) TextButton(onClick = onRetry) { Text("Tentar novamente") }
            }
        }
    }
    if (busy) Dialog(onDismissRequest = {}, properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)) {
        com.quimia.android.presentation.system.HideSystemNavigationBar()
        Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp), color = tokens.surfaceBase) {
            Column(Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CircularProgressIndicator()
                Text("Aguardando confirmação da API…", style = QuimiaTextStyles.medium16, color = tokens.textPrimary)
            }
        }
    }
}
