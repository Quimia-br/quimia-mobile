package com.quimia.android.presentation.screens.profile

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import avatar.QuimiaAvatar
import bottomSheet.QuimiaBottomSheet
import bottomSheet.QuimiaBottomSheetContent
import buttons.*
import card.QuimiaCard
import com.composables.icons.lucide.*
import com.quimia.android.presentation.screens.catalog.CatalogModalScrimColor
import com.quimia.android.presentation.system.HideSystemNavigationBar
import controls.QuimiaSwitch
import controls.QuimiaTextSizeSlider
import input.QuimiaInput
import nav.QuimiaNav
import theme.QuimiaTextStyles
import theme.quimiaColorTokens
import toast.QuimiaToast
import toast.QuimiaToastType

@Composable
fun ProfileScreen(
    displayName: String, email: String, preferences: ProfilePreferences,
    onPreferencesChange: (ProfilePreferences) -> Unit,
    onDisplayNameChange: (String) -> Unit, onPhotoUriChange: (String) -> Unit,
    onBack: () -> Unit, photo: Painter? = null, initialEditVisible: Boolean = false,
) {
    val tokens = quimiaColorTokens()
    val accessibleLayout = LocalDensity.current.fontScale > 1.1f || theme.quimiaDyslexicFontEnabled()
    val context = LocalContext.current
    var editVisible by rememberSaveable { mutableStateOf(initialEditVisible) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) try {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            onPhotoUriChange(uri.toString())
        } catch (_: SecurityException) { message = "Não foi possível salvar o acesso à foto. Escolha outra imagem." }
    }
    Box(Modifier.fillMaxSize().background(tokens.surfaceBackground), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 600.dp).fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height((62.dp - WindowInsets.statusBars.asPaddingValues().calculateTopPadding()).coerceAtLeast(0.dp)))
            QuimiaNav(showBackButton = true, showCloseButton = false, buttonColor = tokens.surfaceBase, onBackClick = onBack)
            BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(24.dp)) {
                val identity: @Composable (Modifier) -> Unit = { identityModifier ->
                    Column(identityModifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(displayName.ifBlank { "Seu nome" }, style = QuimiaTextStyles.medium20, color = tokens.foregroundPrimary)
                            Text(email, Modifier.widthIn(max = if (accessibleLayout) 264.dp else 112.dp).semantics { contentDescription = email }, style = QuimiaTextStyles.medium14,
                                color = tokens.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        QuimiaButton(modifier = Modifier.fillMaxWidth().then(if (!accessibleLayout) Modifier.height(41.dp) else Modifier.heightIn(min = 48.dp)), text = "Editar perfil",
                            textStyle = QuimiaTextStyles.medium14, iconSize = 0, espacamento = 0,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp), containerColor = tokens.secondary,
                            onClick = { editVisible = true })
                    }
                }
                val portrait: @Composable () -> Unit = {
                    Box(Modifier.width(112.dp).height(127.dp)) {
                        QuimiaAvatar(size = 112.dp, photo = photo, initials = profileInitials(displayName), contentDescription = "Foto do perfil")
                        QuimiaIconButton(modifier = Modifier.align(Alignment.BottomStart), icon = Lucide.Pencil, containerColor = tokens.secondary,
                            contentDescription = "Alterar foto do perfil", onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) })
                    }
                }
                if (accessibleLayout || maxWidth < 240.dp) Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    portrait()
                    identity(Modifier.fillMaxWidth())
                } else Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(26.dp)) {
                    identity(Modifier.weight(1f))
                    portrait()
                }
            }
            if (message != null) QuimiaToast(message = message.orEmpty(), type = QuimiaToastType.Error,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp), onDismiss = { message = null })
            QuimiaCard(title = "Tema", description = "Acessibilidade", primaryActionLabel = null, secondaryActionLabel = null,
                modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 12.dp),
                contentPadding = 24.dp, sectionSpacing = 17.dp, endIcon = null) {
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val buttonSize = ((maxWidth - 30.dp) / 4).coerceIn(45.dp, 56.dp)
                    Row(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ProfileTheme.entries.forEach { item ->
                            QuimiaIconButton(modifier = Modifier.size(buttonSize).semantics {
                                selected = preferences.theme == item; role = Role.RadioButton
                                stateDescription = if (preferences.theme == item) "Selecionado" else "Não selecionado"
                            }, icon = when (item) {
                                ProfileTheme.Light -> Lucide.Sun
                                ProfileTheme.Dark -> Lucide.Moon
                                ProfileTheme.HighContrast -> Lucide.Blend
                                ProfileTheme.System -> Lucide.Monitor
                            }, size = QuimiaIconButtonSize.Large, iconSize = 20.dp,
                                containerColor = if (preferences.theme == item) tokens.primary else tokens.secondary,
                                contentDescription = "Tema ${item.label}", onClick = { onPreferencesChange(preferences.copy(theme = item)) })
                        }
                    }
                }
            }
            BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)) {
                if (maxWidth < 312.dp || accessibleLayout) Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProfileTextCard(preferences, onPreferencesChange, Modifier.fillMaxWidth())
                    ProfileFontCard(preferences, onPreferencesChange, Modifier.fillMaxWidth())
                } else Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProfileTextCard(preferences, onPreferencesChange, Modifier.weight(1f))
                    ProfileFontCard(preferences, onPreferencesChange, Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
    if (editVisible) ProfileEditSheet(displayName, email, onDismiss = { editVisible = false }, onSave = {
        onDisplayNameChange(it); editVisible = false
    }, previewBackground = { ProfileScreen(displayName, email, preferences, {}, {}, {}, {}, photo) })
}

