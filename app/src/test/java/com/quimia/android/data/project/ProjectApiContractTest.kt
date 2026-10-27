package com.quimia.android.data.project

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

class ProjectApiContractTest {
    private class TestSessions(initial: ApiSession? = null) : SessionStorage {
        override val session = MutableStateFlow(initial)
        override fun save(value: ApiSession) { session.value = value }
        override fun clear() { session.value = null }
    }
    private val user = ApiUser("11111111-1111-4111-8111-111111111111", "Pessoa de teste", "teste@example.invalid")
    private val original = ApiSession("access-old", "Bearer", 900, "refresh-old", user)
    private val renewed = original.copy(accessToken = "access-new", refreshToken = "refresh-new")
    private lateinit var server: MockWebServer
    private lateinit var sessions: TestSessions
    private lateinit var client: ProjectClient
    private val product = ApiProduct(12, "Produto retornado pelo servidor", "multiuso", "Descrição da API", "7891234567890",
        "https://example.invalid/produto.png", ApiBrand(1, "Marca da API", "https://example.invalid/logo.png"))

    @Before fun setup() {
        server = MockWebServer().apply { start() }
        sessions = TestSessions(original)
        client = ProjectClient(sessions, server.url("/").toString())
    }
    @After fun cleanup() { server.shutdown() }
    private fun json(value: String, status: Int = 200) = MockResponse().setResponseCode(status).setHeader("Content-Type", "application/json").setBody(value)
    private fun request(): RecordedRequest = requireNotNull(server.takeRequest(5, TimeUnit.SECONDS))
    private fun page() = Json.encodeToString(ApiPage(listOf(product), 0, 50, 1, 1))

    @Test fun loginUsesProjectRouteAndNeverSendsExistingBearer() = runBlocking {
        server.enqueue(json(Json.encodeToString(original)))
        val response = client.api.login(apiBody("email" to user.email, "senha" to "senha-de-teste"))
        val sent = request()
        assertEquals("POST", sent.method)
        assertEquals("/api/v1/auth/mobile/login", sent.path)
        assertNull(sent.getHeader("Authorization"))
        val body = Json.parseToJsonElement(sent.body.readUtf8()).jsonObject
        assertEquals(user.email, body.getValue("email").jsonPrimitive.content)
        assertEquals("senha-de-teste", body.getValue("senha").jsonPrimitive.content)
        assertEquals(original, response)
    }

    @Test fun catalogUsesServerPaginationImagesAndIdentifiers() = runBlocking {
        server.enqueue(json(page()))
        val response = client.api.products(0)
        val sent = request()
        assertEquals("/api/v1/produtos?page=0&size=50", sent.path)
        assertEquals("Bearer access-old", sent.getHeader("Authorization"))
        val mapped = response.itens.single().toProduct()
        assertEquals("12", mapped.id)
        assertEquals(product.nome, mapped.name)
        assertEquals(product.fotoUrl, mapped.imageUrl)
        assertEquals(product.marca?.logoUrl, mapped.logoUrl)
        assertEquals(0, mapped.imageRes)
    }

    @Test fun missingPageMetadataIsAContractErrorNotAnEmptyCatalog() = runBlocking {
        server.enqueue(json("""{"itens":[]}"""))
        try { client.api.products(0); fail("Metadados obrigatórios ausentes devem falhar") }
        catch (_: SerializationException) { }
    }

    @Test fun unauthorizedRequestRefreshesOnceAndRetriesWithNewToken() = runBlocking {
        server.enqueue(json("""{"detail":"nao_autenticado"}""", 401))
        server.enqueue(json(Json.encodeToString(renewed)))
        server.enqueue(json(page()))
        assertEquals(1, client.api.products(0).itens.size)
        val first = request(); val refresh = request(); val retry = request()
        assertEquals("Bearer access-old", first.getHeader("Authorization"))
        assertEquals("/api/v1/auth/mobile/refresh", refresh.path)
        assertNull(refresh.getHeader("Authorization"))
        assertEquals("refresh-old", Json.parseToJsonElement(refresh.body.readUtf8()).jsonObject.getValue("refreshToken").jsonPrimitive.content)
        assertEquals("Bearer access-new", retry.getHeader("Authorization"))
        assertEquals(renewed, sessions.session.value)
        assertEquals(3, server.requestCount)
    }

