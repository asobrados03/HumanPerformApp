package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.ProductDetailResponse
import com.humanperformcenter.shared.data.model.ServiceAvailable
import com.humanperformcenter.shared.data.model.ServiceItem
import com.humanperformcenter.shared.domain.repository.ServiceProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class ServiceProductUseCase(private val serviceProductRepository: ServiceProductRepository) {
    suspend fun getAllServices(): List<ServiceAvailable> = withContext(Dispatchers.IO) {
        return@withContext serviceProductRepository.getAllServices()
    }
    suspend fun getServiceProducts(serviceId: Int): List<ServiceItem> = withContext(Dispatchers.IO) {
        return@withContext serviceProductRepository.getServiceProducts(serviceId)
    }
    suspend fun getUserProducts(customerId: Int): List<ServiceItem> = withContext(Dispatchers.IO) {
        return@withContext serviceProductRepository.getUserProducts(customerId)
    }
    suspend fun assignProductToUser(
        userId: Int,
        productId: Int,
        paymentMethod: String,
        couponCode: String? = null,
    ): Boolean = withContext(Dispatchers.IO) {
        serviceProductRepository.assignProductToUser(userId, productId, paymentMethod, couponCode)
    }

    suspend fun unassignProductFromUser(userId: Int, productId: Int): Boolean = withContext(Dispatchers.IO) {
        return@withContext serviceProductRepository.unassignProductFromUser(userId, productId)
    }
    suspend fun getProductDetails(userId: Int, productId: Int): ProductDetailResponse? =
        serviceProductRepository.getProductDetails(userId, productId)

    suspend fun applyCoupon(code: String, userId: Int, productId: Int): Boolean = withContext(Dispatchers.IO) {
        return@withContext serviceProductRepository.applyCoupon(code, userId, productId)
    }
}