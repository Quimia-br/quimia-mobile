package com.quimia.android.presentation.screens.profile

import theme.quimiaSkeleton
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import avatar.QuimiaAvatar
import bottomSheet.QuimiaBottomSheet
import bottomSheet.QuimiaBottomSheetContent
import buttons.*
import card.QuimiaCard
import com.composables.icons.lucide.*
import com.quimia.android.presentation.system.HideSystemNavigationBar
import controls.QuimiaSwitch
import controls.QuimiaTextSizeSlider
import dragger.QuimiaDragger
import input.QuimiaInput
import com.quimia.android.presentation.components.BirthDateField
import com.quimia.android.presentation.components.BirthDateSheet
import nav.QuimiaNav
import theme.LightTokens
import theme.QuimiaTextStyles
import theme.quimiaColorTokens
import theme.quimiaHighContrastEnabled
import theme.quimiaContrastBorder
import title.QuimiaMainTitle
import toast.QuimiaToast
import toast.QuimiaToastType
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import com.quimia.android.data.auth.authErrorMessage

@Composable
fun ProfileScreen(
    displayName: String, email: String, preferences: ProfilePreferences,
    onPreferencesChange: (ProfilePreferences) -> Unit,
    onDisplayNameChange: (String) -> Unit, onPhotoUriChange: (String) -> Unit,
    onBack: () -> Unit, photo: Painter? = null, initialEditVisible: Boolean = false,
    birthDate: String = "", onDetailsChange: ((ProfileDetails) -> Unit)? = null,
    onLogout: () -> Unit = {}, initialPhotoVisible: Boolean = false, initialBirthDateVisible: Boolean = false,
    saveRemote: (suspend (ProfileDetails) -> Unit)? = null,
    savePhotoRemote: (suspend (android.net.Uri) -> Unit)? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var saving by remember { mutableStateOf(false) }
    var editVisible by rememberSaveable { mutableStateOf(initialEditVisible || initialPhotoVisible || initialBirthDateVisible) }
    var birthDateVisible by rememberSaveable { mutableStateOf(initialBirthDateVisible) }
    var photoVisible by rememberSaveable { mutableStateOf(initialPhotoVisible) }
    var draftName by rememberSaveable { mutableStateOf(displayName) }
    var draftEmail by rememberSaveable { mutableStateOf(email) }
    var draftBirthDate by rememberSaveable { mutableStateOf(birthDate) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null && !saving) {
            if (savePhotoRemote != null) scope.launch {
                saving = true; message = null
                try { savePhotoRemote(uri); photoVisible = false }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (error: Exception) { message = if (error is IllegalArgumentException) error.message else authErrorMessage(error) }
                finally { saving = false }
            } else try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                onPhotoUriChange(uri.toString()); photoVisible = false
            } catch (_: SecurityException) { message = "Não foi possível acessar a foto." }
        }
    }
    fun openEditor() {
        draftName = displayName; draftEmail = email; draftBirthDate = birthDate
        message = null; editVisible = true
    }
    val onPhoto = {
        if (!saving) { if (!editVisible) openEditor(); message = null; photoVisible = true }
    }
    BackHandler(enabled = editVisible || photoVisible) {
        if (!saving) { if (photoVisible) photoVisible = false else editVisible = false }
    }
    if (editVisible) ProfileEditScreen(
        name = draftName, email = draftEmail, birthDate = draftBirthDate, photo = photo,
        onNameChange = { draftName = it }, onEmailChange = { draftEmail = it },
        onBirthDateChange = { draftBirthDate = it }, onPhoto = onPhoto,
        onBack = { if (!saving) editVisible = false }, saving = saving, remote = saveRemote != null, message = message, onMessageDismiss = { message = null },
        onSave = {
            val details = ProfileDetails(normalizedProfileName(draftName), draftBirthDate.trim(), draftEmail.trim())
            if (saveRemote == null) {
                if (onDetailsChange != null) onDetailsChange(details) else onDisplayNameChange(details.name)
                editVisible = false
            } else if (!saving) scope.launch {
                saving = true; message = null
                try { saveRemote(details); editVisible = false }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (error: Exception) { message = authErrorMessage(error) }
                finally { saving = false }
            }
        },
    ) else ProfilePreferencesScreen(displayName, email, preferences, onPreferencesChange, onBack,
        photo = photo, onEdit = ::openEditor, onPhoto = onPhoto, onLogout = onLogout)

    if (!editVisible && message != null) androidx.compose.material3.AlertDialog(
        onDismissRequest = { message = null }, text = { Text(message.orEmpty()) },
        confirmButton = { androidx.compose.material3.TextButton(onClick = { message = null }) { Text("Entendi") } })
    if (photoVisible) ProfilePhotoSheet(displayName, photo, message,
        onDismiss = { if (!saving) { photoVisible = false; message = null } },
        onChoose = { if (!saving) photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) })
    if (birthDateVisible) BirthDateSheet(draftBirthDate, onDismiss = { birthDateVisible = false },
        onSave = { draftBirthDate = it; birthDateVisible = false })
}

