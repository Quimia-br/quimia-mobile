package com.quimia.android.data.auth

internal fun googleSignInErrorMessage(statusCode: Int): String = when (statusCode) {
    10 -> "O acesso Google precisa ser configurado para esta versão do app. Você pode entrar com e-mail e senha enquanto isso."
    7 -> "Não foi possível conectar ao Google. Verifique sua conexão e tente novamente."
    else -> "Não foi possível entrar com Google. Tente novamente."
}
