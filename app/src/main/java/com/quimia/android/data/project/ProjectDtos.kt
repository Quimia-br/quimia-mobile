package com.quimia.android.data.project

import kotlinx.serialization.Serializable

@Serializable data class ApiPage<T>(val itens: List<T>, val pagina: Int, val tamanho: Int, val totalItens: Long, val totalPaginas: Int)
@Serializable data class ApiUser(val id: String, val nome: String, val email: String, val dataNasc: String? = null, val fotoUrl: String? = null)
@Serializable data class ApiSession(val accessToken: String, val tokenType: String, val expiresIn: Long, val refreshToken: String, val user: ApiUser)
@Serializable data class ApiBrand(val id: Int, val nome: String, val logoUrl: String? = null)
@Serializable data class ApiProduct(val id: Int, val nome: String, val tipo: String? = null, val descricao: String? = null,
    val codBarras: String? = null, val fotoUrl: String? = null, val marca: ApiBrand? = null)
@Serializable data class ApiRoomSummary(val id: Int, val nome: String, val icone: String, val totalProdutos: Int, val status: String)
@Serializable data class ApiRoomAssessment(val status: String, val totalProdutos: Int, val paresAvaliados: Int,
    val paresIncompativeis: Int, val severidadeMaxima: String? = null, val incompatibilidades: List<ApiIncompatibility>)
@Serializable data class ApiIncompatibility(val produtoIdA: Int, val produtoIdB: Int, val severidade: String? = null, val descricaoRisco: String? = null)
@Serializable data class ApiRoom(val id: Int, val nome: String, val icone: String, val avaliacao: ApiRoomAssessment?, val produtos: List<ApiProduct>)
@Serializable data class ApiSubstance(val id: Int, val nome: String, val casNumero: String? = null, val descricao: String? = null,
    val classes: List<String>, val produtos: List<ApiSubstanceProduct>)
@Serializable data class ApiSubstanceProduct(val id: Int, val nome: String)
@Serializable data class ApiSubstances(val itens: List<ApiSubstance>)
@Serializable data class ApiSavedProducts(val itens: List<ApiSavedProduct>)
@Serializable data class ApiSavedProduct(val produtoId: Int, val nome: String? = null, val status: String)
@Serializable data class ApiPoint(val id: Int, val nome: String, val tipo: String, val cep: String? = null, val estado: String? = null,
    val bairro: String? = null, val rua: String? = null, val numero: Int? = null, val complemento: String? = null)
@Serializable data class ApiAddress(val cep: String? = null, val estado: String? = null, val bairro: String? = null,
    val rua: String? = null, val numero: Int? = null, val complemento: String? = null)
@Serializable data class ApiMixture(val id: Int, val produtoA: ApiProduct, val produtoB: ApiProduct, val resultado: String,
    val severidade: String? = null, val descricaoRisco: String? = null, val consultadoEm: String? = null)
@Serializable data class ApiRecoveryChallenge(val challengeToken: String, val expiresIn: Long)
@Serializable data class ApiRecoveryGrant(val resetToken: String, val expiresIn: Long)
@Serializable data class ApiChatReply(val texto: String, val sessaoId: String, val respondidoEm: String)
@Serializable data class ApiSummary(val produtoId: Int, val resumo: String? = null)
@Serializable data class ApiFirstAid(val produtoId: Int, val encontrada: Boolean, val porExposicao: List<ApiExposure>,
    val orientacoesGerais: List<String>, val notasAoMedico: List<String>)
@Serializable data class ApiExposure(val rotaExposicao: String, val descricao: String? = null, val sintomas: String? = null,
    val tratamentoEspecial: String? = null, val atencaoMedicaImediata: Boolean)
