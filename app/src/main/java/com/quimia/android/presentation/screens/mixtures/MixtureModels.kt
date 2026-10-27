package com.quimia.android.presentation.screens.mixtures

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import com.quimia.android.presentation.screens.errors.GenericErrorKind
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

@Serializable
enum class MixtureIngredientKind { Product, Substance }

@Serializable
data class MixtureIngredient(val id: String, val name: String, val kind: MixtureIngredientKind) {
    val key: String get() = "${kind.name}:$id"
}

@Serializable
enum class MixtureRisk(val title: String, val description: String) {
    Safe("Seguro", "A mistura entre as substâncias é segura."),
    Caution("Cuidado", "A mistura entre as substâncias apresentam riscos médios a saúde"),
    Dangerous("Perigoso", "A mistura entre as substâncias apresentam riscos altos a saúde"),
    Extreme("Perigo extremo", "A mistura entre as substâncias apresentam riscos críticos a saúde. Não os misture sob nenhuma hipótese."),
}

@Serializable
data class MixtureResult(
    val id: String,
    val ingredients: List<MixtureIngredient>,
    val risk: MixtureRisk,
    val createdAt: Long,
    val quantities: List<String?> = emptyList(),
    val demonstration: Boolean = false,
)

@Serializable
data class MixtureState(
    val slots: List<MixtureIngredient?> = listOf(null, null),
    val pendingId: String? = null,
    val result: MixtureResult? = null,
    val history: List<MixtureResult> = emptyList(),
) {
    val canAnalyze: Boolean get() = pendingId == null && slots.size == 2 && slots.all { it != null } &&
        slots.filterNotNull().distinctBy { it.key }.size == 2
}

data class MixtureRequest(val id: String, val ingredients: List<MixtureIngredient>)
sealed interface MixtureReply {
    data class Evaluated(val result: MixtureResult) : MixtureReply
    data class Failed(val kind: GenericErrorKind) : MixtureReply
}
typealias MixtureAnalyzer = suspend (MixtureRequest) -> MixtureReply

@Stable
class MixtureController(initialState: MixtureState = MixtureState()) {
    var state by mutableStateOf(initialState)
        private set

    fun startWith(ingredient: MixtureIngredient) {
        state = state.copy(slots = listOf(ingredient, null), pendingId = null, result = null)
    }

    fun select(slot: Int, ingredient: MixtureIngredient): Boolean {
        if (slot !in 0..1 || state.pendingId != null || state.slots.filterIndexed { index, _ -> index != slot }.any { it?.key == ingredient.key }) return false
        state = state.copy(slots = state.slots.mapIndexed { index, current -> if (index == slot) ingredient else current }, result = null)
        return true
    }

    fun remove(slot: Int) {
        if (slot !in 0..1 || state.pendingId != null) return
        state = state.copy(slots = state.slots.mapIndexed { index, current -> if (index == slot) null else current }, result = null)
    }

    fun begin(): MixtureRequest? {
        if (!state.canAnalyze) return null
        val request = MixtureRequest(UUID.randomUUID().toString(), state.slots.filterNotNull())
        state = state.copy(pendingId = request.id, result = null)
        return request
    }

    fun complete(request: MixtureRequest, reply: MixtureReply): Boolean {
        if (state.pendingId != request.id || state.slots.filterNotNull() != request.ingredients) return false
        val evaluated = (reply as? MixtureReply.Evaluated)?.result?.takeIf {
            it.id.isNotBlank() && it.ingredients == request.ingredients && it.ingredients.size == 2
        }
        state = state.copy(pendingId = null, result = evaluated)
        return evaluated != null
    }

    fun cancel() { state = state.copy(pendingId = null) }
    fun closeResult() { state = state.copy(result = null) }
    fun saveResult(): Boolean {
        val result = state.result ?: return false
        state = state.copy(history = (state.history.filterNot { it.id == result.id } + result).sortedByDescending { it.createdAt }, result = null)
        return true
    }
    fun openResult(id: String) { state.history.firstOrNull { it.id == id }?.let { state = state.copy(result = it) } }
}

internal suspend fun analyzeMixture(analyzer: MixtureAnalyzer?, request: MixtureRequest): MixtureReply = try {
    analyzer?.invoke(request) ?: MixtureReply.Failed(GenericErrorKind.ServiceUnavailable)
} catch (cancelled: CancellationException) { throw cancelled
} catch (_: SocketTimeoutException) { MixtureReply.Failed(GenericErrorKind.Timeout)
} catch (_: Exception) { MixtureReply.Failed(GenericErrorKind.Unknown) }

internal fun restoreMixtureState(encoded: String): MixtureState = runCatching {
    Json.decodeFromString<MixtureState>(encoded).let {
        require(it.slots.size == 2)
        require(it.slots.filterNotNull().distinctBy { ingredient -> ingredient.key }.size == it.slots.filterNotNull().size)
        it.copy(pendingId = null, history = it.history.distinctBy { result -> result.id }.sortedByDescending { result -> result.createdAt })
    }
}.getOrDefault(MixtureState())

val MixtureControllerSaver = Saver<MixtureController, String>(
    save = { Json.encodeToString(it.state) }, restore = { MixtureController(restoreMixtureState(it)) },
)

internal fun mixtureDateLabel(timestamp: Long, now: Long = System.currentTimeMillis(), zone: TimeZone = TimeZone.getDefault()): String {
    val date = Calendar.getInstance(zone).apply { timeInMillis = timestamp }
    val today = Calendar.getInstance(zone).apply { timeInMillis = now }
    fun sameDay() = date.get(Calendar.ERA) == today.get(Calendar.ERA) && date.get(Calendar.YEAR) == today.get(Calendar.YEAR) && date.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    if (sameDay()) return "Hoje"
    today.add(Calendar.DAY_OF_YEAR, -1)
    if (sameDay()) return "Ontem"
    return SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR")).apply { timeZone = zone }.format(Date(timestamp))
}