@Composable
private fun ProfilePortrait(name: String, photo: Painter?, onPhoto: () -> Unit) {
    Box(Modifier.width(112.dp).height(127.dp)) {
        QuimiaAvatar(size = 112.dp, photo = photo, initials = profileInitials(name), contentDescription = "Foto do perfil")
        QuimiaIconButton(modifier = Modifier.align(Alignment.BottomStart),
            icon = Lucide.Pencil, containerColor = quimiaColorTokens().secondary,
            contentDescription = "Alterar foto do perfil", onClick = onPhoto)
    }
}

@Composable
private fun ProfilePreferencesScreen(
    displayName: String, email: String, preferences: ProfilePreferences, onPreferencesChange: (ProfilePreferences) -> Unit,
    onBack: () -> Unit, photo: Painter?, onEdit: () -> Unit, onPhoto: () -> Unit, onLogout: () -> Unit,
) {
    val tokens = quimiaColorTokens()
    val accessibleLayout = LocalDensity.current.fontScale > 1.1f || theme.quimiaDyslexicFontEnabled()
    Box(Modifier.fillMaxSize().background(tokens.surfaceBackground), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 600.dp).fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height((62.dp - WindowInsets.statusBars.asPaddingValues().calculateTopPadding()).coerceAtLeast(0.dp)))
            QuimiaNav(showBackButton = true, showCloseButton = false, buttonColor = tokens.surfaceBase,
                onBackClick = onBack)
            BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(24.dp)) {
                val identity: @Composable (Modifier) -> Unit = { identityModifier ->
                    Column(identityModifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(displayName.ifBlank { "Seu nome" }, modifier = Modifier.quimiaSkeleton(8.dp), style = QuimiaTextStyles.medium20, color = tokens.foregroundPrimary)
                            Text(email, Modifier.quimiaSkeleton(8.dp).widthIn(max = if (accessibleLayout) 264.dp else 112.dp)
                                .semantics { contentDescription = email }, style = QuimiaTextStyles.medium14,
                                color = if (tokens.surfaceBackground == theme.DarkTokens.surfaceBackground) tokens.foregroundPrimary.copy(alpha = .6f) else tokens.textSecondary,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        QuimiaButton(modifier = Modifier.fillMaxWidth()
                            .then(if (!accessibleLayout) Modifier.height(41.dp) else Modifier.heightIn(min = 48.dp)),
                            text = "Editar perfil", textStyle = QuimiaTextStyles.medium14, iconSize = 0, espacamento = 0,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp), containerColor = tokens.secondary, onClick = onEdit)
                    }
                }
                if (accessibleLayout || maxWidth < 240.dp) Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    ProfilePortrait(displayName, photo, onPhoto)
                    identity(Modifier.fillMaxWidth())
                } else Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(26.dp)) {
                    identity(Modifier.weight(1f))
                    ProfilePortrait(displayName, photo, onPhoto)
                }
            }
            QuimiaCard(title = "Tema", description = "Acessibilidade", primaryActionLabel = null, secondaryActionLabel = null,
                modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 12.dp),
                contentPadding = 24.dp, sectionSpacing = 17.dp, endIcon = null) {
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val buttonSize = ((maxWidth - 30.dp) / 4).coerceIn(45.dp, 56.dp)
                    Row(Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ProfileTheme.entries.forEach { item ->
                            val chosen = preferences.theme == item
                            QuimiaIconButton(modifier = Modifier.size(buttonSize).semantics {
                                selected = chosen; role = Role.RadioButton
                                stateDescription = if (chosen) "Selecionado" else "Não selecionado"
                            }, icon = when (item) {
                                ProfileTheme.Light -> Lucide.Sun
                                ProfileTheme.Dark -> Lucide.Moon
                                ProfileTheme.HighContrast -> Lucide.Blend
                                ProfileTheme.System -> Lucide.Monitor
                            }, size = QuimiaIconButtonSize.Large, iconSize = 20.dp, contrastOutline = !chosen,
                                iconColor = if (chosen) LightTokens.foregroundPrimary else tokens.foregroundPrimary,
                                containerColor = if (chosen) tokens.primary else tokens.secondary,
                                contentDescription = "Tema " + item.label, onClick = { onPreferencesChange(preferences.copy(theme = item)) })
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
            QuimiaButton(text = "Sair", modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                .padding(top = if (quimiaHighContrastEnabled()) 18.dp else 8.dp)
                .then(if (accessibleLayout) Modifier.heightIn(min = 48.dp) else Modifier.height(43.dp)),
                iconSize = 0, espacamento = 0, textStyle = QuimiaTextStyles.medium16,
                containerColor = tokens.onForeground, textColor = tokens.foregroundPrimary, onClick = onLogout)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileTextCard(preferences: ProfilePreferences, onChange: (ProfilePreferences) -> Unit, modifier: Modifier) {
    val tokens = quimiaColorTokens()
    QuimiaCard(title = "Texto", description = "Acessibilidade", primaryActionLabel = null, secondaryActionLabel = null,
        modifier = modifier.heightIn(min = if (quimiaHighContrastEnabled()) 168.dp else 158.dp), contentPadding = 24.dp,
        sectionSpacing = 10.dp, endIcon = null) {
        Text("Aa", style = QuimiaTextStyles.medium15, color = tokens.foregroundPrimary)
        Spacer(Modifier.height(10.dp))
        QuimiaTextSizeSlider(step = preferences.textSizeStep, onStepChange = { onChange(preferences.copy(textSizeStep = it)) },
            modifier = Modifier.widthIn(max = 150.dp).fillMaxWidth())
    }
}

@Composable
private fun ProfileFontCard(preferences: ProfilePreferences, onChange: (ProfilePreferences) -> Unit, modifier: Modifier) {
    QuimiaCard(title = "Dislexic Font", description = "Acessibilidade", primaryActionLabel = null, secondaryActionLabel = null,
        modifier = modifier.heightIn(min = if (quimiaHighContrastEnabled()) 168.dp else 158.dp), contentPadding = 24.dp,
        sectionSpacing = 32.dp, endIcon = null) {
        Box(Modifier.fillMaxWidth().height(if (quimiaHighContrastEnabled()) 45.dp else 35.dp)) {
            QuimiaSwitch(checked = preferences.dyslexicFont, onCheckedChange = { onChange(preferences.copy(dyslexicFont = it)) },
                modifier = Modifier.align(Alignment.CenterStart).offset(y = 12.dp),
                contentDescription = "Fonte OpenDyslexic")
        }
    }
}

@Composable
private fun ProfileEditScreen(
    name: String, email: String, birthDate: String, photo: Painter?,
    onNameChange: (String) -> Unit, onEmailChange: (String) -> Unit, onBirthDateChange: (String) -> Unit,
    onBack: () -> Unit, onPhoto: () -> Unit, onSave: () -> Unit, message: String?, onMessageDismiss: () -> Unit,
    saving: Boolean = false, remote: Boolean = false,
) {
    val tokens = quimiaColorTokens()
    Box(Modifier.fillMaxSize().background(tokens.surfaceBackground), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 600.dp).fillMaxSize().statusBarsPadding().imePadding()) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Spacer(Modifier.height((79.dp - WindowInsets.statusBars.asPaddingValues().calculateTopPadding()).coerceAtLeast(0.dp)))
                QuimiaNav(showBackButton = true, showCloseButton = false, onBackClick = onBack)
                Column(Modifier.fillMaxWidth().padding(horizontal = 32.dp), verticalArrangement = Arrangement.spacedBy(31.dp)) {
                    QuimiaMainTitle(title = "Alterar informações do perfil")
                    ProfilePortrait(name, photo, onPhoto)
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        if (message != null) QuimiaToast(message = message, type = QuimiaToastType.Error, onDismiss = onMessageDismiss)
                        QuimiaInput(value = name, onValueChange = { onNameChange(it.take(80)) }, label = "Nome", enabled = !saving,
                            trailingIcon = Lucide.X, trailingIconSize = 16.dp, onTrailingIconClick = { onNameChange("") })
                        BirthDateField(birthDate, { if (!saving) onBirthDateChange(it) }, error = profileBirthDateError(birthDate))
                        QuimiaInput(value = email, onValueChange = { onEmailChange(it.take(254)) }, label = "E-mail", enabled = !remote && !saving, showClearButton = !remote,
                            description = if (remote) "A API ainda não permite alterar o e-mail." else null,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            trailingIcon = Lucide.X, trailingIconSize = 16.dp, onTrailingIconClick = if (remote) null else { { onEmailChange("") } })
                    }
                }
            }
            QuimiaButton(text = if (saving) "Salvando…" else "Salvar", modifier = Modifier.fillMaxWidth().padding(horizontal = 19.dp, vertical = 24.dp).heightIn(min = 58.dp),
                iconSize = 0, espacamento = 0, textStyle = QuimiaTextStyles.medium16,
                enabled = !saving && profileNameError(name) == null && profileEmailError(email) == null && profileBirthDateError(birthDate) == null && (!remote || birthDate.isNotBlank()),
                textColor = LightTokens.foregroundPrimary, onClick = onSave)
        }
    }
}

