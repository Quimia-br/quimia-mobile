package com.quimia.android.data.project

object ApiContractGaps {
    const val Favorites = "Contrato incompleto: não há rota para consultar, favoritar ou desfavoritar produtos e substâncias. Nenhuma alteração foi salva."
    const val SavedPoints = "Contrato incompleto: não há rota de pontos salvos, vínculo com o usuário ou definição do ponto de descarte principal. Nenhuma alteração foi salva."
    const val PointCoordinates = "GET /api/v1/pontos não retorna latitude, longitude ou distância. Os endereços reais podem ser listados, mas não serão posicionados por estimativa no mapa."
    const val City = "Contrato incompleto: LocalizacaoRequest e LocalizacaoResponse não possuem cidade. O endereço não foi enviado, para não descartar esse campo silenciosamente."
    const val PhotoUpload = "PATCH /api/v1/usuarios/me aceita fotoUrl HTTPS, mas não há rota de upload de foto do usuário. Uma imagem local não pode ser enviada como fotoUrl."
    const val LabelScan = "Não há rota mobile que receba imagem do rótulo e devolva produtos identificados. A busca por código de barras existe, mas não substitui reconhecimento de imagem."
    const val SubstanceRoom = "Não há rota para relacionar diretamente uma substância a cômodos. O contrato atual relaciona produtos a cômodos."
}

class ApiContractException(message: String) : Exception(message)
