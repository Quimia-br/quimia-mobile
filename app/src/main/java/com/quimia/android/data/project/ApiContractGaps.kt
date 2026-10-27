package com.quimia.android.data.project

object ApiContractGaps {
    const val Favorites = "Contrato incompleto: não há rota para consultar, favoritar ou desfavoritar produtos e substâncias. Nenhuma alteração foi salva."
    const val SavedPoints = "Contrato incompleto: não há rota de pontos salvos, vínculo com o usuário ou definição do ponto de descarte principal. Nenhuma alteração foi salva."
    const val PointCoordinates = "GET /api/v1/pontos não retorna latitude, longitude ou distância. Os endereços reais podem ser listados, mas não serão posicionados por estimativa no mapa."
    const val City = "A API não retorna nem armazena cidade. Esse campo é consultado pelo CEP; somente CEP, UF, bairro, rua, número e complemento são persistidos na API."
    const val ProfileEmail = "PATCH /api/v1/usuarios/me não permite alterar e-mail. O e-mail exibido é o retornado pela API."
    const val BirthDateClear = "A API aceita alterar a data de nascimento, mas não permite apagá-la: dataNasc=null significa manter o valor atual."
    const val ChatHistory = "A API recebe mensagens e devolve sessaoId, mas não possui rota para listar ou recuperar chats. O histórico mostrado reúne apenas as conversas desta sessão do aplicativo."
    const val PhotoUpload = "PATCH /api/v1/usuarios/me aceita fotoUrl HTTPS, mas não há rota de upload de foto do usuário. Uma imagem local não pode ser enviada como fotoUrl."
    const val LabelScan = "Não há rota mobile que receba imagem do rótulo e devolva produtos identificados. A busca por código de barras existe, mas não substitui reconhecimento de imagem."
    const val SubstanceRoom = "Não há rota para relacionar diretamente uma substância a cômodos. O contrato atual relaciona produtos a cômodos."
}

class ApiContractException(message: String) : java.io.IOException(message)
