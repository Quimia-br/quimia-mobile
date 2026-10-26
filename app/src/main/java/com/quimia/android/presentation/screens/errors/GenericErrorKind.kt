package com.quimia.android.presentation.screens.errors

enum class GenericErrorAction(val label: String) {
    Retry("Tentar novamente"), SignIn("Entrar novamente"), Home("Voltar ao início"),
}

enum class GenericErrorKind(val title: String, val description: String, val action: GenericErrorAction) {
    Unknown("Algo deu errado", "Por favor, tente novamente\ndaqui a pouco", GenericErrorAction.Retry),
    Offline("Você está sem conexão", "Confira sua conexão com a internet e tente novamente.", GenericErrorAction.Retry),
    ServiceUnavailable("Serviço indisponível", "Não conseguimos acessar o serviço agora. Tente daqui a pouco.", GenericErrorAction.Retry),
    SessionExpired("Sua sessão expirou", "Entre novamente na sua conta para continuar usando o Quimia.", GenericErrorAction.SignIn),
    AccessDenied("Acesso não permitido", "Sua conta não tem permissão para acessar este conteúdo.", GenericErrorAction.Home),
    ContentUnavailable("Conteúdo indisponível", "Este conteúdo foi removido ou não está mais disponível.", GenericErrorAction.Home),
    Timeout("Demorou mais que o esperado", "Não foi possível concluir a solicitação a tempo. Tente novamente.", GenericErrorAction.Retry),
}
