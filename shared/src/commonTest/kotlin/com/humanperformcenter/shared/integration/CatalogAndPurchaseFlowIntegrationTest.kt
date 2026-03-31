package integration

import com.humanperformcenter.shared.data.persistence.ServiceProductRepositoryImpl
import com.humanperformcenter.shared.data.remote.implementation.ServiceProductRemoteDataSourceImpl
import com.humanperformcenter.shared.domain.usecase.ServiceProductUseCase
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CatalogAndPurchaseFlowIntegrationTest {

    @Test
    fun services_products_assign_and_unassign_runs_end_to_end() = runTest {
        val apiEngine = MockEngine { request ->
            when {
                request.method == HttpMethod.Get && request.url.encodedPath == "/mobile/services" -> respond(
                    """[{"id":1,"name":"Training"}]""",
                    HttpStatusCode.OK,
                    headersOf("Content-Type", ContentType.Application.Json.toString()),
                )

                request.method == HttpMethod.Get && request.url.encodedPath == "/mobile/service-products" -> respond(
                    """[{"id":31,"name":"Pack 8","service_ids":[1],"isAvailable":true}]""",
                    HttpStatusCode.OK,
                    headersOf("Content-Type", ContentType.Application.Json.toString()),
                )

                request.method == HttpMethod.Post && request.url.encodedPath == "/mobile/users/22/products" -> respond(
                    """{"assignedId":9001}""",
                    HttpStatusCode.OK,
                    headersOf("Content-Type", ContentType.Application.Json.toString()),
                )

                request.method == HttpMethod.Delete && request.url.encodedPath == "/mobile/users/22/products/31" -> respond(
                    "",
                    HttpStatusCode.NoContent,
                )

                else -> error("Unhandled api endpoint: ${request.method} ${request.url}")
            }
        }

        val useCase = ServiceProductUseCase(
            ServiceProductRepositoryImpl(
                ServiceProductRemoteDataSourceImpl(integrationProvider(apiEngine = apiEngine)),
            ),
        )

        val services = useCase.getAllServices()
        val products = useCase.getServiceProducts(serviceId = 1, userId = 22)
        val assign = useCase.assignProductToUser(userId = 22, productId = 31, paymentMethod = "card")
        val unassign = useCase.unassignProductFromUser(userId = 22, productId = 31)

        assertTrue(services.isSuccess)
        assertEquals(1, services.getOrThrow().size)
        assertTrue(products.isSuccess)
        assertEquals(31, products.getOrThrow().first().id)
        assertTrue(assign.isSuccess)
        assertEquals(9001, assign.getOrThrow())
        assertTrue(unassign.isSuccess)
    }
}
