package com.humanperformcenter.shared.domain.repository

import com.humanperformcenter.shared.data.model.ProductDetailResponse
import com.humanperformcenter.shared.data.model.ServiceAvailable
import com.humanperformcenter.shared.data.model.ServiceItem

interface ServiceProductRepository {
    suspend fun getAllServices(): List<ServiceAvailable>
    suspend fun getServiceProducts(serviceId: Int): List<ServiceItem>
    suspend fun getUserProducts(customerId: Int): List<ServiceItem>
    suspend fun assignProductToUser(
        userId: Int,
        productId: Int,
        paymentMethod: String,
        couponCode: String? = null,
    ): Pair<Boolean, String?>
    suspend fun unassignProductFromUser(userId: Int, productId: Int): Boolean
    suspend fun getProductDetails(userId: Int, productId: Int): Result<ProductDetailResponse>
    suspend fun applyCoupon(code: String, userId: Int, productId: Int): Boolean
}