package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.ProductDetailResponse
import com.humanperformcenter.shared.data.model.ServiceAvailable
import com.humanperformcenter.shared.data.model.ServiceItem
import com.humanperformcenter.shared.domain.repository.ServiceProductRepository

class ServiceProductUseCase(private val serviceProductRepository: ServiceProductRepository) {
    suspend fun getAllServices(): List<ServiceAvailable> {
        return serviceProductRepository.getAllServices()
    }
    suspend fun getServiceProducts(serviceId: Int): List<ServiceItem> {
        return serviceProductRepository.getServiceProducts(serviceId)
    }
    suspend fun getUserProducts(customerId: Int): List<ServiceItem> {
        return serviceProductRepository.getUserProducts(customerId)
    }
    suspend fun assignProductToUser(
        userId: Int,
        productId: Int,
        paymentMethod: String,
        couponCode: String? = null,
    ): Pair<Boolean, String?> {
        return serviceProductRepository.assignProductToUser(userId, productId, paymentMethod, couponCode)
    }

    suspend fun unassignProductFromUser(userId: Int, productId: Int): Boolean {
        return serviceProductRepository.unassignProductFromUser(userId, productId)
    }
    suspend fun getProductDetails(userId: Int, productId: Int): ProductDetailResponse? {
        return serviceProductRepository.getProductDetails(userId, productId)
    }

    suspend fun applyCoupon(code: String, userId: Int, productId: Int): Boolean {
        return serviceProductRepository.applyCoupon(code, userId, productId)
    }
}