package com.quimia.android.presentation.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import buttons.QuimiaButton
import buttons.QuimiaIconButton
import card.QuimiaCard
import card.QuimiaCardLayout
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.composables.icons.lucide.*
import com.quimia.android.data.home.HomeMixture
import com.quimia.android.data.home.HomeProduct
import com.quimia.android.presentation.screens.catalog.CatalogModalScrimColor
import com.quimia.android.presentation.screens.errors.GenericErrorKind
import com.quimia.android.presentation.screens.mixtures.mixtureDateLabel
import com.quimia.android.presentation.system.HideSystemNavigationBar
import shapeIcon.QuimiaShapeIcon
import theme.QuimiaTextStyles
import theme.quimiaColorTokens
import theme.quimiaContrastOutline
import theme.LightTokens

internal enum class HomeAssessment { Compatible, Incompatible, Unassessed, Unknown }
internal fun HomeMixture.assessment(): HomeAssessment = when (resultado) {
    "compativel" -> if (severidade.isNullOrBlank()) HomeAssessment.Compatible else HomeAssessment.Unknown
    "incompativel" -> HomeAssessment.Incompatible
    "nao_avaliado" -> HomeAssessment.Unassessed
    else -> HomeAssessment.Unknown
}
internal fun HomeMixture.resultLabel(): String = when (resultado) {
    "compativel" -> "Compatível"
    "incompativel" -> "Incompatível"
    "nao_avaliado" -> "Não avaliado"
    else -> resultado
}
private fun HomeMixture.severityLabel(): String? = severidade?.takeIf { it.isNotBlank() }?.let {
    when (it) { "baixa" -> "Baixa"; "media" -> "Média"; "alta" -> "Alta"; "critica" -> "Crítica"; else -> it }
}

@Composable
internal fun HomeLoading(message: String) {
    com.quimia.android.presentation.components.LoadingSkeleton(
        if (message.contains("perfil")) com.quimia.android.presentation.components.SkeletonLayout.Lines
        else com.quimia.android.presentation.components.SkeletonLayout.History, message)

}

@Composable
internal fun HomeLoadError(kind: GenericErrorKind, subject: String, onRetry: () -> Unit) {
    val tokens = quimiaColorTokens()
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Não foi possível carregar $subject.", style = QuimiaTextStyles.medium16, color = tokens.foregroundPrimary)
        Text(kind.description, style = QuimiaTextStyles.medium14, color = tokens.foregroundSubtle)
        QuimiaButton(text = "Tentar novamente", onClick = onRetry, modifier = Modifier.fillMaxWidth().height(48.dp), iconSize = 0, espacamento = 0)
    }
}

@Composable
private fun HomeProductPhoto(product: HomeProduct?, modifier: Modifier) {
    val url = homePhotoUrl(product?.fotoUrl)
    val photo = rememberAsyncImagePainter(url)
    if (url == null || photo.state is AsyncImagePainter.State.Error) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Icon(Lucide.ImageOff, "Imagem indisponível de ${product?.nome.orEmpty()}", tint = quimiaColorTokens().foregroundSubtle)
        }
    } else Image(photo, product?.nome, modifier, contentScale = ContentScale.Fit)
}

@Composable
private fun HomeAssessmentIcon(item: HomeMixture, size: androidx.compose.ui.unit.Dp = 50.dp) {
    val tokens = quimiaColorTokens()
    val assessment = item.assessment()
    QuimiaShapeIcon(size = size,
        icon = when (assessment) { HomeAssessment.Compatible -> Lucide.Shield; HomeAssessment.Incompatible -> Lucide.Skull; else -> Lucide.TriangleAlert },
        containerColor = when (assessment) { HomeAssessment.Compatible -> tokens.success; HomeAssessment.Incompatible -> tokens.error; else -> tokens.warning },
        iconColor = if (assessment == HomeAssessment.Incompatible) tokens.white else LightTokens.foregroundPrimary)
}

