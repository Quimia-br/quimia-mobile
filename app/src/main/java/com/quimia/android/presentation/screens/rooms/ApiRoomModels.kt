package com.quimia.android.presentation.screens.rooms

import com.quimia.android.data.rooms.*

internal val RoomIcon.apiCode: String get() = when (this) {
    RoomIcon.Mirror -> "mirror"; RoomIcon.Refrigerator -> "refrigerator"; RoomIcon.Fence -> "fence"
    RoomIcon.Laundry -> "laundry"; RoomIcon.DoubleBed -> "double_bed"; RoomIcon.SingleBed -> "single_bed"
    RoomIcon.Toilet -> "toilet"; RoomIcon.Sofa -> "sofa"; RoomIcon.Pool -> "pool"; RoomIcon.Shelving -> "shelving"
}
internal fun apiRoomIcon(code: String): RoomIcon? = RoomIcon.entries.find { it.apiCode == code }
internal fun apiRoomSafety(status: String) = when (status) {
    "seguro" -> RoomSafety.Safe; "critico" -> RoomSafety.Critical; else -> RoomSafety.NotEvaluated
}
internal fun ApiRoomSummary.uiRoom() = QuimiaRoom(id.toString(), nome, apiRoomIcon(icone) ?: RoomIcon.Mirror,
    emptyList(), apiRoomSafety(status), knownIcon = apiRoomIcon(icone) != null)
internal fun ApiRoom.uiRoom() = QuimiaRoom(id.toString(), nome, apiRoomIcon(icone) ?: RoomIcon.Mirror,
    produtos.map { it.id.toString() }, apiRoomSafety(avaliacao.status), knownIcon = apiRoomIcon(icone) != null)
internal fun apiRoomValidation(name: String, icon: RoomIcon?, ids: List<Int>, available: List<Int>): String? = when {
    normalizeRoomName(name).isBlank() -> "Informe o nome do cômodo."
    normalizeRoomName(name).length > 100 -> "Use até 100 caracteres no nome."
    icon == null -> "Escolha um ícone para o cômodo."
    ids.distinct().size < 2 -> "Selecione pelo menos 2 produtos da sua estante."
    ids.distinct().size > 20 -> "Selecione no máximo 20 produtos."
    ids.any { it <= 0 || it !in available } -> "A lista de produtos mudou. Atualize e confira sua seleção."
    else -> null
}
