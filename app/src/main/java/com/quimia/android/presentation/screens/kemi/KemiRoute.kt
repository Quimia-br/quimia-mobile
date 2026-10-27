package com.quimia.android.presentation.screens.kemi

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.quimia.android.BuildConfig
import com.quimia.android.data.auth.*
import com.quimia.android.data.kemi.*
import com.quimia.android.presentation.screens.errors.*
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

@Composable
internal fun KemiRoute(controller: KemiController, userId: String, displayName: String?, onClose: () -> Unit,
    onHome: () -> Unit, onSignInAgain: () -> Unit) {
    val context = LocalContext.current
    val auth = remember(context) { AuthRepositoryProvider.get(context) }
    val repository = remember(auth) { KemiRepository(AuthApiFactory.create(BuildConfig.AUTH_BASE_URL, KemiApi::class.java, 120), auth) }
    KemiScreen(controller, onClose, displayName, onHome = onHome, onSignInAgain = onSignInAgain,
        historyNotice = "Chats desta sessão do app. O histórico ainda não é sincronizado com sua conta.",
        responder = { request ->
            try {
                val message = request.conversation.messages.single { it.id == request.messageId && it.role == KemiRole.User }
                val result = repository.send(userId, request.conversation.id, message.text)
                KemiReply.Answer(result.texto, kemiResponseTime(result.respondidoEm))
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (failure: Exception) {
                val kind = if (failure is KemiContractException) GenericErrorKind.ServiceUnavailable
                    else apiErrorKind(failure, auth.session.value == SessionState.SignedOut)
                val description = when {
                    failure is HttpException && failure.code() == 429 -> "Muitas mensagens em pouco tempo. Aguarde antes de tentar novamente."
                    failure is SerializationException || failure is KemiContractException -> "O Kemi retornou uma resposta inválida. Tente novamente mais tarde."
                    kind == GenericErrorKind.Timeout -> "O envio demorou mais que o esperado. Ele pode ter sido recebido; tentar novamente pode repetir a mensagem."
                    else -> null
                }
                KemiReply.Failure(kind, description)
            }
        })
}
