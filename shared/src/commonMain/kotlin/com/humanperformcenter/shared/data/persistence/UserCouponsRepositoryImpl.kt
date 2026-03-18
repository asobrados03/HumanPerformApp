package com.humanperformcenter.shared.data.persistence

import com.humanperformcenter.shared.data.model.ErrorResponse
import com.humanperformcenter.shared.data.model.payment.Coupon
import com.humanperformcenter.shared.data.network.ApiClient
import com.humanperformcenter.shared.domain.repository.UserCouponsRepository
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

object UserCouponsRepositoryImpl : UserCouponsRepository {
    override suspend fun addCouponToUser(userId: Int, couponCode: String): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            val response = ApiClient.apiClient.post("${ApiClient.baseUrl}/mobile/users/$userId/coupons") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("coupon_code" to couponCode))
                expectSuccess = false
            }

            when (response.status) {
                HttpStatusCode.NoContent, HttpStatusCode.OK, HttpStatusCode.Created -> Unit
                else -> {
                    val error = try {
                        response.body<ErrorResponse>()
                    } catch (_: Exception) {
                        null
                    }
                    val message = error?.error ?: "HTTP ${response.status.value}"
                    throw Exception(message)
                }
            }
        }
    }

    override suspend fun getUserCoupons(userId: Int): Result<List<Coupon>> = runCatching {
        withContext(Dispatchers.IO) {
            val response = ApiClient.apiClient.get("${ApiClient.baseUrl}/mobile/users/$userId/coupons") {
                expectSuccess = false
            }

            when (response.status) {
                HttpStatusCode.NoContent -> emptyList()
                HttpStatusCode.OK -> response.body()
                HttpStatusCode.Forbidden -> {
                    val errorResponse = response.body<ErrorResponse>()
                    throw Exception(errorResponse.error)
                }
                else -> {
                    val raw = response.bodyAsText()
                    throw Exception("Error obteniendo cupones: HTTP ${response.status.value} $raw")
                }
            }
        }
    }
}