@Composable
private fun ProfilePhotoSheet(name: String, photo: Painter?, message: String?, onDismiss: () -> Unit, onChoose: () -> Unit) {
    val content: @Composable ColumnScope.() -> Unit = { ProfilePhotoContent(name, photo, message, onDismiss, onChoose) }
    if (LocalInspectionMode.current) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        Surface(Modifier.widthIn(max = 594.dp).fillMaxWidth().padding(start = 17.dp, end = 17.dp, bottom = 11.dp),
            shape = RoundedCornerShape(40.dp), color = quimiaColorTokens().surfaceBase, shadowElevation = 12.dp, border = quimiaContrastBorder()) {
            QuimiaBottomSheetContent(title = null, showDragHandle = false, showCloseButton = false, contentHeight = null,
                contentPadding = PaddingValues(start = 32.dp, top = 32.dp, end = 32.dp, bottom = 27.dp), content = content)
        }
    } else QuimiaBottomSheet(isVisible = true, onDismissRequest = onDismiss, title = null,
        showDragHandle = false, showCloseButton = false, sheetMaxWidth = 594.dp, contentHeight = null,
        modifier = Modifier.statusBarsPadding().padding(start = 17.dp, end = 17.dp, bottom = 11.dp),
        contentPadding = PaddingValues(start = 32.dp, top = 32.dp, end = 32.dp, bottom = 27.dp),
        shape = RoundedCornerShape(40.dp), scrimColor = Color.Transparent, content = content)
}

