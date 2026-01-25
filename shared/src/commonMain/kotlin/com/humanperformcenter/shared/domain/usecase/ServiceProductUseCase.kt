package com.humanperformcenter.shared.domain.usecase

import com.humanperformcenter.shared.data.model.payment.Coupon
import com.humanperformcenter.shared.data.model.product_service.ProductDetailResponse
import com.humanperformcenter.shared.data.model.product_service.ServiceAvailable
import com.humanperformcenter.shared.data.model.product_service.ServiceItem
import com.humanperformcenter.shared.presentation.ui.models.ProductTypeFilter
import com.humanperformcenter.shared.domain.repository.ServiceProductRepository
import com.humanperformcenter.shared.presentation.ui.SimpleResponse

class ServiceProductUseCase(private val serviceProductRepository: ServiceProductRepository) {
    suspend fun getAllServices(): Result<List<ServiceAvailable>> {
        return serviceProductRepository.getAllServices()
    }
    suspend fun getServiceProducts(serviceId: Int): Result<List<ServiceItem>> {
        return serviceProductRepository.getServiceProducts(serviceId)
    }
    suspend fun getUserProducts(customerId: Int): Result<List<ServiceItem>> {
        return serviceProductRepository.getUserProducts(customerId)
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
    suspend fun getProductDetails(userId: Int, productId: Int): Result<ProductDetailResponse> {
        return serviceProductRepository.getProductDetails(userId, productId)
    }

    suspend fun applyCoupon(code: String, userId: Int, productId: Int): Result<SimpleResponse> {
        return serviceProductRepository.applyCoupon(code, userId, productId)
    }

    fun filterProducts(
        list: List<ServiceItem>,
        filter: ProductTypeFilter,
        sessionCount: Int
    ): List<ServiceItem> {
        return list.filter { producto ->
            val tipoOk = when (filter) {
                ProductTypeFilter.RECURRENT -> producto.tipo_producto == "recurrent"
                ProductTypeFilter.NON_RECURRENT -> producto.tipo_producto != "recurrent"
                ProductTypeFilter.ALL -> true
            }
            val sesionesOk = if (sessionCount == 0) true else producto.session == sessionCount
            tipoOk && sesionesOk
        }
    }

    fun calcularPrecioConDescuento(productoId: Int, precioOriginal: Double, cupones: List<Coupon>)
    : Double {
        val descuentos = cupones
            .filter { it.productIds.isEmpty() || it.productIds.contains(productoId) }
            .map { if (it.isPercentage) precioOriginal * it.discount / 100 else it.discount }
        val mayorDescuento = descuentos.maxOrNull() ?: 0.0
        return (precioOriginal - mayorDescuento).coerceAtLeast(0.0)
    }
}