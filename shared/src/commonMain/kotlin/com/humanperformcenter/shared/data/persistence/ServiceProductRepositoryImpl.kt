package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.product_service.AssignProductRequest
import com.humanperformcenter.shared.data.model.payment.CouponApplyRequest
import com.humanperformcenter.shared.data.model.ErrorResponse
import com.humanperformcenter.shared.data.model.product_service.ProductDetailResponse
import com.humanperformcenter.shared.data.model.product_service.ServiceAvailable
import com.humanperformcenter.shared.data.model.product_service.ServiceItem
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.repository.ServiceProductRepository
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object ServiceProductRepositoryImpl: ServiceProductRepository {
    override suspend fun getAllServices(): List<ServiceAvailable> {
        return try {
            val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/services")
            val text = response.bodyAsText()

            if (text.contains("error", ignoreCase = true)) {
                println("❌ Error del backend: $text")
                emptyList()
            } else {
                Json.decodeFromString(text)
            }
        } catch (e: Exception) {
            println("❌ Excepción al obtener servicios: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getServiceProducts(serviceId: Int): List<ServiceItem> {
        val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/service-products") {
            parameter("primary_service_id", serviceId)
        }
        val raw = response.bodyAsText()
        println("🔵 JSON response de productos: $raw")
        return response.body()
    }

    override suspend fun getUserProducts(customerId: Int): List<ServiceItem> {
        // 1. Hacemos el GET y pedimos JSON
        val response: HttpResponse = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/user-products") {
            parameter("user_id", customerId)
            accept(ContentType.Application.Json)
        }

        return if (response.status.isSuccess()) {
            // 2xx → parseamos a List<ServiceItem>
            response.body()
        } else {
            // 4xx / 5xx → leemos el cuerpo crudo (HTML o JSON de error)
            val errorBody = response.bodyAsText()
            println("ServiceProductRepo Error ${response.status}: $errorBody")
            emptyList()
        }
    }

    override suspend fun assignProductToUser(
        userId: Int,
        productId: Int,
        paymentMethod: String,
        couponCode: String?
    ): Pair<Boolean, String?> {
        val request = AssignProductRequest(
            user_id = userId,
            product_id = productId,
            payment_method = paymentMethod,
            coupon_code = couponCode
        )

        return try {
            val response = ApiClient.apiClient.post("${ApiClient.baseUrl}/mobile/assign-product") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            val body = response.bodyAsText()
            println("📦 Asignación producto → $body")

            val json = Json.parseToJsonElement(body).jsonObject

            if (json["success"]?.jsonPrimitive?.booleanOrNull == true) {
                true to null
            } else {
                false to (json["error"]?.jsonPrimitive?.content ?: "Error desconocido")
            }
        } catch (e: Exception) {
            false to "Error de conexión: ${e.message}"
        }
    }

    override suspend fun unassignProductFromUser(userId: Int, productId: Int): Boolean {
        return try {
            val response = ApiClient.apiClient.delete("${ApiClient.baseUrl}/mobile/unassign-product") {
                parameter("user_id", userId)
                parameter("product_id", productId)
            }
            println("✅ DELETE status: ${response.status}")
            response.status.value in 200..299
        } catch (e: Exception) {
            println("❌ Error al desasignar producto: ${e.message}")
            false
        }
    }

    override suspend fun getProductDetails(userId: Int, productId: Int): Result<ProductDetailResponse> = try {
        val resp: HttpResponse = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/product-details") {
            parameter("user_id", userId)
            parameter("product_id", productId)
            expectSuccess = false
        }

        when (resp.status) {
            HttpStatusCode.OK -> {
                val data: ProductDetailResponse = resp.body()
                println("📦 Producto obtenido: ${data.name}")
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
        println("❌ Excepción en getProductDetails: ${err.message}")
        Result.failure(err)
    }

    override suspend fun applyCoupon(code: String, userId: Int, productId: Int): Boolean {
        return try {
            val response = ApiClient.apiClient.post("${ApiClient.baseUrl}/mobile/apply-coupon") {
                contentType(ContentType.Application.Json)
                setBody(
                    CouponApplyRequest(
                        couponCode = code,
                        userId = userId,
                        productId = productId
                    )
                )
            }

            val responseText = response.bodyAsText()
            println("🎫 Respuesta apply-coupon: $responseText")

            responseText.contains("success", ignoreCase = true)
        } catch (e: Exception) {
            println("❌ Error al aplicar cupón: ${e.message}")
            false
        }
    }
}