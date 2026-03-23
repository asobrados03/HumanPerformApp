package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.payment.Coupon
import com.humanperformcenter.shared.data.model.product_service.ProductDetailResponse
import com.humanperformcenter.shared.data.model.product_service.ServiceAvailable
import com.humanperformcenter.shared.data.model.product_service.Product
import com.humanperformcenter.shared.presentation.ui.models.ProductTypeFilter
import com.humanperformcenter.shared.domain.repository.ServiceProductRepository

class ServiceProductUseCase(private val serviceProductRepository: ServiceProductRepository) {
    suspend fun getAllServices(): Result<List<ServiceAvailable>> {
        return serviceProductRepository.getAllServices()
    }
    suspend fun getServiceProducts(serviceId: Int, userId: Int): Result<List<Product>> {
        return serviceProductRepository.getServiceProducts(serviceId, userId)
    }
    suspend fun getUserProducts(userId: Int): Result<List<Product>> {
        return serviceProductRepository.getUserProducts(userId)
    }
    suspend fun assignProductToUser(
        userId: Int,
        productId: Int,
        paymentMethod: String,
        couponCode: String? = null,
    ): Result<Int> {
        return serviceProductRepository.assignProductToUser(userId, productId, paymentMethod, couponCode)
    }

    suspend fun unassignProductFromUser(userId: Int, productId: Int): Result<Unit> {
        return serviceProductRepository.unassignProductFromUser(userId, productId)
    }
    suspend fun getActiveProductDetail(userId: Int, productId: Int): Result<ProductDetailResponse> {
        return serviceProductRepository.getActiveProductDetail(userId, productId)
    }

    suspend fun getProductDetailHireProduct(productId: Int): Result<Product> {
        return serviceProductRepository.getProductDetailHireProduct(productId)
    }

    fun filterProducts(
        list: List<Product>,
        filter: ProductTypeFilter,
        sessionCount: Int
    ): List<Product> {
        return list.filter { product ->
            val typeMatches = when (filter) {
                ProductTypeFilter.RECURRENT -> product.typeOfProduct == "recurrent"
                ProductTypeFilter.NON_RECURRENT -> product.typeOfProduct != "recurrent"
                ProductTypeFilter.ALL -> true
            }
            val sessionMatches = if (sessionCount == 0) true else product.session == sessionCount
            typeMatches && sessionMatches
        }
    }

    fun calculateDiscountedPrice(productId: Int, originalPrice: Double, coupons: List<Coupon>)
    : Double {
        val availableDiscounts = coupons
            .filter { it.productIds.isEmpty() || it.productIds.contains(productId) }
            .map { if (it.isPercentage) originalPrice * it.discount / 100 else it.discount }
        val highestDiscount = availableDiscounts.maxOrNull() ?: 0.0
        return (originalPrice - highestDiscount).coerceAtLeast(0.0)
    }
}