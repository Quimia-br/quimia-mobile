package com.quimia.android.data.project

import com.quimia.android.presentation.components.BirthDate
import com.quimia.android.presentation.screens.errors.GenericErrorKind
import com.quimia.android.presentation.screens.kemi.KemiReply
import com.quimia.android.presentation.screens.profile.ProfileDetails
import com.quimia.android.presentation.screens.proximity.NearbyAddress
import kotlinx.serialization.json.*
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale

internal fun birthDateToApi(value: String): String {
    val date = BirthDate.parse(value) ?: throw ApiContractException("Data de nascimento inválida. Use DD/MM/AAAA.")
    if (date >= BirthDate.today()) throw ApiContractException("A data de nascimento deve ser anterior a hoje.")
    return String.format(Locale.ROOT, "%04d-%02d-%02d", date.year, date.month, date.day)
}

internal fun birthDateFromApi(value: String?): String {
    if (value == null) return ""
    if (!value.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) throw ApiContractException("dataNasc deve vir da API no formato AAAA-MM-DD.")
    val display = "${value.takeLast(2)}/${value.substring(5, 7)}/${value.take(4)}"
    if (BirthDate.parse(display) == null) throw ApiContractException("A API retornou uma data de nascimento impossível.")
    return display
}

internal fun profileUpdateBody(details: ProfileDetails, current: ApiUser): JsonObject {
    if (details.email != current.email) throw ApiContractException(ApiContractGaps.ProfileEmail)
    if (details.birthDate.isBlank() && current.dataNasc != null) throw ApiContractException(ApiContractGaps.BirthDateClear)
    return buildJsonObject {
        put("nome", details.name)
        if (details.birthDate.isNotBlank()) put("dataNasc", birthDateToApi(details.birthDate))
    }
}

internal fun NearbyAddress.toApiAddress(): ApiAddress {
    validationError()?.let { throw ApiContractException(it) }
    return ApiAddress(postalCode, state, district, street, number.toInt(), complement)
}

internal fun ApiAddress.toNearbyFields(): List<String> =
    listOf(cep.orEmpty(), estado.orEmpty(), "", bairro.orEmpty(), rua.orEmpty(), numero?.toString().orEmpty(), complemento.orEmpty())

internal fun apiErrorKind(error: Throwable): GenericErrorKind = when (error) {
    is SocketTimeoutException -> GenericErrorKind.Timeout
    is ApiContractException, is kotlinx.serialization.SerializationException -> GenericErrorKind.ContentUnavailable
    is HttpException -> when (error.code()) {
        401 -> GenericErrorKind.SessionExpired
        403 -> GenericErrorKind.AccessDenied
        404 -> GenericErrorKind.ContentUnavailable
        408, 504 -> GenericErrorKind.Timeout
        429, in 500..599 -> GenericErrorKind.ServiceUnavailable
        else -> GenericErrorKind.Unknown
    }
    is IOException -> GenericErrorKind.Offline
    else -> GenericErrorKind.Unknown
}

internal fun apiResponseTime(value: String): Long {
    val match = Regex("(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})(?:\\.(\\d{1,9}))?(Z|[+-]\\d{2}:\\d{2})").matchEntire(value)
        ?: throw ApiContractException("respondidoEm deve ser uma data ISO-8601 com fuso horário.")
    val normalized = match.groupValues[1] + "." + match.groupValues[2].padEnd(3, '0').take(3) + match.groupValues[3]
    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ROOT).apply { isLenient = false }
    val position = ParsePosition(0)
    val date = parser.parse(normalized, position)
    if (date == null || position.index != normalized.length) throw ApiContractException("A API retornou respondidoEm inválido.")
    return date.time
}

internal fun ApiChatReply.toKemiReply(): KemiReply.Answer {
    if (texto.isBlank() || !sessaoId.matches(Regex("[A-Za-z0-9_-]{1,64}")))
        throw ApiContractException("A API retornou texto vazio ou sessaoId inválido para a Kemi.")
    return KemiReply.Answer(texto, sessaoId, apiResponseTime(respondidoEm))
}