@Composable
private fun ProfileTextCard(preferences: ProfilePreferences, onChange: (ProfilePreferences) -> Unit, modifier: Modifier) {
    val tokens = quimiaColorTokens()
    QuimiaCard(title = "Texto", description = "Acessibilidade", primaryActionLabel = null, secondaryActionLabel = null,
        modifier = modifier.heightIn(min = 175.dp), contentPadding = 24.dp, sectionSpacing = 27.dp, endIcon = null) {
        Text("Aa", style = QuimiaTextStyles.medium15, color = tokens.foregroundPrimary)
        Spacer(Modifier.height(10.dp))
        QuimiaTextSizeSlider(step = preferences.textSizeStep, onStepChange = { onChange(preferences.copy(textSizeStep = it)) },
            modifier = Modifier.fillMaxWidth().widthIn(max = 150.dp))
    }
}

@Composable
private fun ProfileFontCard(preferences: ProfilePreferences, onChange: (ProfilePreferences) -> Unit, modifier: Modifier) {
    QuimiaCard(title = "Dislexic Font", description = "Acessibilidade", primaryActionLabel = null, secondaryActionLabel = null,
        modifier = modifier.heightIn(min = 175.dp), contentPadding = 24.dp, sectionSpacing = 32.dp, endIcon = null) {
        Box(Modifier.fillMaxWidth().height(52.dp)) {
            QuimiaSwitch(checked = preferences.dyslexicFont, onCheckedChange = { onChange(preferences.copy(dyslexicFont = it)) },
                modifier = Modifier.align(Alignment.BottomStart).offset(y = 10.dp), contentDescription = "Fonte OpenDyslexic")
        }
    }
}

@Composable
private fun ProfileEditSheet(name: String, email: String, onDismiss: () -> Unit, onSave: (String) -> Unit, previewBackground: @Composable () -> Unit) {
    var draftName by rememberSaveable { mutableStateOf(name) }
    val fields: @Composable ColumnScope.() -> Unit = {
        HideSystemNavigationBar()
        Spacer(Modifier.height(16.dp))
        QuimiaInput(value = draftName, onValueChange = { draftName = it.take(80) }, label = "Nome",
            trailingIcon = if (draftName.isNotEmpty()) Lucide.X else null, onTrailingIconClick = { draftName = "" })
        Spacer(Modifier.height(16.dp))
        QuimiaInput(value = email, onValueChange = {}, label = "E-mail", enabled = false, trailingIcon = null)
        Spacer(Modifier.height(24.dp))
    }
    if (LocalInspectionMode.current) Box(Modifier.fillMaxSize()) {
        previewBackground()
        Box(Modifier.fillMaxSize().background(CatalogModalScrimColor), contentAlignment = Alignment.BottomCenter) {
            Surface(Modifier.statusBarsPadding().padding(horizontal = 16.dp, vertical = 16.dp).widthIn(max = 560.dp).fillMaxWidth(),
                shape = RoundedCornerShape(40.dp), color = quimiaColorTokens().surfaceBase) {
                QuimiaBottomSheetContent(title = "Editar perfil", description = "As alterações ficam salvas neste aparelho.", contentHeight = null,
                    primaryActionLabel = "Salvar", secondaryActionLabel = "Cancelar", onClose = onDismiss,
                    primaryActionEnabled = profileNameError(draftName) == null, onPrimaryActionClick = { onSave(normalizedProfileName(draftName)) },
                    onSecondaryActionClick = onDismiss, content = fields)
            }
        }
    } else QuimiaBottomSheet(isVisible = true, onDismissRequest = onDismiss, title = "Editar perfil",
        description = "As alterações ficam salvas neste aparelho.", sheetMaxWidth = 560.dp,
        modifier = Modifier.statusBarsPadding().imePadding().padding(horizontal = 16.dp, vertical = 16.dp),
        shape = RoundedCornerShape(40.dp), scrimColor = CatalogModalScrimColor, contentHeight = null,
        primaryActionLabel = "Salvar", secondaryActionLabel = "Cancelar", primaryActionEnabled = profileNameError(draftName) == null,
        onPrimaryActionClick = { onSave(normalizedProfileName(draftName)) }, onSecondaryActionClick = onDismiss, content = fields)
}
