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
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MultipartBody
import retrofit2.http.*
import com.quimia.android.BuildConfig

@Serializable internal data class ProfilePhotoResponse(val fotoUrl: String)
internal interface ProfilePhotoApi {
    @Multipart @POST("api/v1/usuarios/me/foto")
    suspend fun upload(@Header("Authorization") bearer: String, @Part image: MultipartBody.Part): ProfilePhotoResponse
}

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
    val photoApi = remember { AuthApiFactory.create(BuildConfig.AUTH_BASE_URL, ProfilePhotoApi::class.java) }
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
    if (!loaded || profile == null) Column(Modifier.fillMaxSize().statusBarsPadding()) {
        nav.QuimiaNav(showBackButton = true, onBackClick = onBack, onCloseClick = onBack)
        com.quimia.android.presentation.components.LoadingSkeleton(com.quimia.android.presentation.components.SkeletonLayout.Profile, "Carregando perfil")
    } else {
        val photo = rememberAsyncImagePainter(profile.fotoUrl?.takeIf { it.startsWith("https://") })
        ProfileScreen(profile.nome, profile.email, preferences, onPreferencesChange,
            onDisplayNameChange = {}, onPhotoUriChange = {}, onBack = onBack,
            photo = photo.takeIf { profile.fotoUrl?.startsWith("https://") == true },
            birthDate = profileDisplayDate(profile.dataNasc), onLogout = onLogout,
            savePhotoRemote = { uri ->
                val mime = context.contentResolver.getType(uri)?.lowercase()
                require(mime in setOf("image/jpeg", "image/png", "image/webp")) { "Escolha uma imagem JPEG, PNG ou WebP." }
                val bytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        val output = java.io.ByteArrayOutputStream()
                        val buffer = ByteArray(8192)
                        while (output.size() <= 5 * 1024 * 1024) {
                            val count = input.read(buffer, 0, minOf(buffer.size, 5 * 1024 * 1024 + 1 - output.size()))
                            if (count <= 0) break
                            output.write(buffer, 0, count)
                        }
                        output.toByteArray()
                    }
                        ?: throw IllegalArgumentException("Não foi possível abrir a imagem.")
                }
                require(bytes.isNotEmpty() && bytes.size <= 5 * 1024 * 1024) { "Escolha uma imagem de até 5 MB." }
                val part = MultipartBody.Part.createFormData("imagem", "perfil", bytes.toRequestBody(requireNotNull(mime).toMediaType()))
                val result = repository.withSession(userId) { photoApi.upload(it, part) }
                require(result.fotoUrl.startsWith("https://")) { "A API retornou uma foto inválida." }
                repository.reloadProfile(userId)
                val updated = (repository.session.value as? SessionState.Authenticated)?.profile
                require(updated?.fotoUrl == result.fotoUrl) { "A API não confirmou a foto no perfil." }
                latestUpdated(updated)
            },
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
