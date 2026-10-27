package com.quimia.android.data.project

import com.quimia.android.presentation.screens.catalog.CatalogProduct
import com.quimia.android.presentation.screens.rooms.*
import com.quimia.android.presentation.screens.shelf.ShelfSubstance

fun ApiProduct.toProduct() = CatalogProduct(id.toString(), nome, tipo.orEmpty().replace('_', ' '), 0, 0,
    description = descricao?.takeIf(String::isNotBlank) ?: "Descrição não informada pela API.", barcode = codBarras.orEmpty(),
    imageUrl = fotoUrl, logoUrl = marca?.logoUrl, brandName = marca?.nome.orEmpty())

fun RoomIcon.apiCode(): String = when (this) {
    RoomIcon.Mirror -> "mirror"; RoomIcon.Refrigerator -> "refrigerator"; RoomIcon.Fence -> "fence"
    RoomIcon.Laundry -> "washing-machine"; RoomIcon.DoubleBed -> "bed-double"; RoomIcon.SingleBed -> "bed-single"
    RoomIcon.Toilet -> "toilet"; RoomIcon.Sofa -> "sofa"; RoomIcon.Pool -> "pool"; RoomIcon.Shelving -> "shelving"
}

fun apiRoomSafety(status: String?): RoomSafety = when (status) {
    "seguro" -> RoomSafety.Safe
    "critico" -> RoomSafety.Critical
    else -> RoomSafety.NotEvaluated
}

fun ApiRoom.toRoom(): QuimiaRoom {
    val icon = RoomIcon.entries.firstOrNull { it.apiCode() == icone }
        ?: throw ApiContractException("GET /api/v1/comodos/$id retornou icone='$icone', sem correspondência no contrato do aplicativo.")
    val assessment = avaliacao ?: throw ApiContractException("GET /api/v1/comodos/$id não retornou avaliacao.")
    if (assessment.status !in listOf("seguro", "critico", "atencao", "nao_avaliado"))
        throw ApiContractException("GET /api/v1/comodos/$id retornou avaliacao.status desconhecido: ${assessment.status}.")
    return QuimiaRoom(id.toString(), nome, icon, produtos.map { it.id.toString() }, apiRoomSafety(assessment.status),
        apiStatus = assessment.status, apiDescription = assessment.incompatibilidades.mapNotNull { it.descricaoRisco }.distinct().joinToString("\n"))
}

fun ApiSubstance.toSubstance() = ShelfSubstance(id.toString(), nome,
    listOfNotNull(descricao?.takeIf(String::isNotBlank), casNumero?.let { "CAS: $it" }, classes.takeIf { it.isNotEmpty() }?.joinToString()).joinToString("\n"),
    productIds = produtos.map { it.id.toString() })

suspend fun <T> readApiPages(fetch: suspend (Int) -> ApiPage<T>): List<T> {
    val values = mutableListOf<T>()
    var page = 0
    var expectedTotal: Long? = null
    var expectedPages: Int? = null
    do {
        val result = fetch(page)
        if (result.pagina != page || result.tamanho <= 0 || result.totalItens < 0 || result.totalPaginas < 0 ||
            (page + 1 < result.totalPaginas && result.itens.isEmpty()))
            throw ApiContractException("Resposta paginada inconsistente: confira itens, pagina, tamanho, totalItens e totalPaginas.")
        if ((expectedTotal != null && expectedTotal != result.totalItens) || (expectedPages != null && expectedPages != result.totalPaginas))
            throw ApiContractException("A paginação mudou durante a consulta. Recarregue para não exibir uma lista incompleta.")
        expectedTotal = result.totalItens
        expectedPages = result.totalPaginas
        values.addAll(result.itens)
        page++
    } while (page < result.totalPaginas)
    if (values.size.toLong() != expectedTotal)
        throw ApiContractException("A API informou $expectedTotal itens, mas retornou ${values.size} ao concluir a paginação.")
    return values
}

fun ApiPoint.fullAddress() = listOfNotNull(rua, numero?.toString(), bairro, estado, cep, complemento)
    .filter(String::isNotBlank).joinToString(", ")

fun ApiAddress.fullAddress() = listOfNotNull(rua, numero?.toString(), bairro, estado, cep, complemento)
    .filter(String::isNotBlank).joinToString(", ")

fun ApiSavedProducts.requireSaved(requested: List<String>) {
    val expected = requested.map { it.toInt() }.toSet()
    if (itens.map { it.produtoId }.toSet() != expected || itens.size != expected.size)
        throw ApiContractException("POST /api/v1/estante/produtos não retornou um resultado para cada produto solicitado.")
    val unknown = itens.filter { it.status !in listOf("adicionado", "ja_na_estante", "nao_encontrado") }
    if (unknown.isNotEmpty()) throw ApiContractException("A API retornou status de armazenamento desconhecido: ${unknown.joinToString { it.status }}.")
    val missing = itens.filter { it.status == "nao_encontrado" }
    if (missing.isNotEmpty()) throw ApiContractException("A API não encontrou os produtos ${missing.joinToString { it.produtoId.toString() }}. O retorno foi parcial; confira sua estante.")
}