    @Test fun invalidRefreshClearsSessionWithoutAnInfiniteLoop() = runBlocking {
        server.enqueue(json("{}", 401)); server.enqueue(json("{}", 401))
        try { client.api.products(0); fail("A requisição deve continuar recusada") } catch (_: HttpException) { }
        assertNull(sessions.session.value)
        assertEquals(2, server.requestCount)
    }

    @Test fun refreshCannotRestoreAnAccountAfterLogout() = runBlocking {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = if (request.path == "/api/v1/auth/mobile/refresh") {
                sessions.clear()
                json(Json.encodeToString(renewed))
            } else json("{}", 401)
        }
        try { client.api.products(0); fail("Não deve repetir uma operação após sair") } catch (_: HttpException) { }
        assertNull(sessions.session.value)
        assertEquals(2, server.requestCount)
    }

    @Test fun shelfSaveUsesNumericIdsAndDeletionAcceptsNoContent() = runBlocking {
        server.enqueue(json("""{"itens":[{"produtoId":12,"nome":"Produto 12","status":"adicionado"},{"produtoId":13,"nome":"Produto 13","status":"ja_na_estante"}]}"""))
        client.api.saveProducts(apiIds(listOf("12", "13", "12")))
        val save = request()
        assertEquals("/api/v1/estante/produtos", save.path)
        assertEquals(listOf(12, 13), Json.parseToJsonElement(save.body.readUtf8()).jsonObject.getValue("produtoIds").jsonArray.map { it.jsonPrimitive.int })
        server.enqueue(MockResponse().setResponseCode(204))
        client.api.removeProduct(12)
        val remove = request()
        assertEquals("DELETE", remove.method)
        assertEquals("/api/v1/estante/produtos/12", remove.path)
    }

    @Test fun barcodeSearchDoesNotCallCosmosDirectly() = runBlocking {
        server.enqueue(json(Json.encodeToString(product)))
        assertEquals(product, client.api.barcode(apiBody("codBarras" to "7891234567890")))
        val sent = request()
        assertEquals("POST", sent.method)
        assertEquals("/api/v1/produtos/busca-por-codigo-barras", sent.path)
        assertEquals("7891234567890", Json.parseToJsonElement(sent.body.readUtf8()).jsonObject.getValue("codBarras").jsonPrimitive.content)
    }

    @Test fun recoveryUsesChallengeThenResetTokenAndHandles204() = runBlocking {
        server.enqueue(json("""{"challengeToken":"challenge","expiresIn":900}"""))
        val challenge = client.api.forgot(apiBody("email" to user.email))
        assertEquals("/api/v1/auth/forgot-password", request().path)
        server.enqueue(json("""{"resetToken":"reset","expiresIn":300}"""))
        val grant = client.api.verifyCode(apiBody("email" to user.email, "code" to "1234", "challengeToken" to challenge.challengeToken))
        val verification = request()
        assertEquals("/api/v1/auth/verify-recovery-code", verification.path)
        assertEquals("challenge", Json.parseToJsonElement(verification.body.readUtf8()).jsonObject.getValue("challengeToken").jsonPrimitive.content)
        server.enqueue(MockResponse().setResponseCode(204))
        client.api.reset(apiBody("resetToken" to grant.resetToken, "novaSenha" to "nova-senha-teste"))
        val reset = request()
        assertEquals("/api/v1/auth/reset-password", reset.path)
        assertEquals("reset", Json.parseToJsonElement(reset.body.readUtf8()).jsonObject.getValue("resetToken").jsonPrimitive.content)
    }

    @Test fun maintenanceHtmlIsReportedInsteadOfBecomingFakeData() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(404).setHeader("Content-Type", "text/html").setBody("<html>Site em manutenção</html>"))
        try { client.api.products(0); fail("HTTP 404 não pode gerar produto") }
        catch (error: HttpException) { assertTrue(apiErrorMessage(error).contains("HTML")) }
    }

    @Test fun failedServerRequestNeverBecomesFallbackProduct() = runBlocking {
        server.enqueue(json("{}", 503))
        try { client.api.barcode(apiBody("codBarras" to "123")); fail("503 não pode virar sucesso") }
        catch (error: HttpException) { assertEquals(503, error.code()) }
    }

    @Test fun allPagesAreReadWithoutAssumingOnlyFirstPageExists() = runBlocking {
        val visited = mutableListOf<Int>()
        val values = readApiPages { page -> visited += page; ApiPage(listOf(page), page, 1, 3, 3) }
        assertEquals(listOf(0, 1, 2), visited)
        assertEquals(listOf(0, 1, 2), values)
    }

    @Test fun inconsistentPaginationFailsExplicitly() = runBlocking {
        try { readApiPages<Int> { ApiPage(emptyList(), 0, 50, 10, 2) }; fail("Não pode esconder páginas ausentes") }
        catch (_: ApiContractException) { }
    }

    @Test fun unknownRoomIconDoesNotBecomeAnotherRoomType() {
        try { ApiRoom(1, "Meu cômodo", "codigo-desconhecido", ApiRoomAssessment("seguro", 0, 0, 0, incompatibilidades = emptyList()), emptyList()).toRoom(); fail("Não deve inventar um ícone") }
        catch (error: ApiContractException) { assertTrue(error.message.orEmpty().contains("icone")) }
    }

    @Test fun absentAssessmentDoesNotBecomeSafe() {
        try { ApiRoom(1, "Meu cômodo", "toilet", null, emptyList()).toRoom(); fail("Avaliação ausente deve ser reportada") }
        catch (error: ApiContractException) { assertTrue(error.message.orEmpty().contains("avaliacao")) }
    }

    @Test fun serverAttentionIsNotSilentlyUpgradedToCritical() {
        val room = ApiRoom(1, "Meu cômodo", "toilet", ApiRoomAssessment("atencao", 0, 0, 0, incompatibilidades = emptyList()), emptyList()).toRoom()
        assertEquals("atencao", room.apiStatus)
        assertNotEquals(com.quimia.android.presentation.screens.rooms.RoomSafety.Critical, room.safety)
    }

    @Test fun pointsWithoutCoordinatesAreNotGeocodedOrAssignedRandomPositions() {
        val point = ApiPoint(1, "Local da API", "descarte", rua = "Rua da API", numero = 10)
        assertEquals("Rua da API, 10", point.fullAddress())
        assertFalse(Json.encodeToString(point).contains("latitude"))
    }

    @Test fun httpSuccessWithPartiallyMissingProductsIsNotReportedAsFullSuccess() {
        val response = ApiSavedProducts(listOf(ApiSavedProduct(12, "Produto", "adicionado"), ApiSavedProduct(13, null, "nao_encontrado")))
        try { response.requireSaved(listOf("12", "13")); fail("Sucesso HTTP pode conter produto não encontrado") }
        catch (error: ApiContractException) { assertTrue(error.message.orEmpty().contains("13")) }
    }

    @Test fun missingPerProductResultIsAContractError() {
        try { ApiSavedProducts(emptyList()).requireSaved(listOf("12")); fail("Resultado por produto ausente") }
        catch (_: ApiContractException) { }
    }

    @Test fun chatUsesOnlyTheSessionReturnedByTheApi() = runBlocking {
        server.enqueue(json("""{"texto":"Resposta do servidor","sessaoId":"api-session-1","respondidoEm":"2026-10-25T12:00:00Z"}"""))
        val reply = client.api.chat(apiBody("texto" to "Minha pergunta"))
        val first = request()
        assertEquals("/api/v1/chat/mensagens", first.path)
        assertFalse(Json.parseToJsonElement(first.body.readUtf8()).jsonObject.containsKey("sessaoId"))
        server.enqueue(json(Json.encodeToString(reply)))
        client.api.chat(apiBody("texto" to "Outra pergunta", "sessaoId" to reply.sessaoId))
        assertEquals("api-session-1", Json.parseToJsonElement(request().body.readUtf8()).jsonObject.getValue("sessaoId").jsonPrimitive.content)
    }

    @Test fun htmlWithHttp200IsStillAContractFailureInsteadOfAChatReply() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setHeader("Content-Type", "text/html").setBody("<!doctype html><title>Erro Interno</title>"))
        try { client.api.chat(apiBody("texto" to "Olá")); fail("HTTP 200 com HTML não é resposta da Kemi") }
        catch (error: ApiContractException) { assertTrue(error.message.orEmpty().contains("HTML")) }
        assertEquals(1, server.requestCount)
    }

    @Test fun pointsAlwaysIncludeTheMandatoryTypeFilter() = runBlocking {
        server.enqueue(json("""{"itens":[],"pagina":0,"tamanho":50,"totalItens":0,"totalPaginas":0}"""))
        client.api.points(0, type = "descarte")
        assertEquals("descarte", request().requestUrl?.queryParameter("tipo"))
    }

    @Test fun substanceKeepsTheProductIdsActuallyReturnedByTheApi() = runBlocking {
        server.enqueue(json("""{"itens":[{"id":3,"nome":"Substância da API","classes":["Classe"],"produtos":[{"id":12,"nome":"Produto 12"},{"id":19,"nome":"Produto 19"}]}]}"""))
        val result = client.api.substances().itens.single().toSubstance()
        assertEquals(listOf("12", "19"), result.productIds)
    }

    @Test fun missingAssessmentCountsAndFirstAidFlagsAreNotInvented() {
        try { Json.decodeFromString<ApiRoomAssessment>("""{"status":"seguro"}"""); fail("Contadores ausentes") }
        catch (error: SerializationException) { assertTrue(apiErrorMessage(error).contains("totalProdutos")) }
        try { Json.decodeFromString<ApiFirstAid>("""{"produtoId":12}"""); fail("Disponibilidade de FDS ausente") }
        catch (_: SerializationException) { }
    }

    @Test fun validationProblemsAreVisibleInsteadOfSuccess() = runBlocking {
        server.enqueue(json("""{"detail":"validation","errors":["nome: tamanho inválido"]}""", 400))
        try { client.api.updateProfile(apiBody("nome" to "X")); fail("Validação deve falhar") }
        catch (error: HttpException) { assertTrue(apiErrorMessage(error).contains("nome: tamanho inválido")) }
    }

    @Test fun reportedTotalCannotHideMissingItemsInTheLastPage() = runBlocking {
        try { readApiPages<Int> { ApiPage(listOf(12), 0, 50, 2, 1) }; fail("Falta um item informado pelo servidor") }
        catch (error: ApiContractException) { assertTrue(error.message.orEmpty().contains("2 itens")) }
    }

    @Test fun refreshCannotSwitchTheOwnerOfAnAuthenticatedRequest() = runBlocking {
        server.enqueue(json("{}", 401))
        server.enqueue(json(Json.encodeToString(renewed.copy(user = user.copy(id = "22222222-2222-4222-8222-222222222222")))))
        try { client.api.products(0); fail("Não deve repetir sob outro usuário") }
        catch (_: HttpException) { }
        assertEquals(original, sessions.session.value)
        assertEquals(2, server.requestCount)
    }

    @Test fun absentAddressIsDistinguishedFromAServiceReturningMaintenanceHtml() = runBlocking {
        server.enqueue(json("""{"detail":"not_found"}""", 404))
        try { client.api.address(); fail("Endereço inexistente") }
        catch (error: HttpException) { assertTrue(isApiProblem(error, 404, "not_found")) }
        server.enqueue(MockResponse().setResponseCode(404).setHeader("Content-Type", "text/html").setBody("<html>Site em manutenção</html>"))
        try { client.api.address(); fail("Serviço indisponível") }
        catch (error: HttpException) {
            assertFalse(isApiProblem(error, 404, "not_found"))
            assertTrue(apiErrorMessage(error).contains("HTML"))
        }
    }
}