@Composable
internal fun HomeApiHistoryCard(item: HomeMixture, onClick: () -> Unit) {
    val tokens = quimiaColorTokens()
    QuimiaCard(title = "", showTitle = false, primaryActionLabel = null, secondaryActionLabel = null,
        layout = QuimiaCardLayout.Stacked, contentPadding = 24.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 20.dp)
            .clip(RoundedCornerShape(24.dp)).clickable(role = Role.Button, onClickLabel = "Ver mistura salva", onClick = onClick)) {
        Text("${item.produtoA?.nome.orEmpty()} & ${item.produtoB?.nome.orEmpty()}", style = QuimiaTextStyles.medium15, color = tokens.foregroundPrimary)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(104f).height(169.dp), contentAlignment = Alignment.Center) {
                HomeProductPhoto(item.produtoB, Modifier.offset(x = 18.dp).rotate(6.13f).size(56.62.dp, 119.63.dp))
                HomeProductPhoto(item.produtoA, Modifier.offset(x = (-12).dp).rotate(-14.7f).size(56.62.dp, 119.63.dp))
            }
            Column(Modifier.weight(150f).clip(RoundedCornerShape(32.dp)).background(tokens.surfaceBackground).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                HomeAssessmentIcon(item)
                Text(item.resultLabel(), style = QuimiaTextStyles.medium16, color = tokens.foregroundPrimary)
                item.severityLabel()?.let { Text("Gravidade: $it", style = QuimiaTextStyles.medium14, color = tokens.foregroundSubtle) }
                item.descricaoRisco?.takeIf { it.isNotBlank() }?.let {
                    Text(it, maxLines = 3, overflow = TextOverflow.Ellipsis, style = QuimiaTextStyles.medium14, color = tokens.foregroundSubtle)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(item.consultadoEm?.let(::homeTimestamp)?.let { mixtureDateLabel(it) } ?: "Data não informada",
            style = QuimiaTextStyles.medium14, color = tokens.foregroundSubtle)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeHistoryDetails(item: HomeMixture, onClose: () -> Unit) {
    val tokens = quimiaColorTokens()
    ModalBottomSheet(onDismissRequest = onClose, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.statusBarsPadding().padding(horizontal = 17.dp, vertical = 16.dp), sheetMaxWidth = 560.dp,
        containerColor = tokens.surfaceBase, scrimColor = CatalogModalScrimColor, shape = RoundedCornerShape(40.dp), dragHandle = null) {
        HideSystemNavigationBar()
        Column(Modifier.fillMaxWidth().heightIn(max = 700.dp).quimiaContrastOutline(RoundedCornerShape(40.dp))
            .verticalScroll(rememberScrollState()).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                QuimiaIconButton(icon = Lucide.X, containerColor = tokens.secondary, onClick = onClose, contentDescription = "Fechar mistura salva")
            }
            HomeAssessmentIcon(item, 120.dp)
            Text(item.resultLabel(), style = QuimiaTextStyles.medium24, color = tokens.foregroundPrimary)
            item.severityLabel()?.let { Text("Gravidade: $it", style = QuimiaTextStyles.medium16, color = tokens.foregroundPrimary) }
            item.descricaoRisco?.takeIf { it.isNotBlank() }?.let { Text(it, style = QuimiaTextStyles.medium16, color = tokens.foregroundSubtle) }
            listOf(item.produtoA to item.dosagem?.produtoA, item.produtoB to item.dosagem?.produtoB).forEach { (product, dose) ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    HomeProductPhoto(product, Modifier.size(64.dp, 90.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(product?.nome.orEmpty(), style = QuimiaTextStyles.medium16, color = tokens.foregroundPrimary)
                        dose?.takeIf { it.isNotBlank() }?.let { Text(it, style = QuimiaTextStyles.medium14, color = tokens.foregroundSubtle) }
                    }
                }
            }
            Text(item.consultadoEm?.let(::homeTimestamp)?.let { mixtureDateLabel(it) } ?: "Data não informada",
                style = QuimiaTextStyles.medium14, color = tokens.foregroundSubtle)
            QuimiaButton(text = "Fechar", onClick = onClose, modifier = Modifier.fillMaxWidth().height(48.dp), iconSize = 0, espacamento = 0)
        }
    }
}