@Composable
private fun ProfilePhotoContent(name: String, photo: Painter?, message: String?, onDismiss: () -> Unit, onChoose: () -> Unit) {
    HideSystemNavigationBar()
    val tokens = quimiaColorTokens()
    QuimiaDragger(height = 3.dp, width = 91.dp)
    Spacer(Modifier.height(10.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        QuimiaMainTitle(title = "Alterar foto de\nperfil", modifier = Modifier.weight(1f))
        QuimiaIconButton(icon = Lucide.X, contentDescription = "Fechar alteração de foto",
            containerColor = tokens.secondary, onClick = onDismiss)
    }
    Spacer(Modifier.height(10.dp))
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val portraitSize = minOf(260.dp, maxWidth)
        Box(Modifier.fillMaxWidth().height(portraitSize + 62.dp), contentAlignment = Alignment.Center) {
            QuimiaAvatar(size = portraitSize, photo = photo, initials = profileInitials(name), contentDescription = "Foto selecionada")
        }
    }
    if (message != null) QuimiaToast(message = message, type = QuimiaToastType.Error)
    Spacer(Modifier.height(10.dp))
    QuimiaButton(text = "Escolher da galeria", modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
        iconLeftVector = Lucide.Upload, iconSize = 20, espacamento = 10, fillTextWidth = true,
        containerColor = tokens.secondary, textStyle = QuimiaTextStyles.medium16.copy(textAlign = TextAlign.Center), onClick = onChoose)
}
