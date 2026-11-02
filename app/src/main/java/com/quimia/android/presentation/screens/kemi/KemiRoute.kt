package com.quimia.android.presentation.screens.kemi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.quimia.android.BuildConfig
import com.quimia.android.data.auth.AuthApiFactory
import com.quimia.android.data.auth.AuthRepositoryProvider
import com.quimia.android.data.auth.SessionState
import com.quimia.android.data.kemi.KemiApi
import com.quimia.android.data.kemi.KemiContractException
import com.quimia.android.data.kemi.KemiRepository
import com.quimia.android.presentation.screens.errors.GenericErrorKind
import com.quimia.android.presentation.screens.errors.apiErrorKind
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

@Composable
internal fun KemiRoute(userId: String, controller: KemiController, displayName: String?,
    onClose: () -> Unit, onHome: () -> Unit, onSignInAgain: () -> Unit) {
    val context = LocalContext.current
    val sessions = remember(context) { AuthRepositoryProvider.get(context) }
    val repository = remember(sessions) {
        KemiRepository(AuthApiFactory.create(BuildConfig.AUTH_BASE_URL, KemiApi::class.java, timeoutSeconds = 75), sessions)
    }
    KemiScreen(controller = controller, displayName = displayName,
        onClose = onClose, onHome = onHome, onSignInAgain = onSignInAgain,
        responder = { request ->
            val message = request.conversation.messages.single { it.id == request.messageId }
            try {
                KemiReply.Answer(repository.send(userId, request.conversation.id, message.text))
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (error: Exception) {
                KemiReply.Failure(
                    if (error is KemiContractException) GenericErrorKind.ServiceUnavailable
                    else apiErrorKind(error, sessions.session.value == SessionState.SignedOut),
                    if (error is HttpException && error.code() == 429)
                        "Você atingiu o limite de 20 mensagens por hora. Aguarde para conversar novamente." else null,
                )
            }
        },
        onEndConversation = { repository.close(userId, it) },
    )
}
