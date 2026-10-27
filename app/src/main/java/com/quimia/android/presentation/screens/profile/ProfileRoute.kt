package com.quimia.android.presentation.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.rememberAsyncImagePainter
import com.quimia.android.data.auth.*
import com.quimia.android.presentation.components.BirthDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import com.quimia.android.presentation.screens.errors.*

internal fun profileIsoDate(value: String): String {
    val date = BirthDate.parse(value) ?: throw IllegalArgumentException("Selecione a data completa de nascimento.")
    require(date < BirthDate.today()) { "Selecione uma data anterior a hoje." }
    return "%04d-%02d-%02d".format(java.util.Locale.ROOT, date.year, date.month, date.day)
}
internal fun profileDisplayDate(value: String?): String = value?.takeIf { it.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) }
    ?.let { "${it.takeLast(2)}/${it.substring(5, 7)}/${it.take(4)}" }.orEmpty()

@Composable
internal fun ProfileRoute(userId: String, preferences: ProfilePreferences,
    onPreferencesChange: (ProfilePreferences) -> Unit, onBack: () -> Unit,
    onUpdated: (UserProfile) -> Unit, onLogout: () -> Unit) {
    val context = LocalContext.current
    val repository = remember(context) { AuthRepositoryProvider.get(context) }
    val session by repository.session.collectAsState()
    var loaded by remember(userId) { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var errorKind by remember { mutableStateOf(GenericErrorKind.Unknown) }
    val scope = rememberCoroutineScope()
    val latestUpdated by rememberUpdatedState(onUpdated)
    suspend fun reload() {
        if (loading) return
        loading = true; error = null
        try { repository.reloadProfile(userId)
            loaded = true; (repository.session.value as? SessionState.Authenticated)?.profile?.let(latestUpdated)
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (failure: Exception) { errorKind = apiErrorKind(failure, repository.session.value == SessionState.SignedOut); error = authErrorMessage(failure) }
        finally { loading = false }
    }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, userId) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) scope.launch { reload() } }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(userId) { reload() }
    LaunchedEffect(session) { if (session == SessionState.SignedOut) onLogout() }
    val profile = (session as? SessionState.Authenticated)?.profile?.takeIf { it.id == userId }
    if (error != null && !loading) {
        GenericErrorScreen(errorKind, onClose = { if (loaded) error = null else onBack() },
            description = if (errorKind == GenericErrorKind.Unknown) error.orEmpty() else errorKind.description,
            onAction = { action -> when (action) {
                GenericErrorAction.Retry -> scope.launch { reload() }
                GenericErrorAction.SignIn -> onLogout()
                GenericErrorAction.Home -> onBack()
            } })
        return
    }
    if (!loaded || profile == null) Column(Modifier.fillMaxSize().statusBarsPadding().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (loading) CircularProgressIndicator()
        Text(error ?: "Carregando seu perfil…")
        if (error != null) TextButton(onClick = { scope.launch { reload() } }) { Text("Tentar novamente") }
        TextButton(onClick = onBack) { Text("Voltar") }
    } else {
        val photo = rememberAsyncImagePainter(profile.fotoUrl?.takeIf { it.startsWith("https://") })
        ProfileScreen(profile.nome, profile.email, preferences, onPreferencesChange,
            onDisplayNameChange = {}, onPhotoUriChange = {}, onBack = onBack,
            photo = photo.takeIf { profile.fotoUrl?.startsWith("https://") == true },
            birthDate = profileDisplayDate(profile.dataNasc), onLogout = onLogout,
            saveRemote = { details ->
                require(details.email == profile.email) { "A API não permite alterar o e-mail." }
                val result = repository.completeInitialProfile(normalizedProfileName(details.name), profileIsoDate(details.birthDate), userId)
                latestUpdated(result)
            })
        if (error != null) AlertDialog(onDismissRequest = { error = null }, text = { Text(error.orEmpty()) },
            confirmButton = { TextButton(onClick = { scope.launch { reload() } }) { Text("Tentar novamente") } },
            dismissButton = { TextButton(onClick = { error = null }) { Text("Fechar") } })
    }
}
