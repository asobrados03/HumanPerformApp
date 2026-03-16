package com.humanperformcenter.shared.data.persistence

import com.diamondedge.logging.logging
import com.humanperformcenter.shared.data.model.ErrorResponse
import com.humanperformcenter.shared.data.model.payment.AssignProductResponse
import com.humanperformcenter.shared.data.model.product_service.AssignProductRequest
import com.humanperformcenter.shared.data.model.product_service.ProductDetailResponse
import com.humanperformcenter.shared.data.model.product_service.ServiceAvailable
import com.humanperformcenter.shared.data.model.product_service.Product
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.repository.ServiceProductRepository
import com.humanperformcenter.shared.presentation.ui.SimpleResponse
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.accept
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

object ServiceProductRepositoryImpl: ServiceProductRepository {
    private val log = logging()

    override suspend fun getAllServices()
    : Result<List<ServiceAvailable>> = withContext(Dispatchers.IO) {
        return@withContext runCatching {
            val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/services")
            val text = response.bodyAsText()

            // Check if the response is successful (HTTP 200-299)
            if (!response.status.isSuccess()) {
                throw Exception("Backend error: ${response.status.value} - $text")
            }

            // Logic check: if your API returns "error" inside a 200 OK
            if (text.contains("error", ignoreCase = true)) {
                throw Exception("API returned logic error: $text")
            }

            Json.decodeFromString<List<ServiceAvailable>>(text)
        }
    }

    override suspend fun getServiceProducts(serviceId: Int)
    : Result<List<Product>> = withContext(Dispatchers.IO) {
        return@withContext runCatching {
            // 1. Le pedimos a Ktor que deserialice la LISTA de productos
            val response = ApiClient.apiClient.get(
                "${ApiClient.baseUrl}/mobile/service-products"
            ) {
                parameter("primary_service_id", serviceId)
            }

            // 2. Extraemos el cuerpo como la lista que realmente es
            val products: List<Product> = response.body()
            products
        }
    }

    override suspend fun getUserProducts(customerId: Int)
    : Result<List<Product>> = withContext(Dispatchers.IO) {
        return@withContext runCatching {
            // 1. Hacemos la petición
            val primaryResponse: HttpResponse = ApiClient.apiClient.get(
                "${ApiClient.baseUrl}/mobile/users/$customerId/products"
            ) {
                accept(ContentType.Application.Json)
            }

            if (primaryResponse.status.isSuccess()) {
                val products: List<Product> = primaryResponse.body()
                products
            } else if (primaryResponse.status == HttpStatusCode.NotFound) {
                val legacyResponse: HttpResponse = ApiClient.apiClient.get(
                    "${ApiClient.baseUrl}/mobile/user-products"
                ) {
                    parameter("user_id", customerId)
                    accept(ContentType.Application.Json)
                }

                if (legacyResponse.status.isSuccess()) {
                    val products: List<Product> = legacyResponse.body()
                    products
                } else {
                    val errorBody = legacyResponse.bodyAsText()
                    log.error { "🔴 Error API legacy ${legacyResponse.status}: $errorBody" }
                    throw Exception("Error del servidor: ${legacyResponse.status.value}")
                }
            } else {
                val errorBody = primaryResponse.bodyAsText()
                log.error { "🔴 Error API ${primaryResponse.status}: $errorBody" }
                throw Exception("Error del servidor: ${primaryResponse.status.value}")
            }
        }
    }

    override suspend fun assignProductToUser(
        userId: Int,
        productId: Int,
        paymentMethod: String,
        couponCode: String?
    ): Result<Int> = withContext(Dispatchers.IO) {
        val requestBody = AssignProductRequest(productId, paymentMethod, couponCode)

        return@withContext try {
            val response =
                ApiClient.apiClient.post("${ApiClient.baseUrl}/mobile/users/$userId/products") {
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }

            if (response.status.isSuccess()) {
                val body = response.body<AssignProductResponse>()
                Result.success(body.assignedId ?: 0)
            } else {
                // Aquí capturamos los errores 402 (Saldo) y 409 (Ya existe) del Backend
                val errorBody = response.body<AssignProductResponse>()
                val friendlyMessage = when (response.status.value) {
                    402 -> "Saldo insuficiente en tu monedero."
                    409 -> "Ya tienes este producto activo."
                    404 -> "El producto ya no está disponible."
                    else -> errorBody.error ?: "Error al procesar la solicitud."
                }
                Result.failure(Exception(friendlyMessage))
            }
        } catch (e: Exception) {
            Result.failure(e) // Errores de red
        }
    }

    override suspend fun unassignProductFromUser(userId: Int, productId: Int)
    : Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = ApiClient.apiClient.delete(
                "${ApiClient.baseUrl}/mobile/users/$userId/products/$productId"
            )

            if (response.status.isSuccess()) {
                // Si el backend devuelve 200-299, es un éxito rotundo
                Result.success(Unit)
            } else {
                // Intentamos obtener el mensaje de error del body si existe
                val errorBody = try {
                    response.body<SimpleResponse>()
                } catch (_: Exception) {
                    null
                }
                val message = errorBody?.error ?: "Error del servidor (${response.status.value})"

                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            // Errores de conexión, timeout, serialización, etc.
            log.error { "❌ Excepción en unassignProduct: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getActiveProductDetail(userId: Int, productId: Int)
    : Result<ProductDetailResponse> = withContext(Dispatchers.IO) {
        return@withContext try {
            val resp: HttpResponse = ApiClient.apiClient.get(
                "${ApiClient.baseUrl}/mobile/active-product-detail"
            ) {
                parameter("user_id", userId)
                parameter("product_id", productId)
                expectSuccess = false
            }

            when (resp.status) {
                HttpStatusCode.OK -> {
                    val data: ProductDetailResponse = resp.body()
                    log.info { "📦 Producto obtenido: ${data.name}" }
                    Result.success(data)
                }
                HttpStatusCode.NotFound -> {
                    Result.failure(Exception("Producto no encontrado"))
                }
                HttpStatusCode.BadRequest, HttpStatusCode.Unauthorized -> {
                    val err: ErrorResponse = resp.body() ?: ErrorResponse("Error al obtener detalles")
                    Result.failure(Exception(err.error))
                }
                else -> {
                    Result.failure(Exception("Error al cargar el producto: ${resp.status}"))
                }
            }
        } catch (err: Throwable) {
            log.error { "❌ Excepción en getProductDetails: ${err.message}" }
            Result.failure(err)
        }
    }

    override suspend fun getProductDetailHireProduct(productId: Int)
    : Result<Product> = withContext(Dispatchers.IO) {
        return@withContext runCatching {
            val response: HttpResponse =
                ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/products/$productId") {
                    accept(ContentType.Application.Json)
                }

            if (response.status.isSuccess()) {
                response.body<Product>()
            } else {
                val errorBody = response.bodyAsText()
                log.error { "🔴 Error API ${response.status}: $errorBody" }
                throw Exception("Error del servidor: ${response.status.value}")
            }
        }
    }
}
